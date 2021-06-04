package com.example.beyondpomodoro.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TimerViewModel : ViewModel() {
    var type: String? = null
    var title: String? = null
    var timer: PomodoroTimer = PomodoroTimer(1500u)
    val active: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }
}