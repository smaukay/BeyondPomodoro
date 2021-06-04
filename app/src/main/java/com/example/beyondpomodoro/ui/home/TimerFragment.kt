package com.example.beyondpomodoro.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.beyondpomodoro.R

open class TimerFragment : Fragment() {
    private var notificationId: Int = 0
    protected var breakTimeSeconds: UInt = 5u * 60u
    protected var sessionTimeSeconds: UInt = 25u * 60u

    lateinit var startButton: Button
    lateinit var endButton: Button
    lateinit var textViewSeconds: TextView
    protected open var notificationTitle: String = ""

    protected val sharedData: SharedViewModel by activityViewModels()
    companion object {
    }

    protected lateinit var timerViewModel: TimerViewModel

    open fun startSession() {
        println("DEBUG: starting Session")
        nextState()
    }

    open fun setSessionTime(s: UInt) {
        println("DEBUG: setting session time to $s")
        timerViewModel.timer.setSessionTime(s)
    }

    open fun createViewModel() {
        activity?.let {
            timerViewModel = ViewModelProvider(it).get(TimerViewModel::class.java)
        }
        println("DEBUG: timerviewmodel: $timerViewModel")
    }

    open fun addButtons() {
        println("DEBUG: adding buttons")
        val fragment = this
        view?.findViewWithTag<TextView>("timerDisplay")?.apply {
            println("DEBUG: textview")
            text = convertMinutesToDisplayString(sessionTimeSeconds)

            // onclick open dialog to enter time
            setOnClickListener {
                when(timerViewModel.timer.state.value) {
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
        timerViewModel.timer.state.observe(viewLifecycleOwner, {
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
        timerViewModel.timer.sessionTimeSeconds.observe(viewLifecycleOwner, {
            println("DEBUG: timer set to $it")
            textViewSeconds.apply {
                text = convertMinutesToDisplayString(it)
            }
        })

        timerViewModel.timer.sessionTimeSecondsLeft.observe(viewLifecycleOwner, {
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
        println("DEBUG: state ${timerViewModel.timer.state.value}, ${startButton.text}")
        timerViewModel.timer.nextState()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        createViewModel()
        return inflater.inflate(R.layout.timer_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // get the last activity type on activity creation and store in sharedData
        activity?.getPreferences(Context.MODE_PRIVATE)?.let { prefs ->
            if (prefs.contains("lastSessionType")) {
                sharedData.sessionType?.value = prefs.getString("lastSessionType", "default")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        timerViewModel.active.value = true

        sharedData.sessionType?.let { sessionType ->
            activity?.getPreferences(Context.MODE_PRIVATE)?.let { prefs ->
                sessionTimeSeconds = prefs.getInt("pomodoroTimeFor$sessionType", 25).toUInt() * 60u
                breakTimeSeconds = prefs.getInt("breakTimeFor$sessionType", 5).toUInt() * 60u
            }
        }

        /*
         * the timer state may be active or inactive
         * regardless we only care about updating the view elements
         * so we dont have to do anything special
         * the observer takes care of updating the visual elements
         */

        timerViewModel.timer.sessionTimeSecondsLeft.observe(viewLifecycleOwner, {
            textViewSeconds.text = convertMinutesToDisplayString(it)

            // update visuals
            updateVisualBlocks(it)
        })

        addButtons()
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
        timerViewModel.timer.clockReset()
        textViewSeconds.text = convertMinutesToDisplayString(sessionTimeSeconds)
        timerViewModel.timer.pomodoroReset()

        endButton.visibility = View.INVISIBLE
        startButton.text = context?.getString(R.string.pomodoro_start_session_button)
    }

    open fun confirmEndSession() {
    }

    open fun updateVisualBlocks(secondsUntilFinished: UInt) {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timerViewModel.active.value = false
    }

}

fun convertMinutesToDisplayString(sessionTimeSecondsLeft: UInt) : String {
    return (sessionTimeSecondsLeft/60u).toString().padStart(2, '0') + ":" + (sessionTimeSecondsLeft%60u).toString().padStart(2, '0')
}
