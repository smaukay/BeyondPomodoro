package com.example.beyondpomodoro


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
import com.example.beyondpomodoro.sessiontype.SessionDatabase
import kotlinx.coroutines.runBlocking
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class NewActivityAndTitle {

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
    fun newActivityAndTitle() {
        val floatingActionButton = onView(
            withId(R.id.newSessionTypeButton))
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
allOf(withId(android.R.id.button2), withText("Discard")
))
        materialButton4.perform(scrollTo(), click())
        
        val materialButton5 = onView(
allOf(withId(R.id.button2), withText("Start"),
isDisplayed()))
        materialButton5.perform(click())
        
        val materialButton6 = onView(
allOf(withId(R.id.button2), withText("Pause"),
isDisplayed()))
        materialButton6.perform(click())
        
        val materialButton7 = onView(
allOf(withId(R.id.button3), withText("End"),
isDisplayed()))
        materialButton7.perform(click())
        
        val appCompatImageButton = onView(
allOf(withContentDescription("Open navigation drawer"),
childAtPosition(
allOf(withId(R.id.toolbar),
childAtPosition(
withClassName(`is`("com.google.android.material.appbar.AppBarLayout")),
0)),
1),
isDisplayed()))
        appCompatImageButton.perform(click())
        
        val navigationMenuItemView = onView(
allOf(withId(R.id.sessionInfoFragment),
childAtPosition(
allOf(withId(R.id.design_navigation_view),
childAtPosition(
withId(R.id.nav_view),
0)),
1),
isDisplayed()))
        navigationMenuItemView.perform(click())
        
        val appCompatEditText3 = onView(
allOf(withId(R.id.editTextSessionInfoTitle),
childAtPosition(
allOf(withId(R.id.linearLayout2),
childAtPosition(
withId(R.id.sessionListFragment),
0)),
0),
isDisplayed()))
        appCompatEditText3.perform(replaceText("newtitle ^%$&$&../ <>"), closeSoftKeyboard())
        
        val appCompatEditText4 = onView(
allOf(withId(R.id.editTextSessionInfoTitle), withText("newtitle ^%$&$&../ <>"),
childAtPosition(
allOf(withId(R.id.linearLayout2),
childAtPosition(
withId(R.id.sessionListFragment),
0)),
0),
isDisplayed()))
        appCompatEditText4.perform(pressImeActionButton())
        runBlocking {

            val context = ApplicationProvider.getApplicationContext<Context>()
            val dao = SessionDatabase.getInstance(context).sessionDao()

            val session = dao.getLatestSession()

            session.sessionTime?.let { assertThat(it, Matchers.equalTo(1500)) }?: run{ assert(false)}
            session.breakTime?.let { assertThat(it, Matchers.equalTo(300)) }?: run{ assert(false)}
            session.title?.let { assertThat(it, Matchers.equalTo("newtitle ^%$&$&../ <>")) }?: run{ assert(false)}
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
