package com.example.beyondpomodoro

import android.content.res.ColorStateList
import android.view.View
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.internal.util.Checks
import com.google.android.material.chip.Chip
import org.hamcrest.Description
import org.hamcrest.Matcher


fun withBgColor(color: ColorStateList): Matcher<View?>? {
    Checks.checkNotNull(color)
    return object : BoundedMatcher<View?, Chip>(Chip::class.java) {
        override fun matchesSafely(c: Chip): Boolean {
            return color == c.chipBackgroundColor
        }

        override fun describeTo(description: Description) {
            description.appendText("with text color: ")
        }
    }
}