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
    private var tickCb: ((Long) -> Unit)? = null
    private var timerCb: ((Long) -> Unit)? = null
    private var changeStateCb: ((State) -> Unit)? = null
    private var isTickActive: Boolean = false

    fun onTick(secondsUntilFinished: UInt) {
        when(isTickActive) {
            true -> {
                persistentTimedNotification(this, secondsUntilFinished, _title)
                tickCb?.let{ func -> func(secondsUntilFinished.toLong()) }
            }
            false -> {}
        }
    }

    override fun onCreate() {
        super.onCreate()
        println("DEBUG: service created")
        _timer.sessionTimeSeconds.observe(this, Observer<UInt> {
            timerCb?.let { func ->
                func(it.toLong())
            }
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
                    isTickActive = false
                    println("DEBUG: Notifying")
                    endNotification(this, _title, _type)
                }
                State.ACTIVE_PAUSED -> {
                    isTickActive = false
                    _timer.sessionTimeSecondsLeft.value?.let { it1 ->
                        persistentTimedNotification(this,
                            it1, _title)
                    }
                }
                State.ACTIVE_RUNNING -> {
                    // attach an observer
                    println("DEBUG: State is active")
                    isTickActive = true
                }
                State.INACTIVE -> {
                    // no notification needed
                    with(NotificationManagerCompat.from(this)) {
                        println("DEBUG: removing all notifications")
                        cancelAll()
                    }
                    isTickActive = false
                    println("DEBUG: no notification")
                }
            }

            // required for UI updates
            changeStateCb?.let { func -> func(it) }
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

        fun setCallback(tickCb: ((Long) -> Unit)?,
                        timerCb: ((Long) -> Unit)?,
                        changeStateCb: ((State) -> Unit)?) {
            this@TimerService.tickCb = tickCb
            this@TimerService.timerCb = timerCb
            this@TimerService.changeStateCb = changeStateCb
        }

        fun title(s: String) {
            _title = s
        }

        fun type(s: String) {
            _type = s
        }
    }
}