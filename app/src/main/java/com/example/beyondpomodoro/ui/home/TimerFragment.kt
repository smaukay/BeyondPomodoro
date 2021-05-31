package com.example.beyondpomodoro.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.beyondpomodoro.R

open class TimerFragment : Fragment() {
    private var notificationId: Int = 0
    protected var breakTimeSeconds: UInt = 5u * 60u
    protected var sessionTimeSeconds: UInt = 25u * 60u

    protected var timer: PomodoroTimer? = null
    protected open var notificationTitle: String = ""

    protected val sharedData: SharedViewModel by activityViewModels()
    companion object {
    }

    protected lateinit var timerViewModel: TimerViewModel

    open fun startSession() {
    }

    open fun setSessionTime(s: UInt) {
        timer = view?.let { PomodoroTimer(s, it,this) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.timer_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        timerViewModel = ViewModelProvider(this).get(TimerViewModel::class.java)

        // get the last activity type on activity creation and store in sharedData
        activity?.getPreferences(Context.MODE_PRIVATE)?.let { prefs ->
            if (prefs.contains("lastSessionType")) {
                sharedData.sessionType?.value = prefs.getString("lastSessionType", "default")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedData.sessionType?.let { sessionType ->
            activity?.getPreferences(Context.MODE_PRIVATE)?.let { prefs ->
                sessionTimeSeconds = prefs.getInt("pomodoroTimeFor$sessionType", 25).toUInt() * 60u
                breakTimeSeconds = prefs.getInt("breakTimeFor$sessionType", 5).toUInt() * 60u
            }
        }
    }

    open fun onTimerFinish() {
        context?.let {
            with(NotificationManagerCompat.from(it)) {
                cancelAll()
            }
        }

        println("DEBUG: Notifying")
        endNotification()
    }

    open fun persistentTimedNotification() {
        context?.let {
            val builder = NotificationCompat.Builder(it, getString(R.string.persistent_channel_id))
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle("$notificationTitle")
                .setContentText("Time remaining: ${timer?.convertMinutesToDisplayString()}")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
            with(NotificationManagerCompat.from(it)) {
                // notificationId is a unique int for each notification that you must define
                notify(notificationId, builder.build())
            }
        }
    }

    open fun endNotification() {
        context?.let {
            val builder = NotificationCompat.Builder(it, getString(R.string.alert_channel_id))
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle("$notificationTitle")
                .setContentText("Session complete")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            with(NotificationManagerCompat.from(it)) {
                // notificationId is a unique int for each notification that you must define
                notify(notificationId + 1, builder.build())
            }
        }
    }

    open fun saveSession() {
    }

    open fun endSession() {
    }

    open fun confirmEndSession() {
    }

    open fun updateVisualBlocks(millisUntilFinished: Long) {
        persistentTimedNotification()
    }

}