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
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.beyondpomodoro.R
import com.example.beyondpomodoro.databinding.FragmentHomeBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.*
import kotlin.math.ceil

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

open class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    open fun saveSession() {
        val toast = Toast.makeText(
            view?.context,
            "Session saved",
            Toast.LENGTH_SHORT
        )
        toast.show()

        // end time in millis
        homeViewModel.sessionEndTimeMillis = Calendar.getInstance().timeInMillis

        // create a calendar event description from tags added
        var descriptionSuggestion = view?.findViewById<ChipGroup>(R.id.chipGroup)?.children?.toList()?.map { c -> (c as Chip).text.toString() }?.reduceOrNull() { acc, c -> "$acc, $c" }
        if (descriptionSuggestion.isNullOrBlank()) {
            descriptionSuggestion = ""
        }
        // create calendar intent
        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, homeViewModel.sessionStartTimeMillis)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, homeViewModel.sessionEndTimeMillis)
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
        return (homeViewModel.sessionTimeSecondsLeft/60u).toString().padStart(2, '0') + ":" + (homeViewModel.sessionTimeSecondsLeft%60u).toString().padStart(2, '0')
    }

    private fun clockReset() {
        // no save
        // reset the clock
        homeViewModel.countDownTimer?.cancel()

        // set timer back to full
        homeViewModel.sessionTimeSecondsLeft = homeViewModel.sessionTimeSeconds
        // set text back to timer
        homeViewModel.textViewSeconds?.text = convertMinutesToDisplayString()
    }

    private fun pomodoroReset() {
        // set pomodoro completeness as false
        homeViewModel.pomodoroComplete = false
        homeViewModel.pomodoroActive = false
    }

    open fun endSession() {

        clockReset()
        pomodoroReset()

        // endbutton hide
        homeViewModel.endButton?.visibility = INVISIBLE

        // start button
        homeViewModel.button?.text = getString(R.string.pomodoro_start_session_button)

        // show all visual image blocks
        showAllVisualBlocks()

        findNavController().navigate(R.id.action_nav_pomodoro_to_analyticsFragment)
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

        setupVisualBlocks(view)
        homeViewModel.chipGroup = view.findViewById<ChipGroup>(R.id.chipGroup)

        // all existing tags
        homeViewModel.tags.forEach {
            val chip = Chip(this.requireContext())
            chip.text = it.key
            chip.setCloseIconVisible(true)
            chip.setOnCloseIconClickListener {
                // remove chip from chipgroup
                homeViewModel.chipGroup?.removeView(chip)
                homeViewModel.tags.remove(tag)
            }
            homeViewModel.chipGroup?.addView(chip)
        }


        homeViewModel.editTags = view.findViewById<EditText>(R.id.editTextTags)
        homeViewModel.editTags?.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEND -> {
                    // add a new chip to the group
                    val tag = homeViewModel.editTags?.text.toString()
                    when(homeViewModel.chipGroup?.children?.toList()?.any { c -> ((c as Chip).text.toString()) == tag }) {
                        false -> {
                            val chip = Chip(this.requireContext())
                            chip.text = tag
                            chip.setCloseIconVisible(true)
                            chip.setOnCloseIconClickListener {
                                // remove chip from chipgroup
                                homeViewModel.chipGroup?.removeView(chip)
                                homeViewModel.tags.remove(tag)
                            }
                            homeViewModel.chipGroup?.addView(chip)
                            homeViewModel.tags.put(tag, tag)
                        }
                        else ->
                            {
                                // TODO: highlight the chip if already present
                            }
                    }
                    homeViewModel.editTags?.setText("")
                    true
                }
                else -> {
                    false
                }
            }
        }

        homeViewModel.textViewSeconds = view.findViewById<TextView>(R.id.textView2).apply {
            text = convertMinutesToDisplayString()

            // onclick open dialog to enter time
            setOnClickListener {
                when(cls.homeViewModel.pomodoroActive and !cls.homeViewModel.pomodoroComplete) {
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

        homeViewModel.endButton = view.findViewById<Button>(R.id.button4).apply {
            this.setOnClickListener {
                confirmEndSession()
            }
        }

        homeViewModel.button = view.findViewById<Button>(R.id.button).apply {
            val but = this
            this.setOnClickListener {
                if (cls.homeViewModel.pomodoroActive) {
                    cls.homeViewModel.countDownTimer?.cancel()
                    cls.homeViewModel.pomodoroActive = false
                    but.text = context.getString(R.string.pomodoro_resume_session_button)
                }
                else if ( (cls.homeViewModel.pomodoroActive == false) and (cls.homeViewModel.pomodoroComplete == false) ){
                    cls.homeViewModel.pomodoroActive = true
                    cls.homeViewModel.countDownTimer = countDownTimerCreate(view, cls, (cls.homeViewModel.sessionTimeSecondsLeft * 1000u).toLong(), but)
                    cls.homeViewModel.countDownTimer?.start()
                    cls.homeViewModel.textViewSeconds!!.text = convertMinutesToDisplayString()

                    but.text = context.getString(R.string.pomodoro_pause_session_button)
                    homeViewModel.endButton?.visibility = View.VISIBLE

                    // save session start time
                    cls.homeViewModel.sessionStartTimeMillis = Calendar.getInstance().timeInMillis
                }
                else if (cls.homeViewModel.pomodoroComplete) {
                    // this is now a save session button
                    cls.saveSession()
                }
            }
        }
    }

    fun setupVisualBlocks(view: View) {
        // create array of buttons
        // TODO: setup colour themes etc if needed
        val imageButtonIds = arrayOf(
            R.id.imageButton1,
            R.id.imageButton2,
            R.id.imageButton3,
            R.id.imageButton4,
            R.id.imageButton5,
            R.id.imageButton6,
            R.id.imageButton7,
            R.id.imageButton8,
            R.id.imageButton9,
        )

        homeViewModel.imageButtonList = imageButtonIds.map {
            view.findViewById(it)
        }

        println("DEBUG: Found $homeViewModel.imageButtonList.size visual blocks")
    }

    fun showAllVisualBlocks() {
        // when user ends session, set all visual blocks back to active
        homeViewModel.imageButtonList?.forEach {
            it?.visibility = VISIBLE
        }
    }

    fun disappearVisualBlockAt(idx: Int) {
        homeViewModel.imageButtonList?.let { list ->
            list[idx]?.let {
                it.visibility = VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    open fun updateVisualBlocks(millisUntilFinished: Long) {
        homeViewModel.sessionTimeSecondsLeft = (millisUntilFinished.toUInt())/1000u
        homeViewModel.textViewSeconds?.text = convertMinutesToDisplayString()

        // check if any visualblocks to be disappeared?
        val numBlocksShow = ceil(((millisUntilFinished.toFloat() / 1000f) / (homeViewModel.sessionTimeSeconds.toFloat()) * 9f)).toUInt()
        println("numblocks: $numBlocksShow")
        if(homeViewModel.numBlocksShow != numBlocksShow) {
            // number of blocks to show changed
            homeViewModel.imageButtonList?.let {
                it.subList(numBlocksShow.toInt(), 9).forEach {
                    it?.visibility = INVISIBLE
                }
                homeViewModel.numBlocksShow = numBlocksShow
            }
        }
    }

    private fun hideAllVisualBlocks() {
        homeViewModel.imageButtonList?.forEach {
            it?.visibility = INVISIBLE
        }

    }

    fun setSessionTime(s: UInt) {
        homeViewModel.sessionTimeSeconds = s
        homeViewModel.sessionTimeSecondsLeft = s
        homeViewModel.textViewSeconds?.text = convertMinutesToDisplayString()
    }

    open fun onTimerFinish() {
        hideAllVisualBlocks()

        val toast = Toast.makeText(
            view?.context,
            getString(R.string.pomodoro_toast_session_complete),
            Toast.LENGTH_SHORT
        )
        toast.show()
        homeViewModel.button?.text = getString(R.string.pomodoro_save_session_button)
        homeViewModel.pomodoroComplete = true
        homeViewModel.pomodoroActive = false
    }

    open fun confirmEndSession() {
        // Confirm if session to be saved?
        EndSessionDialogFragment(this).show(parentFragmentManager, "end_session")
    }
}

class HomePomodoroBreakFragment: HomeFragment() {
    private lateinit var breakViewModel: BreakViewModel
    private var _binding: FragmentHomeBinding? = null


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onTimerFinish() {
        val toast = Toast.makeText(
            view?.context,
            getString(R.string.pomodoro_toast_session_complete),
            Toast.LENGTH_SHORT
        )
        toast.show()
        breakViewModel.button?.text = getString(R.string.pomodoro_save_session_button)
        breakViewModel.pomodoroComplete = true
        breakViewModel.pomodoroActive = false

        // hide end button
        breakViewModel.endButton?.visibility = INVISIBLE
    }

    fun backToPomodoro () {
        // TODO:
        // replace the fragment with a HomeFragment

    }

    override fun saveSession() {
        backToPomodoro()
    }

    override fun endSession() {
        // endbutton hide if prematurely ended
        breakViewModel.endButton?.visibility = INVISIBLE

        // start button
        breakViewModel.button?.text = getString(R.string.pomodoro_end_break_button)

        backToPomodoro()
    }

    override fun confirmEndSession() {
        // check if break should be ended?
        // or just go ahead and end it anyway?
        endSession()
    }
    override fun updateVisualBlocks(millisUntilFinished: Long) {
        breakViewModel.sessionTimeSecondsLeft = (millisUntilFinished.toUInt())/1000u
        breakViewModel.textViewSeconds?.text = convertMinutesToDisplayString()
    }
}

fun countDownTimerCreate(view: View, cls: HomeFragment, millisLeft: Long, but: Button) : CountDownTimer {
    return object: CountDownTimer(millisLeft, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            cls.updateVisualBlocks(millisUntilFinished)
        }

        override fun onFinish() {
            cls.onTimerFinish()
        }
    }
}