package com.example.beyondpomodoro.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.beyondpomodoro.R

open class TimerFragment : Fragment() {
    protected var timer: PomodoroTimer? = null

    companion object {
    }

    protected lateinit var timerViewModel: TimerViewModel

    open fun startSession() {
    }

    fun setSessionTime(s: UInt) {
        println("DEBUG: setSessionTime, $s")
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
        val toast = Toast.makeText(
            view?.context,
            getString(R.string.pomodoro_toast_session_complete),
            Toast.LENGTH_SHORT
        )
        toast.show()

    }

    open fun saveSession() {
    }

    open fun endSession() {
    }

    open fun confirmEndSession() {
    }

    open fun updateVisualBlocks(millisUntilFinished: Long) {

    }

}