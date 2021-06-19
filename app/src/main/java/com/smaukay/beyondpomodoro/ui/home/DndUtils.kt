package com.smaukay.beyondpomodoro.ui.home

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi

fun setRingerNormal(service: Service) {
    (service.getSystemService(Context.AUDIO_SERVICE) as AudioManager).ringerMode = AudioManager.RINGER_MODE_NORMAL
}

fun setDoNotDisturb(service: Service) {
    (service.getSystemService(Context.AUDIO_SERVICE) as AudioManager).ringerMode = AudioManager.RINGER_MODE_SILENT
}

@RequiresApi(Build.VERSION_CODES.M)
fun hasDndPermissions(getSystemService: (String) -> Any): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.isNotificationPolicyAccessGranted
    } else {
        return true
    }
}