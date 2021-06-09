package com.example.beyondpomodoro.ui.home

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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.beyondpomodoro.R
import com.example.beyondpomodoro.databinding.FragmentHomeBinding
import com.example.beyondpomodoro.sessiontype.Pomodoro
import com.example.beyondpomodoro.sessiontype.Session
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.ceil

open class HomeFragment : TimerFragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    var editTitle: EditText? = null
    var editTags: EditText? = null
    var chipGroup: ChipGroup? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // tag colours in an array
    private val tagColours = listOf(R.color.tag_1, R.color.tag_2, R.color.tag_3, R.color.tag_4, R.color.tag_5)

    override fun addButtons() {
        notificationTitle("Session running")
        type("Pomodoro")
        timer.setSessionTime(sessionTimeSeconds)
        populateTags()
        super.addButtons()
        view?.let { setupVisualBlocks(it) }
        updateVisualBlocks(sessionTimeSeconds)
        view?.findViewById<TextView>(R.id.activity_name).apply {
            this?.text = title
        }
    }

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
            .putExtra(CalendarContract.Events.TITLE, title)
            .putExtra(CalendarContract.Events.DESCRIPTION, descriptionSuggestion)
            // .putExtra(CalendarContract.Events.EVENT_LOCATION, "The gym")
            // .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
            // .putExtra(Intent.EXTRA_EMAIL, "rowan@example.com,trevor@example.com")
        startActivity(intent)

        // after saving we end session anyway
        endSession()
    }

    override fun endSession() {
        super.endSession()

        // show all visual image blocks
        showAllVisualBlocks()

        // clear title field
        editTitle?.setText("")
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

    fun populateTags() {
        // all existing tags
        println("DEBUG: tags already found: ${tags.size}")

        tags.forEach {
            val chip = Chip(this.requireContext())
            chip.text = it
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener { view ->
                // remove chip from chipgroup
                chipGroup?.removeView(chip)
                tags.remove(it)
            }

            activity?.getPreferences(Context.MODE_PRIVATE)?.let { prefs ->
                if(prefs.contains("tag_colour_${it}")) {
                    chip.chipBackgroundColor = ColorStateList.valueOf(prefs.getInt("tag_colour_${it}", 1))
                }
            }
            chipGroup?.addView(chip)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chipGroup = view.findViewById(R.id.chipGroup)

        editTitle = view.findViewById<EditText>(R.id.editTextTitle)?.apply {
            setText("")
            doOnTextChanged { text, start, before, count ->
                title = text.toString()
            }
        }
        editTags = view.findViewById(R.id.editTextTags)
        editTags?.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEND -> {
                    // add a new chip to the group
                    val tag = editTags?.text.toString()
                    when(chipGroup?.children?.toList()?.any { c -> ((c as Chip).text.toString()) == tag }) {
                        false -> {
                            val chip = Chip(this.requireContext())
                            chip.text = tag
                            chip.isCloseIconVisible = true
                            chip.setOnCloseIconClickListener { _ ->
                                // remove chip from chipgroup
                                chipGroup?.removeView(chip)
                                tags.remove(v.tag)
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
                                            with(prefs.edit()) {
                                                putInt("tag_colour_${tag}", colourSelect)
                                                apply()
                                            }
                                            ColorStateList.valueOf(colourSelect)
                                        }
                                    }
                                }
                            }
                            tags.add(tag)
                            chipGroup?.addView(chip)
                        }
                        else ->
                            {
                                // TODO: highlight the chip if already present
                            }
                    }

                    editTags?.setText("")
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
    }

    override fun addToSessionList() {
        super.addToSessionList()
        // did user start this session?
        // if so, then either the session is currrently paused or it's running
        if(timer.state.value == State.INACTIVE) {
            println("DEBUG: not saving because timer state is ${timer.state.value}")
            return
        }

        println("DEBUG: adding to session list")
        // get tags
        val tags = view?.let {
            it.findViewById<ChipGroup>(R.id.chipGroup).children.toList().map { e ->
                (e as Chip).text.toString()
            }.toSet()
        }?: run {
            setOf<String>()
        }

        lifecycleScope.launch {
            println("DEBUG: coroutine starting")

            sessionId?.let {
                sessionDao?.updatePomodoro(
                    Pomodoro(
                        homeViewModel.title,
                        sessionTimeSeconds.toInt(),
                        System.currentTimeMillis(),
                        tags,
                        it
                    )
                )
            }?: run {
                // add a new session to database
                sessionId = sessionDao?.addSession(
                    Session(homeViewModel.title,
                        sessionTimeSeconds.toInt(),
                        300,
                        System.currentTimeMillis(),
                        tags
                    )
                )?.toInt()
            }

            println("DEBUG: session added to db")
        }
    }

    override fun updateVisualBlocks(secondsUntilFinished: UInt) {
        super.updateVisualBlocks(secondsUntilFinished)

        println("DEBUG: calling update visual blocks with $secondsUntilFinished")
        // check if any visualblocks to be disappeared?

        val numBlocksShow = timer.sessionTimeSeconds.value?.let { value ->
            val res = ceil((secondsUntilFinished.toDouble()/value.toDouble()) * 9f).toInt()
            println("DEBUG: showing $res blocks since value is $value")
            res
        } ?: run {
            0
        }

        if(homeViewModel.numBlocksShow != numBlocksShow) {
            // number of blocks to show changed
            homeViewModel.imageButtonList?.let { it ->
                it.subList(numBlocksShow, 9).forEach {
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
        super.startSession()
        homeViewModel.sessionStartTimeMillis = Calendar.getInstance().timeInMillis
    }

    override fun onTimerFinish() {
        super.onTimerFinish()
        hideAllVisualBlocks()

        // hide end button
        startButton.text = view?.context?.getString(R.string.pomodoro_save_session_button)
        endButton.visibility = INVISIBLE
        controlButtonAction {
            saveSession()
        }
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

