package com.smaukay.beyondpomodoro.ui.home

import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import com.smaukay.beyondpomodoro.MainActivity
import com.smaukay.beyondpomodoro.R

fun persistentNotification(context: Context, secondsUntilFinished: UInt, notificationTitle: String, type: String, state: State): Notification {
    val action = when(state) {
        State.ACTIVE_RUNNING -> "Pause"
        State.ACTIVE_PAUSED -> "Resume"
        State.INACTIVE -> "Start"
        State.COMPLETE -> "Open"
    }
    val builder = NotificationCompat.Builder(context, context.getString(R.string.persistent_channel_id))
        .setSmallIcon(R.drawable.app_logo)
        .setContentTitle(notificationTitle)
        .setContentText("Time remaining: ${convertMinutesToDisplayString(secondsUntilFinished)}")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setContentIntent(
            // Create the TaskStackBuilder
            openApp(context, type)
        )
        .addAction(R.drawable.ic_menu_send,
            action,
            PendingIntent.getBroadcast(context,
                0,
                Intent("com.smaukay.beyondpomodoro.$action").putExtra("type", type),
                FLAG_UPDATE_CURRENT
            )
        )

    with(NotificationManagerCompat.from(context)) {
        // notificationId is a unique int for each notification that you must define

        return builder.build()
    }
}

fun persistentTimedNotification(context: Context, secondsUntilFinished: UInt, notificationTitle: String, type: String, state: State) {
    // create timer service
    with(NotificationManagerCompat.from(context)) {
        val notification = persistentNotification(
            context,
            secondsUntilFinished,
            notificationTitle,
            type,
            state
        )
        notify(1, notification)
    }
}

fun endNotification(context: Context, notificationTitle: String, type: String) {
    // switch off ringer

    (context as Service).stopForeground(true)
    val builder = NotificationCompat.Builder(context, context.getString(R.string.alert_channel_id))
        .setSmallIcon(R.drawable.app_logo)
        .setContentTitle(notificationTitle)
        .setContentText("$type complete")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(
            openApp(context, type)
        )
    with(NotificationManagerCompat.from(context)) {
        // notificationId is a unique int for each notification that you must define

        notify( 1, builder.build())
    }
}

fun openApp(context: Context, type: String?): PendingIntent {
    return NavDeepLinkBuilder(context)
        .setComponentName(MainActivity::class.java)
        .setGraph(R.navigation.mobile_navigation)
        .setDestination(
            when (type) {
                "Pomodoro" -> R.id.pomodoroFragment
                "Break" -> R.id.breakFragment
                else -> R.id.sessionListFragment
            }
        )
        .createPendingIntent()
}

class TimerActions: BroadcastReceiver() {
    var timer: PomodoroTimer? = null
    fun doOnAction(a: String, type: String?, context: Context?) {

        when(a) {
            "Pause" -> {
                timer?.nextState()
            }
            "Resume" -> {
                timer?.nextState()
            }
            "Open" -> {
                // this requires opening the app
                context?.let {
                    openApp(it, type)
                }
            }
            "Start" -> {

            }
        }
    }
    override fun onReceive(context: Context?, intent: Intent?) {

        val action = intent?.action
        val type = intent?.getStringExtra("type")

        action?.let {
            doOnAction(it.split(".").last(), type, context)
        }
    }
}
