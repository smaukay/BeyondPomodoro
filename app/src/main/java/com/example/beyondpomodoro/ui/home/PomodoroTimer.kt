package com.example.beyondpomodoro.ui.home

import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.beyondpomodoro.R

class PomodoroTimer(sessionTimeSeconds: UInt, view: View, fragment: TimerFragment) {

    enum class State {
        INACTIVE {
            override fun nextState() = ACTIVE_RUNNING
        },

        ACTIVE_RUNNING {
            override fun nextState() = ACTIVE_PAUSED
        },

        ACTIVE_PAUSED {
            override fun nextState() = ACTIVE_RUNNING
        },

        COMPLETE {
            override fun nextState() = INACTIVE
        };

        abstract fun nextState(): State
    }

    var state: State = State.INACTIVE

    var sessionTimeSecondsLeft: UInt = sessionTimeSeconds
    var sessionTimeSeconds: UInt = sessionTimeSeconds
    lateinit var startButton: Button
    lateinit var endButton: Button
    lateinit var textViewSeconds: TextView
    var countDownTimer: CountDownTimer? = null
    private val fragment = fragment
    private var view = view

    init
    {
        val cls = this
        textViewSeconds = view.findViewWithTag<TextView>("timerDisplay").apply {
            text = convertMinutesToDisplayString()

            // onclick open dialog to enter time
            setOnClickListener {
                when(state) {
                    State.ACTIVE_PAUSED, State.ACTIVE_RUNNING ->
                    {
                        //TODO: check if this works (i.e. no mutable) when paused
                        val toast = Toast.makeText(
                            view.context,
                            "The session is already active. You can end it if you like.",
                            Toast.LENGTH_SHORT
                        )
                        toast.show()
                    }
                    State.INACTIVE, State.COMPLETE -> {
                        SetTimeDialogFragment(fragment).show(fragment.parentFragmentManager, "pick_session_time")
                    }

                }

            }
        }

        endButton = view.findViewWithTag<Button>("endButton").apply {
            this.setOnClickListener {
                fragment.confirmEndSession()
            }
        }

        startButton = view.findViewWithTag<Button>("startButton").apply {
            this.setOnClickListener {
                cls.nextState()
            }
        }
    }
    private fun convertMinutesToDisplayString() : String {
        return (sessionTimeSecondsLeft/60u).toString().padStart(2, '0') + ":" + (sessionTimeSecondsLeft%60u).toString().padStart(2, '0')
    }

    private fun nextState() {
        when(state) {
            State.INACTIVE, State.ACTIVE_PAUSED -> {
                countDownTimerCreate((sessionTimeSecondsLeft * 1000u).toLong())
                countDownTimer?.start()
                textViewSeconds.text = convertMinutesToDisplayString()

                startButton.text = view.context.getString(R.string.pomodoro_pause_session_button)
                endButton.visibility = View.VISIBLE
                fragment.startSession()
            }

            State.ACTIVE_RUNNING -> {
                countDownTimer?.cancel()
                startButton.text = view.context.getString(R.string.pomodoro_resume_session_button)
            }

            State.COMPLETE -> {
                // this is now a save session button
                fragment.saveSession()
            }
        }
        state = state.nextState()
    }

    fun buttonsReset() {
        // endbutton hide
        endButton.visibility = View.INVISIBLE

        // start button
        startButton.text = view.context.getString(R.string.pomodoro_start_session_button)
    }

    fun pomodoroReset() {
        // set pomodoro completeness as false
        state = State.INACTIVE
    }

    fun clockReset() {
        // no save
        // reset the clock
        countDownTimer?.cancel()

        // set timer back to full
        sessionTimeSecondsLeft = sessionTimeSeconds
        // set text back to timer
        textViewSeconds.text = convertMinutesToDisplayString()
    }

    private fun updateVisualBlocks(millisUntilFinished: Long) {
        sessionTimeSecondsLeft = (millisUntilFinished.toUInt())/1000u
        textViewSeconds.text = convertMinutesToDisplayString()
    }

    private fun onTimerFinish() {
        startButton.text = view.context.getString(R.string.pomodoro_save_session_button)
        state = State.COMPLETE
        // hide end button
        endButton.visibility = View.INVISIBLE
    }

    private fun countDownTimerCreate(millisLeft: Long) {
        countDownTimer = object: CountDownTimer(millisLeft, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateVisualBlocks(millisUntilFinished)
                fragment.updateVisualBlocks(millisUntilFinished)
            }

            override fun onFinish() {
                onTimerFinish()
                fragment.onTimerFinish()
            }
        }
    }

}