package com.example.beyondpomodoro.ui.home

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.beyondpomodoro.R
import com.example.beyondpomodoro.databinding.FragmentHomeBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.*

class EndSessionDialogFragment(caller: HomeFragment) : DialogFragment() {
    private val caller: HomeFragment = caller
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.dialog_end_session)
                .setPositiveButton(R.string.end_session_save,
                    DialogInterface.OnClickListener { dialog, id ->
                        // save session fragment
                        this.caller.saveSession()
                    })
                .setNegativeButton(R.string.end_session_nosave,
                    DialogInterface.OnClickListener { dialog, id ->
                        // no save :(
                        this.caller.endSession()
                    })
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}

class SetTimeDialogFragment(caller: HomeFragment): DialogFragment() {
    private val caller: HomeFragment = caller

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val minutesEditText = view.findViewById<TextView>(R.id.editTextSessionMinutes)
        view.findViewById<Button>(R.id.set_time_button).apply {

            setOnClickListener {
                when(minutesEditText.text.isNotEmpty()) {
                    true -> caller.setSessionTime(minutesEditText.text.toString().toUInt() * 60u)
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

class HomeFragment : Fragment() {

    private var sessionTimeSecondsLeft: UInt = 30u
    private var sessionTimeSeconds: UInt = 30u
    private var editTags: EditText? = null
    private var pomodoroComplete: Boolean = false
    private var button: Button? = null
    private var endButton: Button? = null
    private var textViewSeconds: TextView? = null
    private var countDownTimer: CountDownTimer? = null
    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    // event variables
    private var sessionStartTimeMillis: Long? = null
    private var sessionEndTimeMillis: Long? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // timer state
    private var pomodoroActive = false

    fun saveSession() {
        val toast = Toast.makeText(
            view?.context,
            "Session saved",
            Toast.LENGTH_SHORT
        )
        toast.show()

        // end time in millis
        sessionEndTimeMillis = Calendar.getInstance().timeInMillis

        // create a calendar event description from tags added
        var descriptionSuggestion = view?.findViewById<ChipGroup>(R.id.chipGroup)?.children?.toList()?.map { c -> (c as Chip).text.toString() }?.reduceOrNull() { acc, c -> "$acc, $c" }
        if (descriptionSuggestion.isNullOrBlank()) {
            descriptionSuggestion = ""
        }
        // create calendar intent
        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, sessionStartTimeMillis)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, sessionEndTimeMillis)
            .putExtra(CalendarContract.Events.TITLE, "")
            .putExtra(CalendarContract.Events.DESCRIPTION, descriptionSuggestion)
            // .putExtra(CalendarContract.Events.EVENT_LOCATION, "The gym")
            // .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
            // .putExtra(Intent.EXTRA_EMAIL, "rowan@example.com,trevor@example.com")
        startActivity(intent)


        // after saving we end session anyway
        this.endSession();
    }

    fun convertMinutesToDisplayString() : String {
        return (sessionTimeSecondsLeft/60u).toString().padStart(2, '0') + ":" + (sessionTimeSecondsLeft%60u).toString().padStart(2, '0')
    }

    fun endSession() {
        // no save
        // reset the clock
        countDownTimer?.cancel()

        // set timer back to full
        sessionTimeSecondsLeft = sessionTimeSeconds
        // set text back to timer
        textViewSeconds?.text = convertMinutesToDisplayString()

        // endbutton hide
        this.endButton?.visibility = INVISIBLE

        // start button
        this.button?.text = getString(R.string.pomodoro_start_session_button)

        // set pomodoro completeness as false
        this.pomodoroComplete = false
        this.pomodoroActive = false

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val cls = this

        this.editTags = view.findViewById<EditText>(R.id.editTextTags)
        this.editTags?.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEND -> {
                    // add a new chip to the group
                    val tag = cls.editTags?.text.toString()
                    val chipgroup = view.findViewById<ChipGroup>(R.id.chipGroup)
                    when(chipgroup.children.toList().any { c -> ((c as Chip).text.toString()) == tag }) {
                        false -> {
                            val chip = Chip(this.requireContext())
                            chip.text = tag
                            chip.setCloseIconVisible(true)
                            chip.setOnCloseIconClickListener {
                                // remove chip from chipgroup
                                chipgroup.removeView(chip)
                            }
                            chipgroup.addView(chip)
                        }
                        else ->
                            {
                                // TODO: highlight the chip if already present
                            }
                    }
                    cls.editTags?.setText("")
                    true
                }
                else -> {
                    false
                }
            }
        }

        this.textViewSeconds = view.findViewById<TextView>(R.id.textView2).apply {
            text = convertMinutesToDisplayString()

            // onclick open dialog to enter time
            setOnClickListener {
                when(cls.pomodoroActive and !cls.pomodoroComplete) {
                    true ->
                        {
                            //TODO: send toast saying time cannot be changed
                            val toast = Toast.makeText(
                                view?.context,
                                "The session is already active. You can end it if you like.",
                                Toast.LENGTH_SHORT
                            )
                            toast.show()
                        }
                    false -> {
                        SetTimeDialogFragment(cls).show(parentFragmentManager, "pick_session_time")
                    }

                }

            }
        }
        this.endButton = view.findViewById<Button>(R.id.button4).apply {
            this.setOnClickListener {
                // Confirm if session to be saved?
                EndSessionDialogFragment(cls).show(parentFragmentManager, "end_session")
            }
        }
        this.button = view.findViewById<Button>(R.id.button).apply {
            val but = this
            this.setOnClickListener {
                if (cls.pomodoroActive) {
                    cls.countDownTimer?.cancel()
                    cls.pomodoroActive = false
                    but.text = context.getString(R.string.pomodoro_resume_session_button)
                }
                else if ( (cls.pomodoroActive == false) and (cls.pomodoroComplete == false) ){
                    cls.pomodoroActive = true
                    cls.countDownTimer = cls.countDownTimerCreate(textViewSeconds!!, cls, (cls.sessionTimeSecondsLeft * 1000u).toLong(), but)
                    cls.countDownTimer?.start()
                    textViewSeconds!!.text = convertMinutesToDisplayString()

                    but.text = context.getString(R.string.pomodoro_pause_session_button)
                    endButton?.visibility = View.VISIBLE

                    // save session start time
                    cls.sessionStartTimeMillis = Calendar.getInstance().timeInMillis
                }
                else if (cls.pomodoroComplete) {
                    // this is now a save session button
                    cls.saveSession()
                }
            }
        }
    }

    private fun changeTime() {
        TODO("Not yet implemented")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun countDownTimerCreate(textSeconds: TextView, cls: HomeFragment, millisLeft: Long, but: Button) : CountDownTimer {
        return object: CountDownTimer(millisLeft, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                cls.sessionTimeSecondsLeft = cls.sessionTimeSecondsLeft - 1u
                textSeconds.text = convertMinutesToDisplayString()
            }

            override fun onFinish() {
                val toast = Toast.makeText(
                    view?.context,
                    cls.getString(R.string.pomodoro_toast_session_complete),
                    Toast.LENGTH_SHORT
                )
                toast.show()
                but.text = getString(R.string.pomodoro_save_session_button)
                cls.pomodoroComplete = true
                cls.pomodoroActive = false
            }
        }
    }

    fun setSessionTime(s: UInt) {
        sessionTimeSeconds = s
        sessionTimeSecondsLeft = s
        textViewSeconds?.text = convertMinutesToDisplayString()
    }
}

