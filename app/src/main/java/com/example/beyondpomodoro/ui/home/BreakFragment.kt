package com.example.beyondpomodoro.ui.home

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.beyondpomodoro.R
import com.example.beyondpomodoro.sessiontype.Break
import kotlinx.coroutines.launch

class BreakFragment : TimerFragment() {

    private var sprites: List<Int> = listOf(R.drawable.fox_run, R.drawable.worm_run_idle, R.drawable.blue_slime, R.drawable.green_slime, R.drawable.orange_slime)
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

        setHasOptionsMenu(true)
    }

    override fun addButtons() {
        super.addButtons()
        setSessionTime(breakTimeSeconds)
        notificationTitle(getString(R.string.notification_break_message))
        type("Break")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == android.R.id.home) {
            handleBackButton()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleBackButton() {

        setSessionTime(sessionTimeSeconds)
        timerReset()
        findNavController().popBackStack()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isEnabled) {
                    isEnabled = false
                    handleBackButton()
                }
            }
        })

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

    override fun doNotDisturb() {
        super.doNotDisturb()

    }

    override fun onTimerFinish() {
        super.onTimerFinish()
        // hide end button
        endButton.visibility = View.INVISIBLE
        controlButtonAction {
            endSession()
        }
        val toast = Toast.makeText(
            view?.context,
            getString(R.string.pomodoro_break_end_message),
            Toast.LENGTH_SHORT
        )
        toast.show()

        startButton.text = getString(R.string.pomodoro_break_end)
    }

    fun backToPomodoro () {
        findNavController().navigateUp()
    }

    override fun saveSession() {
        backToPomodoro()
    }

    override fun endSession() {
        super.endSession()
        startButton.text = context?.getString(R.string.pomodoro_break_end)
        endButton.visibility = View.INVISIBLE
        super.setSessionTime(sessionTimeSeconds)
        timerReset()
        backToPomodoro()
    }

    override fun confirmEndSession() {
        // check if break should be ended?
        // or just go ahead and end it anyway?
        endSession()
    }

    override fun addToSessionList() {
        super.addToSessionList()
        // did user start this session?
        // if so, then either the session is currrently paused or it's running
        if (timer.state.value == State.INACTIVE) {

            return
        }

        lifecycleScope.launch {
            sessionId.let {
                sessionDao?.updateBreak(
                    Break(
                        breakTimeSeconds.toInt(),
                        it
                    )
                )
            }


        }
    }
}
