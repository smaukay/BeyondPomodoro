package com.smaukay.beyondpomodoro

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions

class ViewActionClickRecyclerViewItem: ViewAction {
    override fun getConstraints() = null

    override fun getDescription(): String {
        return "Long click on the linear layout"
    }

    override fun perform(uiController: UiController?, view: View?) {
        ViewActions.click().perform(uiController, view?.findViewById(R.id.ringerStatus))
    }
}