package com.example.beyondpomodoro

import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.beyondpomodoro.ui.home.HomeFragment
import com.example.beyondpomodoro.ui.home.SetTimeDialogFragment
import com.example.beyondpomodoro.ui.home.TimerFragment
import java.util.*

class PomodoroTimer(sessionTimeSeconds: UInt, view: View, fragment: TimerFragment) {
    public var sessionTimeSecondsLeft: UInt = sessionTimeSeconds
    public var sessionTimeSeconds: UInt = sessionTimeSeconds
    public var pomodoroComplete: Boolean = false
    public var pomodoroActive = false
    public lateinit var startButton: Button
    public lateinit var endButton: Button
    public lateinit var textViewSeconds: TextView
    public var countDownTimer: CountDownTimer? = null
    private val fragment = fragment
    private var view = view

    init
    {
        val cls = this
        textViewSeconds = view.findViewById<TextView>(R.id.textView2).apply {
            text = convertMinutesToDisplayString()

            // onclick open dialog to enter time
            setOnClickListener {
                when(pomodoroActive and !pomodoroComplete) {
                    true ->
                    {
                        //TODO: send toast saying time cannot be changed
                        val toast = Toast.makeText(
                            view.context,
                            "The session is already active. You can end it if you like.",
                            Toast.LENGTH_SHORT
                        )
                        toast.show()
                    }
                    false -> {
                        SetTimeDialogFragment(fragment).show(fragment.parentFragmentManager, "pick_session_time")
                    }

                }

            }
        }

        endButton = view.findViewById<Button>(R.id.button4).apply {
            this.setOnClickListener {
                fragment.confirmEndSession()
            }
        }

        startButton = view.findViewById<Button>(R.id.button).apply {
            this.setOnClickListener {
                cls.toggle()
            }
        }
    }
    fun convertMinutesToDisplayString() : String {
        return (sessionTimeSecondsLeft/60u).toString().padStart(2, '0') + ":" + (sessionTimeSecondsLeft%60u).toString().padStart(2, '0')
    }

    fun toggle() {
        if (pomodoroActive) {
            countDownTimer?.cancel()
            pomodoroActive = false
            startButton.text = view.context.getString(R.string.pomodoro_resume_session_button)
        }
        else if ( (pomodoroActive == false) and (pomodoroComplete == false) ){
            pomodoroActive = true
            countDownTimerCreate((sessionTimeSecondsLeft * 1000u).toLong())
            countDownTimer?.start()
            textViewSeconds!!.text = convertMinutesToDisplayString()

            startButton.text = view.context.getString(R.string.pomodoro_pause_session_button)
            endButton?.visibility = View.VISIBLE
            fragment.startSession()
        }
        else if (pomodoroComplete) {
            // this is now a save session button
            fragment.saveSession()
        }
    }

    fun buttonsReset() {
        // endbutton hide
        endButton?.visibility = View.INVISIBLE

        // start button
        startButton?.text = view.context.getString(R.string.pomodoro_start_session_button)
    }


    fun pomodoroReset() {
        // set pomodoro completeness as false
        pomodoroComplete = false
        pomodoroActive = false
    }

    fun clockReset() {
        // no save
        // reset the clock
        countDownTimer?.cancel()

        // set timer back to full
        sessionTimeSecondsLeft = sessionTimeSeconds
        // set text back to timer
        textViewSeconds?.text = convertMinutesToDisplayString()
    }

    private fun updateVisualBlocks(millisUntilFinished: Long) {
        sessionTimeSecondsLeft = (millisUntilFinished.toUInt())/1000u
        textViewSeconds?.text = convertMinutesToDisplayString()
    }

    private fun onTimerFinish() {
        startButton?.text = view.context.getString(R.string.pomodoro_save_session_button)
        pomodoroComplete = true
        pomodoroActive = false
        // hide end button
        endButton?.visibility = View.INVISIBLE
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