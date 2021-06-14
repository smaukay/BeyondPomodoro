package com.example.beyondpomodoro.ui.home

import android.app.Activity
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.beyondpomodoro.MainActivity
import com.example.beyondpomodoro.R
import com.example.beyondpomodoro.sessiontype.Session
import com.example.beyondpomodoro.sessiontype.SessionDao
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


open class TimerFragment : Fragment() {
    protected lateinit var dndCheck: ActivityResultLauncher<Intent>
    protected var tags: MutableList<String> = mutableListOf()
    protected var breakTimeSeconds: UInt = 5u * 60u
    protected var sessionTimeSeconds: UInt = 25u * 60u
    protected var sessionId: Int? = null
    protected var title: String? = null

    lateinit var startButton: Button
    lateinit var endButton: Button
    lateinit var textViewSeconds: TextView
    protected lateinit var timer: PomodoroTimer
    protected lateinit var notificationTitle: (String) -> Unit
    protected lateinit var type: (String) -> Unit
    protected var sessionDao: SessionDao? = null
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
            println("DEBUG: service connected")
        }

        fun bindCallbacks() {
            timer.sessionTimeSecondsLeft.observe(viewLifecycleOwner, { millisUntilFinished ->
                onTick(millisUntilFinished.toUInt())
            })
            timer.state.observe(viewLifecycleOwner, {
                changeState(it)
            })
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timer.sessionTimeSecondsLeft.removeObservers(viewLifecycleOwner)
            timer.state.removeObservers(viewLifecycleOwner)
            println("DEBUG: service disconnected")
        }
    }

    protected val sharedData: SharedViewModel by activityViewModels()
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
        println("DEBUG: tags found ${this.tags}")
        tags.clear()
        s.tags?.map {
            tags.add(it)
        }
        println("DEBUG: tags found ${tags}")
        title = s.title
        dnd = s.dnd
    }

    open fun afterServiceConnected(bindCallbacks: () -> Unit) {
        // fetch most recent session
        val cls = this
        lifecycleScope.launch {
            println("DEBUG: launching after service connected coroutine")
            sharedData.sid?.let {
                println("DEBUG: sid has been set")
                sessionDao?.getSession(it)?.let { s ->
                    readSession(s)
                }

            }?: run {
                sessionDao?.getLatestSession()?.apply {
                    readSession(this)
                }
            }

            println("DEBUG: sid found: ${cls.sessionId}")
            println("DEBUG: timer value was ${cls.sessionTimeSeconds}")

            addButtons()
            bindCallbacks()
            lifecycleScope.launch {
                sessionId?.let {
                    sessionDao?.getTitle(it)?.let { s ->
                        s.collect { t ->
                            title = t
                            updateTitle(t)
                        }
                    }
                }
            }
            lifecycleScope.launch {
                sessionId?.let {
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
        println("DEBUG: dnd: $dnd")
    }

    open fun ringerNormal() {
        println("DEBUG: dnd: $dnd")
    }

    open fun startSession() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // set DND on
            val notificationManager =
                activity?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.isNotificationPolicyAccessGranted) {
            } else {
                // Ask the user to grant access
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)

                dndCheck.launch(intent)
            }
        }

        println("DEBUG: starting Session")
        nextState()
    }

    open fun setSessionTime(s: UInt) {
        println("DEBUG: setting session time to $s")
        timer.setSessionTime(s)
    }

    open fun addButtons() {
        println("DEBUG: adding buttons")
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
            println("DEBUG: endbutton")
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
            println("DEBUG: setting textview to $seconds")
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
        println("DEBUG: state ${timer.state.value}, ${startButton.text}")
        timer.nextState()
    }

    open fun fetchDao() {
        val db = (activity as MainActivity).db
        println("DEBUG: db: $db")
        sessionDao = db.sessionDao()
    }

    open fun bindService() {
        Intent(context, TimerService::class.java).also { intent ->
            context?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dndCheck = registerForActivityResult((ActivityResultContracts.StartActivityForResult())) {
            when(it.resultCode) {
                Activity.RESULT_OK -> {
                    // call dnd now
                    doNotDisturb()
                }
                else -> {}
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
        timerReset()
        addToSessionList()
    }

    open fun confirmEndSession() {
    }

    open fun onTick(secondsLeft: UInt) {
        println("DEBUG: onTick call with $secondsLeft")
        textViewSeconds.text = convertMinutesToDisplayString(secondsLeft)

        // update visuals
        updateVisualBlocks(secondsLeft)
    }

    open fun updateVisualBlocks(secondsUntilFinished: UInt) {
    }

    fun timerReset() {
        timer.clockReset()
        timer.pomodoroReset()
    }
}

fun convertMinutesToDisplayString(sessionTimeSecondsLeft: UInt) : String {
    return (sessionTimeSecondsLeft/60u).toString().padStart(2, '0') + ":" + (sessionTimeSecondsLeft%60u).toString().padStart(2, '0')
}
