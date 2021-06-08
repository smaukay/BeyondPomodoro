package com.example.beyondpomodoro.ui.home

import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class HomeViewModel : ViewModel() {

    var title: String = ""
    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    var numBlocksShow: Int = 9
    var imageButtonList: List<ImageView?>? = null
    var tags: MutableMap<String, String> = mutableMapOf()

    // event variables
    var sessionStartTimeMillis: Long? = null
    var sessionEndTimeMillis: Long? = null


}
