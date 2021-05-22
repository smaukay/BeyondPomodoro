package com.example.beyondpomodoro.ui.home

import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.chip.ChipGroup

open class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    public var numBlocksShow: UInt = 9u
    public var imageButtonList: List<ImageView?>? = null
    public var sessionTimeSecondsLeft: UInt = 30u
    public var sessionTimeSeconds: UInt = 30u
    public var editTags: EditText? = null
    public var pomodoroComplete: Boolean = false
    public var button: Button? = null
    public var endButton: Button? = null
    public var textViewSeconds: TextView? = null
    public var countDownTimer: CountDownTimer? = null
    public var chipGroup: ChipGroup? = null
    public var tags: MutableMap<String, String> = mutableMapOf()

    // event variables
    public var sessionStartTimeMillis: Long? = null
    public var sessionEndTimeMillis: Long? = null

    // timer state
    public var pomodoroActive = false

}

class BreakViewModel : HomeViewModel() {

}