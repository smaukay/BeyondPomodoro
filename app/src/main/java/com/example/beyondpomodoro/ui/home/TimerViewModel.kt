package com.example.beyondpomodoro.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TimerViewModel : ViewModel() {
    var type: String? = null
    var title: String? = null
    var timer: PomodoroTimer? = null
    val active: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }
}