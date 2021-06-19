package com.smaukay.beyondpomodoro.ui.home

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.smaukay.beyondpomodoro.R

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
                        caller.addToSessionList()
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
