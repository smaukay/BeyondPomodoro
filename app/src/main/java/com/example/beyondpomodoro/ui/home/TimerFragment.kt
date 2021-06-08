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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.beyondpomodoro.MainActivity
import com.example.beyondpomodoro.R
import com.example.beyondpomodoro.sessiontype.Session
import com.example.beyondpomodoro.sessiontype.SessionDao
import kotlinx.coroutines.launch

open class TimerFragment : Fragment() {
    protected var tags: MutableList<String> = mutableListOf()
    protected var breakTimeSeconds: UInt = 5u * 60u
    protected var sessionTimeSeconds: UInt = 25u * 60u
    protected var sessionId: Int? = null
    protected var title: String? = null

    lateinit var startButton: Button
    lateinit var endButton: Button
    lateinit var textViewSeconds: TextView
    protected lateinit var timer: PomodoroTimer
    protected lateinit var notificationTitle: (String) -> Unit
    protected lateinit var type: (String) -> Unit
    protected var sessionDao: SessionDao? = null

    protected val connection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.LocalBinder
            timer = binder.timer
            notificationTitle = {
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
    }

    open fun readSession(s: Session) {
        sessionTimeSeconds = s.sessionTime?.toUInt() ?: run { 1500u }
        breakTimeSeconds = s.breakTime?.toUInt() ?: run { 300u }
        sessionId = s.sid
        println("DEBUG: tags found ${this.tags}")
        tags.clear()
        s.tags?.map {
            tags.add(it)
        }
        println("DEBUG: tags found ${tags}")
        title = s.title
    }

    open fun afterServiceConnected() {
        // fetch most recent session
        val cls = this
        lifecycleScope.launch {
            sharedData.sid?.let {
                println("DEBUG: sid has been set")
                sessionDao?.getSession(it).apply {
                    this?.let { s ->
                        readSession(s)
                    }
                }
            }?: run {
                sessionDao?.getLatestSession()?.apply {
                    readSession(this)
                }
            }

            println("DEBUG: sid found: ${cls.sessionId}")
            println("DEBUG: timer value was ${cls.sessionTimeSeconds}")

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

    open fun addButtons() {
        println("DEBUG: adding buttons")
        val fragment = this
        view?.findViewWithTag<TextView>("timerDisplay")?.apply {
            println("DEBUG: textview")
            text = timer.sessionTimeSeconds.value?.let { convertMinutesToDisplayString(it) }

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

    open fun fetchDao() {
        val db = (activity as MainActivity).db
        println("DEBUG: db: $db")
        sessionDao = db.sessionDao()
    }

    open fun bindService() {
        Intent(context, TimerService::class.java).also { intent ->
            context?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.timer_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchDao()
        bindService()
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

    open fun addToSessionList() {

    }

    open fun endSession() {
        addToSessionList()
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
