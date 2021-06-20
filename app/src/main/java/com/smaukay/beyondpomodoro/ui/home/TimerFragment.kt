package com.smaukay.beyondpomodoro.ui.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.smaukay.beyondpomodoro.MainActivity
import com.smaukay.beyondpomodoro.R
import com.smaukay.beyondpomodoro.sessiontype.Dnd
import com.smaukay.beyondpomodoro.sessiontype.Session
import com.smaukay.beyondpomodoro.sessiontype.SessionDao
import com.smaukay.beyondpomodoro.sessiontype.TagsDao
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.properties.Delegates


open class TimerFragment : Fragment() {
    protected lateinit var dndCheck: ActivityResultLauncher<Intent>
    protected lateinit var ringerCheck: ActivityResultLauncher<Intent>
    protected var tags: MutableList<String> = mutableListOf()
    protected var breakTimeSeconds: UInt = 5u * 60u
    protected var sessionTimeSeconds: UInt = 25u * 60u
    protected var sessionId by Delegates.notNull<Int>()
    protected var title: String? = null

    lateinit var startButton: Button
    lateinit var endButton: Button
    lateinit var textViewSeconds: TextView
    protected lateinit var timer: PomodoroTimer
    protected lateinit var notificationTitle: (String) -> Unit
    protected lateinit var type: (String) -> Unit
    protected var sessionDao: SessionDao? = null
    protected var tagsDao: TagsDao? = null
    protected var dnd: Boolean? = null

    protected val connection = object: ServiceConnection {
        private lateinit var binder: TimerService.LocalBinder
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            binder = service as TimerService.LocalBinder
            timer = binder.timer
            notificationTitle = {
                binder.title(it)
            }
            type = {
                binder.type(it)
            }
            afterServiceConnected {
                bindCallbacks()
            }

        }

        fun bindCallbacks() {
            timer.sessionTimeSecondsLeft.observe(viewLifecycleOwner, { millisUntilFinished ->
                Log.d("TimerFragment", "onTick: $millisUntilFinished")
                onTick(millisUntilFinished.toUInt())
            })
            timer.state.observe(viewLifecycleOwner, {
                Log.d("TimerFragment", "state: $it")
                changeState(it)
            })
            timer.percentage.observe(viewLifecycleOwner, {
                Log.d("TimerFragment", "percentage: $it")
                updateVisualBlocks(it)
            })
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }

    companion object {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        context?.unbindService(connection)
    }

    open fun readSession(s: Session) {

        sessionTimeSeconds = s.sessionTime?.toUInt() ?: run { 1500u }
        breakTimeSeconds = s.breakTime?.toUInt() ?: run { 300u }
        sessionId = s.sid

        tags.clear()
        s.tags?.map {
            tags.add(it)
        }

        title = s.title
        dnd = s.dnd

    }

    open fun afterServiceConnected(bindCallbacks: () -> Unit) {
        // fetch most recent session
        lifecycleScope.launch {

            getRunningActivityId(activity)?.let {
                sessionDao?.getSession(it)?.let { s ->
                    readSession(s)
                }

            }?: run {
                sessionDao?.getLatestSession()?.apply {
                    readSession(this)
                    sessionId.let { setRunningActivityId(activity, it) }
                }
            }

            addButtons()
            bindCallbacks()
            lifecycleScope.launch {
                sessionId.let {
                    sessionDao?.getTitle(it)?.let { s ->
                        s.collect { t ->
                            title = t
                            updateTitle(t)
                        }
                    }
                }
            }
            lifecycleScope.launch {
                sessionId.let {
                    sessionDao?.getDnd(it)?.let { s ->
                        s.collect { d ->
                            dnd = d
                            updateDnd(d)
                        }
                    }
                }
            }
        }
    }

    open fun updateDnd(d: Boolean) {
    }

    open fun updateTitle(t: String) {
    }

    open fun doNotDisturb() {

    }

    open fun ringerNormal() {

    }

    open fun getDndPermissions(ifAllowed: (Intent) -> Unit) {
        // set DND on
        // Ask the user to grant access
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        ifAllowed(intent)
    }

    open fun startSession() {

        nextState()
    }

    open fun setSessionTime(s: UInt) {

        timer.setSessionTime(s)
    }

    open fun addButtons() {

        val fragment = this

        view?.findViewWithTag<TextView>("timerDisplay")?.apply {
            // onclick open dialog to enter time
            setOnClickListener {
                when(timer.state.value) {
                    State.ACTIVE_PAUSED, State.ACTIVE_RUNNING -> {
                        val toast = Toast.makeText(
                            view?.context,
                            context.getString(R.string.session_already_active_message),
                            Toast.LENGTH_SHORT
                        )
                        toast.show()
                    }
                    State.INACTIVE, State.COMPLETE -> {
                        SetTimeDialogFragment(fragment).show(fragment.parentFragmentManager, "pick_session_time")
                    }

                }

            }
        }?.let {
            textViewSeconds = it
        }

        view?.findViewWithTag<Button>("endButton")?.apply {

            setOnClickListener {
                confirmEndSession()
            }
        }?.let {
            endButton = it
        }

        view?.findViewWithTag<Button>("startButton")?.apply {
            startButton = this
        }

        timer.state.value?.let{
            changeState(it)
        }
    }

