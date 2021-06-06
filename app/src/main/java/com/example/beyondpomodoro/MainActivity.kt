package com.example.beyondpomodoro

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.beyondpomodoro.databinding.ActivityMainBinding
import com.example.beyondpomodoro.ui.home.*
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    protected lateinit var timerViewModel: TimerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // create timer service
        Intent(this, TimerService::class.java).also { intent ->
            startService(intent)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.pomodoroFragment, R.id.aboutFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the persistent NotificationChannel for showing timer
            val name = getString(R.string.alert_channel_name)
            val descriptionText = getString(R.string.alert_channel_description)
            val alertChannel = NotificationChannel(getString(R.string.alert_channel_id), name, NotificationManager.IMPORTANCE_DEFAULT)
            alertChannel.description = descriptionText

            val persistentChannel = NotificationChannel(getString(R.string.persistent_channel_id), getString(R.string.persistent_channel_name), NotificationManager.IMPORTANCE_LOW)
            persistentChannel.description = getString(R.string.persistent_channel_description)
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(alertChannel)
            notificationManager.createNotificationChannel(persistentChannel)
        }

        // notification based on viewmodel changes
        timerViewModel = ViewModelProvider(this).get(TimerViewModel::class.java)

        timerViewModel.timer.state.observe(this, Observer<State> {
            when(it) {
                State.COMPLETE -> {
                    with(NotificationManagerCompat.from(this)) {
                        cancelAll()
                    }
                    // remove observers
                    timerViewModel.timer.sessionTimeSecondsLeft.removeObservers(this)

                    println("DEBUG: Notifying")
                    endNotification(this, timerViewModel.title.orEmpty(), timerViewModel.type.orEmpty())
                }
                State.ACTIVE_PAUSED, State.ACTIVE_RUNNING -> {
                    // attach an observer
                    timerViewModel.timer.sessionTimeSecondsLeft.observe(this, Observer<UInt> {
                        println("DEBUG: observer activated")
                        // update notification
                        persistentTimedNotification(this, it, timerViewModel.title.orEmpty())
                    })
                }
                State.INACTIVE -> {
                    // no notification needed
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}