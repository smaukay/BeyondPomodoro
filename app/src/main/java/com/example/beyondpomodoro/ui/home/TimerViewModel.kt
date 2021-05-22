package com.example.beyondpomodoro.ui.home

import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModel

class TimerViewModel : ViewModel() {
    var sessionTimeSecondsLeft: UInt = 30u
    var sessionTimeSeconds: UInt = 30u
    var textViewSeconds: TextView? = null

}