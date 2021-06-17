package com.example.beyondpomodoro.ui.home

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.media.AudioManager
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.beyondpomodoro.R
import com.example.beyondpomodoro.databinding.FragmentHomeBinding
import com.example.beyondpomodoro.sessiontype.Dnd
import com.example.beyondpomodoro.sessiontype.Pomodoro
import com.example.beyondpomodoro.sessiontype.Session
import com.example.beyondpomodoro.sessiontype.Tags
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.floor

open class HomeFragment : TimerFragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    protected lateinit var waitForCalendar: ActivityResultLauncher<Intent>

    var editTitle: EditText? = null
    var editTags: EditText? = null
    var chipGroup: FlexboxLayout? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // tag colours in an array
    private val tagColours = listOf(R.color.tag_1, R.color.tag_2, R.color.tag_3, R.color.tag_4, R.color.tag_5)

    override fun ringerNormal() {
        super.ringerNormal()
        println("DEBUG: dnd in homefrag: $dnd")
        when(dnd) {
            true -> {
                getDndPermissions()
                (activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager).ringerMode = AudioManager.RINGER_MODE_NORMAL
            }
            false -> {}
        }
    }

    private fun afterSaveSession() {
        // after saving we end session anyway
        endSession()
    }

    override fun updateDnd(d: Boolean) {
        super.updateDnd(d)
        timer.state.value?.apply{
            changeState(this)
        }
    }

    override fun doNotDisturb() {
        super.doNotDisturb()
        println("DEBUG: dnd in homefrag: $dnd")
        when(dnd) {
            true -> {
                getDndPermissions()
                (activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager).ringerMode = AudioManager.RINGER_MODE_SILENT
            }
            false -> {}
        }
    }

    override fun addButtons() {
        super.addButtons()
        setSessionTime(sessionTimeSeconds)
        notificationTitle("Session running")
        type("Pomodoro")
        populateTags()
        view?.let { setupVisualBlocks(it) }
        view?.findViewById<Switch>(R.id.switch1)?.apply {
            dnd?.let{
                isChecked = it
            }
            setOnCheckedChangeListener { buttonView, isChecked ->
                // save preference to database
                lifecycleScope.launch {
                    sessionId?.let {
                        sessionDao?.updateDnd(Dnd(isChecked, it))
                    }
                }
            }
        }
    }

    override fun updateTitle(t: String) {
        super.updateTitle(t)
        view?.findViewById<TextView>(R.id.activityName).apply {
            this?.text = t
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
        var descriptionSuggestion = tags.reduceOrNull { acc, c -> "$acc, $c" }
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
        waitForCalendar.launch(intent)
    }

    override fun endSession() {
        super.endSession()
        textViewSeconds.text = convertMinutesToDisplayString(sessionTimeSeconds)
        startButton.text = context?.getString(R.string.pomodoro_start_session_button)
        endButton.visibility = View.INVISIBLE
        // show all visual image blocks
        showAllVisualBlocks()

        // clear title field
        editTitle?.setText("")
        super.setSessionTime(breakTimeSeconds)
        timerReset()
        findNavController().navigate(R.id.action_nav_pomodoro_to_breakFragment)
    }

    override fun setSessionTime(s: UInt) {
        super.setSessionTime(s)
        sessionTimeSeconds = s
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        waitForCalendar = registerForActivityResult((ActivityResultContracts.StartActivityForResult())) {
            afterSaveSession()
        }
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

        tags.forEach { tag ->
            chipGroup?.let {
                addNewChip(tag, it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeViewModel.numBlocksShow.observe(viewLifecycleOwner, Observer<UInt> {numBlocks ->
            println("DEBUG: Updating visual blocks $numBlocks")
            // number of blocks to show changed
            homeViewModel.imageButtonList?.let { it ->
                it.subList(0, numBlocks.toInt()).forEach {
                    it?.visibility = VISIBLE
                }
                it.subList(numBlocks.toInt(), 9).forEach {
                    it?.visibility = INVISIBLE
                }
            }
        })

        editTitle = view.findViewById<EditText>(R.id.editTextTitle)?.apply {
            setText("")
            doOnTextChanged { text, start, before, count ->
                title = text.toString()
            }
        }

        editTags = view.findViewById<EditText>(R.id.editTextTags).apply {
        }
        editTags?.setOnEditorActionListener { v, actionId, event ->
            println("DEBUG: actionId: $actionId")
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEND -> {
                    val s = editTags?.text.toString()
                    println("DEBUG: Tags $s")
                    handleTags(s)
                    true
                }
                else -> {
                    false
                }
            }
        }
        chipGroup = view.findViewById<FlexboxLayout>(R.id.recipient_group_FL)
   }

    private fun addNewChip(userTag: String, chipGroup: FlexboxLayout) {
        val chip = Chip(context)
        chip.text = userTag
        // chip.chipIcon = ContextCompat.getDrawable(requireContext(), R.mipmap.ic_launcher_round)
        chip.isCloseIconEnabled = true
        chip.isClickable = true
        chip.isCheckable = false
        setTagColour(userTag, chip)
        chipGroup.addView(chip as View, chipGroup.childCount - 1)
        chip.setOnCloseIconClickListener {
            chipGroup.removeView(chip as View)
            tags.remove(chip.text.toString())
        }
    }

    private fun setTagColour(s: String, c: Chip) {

        // does this tag exist in database?
        lifecycleScope.launch {
            var colour = tagsDao?.getTagColour(s)
            when(colour) {
                null -> {
                    // choose colour from list
                    context?.let {
                        colour = ColorStateList.valueOf(ContextCompat.getColor(it, tagColours.random()))
                    }
                    println("DEBUG: adding colour $colour")
                    tagsDao?.addTag(
                        Tags(
                            colour,
                            s
                        )
                    )
                }
                else -> {
                    colour
                }
            }

            c.chipBackgroundColor = colour
        }

        // if so, what colour was assigned to it?
        // return activity?.getPreferences(Context.MODE_PRIVATE)?.let { prefs ->
        //     if (prefs.contains("tag_colour_${tag}")) {
        //         ColorStateList.valueOf(
        //             prefs.getInt(
        //                 "tag_colour_${tag}",
        //                 1
        //             )
        //         )
        //     } else {
        //         context?.let {
        //             ContextCompat.getColor(it, tagColours.random())
        //                 .let { colourSelect ->
        //                     // save this colour for this tag
        //                     with(prefs.edit()) {
        //                         putInt(
        //                             "tag_colour_${tag}",
        //                             colourSelect
        //                         )
        //                         apply()
        //                     }
        //                     ColorStateList.valueOf(colourSelect)
        //                 }
        //         }
        //     }
        // }?: run {
        //     ColorStateList.valueOf(0)
        // }
    }

    private fun handleTags(s: String) {
        // if comma separated, split them into multiple tags
        val tagsList = s.split(",")
        println("DEBUG: tagsList: $tagsList")
        tagsList.forEach { t ->
            val tag = t.trim()
            if (tag.isEmpty()) {
                return@forEach
            }
            // add a new chip to the group
            when (tags.any { c -> c == tag }) {
                false -> {
                    chipGroup?.let {
                        addNewChip(tag, it)
                    }

                    tags.add(tag)
                }
                else -> {
                }
            }
        }
        editTags?.setText("")
    }

    override fun setupVisualBlocks(view: View) {
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

        lifecycleScope.launch {
            println("DEBUG: coroutine starting with dnd: $dnd")

            dnd?.let { dnd ->
                sessionId?.let {
                    println("DEBUG: session exists: ${sessionDao?.getSession(sessionId!!)}")
                    println("DEBUG: Updating with tags: $tags")
                    sessionDao?.updatePomodoro(
                        Pomodoro(
                            sessionTimeSeconds.toInt(),
                            System.currentTimeMillis(),
                            tags.toSet(),
                            dnd,
                            it
                        )
                    )
                }?: run {
                    // add a new session to database
                    println("DEBUG: adding new session")
                    sessionId = sessionDao?.addSession(
                        Session(homeViewModel.title,
                            sessionTimeSeconds.toInt(),
                            300,
                            System.currentTimeMillis(),
                            tags.toSet(),
                            dnd
                        )
                    )?.toInt()
                }
            }


            println("DEBUG: session added to db")
        }
    }

    override fun updateVisualBlocks(secondsUntilFinished: UInt) {
        super.updateVisualBlocks(secondsUntilFinished)

        println("DEBUG: calling update visual blocks with $secondsUntilFinished/${timer.sessionTimeSeconds.value}")
        // check if any visualblocks to be disappeared?

        homeViewModel.numBlocksShow.apply {
            val res = this
            timer.sessionTimeSeconds.value?.toDouble()?.let {
                res.value = floor((secondsUntilFinished.toDouble()/it) * 9f).toUInt()
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
        endButton.visibility = VISIBLE
        endButton.text = view?.context?.getString(R.string.pomodoro_discard_session)
        endButton.apply {
            println("DEBUG: end button setting on clicker")
            setOnClickListener {
                confirmEndSession()
            }
        }
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

