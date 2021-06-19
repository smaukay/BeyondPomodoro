package com.smaukay.beyondpomodoro.ui.home

import android.content.ContextWrapper
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip
import com.smaukay.beyondpomodoro.R
import com.smaukay.beyondpomodoro.databinding.FragmentHomeBinding
import com.smaukay.beyondpomodoro.sessiontype.*
import kotlinx.coroutines.launch
import java.util.*

open class HomeFragment : TimerFragment() {

    private var _binding: FragmentHomeBinding? = null
    protected lateinit var waitForCalendar: ActivityResultLauncher<Intent>

    var imageButtonList: List<ImageView?>? = null
    var editNotes: EditText? = null
    var editTags: EditText? = null
    var chipGroup: FlexboxLayout? = null

    val notes: MutableLiveData<String> by lazy {
        MutableLiveData<String>("")
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // tag colours in an array
    private val tagColours = listOf(R.color.tag_1, R.color.tag_2, R.color.tag_3, R.color.tag_4, R.color.tag_5)

    @RequiresApi(Build.VERSION_CODES.M)
    override fun ringerNormal() {
        super.ringerNormal()

        when(dnd) {
            true -> {
                if(hasDndPermissions((this as ContextWrapper))) {
                    _setRingerNormal()
                } else {
                    getDndPermissions {
                        ringerCheck.launch(it)
                    }
                }
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

        activity?.findViewById<Switch>(R.id.switch1)?.apply {
            isChecked = d
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun doNotDisturb() {
        super.doNotDisturb()

        when(dnd) {
            true -> {
                Log.d("TimerFragment", "dnd set is requested by user")
                if (hasDndPermissions((this as ContextWrapper))) {
                    _doNotDisturb()
                }
                else {
                    getDndPermissions {
                        dndCheck.launch(it)
                    }
                }
            }
            false -> {}
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
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
                    sessionId.let {
                        sessionDao?.updateDnd(Dnd(isChecked, it))

                        if(!isChecked) {
                            // DND has been switched off
                            ringerNormal()
                        }
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
        // end time in millis
        val sessionEndTimeMillis = Calendar.getInstance().timeInMillis
        Log.d("HomeFragment", "session end time: $sessionEndTimeMillis")

        // create a calendar event description from tags added
        var descriptionSuggestion = tags.reduceOrNull { acc, c -> "$acc, $c" }
        if (descriptionSuggestion.isNullOrBlank()) {
            descriptionSuggestion = ""
        }

        lifecycleScope.launch {
            val sessionStartTimeMillis = sessionDao?.getSession(sessionId)?.usedAt
            Log.d("HomeFragment", "session start time: $sessionStartTimeMillis")
            // create calendar intent
            val intent = Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, sessionStartTimeMillis)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, sessionEndTimeMillis)
                .putExtra(CalendarContract.Events.TITLE, title)
                .putExtra(CalendarContract.Events.DESCRIPTION, descriptionSuggestion)
            waitForCalendar.launch(intent)
        }
    }

    override fun endSession() {
        super.endSession()
        textViewSeconds.text = convertMinutesToDisplayString(sessionTimeSeconds)
        startButton.text = context?.getString(R.string.pomodoro_start_session_button)
        endButton.visibility = View.INVISIBLE
        // show all visual image blocks
        showAllVisualBlocks()

        // clear title field
        notes.value = ""
        super.setSessionTime(breakTimeSeconds)
        timerReset()
        findNavController().navigate(R.id.action_nav_pomodoro_to_breakFragment)
    }

    override fun setSessionTime(s: UInt) {
        super.setSessionTime(s)
        sessionTimeSeconds = s
    }

    @RequiresApi(Build.VERSION_CODES.M)
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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun populateTags() {
        // all existing tags


        tags.forEach { tag ->
            chipGroup?.let {
                addNewChip(tag, it)
            }
        }
    }

    fun updateBlocksDisplay(numBlocks: UInt) {
        // number of blocks to show changed
        imageButtonList?.let { it ->
            it.subList(0, numBlocks.toInt()).forEach {
                it?.visibility = VISIBLE
            }
            it.subList(numBlocks.toInt(), 9).forEach {
                it?.visibility = INVISIBLE
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        readSessionNotes(activity)?.let {
            notes.value = it
        }
        notes.observe(viewLifecycleOwner, {
            if(it != editNotes?.text.toString()) {
                editNotes?.setText(it)
            }

            saveSessionNotes(activity, it)
        })
        editNotes = view.findViewById<EditText>(R.id.editTextTitle)?.apply {
            doOnTextChanged { text, start, before, count ->
                if (notes.value != text.toString()) {
                    notes.value = text.toString()
                }
            }
        }

        editTags = view.findViewById<EditText>(R.id.editTextTags).apply {
        }
        editTags?.setOnEditorActionListener { v, actionId, event ->

            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEND -> {
                    val s = editTags?.text.toString()

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
    }

    private fun handleTags(s: String) {
        // if comma separated, split them into multiple tags
        val tagsList = s.split(",")

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

        imageButtonList = imageButtonIds.map {
            view.findViewById(it)
        }
    }

    private fun showAllVisualBlocks() {
        // when user ends session, set all visual blocks back to active
        imageButtonList?.forEach {
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
            return
        }

        lifecycleScope.launch {
            dnd?.let { dnd ->
                sessionDao?.updatePomodoro(
                    Pomodoro(
                        sessionTimeSeconds.toInt(),
                        tags.toSet(),
                        dnd,
                        sessionId
                    )
                )
            }
        }
    }

    override fun updateVisualBlocks(numBlocks: UInt) {
        super.updateVisualBlocks(numBlocks)
        updateBlocksDisplay(numBlocks)
    }

    private fun hideAllVisualBlocks() {
        imageButtonList?.forEach {
            it?.visibility = INVISIBLE
        }
    }

    override fun startSession() {
        super.startSession()
        lifecycleScope.launch {
            sessionDao?.activateSession(
                UseTime(
                    Calendar.getInstance().timeInMillis,
                    sessionId
                )
            )
        }
    }

    override fun onTimerFinish() {
        super.onTimerFinish()
        hideAllVisualBlocks()

        // hide end button
        startButton.text = view?.context?.getString(R.string.pomodoro_save_session_button)
        endButton.visibility = VISIBLE
        endButton.text = view?.context?.getString(R.string.pomodoro_discard_session)
        endButton.apply {

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
        Log.d("HomeFragment", "Confirm end of session")
        EndSessionDialogFragment(this).show(parentFragmentManager, "end_session")
    }
}

