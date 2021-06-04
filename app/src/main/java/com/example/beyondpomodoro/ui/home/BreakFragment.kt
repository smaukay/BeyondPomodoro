package com.example.beyondpomodoro.ui.home

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.beyondpomodoro.R

class BreakFragment : TimerFragment() {

    private var sprites: List<Int> = listOf(R.drawable.fox_run, R.drawable.worm_run_idle, R.drawable.blue_slime, R.drawable.green_slime, R.drawable.orange_slime)
    companion object {
    }

    private lateinit var viewModel: BreakViewModel

    override var notificationTitle = "Break running"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.createViewModel()
        return inflater.inflate(R.layout.break_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(BreakViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        super.addButtons()
        timerViewModel.timer.setSessionTime(breakTimeSeconds)

        view.findViewById<ImageView>(R.id.breakSprite)?.let {
            // TODO: set sprite based on how well the session went?
            it.setBackgroundResource(sprites.random())
            (it.background as AnimationDrawable).start()
        }
    }

    override fun setSessionTime(s: UInt) {
        super.setSessionTime(s)
        breakTimeSeconds = s
    }

    override fun onTimerFinish() {
        val toast = Toast.makeText(
            view?.context,
            getString(R.string.pomodoro_break_end_message),
            Toast.LENGTH_SHORT
        )
        toast.show()

        startButton.text = getString(R.string.pomodoro_break_end)
    }

    fun backToPomodoro () {
        // TODO:
        // replace the fragment with a HomeFragment
        findNavController().navigateUp()
    }

    override fun saveSession() {
        backToPomodoro()
    }

    override fun endSession() {
        timerViewModel.timer.clockReset()
        timerViewModel.timer.pomodoroReset()
        super.buttonsReset()

        backToPomodoro()
    }

    override fun confirmEndSession() {
        // check if break should be ended?
        // or just go ahead and end it anyway?
        endSession()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // since the screen is changing, the entered tags along with the session time, break time can be saved with an ID
        activity?.getPreferences(Context.MODE_PRIVATE)?.let {
            sharedData.sessionType?.toString()?.let { sessionId ->
                it.edit().apply {
                    timerViewModel.timer.sessionTimeSeconds.let { sessionTimeSeconds ->
                        sessionTimeSeconds.value?.let { value ->
                            putInt("breakTimeFor${sessionId}", (value/60u).toInt())
                        }
                    }
                    apply()
                }
            }
        }
    }
}
