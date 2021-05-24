package com.example.beyondpomodoro.ui.home

import android.content.res.ColorStateList
import android.widget.EditText
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.chip.ChipGroup

open class HomeViewModel : ViewModel() {

    var title: String = ""
    var editTitle: EditText? = null
    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    var numBlocksShow: UInt = 9u
    var imageButtonList: List<ImageView?>? = null
    var editTags: EditText? = null
    var chipGroup: ChipGroup? = null
    var tags: MutableMap<String, ColorStateList> = mutableMapOf()

    // event variables
    var sessionStartTimeMillis: Long? = null
    var sessionEndTimeMillis: Long? = null


}
