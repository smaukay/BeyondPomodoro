package com.example.beyondpomodoro.ui.home

import android.text.Editable
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

    public var numBlocksShow: UInt = 9u
    public var imageButtonList: List<ImageView?>? = null
    public var editTags: EditText? = null
    public var chipGroup: ChipGroup? = null
    public var tags: MutableMap<String, String> = mutableMapOf()

    // event variables
    public var sessionStartTimeMillis: Long? = null
    public var sessionEndTimeMillis: Long? = null


}
