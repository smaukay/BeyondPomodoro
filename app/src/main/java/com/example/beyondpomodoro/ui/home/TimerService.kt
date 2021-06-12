package com.example.beyondpomodoro.ui.home

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer

class TimerService : LifecycleService() {

    var _timer: PomodoroTimer = PomodoroTimer(1500u)
    var _title = ""
    var _type = ""
    private val binder = LocalBinder()

    fun onTick(secondsUntilFinished: UInt) {
        when(_timer.state.value) {
            State.INACTIVE -> {}
            else -> persistentTimedNotification(this, secondsUntilFinished, _title)
        }
    }

    override fun onCreate() {
        super.onCreate()
        println("DEBUG: service created")
        _timer.sessionTimeSeconds.observe(this, Observer<UInt> {
        })
        _timer.sessionTimeSecondsLeft.observe(this, Observer<UInt>{
            onTick(it)
        })
        _timer.state.observe(this, Observer<State> {
            println("DEBUG: State is $it")
            when(it) {
                State.COMPLETE -> {
                    with(NotificationManagerCompat.from(this)) {
                        cancelAll()
                    }
                    println("DEBUG: Notifying")
                    endNotification(this, _title, _type)
                }
                State.ACTIVE_PAUSED -> {
                    _timer.sessionTimeSecondsLeft.value?.let { it1 ->
                        persistentTimedNotification(this,
                            it1, _title)
                    }
                }
                State.ACTIVE_RUNNING -> {
                    // attach an observer
                    println("DEBUG: State is active")
                }
                State.INACTIVE -> {
                    // no notification needed
                    with(NotificationManagerCompat.from(this)) {
                        println("DEBUG: removing all notifications")
                        cancelAll()
                    }
                    println("DEBUG: no notification")
                }
            }
        })
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
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