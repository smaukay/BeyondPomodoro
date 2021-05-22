package com.example.beyondpomodoro.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.beyondpomodoro.R

class BreakFragment : TimerFragment() {

    companion object {
    }

    private lateinit var viewModel: BreakViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.break_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(BreakViewModel::class.java)
    }

    override fun onTimerFinish() {
        val toast = Toast.makeText(
            view?.context,
            getString(R.string.pomodoro_break_end_message),
            Toast.LENGTH_SHORT
        )
        toast.show()

        timer.startButton.text = getString(R.string.pomodoro_break_end)
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
        backToPomodoro()
    }

    override fun confirmEndSession() {
        // check if break should be ended?
        // or just go ahead and end it anyway?
        endSession()
    }
    override fun updateVisualBlocks(millisUntilFinished: Long) {
    }
}
