package com.example.beyondpomodoro.ui.home

import android.content.Context
import androidx.fragment.app.FragmentActivity

fun getRunningActivityId(activity: FragmentActivity?): Int? {
    val id = activity?.getPreferences(Context.MODE_PRIVATE)?.let {
        when(it.contains("activityId")) {
            true -> it.getInt("activityId", 0)
            false -> null
        }
    }
    return id
}

fun setRunningActivityId(activity: FragmentActivity?, id: Int) {
    activity?.getPreferences(Context.MODE_PRIVATE)?.let {
        it.edit().apply() {
            putInt("activityId", id)

            // crucial to commit right now
            // otherwise the next fragment reads a null on the sharedprefs
            commit()
        }
    }
}