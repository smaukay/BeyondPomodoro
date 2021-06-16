package com.example.beyondpomodoro


import android.support.test.uiautomator.By
import android.support.test.uiautomator.UiDevice
import android.support.test.uiautomator.UiObject2
import android.support.test.uiautomator.Until
import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class NotificationSessionClickTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)


    @Test
    fun notificationSessionClickTest() {
        val floatingActionButton = onView(
allOf(withId(R.id.newSessionTypeButton), withContentDescription("Create new session type"),
    isDisplayed()))
        floatingActionButton.perform(click())
        
        val materialButton = onView(
allOf(withText("Start"),
    isDisplayed()))
        materialButton.perform(click())

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.openNotification()
        device.wait(Until.hasObject(By.text("Session running")), 1000)
        val title: UiObject2 = device.findObject(By.text("Session running"))
        assertThat("Session running", equalTo(title.text))
        title.click()

        device.findObject(By.textContains("Session")).click()
        // check that session fragment is opened
        onView(allOf(withId(R.id.button))).check(
            ViewAssertions.matches(
                isDisplayed()
            )
        )
        val materialButton2 = onView(
            allOf(withText("Pause"),
                isDisplayed()))
        materialButton2.perform(click())

        val materialButton3 = onView(
            allOf(withText("End"),
                isDisplayed()))
        materialButton3.perform(click())

        val materialButton4 = onView(
            allOf(withText("Discard")
            ))
        materialButton4.perform(ViewActions.scrollTo(), click())

    }
    
    private fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
