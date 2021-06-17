package com.example.beyondpomodoro


import android.content.Intent
import android.support.test.uiautomator.*
import android.view.View
import android.view.ViewGroup
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.ServiceTestRule
import androidx.test.runner.AndroidJUnit4
import com.example.beyondpomodoro.ui.home.State
import com.example.beyondpomodoro.ui.home.TimerService
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
class NotificationBreakClickPauseTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)
    var mServiceTestRule = ServiceTestRule()

    @Test
    fun notificationBreakClickPauseTest() {
        val floatingActionButton = onView(
            allOf(withId(R.id.newSessionTypeButton), withContentDescription("Create new session type"),
                isDisplayed()))
        floatingActionButton.perform(click())

        val materialButton = onView(
            allOf(withId(R.id.button), withText("Start"),
                isDisplayed()))
        materialButton.perform(click())

        val materialButton2 = onView(
            allOf(withId(R.id.button), withText("Pause"),
                isDisplayed()))
        materialButton2.perform(click())

        val materialButton3 = onView(
            allOf(withId(R.id.button4), withText("End"),
                isDisplayed()))
        materialButton3.perform(click())

        val materialButton4 = onView(
            allOf(withText("Discard")
            ))
        materialButton4.perform(ViewActions.scrollTo(), click())

        onView(
            allOf(withId(R.id.button2), withText("Start"),
                isDisplayed())).perform(click())
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.openNotification()
        device.wait(Until.hasObject(By.text("Break time. Stretch. Relax. Hydrate.")), 2000)
        val title: UiObject2 = device.findObject(By.text("Break time. Stretch. Relax. Hydrate."))
        assertThat("Break time. Stretch. Relax. Hydrate.", equalTo(title.text))

        device.wait(Until.hasObject((By.text("Pause"))), 5000)
        device.findObject(By.text("Pause")).click()
        title.click()
        // check if timer has state pause
        val timer = (mServiceTestRule.bindService(
            Intent(getApplicationContext(), TimerService::class.java)) as TimerService.LocalBinder).timer
        Thread.sleep(2000)
        assertThat(timer.state.value, equalTo(State.ACTIVE_PAUSED))

        device.openNotification()
        device.wait(Until.hasObject((By.text("Resume"))), 5000)
        device.findObject(By.text("Resume")).click()
        title.click()
        Thread.sleep(2000)
        assertThat(timer.state.value, equalTo(State.ACTIVE_RUNNING))

        device.pressRecentApps()
        val myApp: UiObject = device.findObject(UiSelector().description("BeyondPomodoro"))
        myApp.clickAndWaitForNewWindow()
        onView(
            allOf(withText("Pause"),
                isDisplayed())).perform(click())

        onView(
            allOf(withText("End"),
                isDisplayed())).perform(click())
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
