package com.smaukay.beyondpomodoro.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.smaukay.beyondpomodoro.R

class SetTimeDialogFragment(caller: TimerFragment): DialogFragment() {
    private val caller: TimerFragment = caller

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val minutesEditText = view.findViewById<EditText>(R.id.editTextSessionMinutes)
        view.findViewById<Button>(R.id.set_time_button).apply {

            setOnClickListener {
                when(minutesEditText.text.isNotEmpty()) {
                    true -> {
                        caller.setSessionTime(minutesEditText.text.toString().toUInt() * 60u)
                        caller.timerReset()
                    }
                }
                dismiss()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.pick_sesstion_time, container, false)
    }
}