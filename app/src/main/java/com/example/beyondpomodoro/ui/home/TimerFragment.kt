package com.example.beyondpomodoro.ui.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.beyondpomodoro.R
import com.example.beyondpomodoro.sessiontype.Session
import com.example.beyondpomodoro.sessiontype.SessionDao
import com.example.beyondpomodoro.sessiontype.SessionDatabase
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch

open class TimerFragment : Fragment() {
    protected var breakTimeSeconds: UInt = 5u * 60u
    protected var sessionTimeSeconds: UInt = 25u * 60u

    lateinit var startButton: Button
    lateinit var endButton: Button
    lateinit var textViewSeconds: TextView
    protected lateinit var timer: PomodoroTimer
    protected lateinit var title: (String) -> Unit
    protected lateinit var type: (String) -> Unit
    protected var sessionDao: SessionDao? = null

    protected val connection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.LocalBinder
            timer = binder.timer
            title = {
                binder.title(it)
            }
            type = {
                binder.type(it)
            }
            afterServiceConnected()
            println("DEBUG: service connected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            println("DEBUG: service disconnected")
        }
    }

    protected val sharedData: SharedViewModel by activityViewModels()
    companion object {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        context?.unbindService(connection)

        // get tags
        val tags = view?.let {
            it.findViewById<ChipGroup>(R.id.chipGroup).children.toList().map { e ->
                (e as Chip).text.toString()
            }
        }?: run {
            listOf<String>()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // add a new session to database
            sessionDao?.addSession(
                Session(sessionTimeSeconds.toInt(),
                    300,
                    System.currentTimeMillis(),
                    tags,
                    1
                )
            )
        }
    }

    open fun afterServiceConnected() {
        // fetch most recent session
        val cls = this
        viewLifecycleOwner.lifecycleScope.launch {
            sessionDao?.getLatestSession()?.apply {
                cls.sessionTimeSeconds = this.sessionTime?.toUInt() ?: run { 1500u }
            }

            addButtons()
            /*
             * the timer state may be active or inactive
             * regardless we only care about updating the view elements
             * so we dont have to do anything special
             * the observer takes care of updating the visual elements
             */
            timer.sessionTimeSecondsLeft.observe(viewLifecycleOwner, {
                textViewSeconds.text = convertMinutesToDisplayString(it)

                // update visuals
                updateVisualBlocks(it)
            })
        }
    }

    open fun startSession() {
        println("DEBUG: starting Session")
        nextState()
    }

    open fun setSessionTime(s: UInt) {
        println("DEBUG: setting session time to $s")
        timer.setSessionTime(s)
    }

    override fun onStart() {
        super.onStart()
    }

    open fun addButtons() {
        println("DEBUG: adding buttons")
        val fragment = this
        view?.findViewWithTag<TextView>("timerDisplay")?.apply {
            println("DEBUG: textview")
            text = convertMinutesToDisplayString(sessionTimeSeconds)

            // onclick open dialog to enter time
            setOnClickListener {
                when(timer.state.value) {
                    State.ACTIVE_PAUSED, State.ACTIVE_RUNNING -> {
                        //TODO: check if this works (i.e. no mutable) when paused
                        val toast = Toast.makeText(
                            view?.context,
                            context.getString(R.string.session_already_active_message),
                            Toast.LENGTH_SHORT
                        )
                        toast.show()
                    }
                    State.INACTIVE, State.COMPLETE -> {
                        SetTimeDialogFragment(fragment).show(fragment.parentFragmentManager, "pick_session_time")
                    }

                }

            }
        }?.let {
            textViewSeconds = it
        }

        view?.findViewWithTag<Button>("endButton")?.apply {
            println("DEBUG: endbutton")
            setOnClickListener {
                confirmEndSession()
            }
        }?.let {
            endButton = it
        }

       controlButtonAction {
           startSession()
       }

        // when timer state changes
        timer.state.observe(viewLifecycleOwner, {
            when(it) {
                State.COMPLETE -> {
                    // hide end button
                    startButton.text = view?.context?.getString(R.string.pomodoro_save_session_button)
                    endButton.visibility = View.INVISIBLE
                    controlButtonAction {
                        saveSession()
                    }
                }

                State.INACTIVE -> {
                    startButton.text = view?.context?.getString(R.string.pomodoro_start_session_button)
                    endButton.visibility = View.VISIBLE
                    controlButtonAction {
                        startSession()
                    }
                }

                State.ACTIVE_PAUSED -> {
                    startButton.text = view?.context?.getString(R.string.pomodoro_resume_session_button)
                    endButton.visibility = View.VISIBLE
                    controlButtonAction {
                        startSession()
                    }
                }

                State.ACTIVE_RUNNING -> {
                    startButton.text = view?.context?.getString(R.string.pomodoro_pause_session_button)
                }
            }
        })

        // when user changes session time
        timer.sessionTimeSeconds.observe(viewLifecycleOwner, {
            println("DEBUG: timer set to $it")
            textViewSeconds.apply {
                text = convertMinutesToDisplayString(it)
            }
        })

        timer.sessionTimeSecondsLeft.observe(viewLifecycleOwner, {
            textViewSeconds.apply {
                text = convertMinutesToDisplayString(it)
            }
        })
    }

    fun controlButtonAction(func: () -> Unit) {
        view?.findViewWithTag<Button>("startButton")?.apply {
            setOnClickListener {
                func()
            }
        }?.let {
            startButton = it
        }
    }

    open fun nextState() {
        println("DEBUG: state ${timer.state.value}, ${startButton.text}")
        timer.nextState()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.timer_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // create database instance
        val db = context?.let {
            Room.databaseBuilder(
                it,
                SessionDatabase::class.java, "session-types"
            ).build()
        }

        sessionDao = db?.sessionDao()

        // get the last activity type on activity creation and store in sharedData
        activity?.getPreferences(Context.MODE_PRIVATE)?.let { prefs ->
            if (prefs.contains("lastSessionType")) {
                sharedData.sessionType?.value = prefs.getString("lastSessionType", "default")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Intent(context, TimerService::class.java).also { intent ->
            context?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        sharedData.sessionType?.let { sessionType ->
            activity?.getPreferences(Context.MODE_PRIVATE)?.let { prefs ->
                sessionTimeSeconds = prefs.getInt("pomodoroTimeFor$sessionType", 25).toUInt() * 60u
                breakTimeSeconds = prefs.getInt("breakTimeFor$sessionType", 5).toUInt() * 60u
            }
        }

    }

    fun buttonsReset() {
        // endbutton hide
        endButton.visibility = View.INVISIBLE

        // start button
        startButton.text = view?.context?.getString(R.string.pomodoro_start_session_button)
    }

    open fun onTimerFinish() {
    }

    open fun saveSession() {
    }

    open fun endSession() {
        timer.clockReset()
        textViewSeconds.text = convertMinutesToDisplayString(sessionTimeSeconds)
        timer.pomodoroReset()

        endButton.visibility = View.INVISIBLE
        startButton.text = context?.getString(R.string.pomodoro_start_session_button)
    }

    open fun confirmEndSession() {
    }

    open fun updateVisualBlocks(secondsUntilFinished: UInt) {
    }
}

fun convertMinutesToDisplayString(sessionTimeSecondsLeft: UInt) : String {
    return (sessionTimeSecondsLeft/60u).toString().padStart(2, '0') + ":" + (sessionTimeSecondsLeft%60u).toString().padStart(2, '0')
}
