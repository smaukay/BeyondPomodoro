package com.smaukay.beyondpomodoro.ui.home

import android.content.BroadcastReceiver
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.smaukay.beyondpomodoro.R

class TimerService : LifecycleService() {
    private var isForeground: Boolean = false
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

    @RequiresApi(Build.VERSION_CODES.O)
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
                    stopForeground()
                    if(hasDndPermissions(this as ContextWrapper)){
                        setRingerNormal(this)
                    }
                    else {
                        // do nothing
                        // the notification is gonna be missed :(
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
                    startForegroundIfNot()
                }
                State.INACTIVE -> {
                    stopForeground()
                    // no notification needed
                    with(NotificationManagerCompat.from(this)) {
                        cancelAll()
                        Log.d("TimerService", "All notifications cancelled")
                    }
                }
            }
        })
    }

    fun startForegroundIfNot() {
        if (!isForeground) {
            val builder = NotificationCompat.Builder(this, this.getString(R.string.persistent_channel_id))
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle("$_type running")
                .setContentText("")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOnlyAlertOnce(true)
                .setContentIntent(
                    // Create the TaskStackBuilder
                    openApp(this, _type)
                )
            with(NotificationManagerCompat.from(this)) {
                startForeground(1, builder.build())
            }
            isForeground = true
        }
    }

    fun stopForeground() {
        if(isForeground) {
            with(NotificationManagerCompat.from(this)) {
                stopForeground(true)
            }
            isForeground = false
            Log.d("TimerService", "Foreground service stopped")
        }
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