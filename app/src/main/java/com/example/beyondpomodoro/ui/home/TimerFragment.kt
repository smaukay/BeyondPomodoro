package com.example.beyondpomodoro.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.beyondpomodoro.R

open class TimerFragment : Fragment() {
    private var notificationId: Int = 0
    protected var timer: PomodoroTimer? = null
    protected open var notificationTitle: String = ""

    companion object {
    }

    protected lateinit var timerViewModel: TimerViewModel

    open fun startSession() {
    }

    fun setSessionTime(s: UInt) {
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
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        timer = PomodoroTimer(30u, view,this)
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