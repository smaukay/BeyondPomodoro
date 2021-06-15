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
class SessionNotesTagsTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun sessionNotesTagsTest() {
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
6),
isDisplayed()))
        appCompatEditText.perform(replaceText("sample title ./.4#4q3"), closeSoftKeyboard())
        
        val appCompatEditText2 = onView(
allOf(withId(R.id.editTextTags),
childAtPosition(
allOf(withId(R.id.home_layout),
childAtPosition(
withId(R.id.nav_host_fragment_content_main),
0)),
2),
isDisplayed()))
        appCompatEditText2.perform(replaceText("tag1"), closeSoftKeyboard())
        
        val appCompatEditText3 = onView(
allOf(withId(R.id.editTextTags), withText("tag1"),
childAtPosition(
allOf(withId(R.id.home_layout),
childAtPosition(
withId(R.id.nav_host_fragment_content_main),
0)),
2),
isDisplayed()))
        appCompatEditText3.perform(pressImeActionButton())
        
        val appCompatEditText4 = onView(
allOf(withId(R.id.editTextTags),
childAtPosition(
allOf(withId(R.id.home_layout),
childAtPosition(
withId(R.id.nav_host_fragment_content_main),
0)),
2),
isDisplayed()))
        appCompatEditText4.perform(replaceText("tag2, tag3"), closeSoftKeyboard())
        
        val appCompatEditText5 = onView(
allOf(withId(R.id.editTextTags), withText("tag2, tag3"),
childAtPosition(
allOf(withId(R.id.home_layout),
childAtPosition(
withId(R.id.nav_host_fragment_content_main),
0)),
2),
isDisplayed()))
        appCompatEditText5.perform(pressImeActionButton())
        
        val materialTextView = onView(
allOf(withId(R.id.textView2), withText("25:00"),
childAtPosition(
allOf(withId(R.id.home_layout),
childAtPosition(
withId(R.id.nav_host_fragment_content_main),
0)),
5),
isDisplayed()))
        materialTextView.perform(click())
        
        val appCompatEditText6 = onView(
allOf(withId(R.id.editTextSessionMinutes),
childAtPosition(
allOf(withId(R.id.linearLayout),
childAtPosition(
withId(android.R.id.content),
0)),
1),
isDisplayed()))
        appCompatEditText6.perform(replaceText("34"), closeSoftKeyboard())
        
        val materialButton = onView(
allOf(withId(R.id.set_time_button), withText("Set"),
childAtPosition(
allOf(withId(R.id.linearLayout),
childAtPosition(
withId(android.R.id.content),
0)),
2),
isDisplayed()))
        materialButton.perform(click())
        
        val materialButton2 = onView(
allOf(withId(R.id.button), withText("Start"),
childAtPosition(
allOf(withId(R.id.home_layout),
childAtPosition(
withId(R.id.nav_host_fragment_content_main),
0)),
4),
isDisplayed()))
        materialButton2.perform(click())
        
        val materialButton3 = onView(
allOf(withId(R.id.button), withText("Pause"),
childAtPosition(
allOf(withId(R.id.home_layout),
childAtPosition(
withId(R.id.nav_host_fragment_content_main),
0)),
4),
isDisplayed()))
        materialButton3.perform(click())
        
        val button = onView(
allOf(withText("tag1"),
withParent(allOf(withId(R.id.chipGroup),
withParent(withId(R.id.home_layout)))),
isDisplayed()))
        button.check(matches(isDisplayed()))
        
        val button2 = onView(
allOf(withText("tag2"),
withParent(allOf(withId(R.id.chipGroup),
withParent(withId(R.id.home_layout)))),
isDisplayed()))
        button2.check(matches(isDisplayed()))
        
        val button3 = onView(
allOf(withText("tag3"),
withParent(allOf(withId(R.id.chipGroup),
withParent(withId(R.id.home_layout)))),
isDisplayed()))
        button3.check(matches(isDisplayed()))

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
