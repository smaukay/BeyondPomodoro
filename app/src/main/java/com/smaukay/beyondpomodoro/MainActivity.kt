package com.smaukay.beyondpomodoro

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.smaukay.beyondpomodoro.databinding.ActivityMainBinding
import com.smaukay.beyondpomodoro.sessiontype.SessionDatabase
import com.smaukay.beyondpomodoro.ui.home.TimerService

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    lateinit var db: SessionDatabase

    override fun onDestroy() {
        super.onDestroy()
        Log.d(localClassName, "DEBUG: destorying activity")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(localClassName, "DEBUG: Creating activity")
        db = SessionDatabase.getInstance(this)

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
