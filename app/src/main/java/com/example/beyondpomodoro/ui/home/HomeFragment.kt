package com.example.beyondpomodoro.ui.home

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
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
                    { dialog, id ->
                        // save session fragment
                        caller.saveSession()
                    })
                .setNegativeButton(R.string.end_session_nosave,
                    { dialog, id ->
                        // no save :(
                        caller.endSession()
                    })
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}

class SetTimeDialogFragment(caller: TimerFragment): DialogFragment() {
    private val caller: TimerFragment = caller

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val minutesEditText = view.findViewById<EditText>(R.id.editTextSessionMinutes)
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

open class HomeFragment : TimerFragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    // override notification title
    override var notificationTitle = "Pomodoro Running"

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // tag colours in an array
    private val tagColours = listOf(R.color.tag_1, R.color.tag_2, R.color.tag_3, R.color.tag_4, R.color.tag_5)
    override fun saveSession() {
        val toast = Toast.makeText(
            view?.context,
            "Session saved",
            Toast.LENGTH_SHORT
        )
        toast.show()

        // end time in millis
        homeViewModel.sessionEndTimeMillis = Calendar.getInstance().timeInMillis

        // create a calendar event description from tags added
        var descriptionSuggestion = view?.findViewById<ChipGroup>(R.id.chipGroup)?.children?.toList()?.map { c -> (c as Chip).text.toString() }?.reduceOrNull { acc, c -> "$acc, $c" }
        if (descriptionSuggestion.isNullOrBlank()) {
            descriptionSuggestion = ""
        }
        // create calendar intent
        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, homeViewModel.sessionStartTimeMillis)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, homeViewModel.sessionEndTimeMillis)
            .putExtra(CalendarContract.Events.TITLE, homeViewModel.title)
            .putExtra(CalendarContract.Events.DESCRIPTION, descriptionSuggestion)
            // .putExtra(CalendarContract.Events.EVENT_LOCATION, "The gym")
            // .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
            // .putExtra(Intent.EXTRA_EMAIL, "rowan@example.com,trevor@example.com")
        startActivity(intent)


        // after saving we end session anyway
        endSession()
    }

    override fun endSession() {

        timer?.clockReset()
        timer?.pomodoroReset()
        timer?.buttonsReset()

        // show all visual image blocks
        showAllVisualBlocks()

        // clear title field
        homeViewModel.editTitle?.setText("")
        findNavController().navigate(R.id.action_nav_pomodoro_to_breakFragment)
    }

    override fun setSessionTime(s: UInt) {
        super.setSessionTime(s)
        sessionTimeSeconds = s
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val cls = this

        timer = PomodoroTimer(sessionTimeSeconds, view,this)

        setupVisualBlocks(view)
        homeViewModel.chipGroup = view.findViewById(R.id.chipGroup)

        // all existing tags
        println("DEBUG: tags already found: ${homeViewModel.tags.size}")
        homeViewModel.tags.forEach {
            val chip = Chip(this.requireContext())
            chip.text = it.key
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener { view ->
                // remove chip from chipgroup
                homeViewModel.chipGroup?.removeView(chip)
                homeViewModel.tags.remove(it.key)
            }

            activity?.getPreferences(Context.MODE_PRIVATE)?.let { prefs ->
                if(prefs.contains("tag_colour_${it.key}")) {
                    chip.chipBackgroundColor = ColorStateList.valueOf(prefs.getInt("tag_colour_${it.key}", 1))
                }
            }
            homeViewModel.chipGroup?.addView(chip)
        }

        homeViewModel.editTitle = view.findViewById<EditText>(R.id.editTextTitle).apply {
            doOnTextChanged { text, start, before, count ->
                homeViewModel.title = text.toString()
            }
        }

        homeViewModel.editTags = view.findViewById(R.id.editTextTags)
        homeViewModel.editTags?.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEND -> {
                    // add a new chip to the group
                    val tag = homeViewModel.editTags?.text.toString()
                    when(homeViewModel.chipGroup?.children?.toList()?.any { c -> ((c as Chip).text.toString()) == tag }) {
                        false -> {
                            val chip = Chip(this.requireContext())
                            chip.text = tag
                            chip.isCloseIconVisible = true
                            chip.setOnCloseIconClickListener {
                                // remove chip from chipgroup
                                homeViewModel.chipGroup?.removeView(chip)
                                homeViewModel.tags.remove(tag)
                            }
                            // TODO: was this tag previously entered?
                            // if so, what colour was assigned to it?
                            activity?.getPreferences(Context.MODE_PRIVATE)?.let { prefs ->
                                chip.chipBackgroundColor = if(prefs.contains("tag_colour_${tag}")) {
                                    ColorStateList.valueOf(prefs.getInt("tag_colour_${tag}", 1))
                                } else {
                                    context?.let {
                                        ContextCompat.getColor(it, tagColours.random() ).let { colourSelect ->
                                            // save this colour for this tag
                                            with(prefs?.edit()) {
                                                putInt("tag_colour_${tag}", colourSelect)
                                                apply()
                                            }
                                            ColorStateList.valueOf(colourSelect)
                                        }
                                    }
                                }
                            }
                            homeViewModel.tags[tag] = tag
                            println("DEBUG: added to tags: ${homeViewModel.tags.size}")
                            homeViewModel.chipGroup?.addView(chip)
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
    }

    private fun setupVisualBlocks(view: View) {
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

    }

    private fun showAllVisualBlocks() {
        // when user ends session, set all visual blocks back to active
        homeViewModel.imageButtonList?.forEach {
            it?.visibility = VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // did user start this session?
        // if so, then either the session is currrently paused or it's running
        if(timer?.state == State.INACTIVE) {
            return
        }

        // since the screen is changing, the entered tags along with the session time, break time can be saved with an ID
        activity?.getPreferences(Context.MODE_PRIVATE)?.let {
            val tags = homeViewModel.chipGroup?.children?.toList()?.sortedBy { c -> c.toString() }?.map { c ->
                c.toString()
            }?.reduceOrNull { acc, s -> "$acc,$s" }

            var sessionId = if(tag != null) {
                tags
            } else {
                ""
            } + ";${(sessionTimeSeconds/60u)};5"

            when(it.contains("sessionList")) {
                true -> {
                    it.getString("sessionList", "")?.split("<SESNAME>")?.let {
                            sessionList ->
                        if(sessionList.contains(sessionId)) {
                            sharedData.sessionType?.value = sessionId
                        } else {
                            it.edit().apply {
                                putString("sessionList", "${it.getString("sessionList", "")}<SESNAME>$sessionId")
                                apply()
                            }
                        }
                    }
                }
                false -> {
                    it.edit().apply {
                        putString("sessionList", "$sessionId")
                        apply()
                    }
                }
            }

            it.edit().apply {
                timer?.sessionTimeSeconds?.let { sessionTimeSeconds ->
                    putInt("pomodoroTimeFor${sessionId}", (sessionTimeSeconds/ 60u).toInt())
                }
                putInt("breakTimeFor${sessionId}", 5)
                putString("tagsFor${sessionId}", tags)
                apply()
            }
        }
    }

    override fun updateVisualBlocks(millisUntilFinished: Long) {
        super.updateVisualBlocks(millisUntilFinished)

        // check if any visualblocks to be disappeared?
        val numBlocksShow = ceil(((millisUntilFinished.toFloat() / 1000f) / (timer!!.sessionTimeSeconds.toFloat()) * 9f)).toUInt()
        if(homeViewModel.numBlocksShow != numBlocksShow) {
            // number of blocks to show changed
            homeViewModel.imageButtonList?.let { it ->
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

    override fun startSession() {
        homeViewModel.sessionStartTimeMillis = Calendar.getInstance().timeInMillis
    }

    override fun onTimerFinish() {
        super.onTimerFinish()
        hideAllVisualBlocks()

        val toast = Toast.makeText(
            view?.context,
            getString(R.string.pomodoro_toast_session_complete),
            Toast.LENGTH_SHORT
        )
        toast.show()
    }

    override fun confirmEndSession() {
        // Confirm if session to be saved?
        EndSessionDialogFragment(this).show(parentFragmentManager, "end_session")
    }
}

