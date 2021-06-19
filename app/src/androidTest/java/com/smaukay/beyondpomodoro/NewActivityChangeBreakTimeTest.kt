package com.smaukay.beyondpomodoro


import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.smaukay.beyondpomodoro.sessiontype.SessionDatabase
import kotlinx.coroutines.runBlocking
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class NewActivityChangeBreakTimeTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    val context = ApplicationProvider.getApplicationContext<Context>()
    val dao = SessionDatabase.getInstance(context).sessionDao()
    @Before
    fun clearDb() {
        runBlocking {
            SessionDatabase.getInstance(context).clearAllTables()
        }
    }
    @Test
    fun newActivityChangeBreakTimeTest() {
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
allOf(withId(android.R.id.button2), withText("Discard"),
))
        materialButton4.perform(scrollTo(), click())
        
        val materialTextView = onView(
allOf(withId(R.id.textView5), withText("05:00"),
childAtPosition(
childAtPosition(
withId(R.id.nav_host_fragment_content_main),
0),
0),
isDisplayed()))
        materialTextView.perform(click())
        
        val appCompatEditText = onView(
allOf(withId(R.id.editTextSessionMinutes),
childAtPosition(
allOf(withId(R.id.linearLayout),
childAtPosition(
withId(android.R.id.content),
0)),
1),
isDisplayed()))
        appCompatEditText.perform(replaceText("6"), closeSoftKeyboard())
        
        val materialButton5 = onView(
allOf(withId(R.id.set_time_button), withText("Set"),
childAtPosition(
allOf(withId(R.id.linearLayout),
childAtPosition(
withId(android.R.id.content),
0)),
2),
isDisplayed()))
        materialButton5.perform(click())
        
        val materialButton6 = onView(
allOf(withId(R.id.button2), withText("Start"),
isDisplayed()))
        materialButton6.perform(click())
        
        val materialButton7 = onView(
allOf(withId(R.id.button2), withText("Pause"),
isDisplayed()))
        materialButton7.perform(click())
        
        val materialButton8 = onView(
allOf(withId(R.id.button3), withText("End"),
isDisplayed()))
        materialButton8.perform(click())
        runBlocking {

            val context = ApplicationProvider.getApplicationContext<Context>()
            val dao = SessionDatabase.getInstance(context).sessionDao()

            val session = dao.getLatestSession()

            session.sessionTime?.let { assertThat(it, Matchers.equalTo(25 * 60)) }?: run{ assert(false)}
            session.breakTime?.let { assertThat(it, Matchers.equalTo(6 * 60)) }?: run{ assert(false)}
            session.title?.let { assertThat(it, Matchers.equalTo("")) }?: run{ assert(false)}
            assertThat(session.dnd, Matchers.equalTo(false))
        }
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
