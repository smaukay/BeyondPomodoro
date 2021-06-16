package com.example.beyondpomodoro.ui.home

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import com.example.beyondpomodoro.MainActivity
import com.example.beyondpomodoro.R

fun persistentTimedNotification(context: Context, secondsUntilFinished: UInt, notificationTitle: String, type: String) {
    val builder = NotificationCompat.Builder(context, context.getString(R.string.persistent_channel_id))
        .setSmallIcon(R.drawable.app_logo)
        .setContentTitle(notificationTitle)
        .setContentText("Time remaining: ${convertMinutesToDisplayString(secondsUntilFinished)}")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setContentIntent(
            NavDeepLinkBuilder(context)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.mobile_navigation)
            .setDestination(
                when(type) {
                    "Pomodoro" -> R.id.pomodoroFragment
                    "Break" -> R.id.breakFragment
                    else -> R.id.sessionInfoFragment
                }
            )
            .createPendingIntent()
        )

    with(NotificationManagerCompat.from(context)) {
        // notificationId is a unique int for each notification that you must define
        println("DEBUG: notify function")
        notify(0, builder.build())
    }
}

fun endNotification(context: Context, notificationTitle: String, type: String) {
    val builder = NotificationCompat.Builder(context, context.getString(R.string.alert_channel_id))
        .setSmallIcon(R.drawable.app_logo)
        .setContentTitle(notificationTitle)
        .setContentText("$type complete")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT).setContentIntent(
            NavDeepLinkBuilder(context)
                .setComponentName(MainActivity::class.java)
                .setGraph(R.navigation.mobile_navigation)
                .setDestination(
                    when(type) {
                        "Pomodoro" -> R.id.pomodoroFragment
                        "Break" -> R.id.breakFragment
                        else -> R.id.sessionListFragment
                    }
                )
                .createPendingIntent()
        )
    with(NotificationManagerCompat.from(context)) {
        // notificationId is a unique int for each notification that you must define
        println("DEBUG: end notify function")
        notify( 0, builder.build())
    }
}
