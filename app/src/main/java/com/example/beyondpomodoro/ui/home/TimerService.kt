package com.example.beyondpomodoro.ui.home

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer

class TimerService : LifecycleService() {
    private var receiver: BroadcastReceiver? = null
    var _timer: PomodoroTimer = PomodoroTimer(1500u)
    var _title = ""
    var _type = ""
    private val binder = LocalBinder()

    fun onTick(secondsUntilFinished: UInt) {
        when(_timer.state.value) {
            State.INACTIVE -> {}
            else -> persistentTimedNotification(this, secondsUntilFinished, _title, _type, _timer.state.value?: run {State.INACTIVE})
        }
    }

    override fun onCreate() {
        super.onCreate()

        receiver = TimerActions().apply {
            timer = _timer
        }
        registerReceiver(receiver, IntentFilter().apply{
            addAction("com.smaukay.beyondpomodoro.Pause")
            addAction("com.smaukay.beyondpomodoro.Resume")
            addAction("com.smaukay.beyondpomodoro.Open")
            addAction("com.smaukay.beyondpomodoro.Start")
        })
        _timer.sessionTimeSeconds.observe(this, Observer<UInt> {
        })
        _timer.sessionTimeSecondsLeft.observe(this, Observer<UInt>{
            onTick(it)
        })
        _timer.state.observe(this, Observer<State> {

            when(it) {
                State.COMPLETE -> {
                    with(NotificationManagerCompat.from(this)) {
                        cancelAll()
                    }

                    endNotification(this, _title, _type)
                }
                State.ACTIVE_PAUSED -> {
                    _timer.sessionTimeSecondsLeft.value?.let { it1 ->
                        persistentTimedNotification(this,
                            it1, _title, _type, it)
                    }
                }
                State.ACTIVE_RUNNING -> {
                    // attach an observer

                }
                State.INACTIVE -> {
                    // no notification needed
                    with(NotificationManagerCompat.from(this)) {

                        cancelAll()
                    }

                }
            }
        })
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {

        return super.onUnbind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)

        with(NotificationManagerCompat.from(this)) {
            cancelAll()
        }
    }

    inner class LocalBinder: Binder() {
        val timer = _timer

        fun title(s: String) {
            _title = s
        }

        fun type(s: String) {
            _type = s
        }
    }
}