package com.example.beyondpomodoro.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel: ViewModel() {

    var sessionType: MutableLiveData<String>? = null
}