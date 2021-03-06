package com.smaukay.beyondpomodoro.ui.home

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import kotlin.math.floor


enum class State {
    INACTIVE {
        override fun nextState() = ACTIVE_RUNNING
    },

    ACTIVE_RUNNING {
        override fun nextState() = ACTIVE_PAUSED
    },

    ACTIVE_PAUSED {
        override fun nextState() = ACTIVE_RUNNING
    },

    COMPLETE {
        override fun nextState() = INACTIVE
    };

    abstract fun nextState(): State
}

class PomodoroTimer(sessionTimeSeconds: UInt) {
    val percentage: MutableLiveData<UInt> by lazy {
        MutableLiveData<UInt>(9u)
    }
    val state: MutableLiveData<State> by lazy {
        MutableLiveData<State>(State.INACTIVE)
    }

    val sessionTimeSecondsLeft: MutableLiveData<UInt> by lazy {
        MutableLiveData<UInt>(sessionTimeSeconds)
    }

    val sessionTimeSeconds: MutableLiveData<UInt> by lazy {
        MutableLiveData<UInt>(sessionTimeSeconds)
    }

    var countDownTimer: CountDownTimer? = null

    fun nextState() {
        when(state.value) {
            State.INACTIVE, State.ACTIVE_PAUSED -> {
                sessionTimeSecondsLeft.value?.times(1000u)?.let { countDownTimerCreate(it.toLong()) }
                countDownTimer?.start()
            }

            State.ACTIVE_RUNNING -> {
                countDownTimer?.cancel()
            }

            State.COMPLETE -> {
            }
        }
        state.value = state.value?.nextState()

    }

    fun pomodoroReset() {
        // set pomodoro completeness as false
        state.apply {
            value = State.INACTIVE
        }
    }

    fun clockReset() {
        // no save
        // reset the clock
        countDownTimer?.cancel()

        // set timer back to full
        sessionTimeSecondsLeft.apply {
            value = sessionTimeSeconds.value
        }
    }

    private fun updatePercentage() {
        percentage.apply {
            value = sessionTimeSeconds.value?.toDouble()?.let {
                (sessionTimeSecondsLeft.value?.times(9u))?.toDouble()?.div(it)
                    ?.let { floor(it).toUInt() }
            }
        }
    }

    private fun updateVisualBlocks(millisUntilFinished: Long) {
        sessionTimeSecondsLeft.apply {
            value = (millisUntilFinished.toUInt())/1000u
        }

        updatePercentage()
    }

    fun setSessionTime(s: UInt) {

        sessionTimeSeconds.apply {
            value = s
        }

        sessionTimeSecondsLeft.apply {
            value = s
        }

        updatePercentage()
    }

    private fun onTimerFinish() {
        state.apply {
            value = State.COMPLETE
        }
    }

    private fun countDownTimerCreate(millisLeft: Long) {
        countDownTimer = object: CountDownTimer(millisLeft, 1000) {
            override fun onTick(millisUntilFinished: Long) {

                updateVisualBlocks(millisUntilFinished)
            }

            override fun onFinish() {
                onTimerFinish()
            }
        }
        updatePercentage()
    }
}
