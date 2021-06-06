package com.example.beyondpomodoro.ui.home

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class TimerService : Service() {

    var _timer: PomodoroTimer = PomodoroTimer(1500u)
    private val binder = LocalBinder()

    override fun onCreate() {
        super.onCreate()
        println("DEBUG: service created")
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        println("DEBUG: unbind called")
        return super.onUnbind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("DEBUG: service started")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        println("DEBUG: service destroyed")
    }
    inner class LocalBinder: Binder() {
        val timer = _timer
    }
}