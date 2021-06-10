package com.example.beyondpomodoro

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import com.example.beyondpomodoro.sessiontype.*
import com.example.beyondpomodoro.ui.home.SharedViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch


/**
 * A fragment representing a list of Items.
 */
class SessionInfoFragment : Fragment() {

    private var sessionDao: SessionDao? = null
    private var columnCount = 1
    private var sessions: List<SessionType>? = null

    protected val sharedData: SharedViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
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
                // create a new database entry with a new session id and default values of on and off time
                lifecycleScope.launch {
                    sharedData.sid = sessionDao?.addSession(
                        Session("", 1500, 300, null, setOf<String>())
                    )?.toInt()

                    // navigate to Home Fragment
                    findNavController().navigate(R.id.action_sessionInfoFragment_to_pomodoroFragment)
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
                        SessionType(idx.toUInt(),
                            e.sid.toString(),
                            e.title?: run {""},
                            e.sessionTime?.toUInt()?.div(60u)?: run {25u},
                            e.breakTime?.toUInt()?.div(60u)?: run {5u},
                            e.tags?.toList()?: run{ listOf<String>()})
                    }

                    val sessionList = SessionList(sessions!!)
                    adapter = MySessionInfoRecyclerViewAdapter(sessionList.items, {
                        context.toast("${it.id}, ${it.title}")

                        // selected session id saved
                        sharedData.sid = it.id.toInt()

                        // navigate to Home Fragment
                        findNavController().navigate(R.id.action_sessionInfoFragment_to_pomodoroFragment)
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
                    });
                }
            }
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        println("DEBUG: destorying recycler view... ")
        println("DEBUG: $sessions")
        
        lifecycleScope.launch {
            sessions?.map {
                sessionDao?.updateTitle(Title(it.title, it.id.toInt()))
            }
        }
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