    protected fun onTimerChange(seconds: UInt) {
        textViewSeconds.apply {
            text = convertMinutesToDisplayString(seconds)
        }
    }

    open fun setupVisualBlocks(view: View){

    }

    protected fun changeState(state: State) {
        // when timer state changes
        when(state) {
            State.COMPLETE -> {
                onTimerFinish()
            }

            State.INACTIVE -> {
                view?.let { setupVisualBlocks(it) }
                timer.clockReset()
                startButton.text = view?.context?.getString(R.string.pomodoro_start_session_button)
                endButton.visibility = View.INVISIBLE
                controlButtonAction {
                    startSession()
                }
                ringerNormal()
            }

            State.ACTIVE_PAUSED -> {
                startButton.text = view?.context?.getString(R.string.pomodoro_resume_session_button)
                endButton.visibility = View.VISIBLE
                controlButtonAction {
                    nextState()
                }
                ringerNormal()
            }

            State.ACTIVE_RUNNING -> {
                startButton.text = view?.context?.getString(R.string.pomodoro_pause_session_button)
                controlButtonAction {
                    nextState()
                }
                endButton.visibility = View.INVISIBLE
                doNotDisturb()
            }
        }
    }

    fun controlButtonAction(func: () -> Unit) {
        view?.findViewWithTag<Button>("startButton")?.apply {
            setOnClickListener {
                func()
            }
        }?.let {
            startButton = it
        }
    }

    open fun nextState() {

        timer.nextState()
    }

    open fun fetchDao() {
        val db = (activity as MainActivity).db

        sessionDao = db.sessionDao()
        tagsDao = db.tagsDao()
    }

    open fun bindService() {
        Intent(context, TimerService::class.java).also { intent ->
            context?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun _setRingerNormal() {
        Log.d("TimerFragment", "Setting ringer normal")
        (activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager).ringerMode = AudioManager.RINGER_MODE_NORMAL
    }

    fun _doNotDisturb() {
        Log.d("TimerFragment", "Setting dnd")
        (activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager).ringerMode = AudioManager.RINGER_MODE_SILENT
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dndCheck = registerForActivityResult((ActivityResultContracts.StartActivityForResult())) {
            Log.d("TimerFragment", "time to set dnd")
            when(hasDndPermissions({
                requireActivity().getSystemService(it)
            })) {
                true -> {
                    // call dnd now
                    _doNotDisturb()
                }
                false -> {
                    Log.d("TimerFragment", "cannot set dnd, setting back to false")
                    // save preference to database
                    lifecycleScope.launch {
                        sessionId.let {
                            sessionDao?.updateDnd(Dnd(false, it))
                        }
                    }
                }
            }
        }
        ringerCheck = registerForActivityResult((ActivityResultContracts.StartActivityForResult())) {
            Log.d("TimerFragment", "time to set ringer on")
            when(hasDndPermissions({
                requireActivity().getSystemService(it)
            })) {
                true -> {
                    // set ringer normal
                    _setRingerNormal()
                }
                false -> {
                    Log.d("TimerFragment", "cannot set dnd, setting back to false")
                    // save preference to database
                    lifecycleScope.launch {
                        sessionId.let {
                            sessionDao?.updateDnd(Dnd(false, it))
                        }
                    }
                }
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.timer_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fetchDao()
        bindService()
        super.onViewCreated(view, savedInstanceState)
    }

    open fun onTimerFinish() {
        // set timer display to zero
        textViewSeconds.text = convertMinutesToDisplayString(0u)
    }

    open fun saveSession() {
    }

    open fun addToSessionList() {

    }

    open fun endSession() {
        addToSessionList()
        timerReset()
    }

    open fun confirmEndSession() {
    }

    open fun onTick(secondsLeft: UInt) {

        textViewSeconds.text = convertMinutesToDisplayString(secondsLeft)
    }

    open fun updateVisualBlocks(numBlocks: UInt) {
    }

    fun timerReset() {
        timer.clockReset()
        timer.pomodoroReset()
    }
}

fun convertMinutesToDisplayString(sessionTimeSecondsLeft: UInt) : String {
    return (sessionTimeSecondsLeft/60u).toString().padStart(2, '0') + ":" + (sessionTimeSecondsLeft%60u).toString().padStart(2, '0')
}
