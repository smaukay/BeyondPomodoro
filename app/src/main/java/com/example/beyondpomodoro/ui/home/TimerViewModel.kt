package com.example.beyondpomodoro.ui.home

import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModel

class TimerViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    // timer state
    public var pomodoroActive = false
    public var sessionTimeSecondsLeft: UInt = 30u
    public var sessionTimeSeconds: UInt = 30u
    public var pomodoroComplete: Boolean = false
    public var button: Button? = null
    public var endButton: Button? = null
    public var textViewSeconds: TextView? = null
    public var countDownTimer: CountDownTimer? = null

}