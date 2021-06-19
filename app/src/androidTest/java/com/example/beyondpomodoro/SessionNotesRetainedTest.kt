package com.example.beyondpomodoro


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class SessionNotesRetainedTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun sessionNotesRetainedTest() {
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
        
        val appCompatEditText = onView(
allOf(withId(R.id.editTextTitle),
childAtPosition(
allOf(withId(R.id.home_layout),
childAtPosition(
withId(R.id.nav_host_fragment_content_main),
0)),
5),
isDisplayed()))
        appCompatEditText.perform(replaceText("saoijasjksadjkaksdj^^5%44AASD   asdad\uD83D\uDE18\uD83D\uDE04\uD83D\uDE01\uD83D\uDE06"), closeSoftKeyboard())
        
        val materialButton = onView(
allOf(withId(R.id.button), withText("Start"),
childAtPosition(
allOf(withId(R.id.home_layout),
childAtPosition(
withId(R.id.nav_host_fragment_content_main),
0)),
3),
isDisplayed()))
        materialButton.perform(click())

        clickDisplayedNotification("Session running")
        focusOnApp()

        val editText = onView(
allOf(withId(R.id.editTextTitle),
isDisplayed()))
        editText.check(matches(withText("saoijasjksadjkaksdj^^5%44AASD   asdad\uD83D\uDE18\uD83D\uDE04\uD83D\uDE01\uD83D\uDE06")))

        focusOnApp()

        val editText2 = onView(
allOf(withId(R.id.editTextTitle),
isDisplayed()))
        editText2.check(matches(withText("saoijasjksadjkaksdj^^5%44AASD   asdad\uD83D\uDE18\uD83D\uDE04\uD83D\uDE01\uD83D\uDE06")))

        val materialButton2 = onView(
            allOf(withId(R.id.button), withText("Pause"),
                isDisplayed()))
        materialButton2.perform(click())

        val materialButton3 = onView(
            allOf(withId(R.id.button4), withText("End"),
                isDisplayed()))
        materialButton3.perform(click())

        val materialButton4 = onView(
            allOf(withId(android.R.id.button2), withText("Discard")
            ))
        materialButton4.perform(scrollTo(), click())
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
