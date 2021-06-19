package com.smaukay.beyondpomodoro.ui.home

import android.app.Service
import android.content.Context
import android.media.AudioManager

fun setRingerNormal(service: Service) {
    (service.getSystemService(Context.AUDIO_SERVICE) as AudioManager).ringerMode = AudioManager.RINGER_MODE_NORMAL
}

fun setDoNotDisturb(service: Service) {
    (service.getSystemService(Context.AUDIO_SERVICE) as AudioManager).ringerMode = AudioManager.RINGER_MODE_SILENT
}

