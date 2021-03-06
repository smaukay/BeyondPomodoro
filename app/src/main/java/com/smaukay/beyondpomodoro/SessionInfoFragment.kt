package com.smaukay.beyondpomodoro

import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import com.smaukay.beyondpomodoro.sessiontype.*
import com.smaukay.beyondpomodoro.ui.home.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch


/**
 * A fragment representing a list of Items.
 */
class SessionInfoFragment : Fragment() {

    private var sessionDao: SessionDao? = null
    private var columnCount = 1
    private var sessions: List<SessionType>? = null
    private var timer: PomodoroTimer? = null

    protected val connection = object: ServiceConnection {
        private lateinit var binder: TimerService.LocalBinder
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            binder = service as TimerService.LocalBinder
            timer = binder.timer

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timer = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    open fun bindService() {
        Intent(context, TimerService::class.java).also { intent ->
            context?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun confirmFirst(onConfirmCall: () -> Unit) {
        // create a new database entry with a new session id and default values of on and off time
        lifecycleScope.launch {
            // check if a session already active?
            if (timer?.state?.value != State.INACTIVE) {
                val runningSession = getRunningActivityId(activity)?.let {
                    sessionDao?.getSession(it)
                }
                activity?.let {
                    // Use the Builder class for convenient dialog construction
                    val builder = AlertDialog.Builder(it)
                    val dialogTitle = runningSession?. let{
                        "Activity ${runningSession.title} is currently running. Cancel it?"
                    }?: run {
                        "An activity is already running. Cancel it?"
                    }
                    builder.setMessage(dialogTitle)
                        .setPositiveButton(R.string.cancel_activity_no,
                            DialogInterface.OnClickListener { dialog, id ->
                                // User cancelled the dialog
                                // do nothing
                            })
                        .setNegativeButton(R.string.cancel_activity_yes,
                            DialogInterface.OnClickListener { dialog, id ->
                                onConfirmCall()
                            })
                    // Create the AlertDialog object and return it
                    builder.create()
                }?.show()
            } else {
                onConfirmCall()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_session_info_list, container, false)
        val recyclerView = view.findViewWithTag<RecyclerView>("session_list")

        // fab action
        view.findViewById<FloatingActionButton>(R.id.newSessionTypeButton).apply {
            setOnClickListener {
                confirmFirst {
                    timer?.clockReset()
                    timer?.pomodoroReset()
                    lifecycleScope.launch {
                        sessionDao?.addSession(
                            Session("", 1500, 300, null, setOf<String>())
                        )?.toInt()?.let { it1 ->
                            setRunningActivityId(
                                activity,
                                it1
                            )
                        }
                        findNavController().navigate(R.id.action_sessionInfoFragment_to_pomodoroFragment)
                    }
                }
            }
        }

        // Set the adapter
        if (recyclerView is RecyclerView) {
            with(recyclerView) {

                addItemDecoration(DividerItemDecoration(context, VERTICAL))

                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }

                // fetch sessions from database
                lifecycleScope.launch {
                    sessionDao = (activity as MainActivity).db.sessionDao()
                    sessions = sessionDao?.getSessions()?.mapIndexed {idx, e ->

                        val res = SessionType(idx.toUInt(),
                            e.sid.toString(),
                            e.title?: run {""},
                            e.sessionTime?.toUInt()?.div(60u)?: run {25u},
                            e.breakTime?.toUInt()?.div(60u)?: run {5u},
                            e.tags?.toList()?: run{ listOf<String>()},
                            e.dnd
                        )

                        res
                    }

                    val sessionList = SessionList(sessions!!)

                    adapter = MySessionInfoRecyclerViewAdapter(sessionList.items, {sessionType ->
                        if (getRunningActivityId(activity) == sessionType.id.toInt()) {
                            // navigate to Home Fragment
                            findNavController().navigate(R.id.action_sessionInfoFragment_to_pomodoroFragment)
                        }
                        else {
                            confirmFirst {
                                // selected session id saved
                                setRunningActivityId(activity, sessionType.id.toInt())

                                // timer has to be changed accordingly
                                // so read the session first
                                lifecycleScope.launch {
                                    val selectedSession = sessionDao?.getSession(sessionType.id.toInt())
                                    selectedSession?.sessionTime?.toUInt()?.apply{
                                        timer?.setSessionTime(this)
                                    }
                                }
                                timer?.clockReset()
                                timer?.pomodoroReset()
                                context.toast("${sessionType.id}, ${sessionType.title}")

                                // navigate to Home Fragment
                                findNavController().navigate(R.id.action_sessionInfoFragment_to_pomodoroFragment)
                            }
                        }
                    }, { sessionType ->
                        // show confirmation dialog on long click
                        activity?.let {
                            // Use the Builder class for convenient dialog construction
                            val builder = AlertDialog.Builder(it)
                            builder.setMessage(R.string.delete_activity_confirmation)
                                .setPositiveButton(R.string.delete_activity_cancel,
                                    DialogInterface.OnClickListener { dialog, id ->
                                        // User cancelled the dialog
                                        // do nothing
                                    })
                                .setNegativeButton(R.string.delete_activity_yes,
                                    DialogInterface.OnClickListener { dialog, id ->
                                        // remove this item
                                        recyclerView.adapter?.notifyItemRemoved(sessionList.items.indexOf(sessionType))
                                        sessionList.items.remove(sessionType)

                                        // remove from database as well
                                        lifecycleScope.launch {
                                            sessionDao?.removeSession(sessionType.id.toInt())
                                        }
                                    })
                            // Create the AlertDialog object and return it
                            builder.create()
                        }?.show()
                        true
                    }, { item ->
                        lifecycleScope.launch {
                            sessionDao?.updateTitle(Title(item.title, item.id.toInt()))
                        }
                    })

                }
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindService()
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            SessionInfoFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}