package com.example.beyondpomodoro


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class PomodoroStateMachineButtonsTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun pomodoroStateMachineButtonsTest() {
        val floatingActionButton = onView(
allOf(withId(R.id.newSessionTypeButton), withContentDescription("Create new session type"),
childAtPosition(
allOf(withId(R.id.sessionListConstraintLayout),
childAtPosition(
withId(R.id.nav_host_fragment_content_main),
0)),
1),
isDisplayed()))
        floatingActionButton.perform(click())
        
        val button = onView(
allOf(withId(R.id.button), withText("START"),
isDisplayed()))
        button.check(matches(isDisplayed()))
        
        val materialButton = onView(
allOf(withId(R.id.button), withText("Start"),
isDisplayed()))
        materialButton.perform(click())
        
        val button2 = onView(
allOf(withId(R.id.button), withText("PAUSE"),
isDisplayed()))
        button2.check(matches(isDisplayed()))
        
        val materialButton2 = onView(
allOf(withId(R.id.button), withText("Pause"),
isDisplayed()))
        materialButton2.perform(click())
        
        val button3 = onView(
allOf(withId(R.id.button), withText("RESUME"),
isDisplayed()))
        button3.check(matches(isDisplayed()))
        
        val button4 = onView(
allOf(withId(R.id.button4), withText("END"),
isDisplayed()))
        button4.check(matches(isDisplayed()))
        button4.perform(click())
        val materialButton5 = onView(
            allOf(withId(android.R.id.button2), withText("Discard"),
                childAtPosition(
                    childAtPosition(
                        withClassName(Matchers.`is`("android.widget.ScrollView")),
                        0),
                    2)))
        materialButton5.perform(ViewActions.scrollTo(), click())
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
