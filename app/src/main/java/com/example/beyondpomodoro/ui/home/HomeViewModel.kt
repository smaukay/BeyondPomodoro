package com.example.beyondpomodoro.ui.home

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

    var numBlocksShow: Int = 9
    var imageButtonList: List<ImageView?>? = null
    var editTags: EditText? = null
    var chipGroup: ChipGroup? = null
    var tags: MutableMap<String, String> = mutableMapOf()

    // event variables
    var sessionStartTimeMillis: Long? = null
    var sessionEndTimeMillis: Long? = null


}
