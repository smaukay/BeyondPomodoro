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

    override fun onCreate() {
        super.onCreate()
        println("DEBUG: service created")
        _timer.state.observe(this, Observer<State> {
            when(it) {
                State.COMPLETE -> {
                    with(NotificationManagerCompat.from(this)) {
                        cancelAll()
                    }
                    // remove observers
                    _timer.sessionTimeSecondsLeft.removeObservers(this)

                    println("DEBUG: Notifying")
                    endNotification(this, _title, _type)
                }
                State.ACTIVE_PAUSED, State.ACTIVE_RUNNING -> {
                    // attach an observer
                    _timer.sessionTimeSecondsLeft.observe(this, Observer<UInt> {
                        println("DEBUG: observer activated")
                        // update notification
                        persistentTimedNotification(this, it, _title)
                    })
                }
                State.INACTIVE -> {
                    // no notification needed
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