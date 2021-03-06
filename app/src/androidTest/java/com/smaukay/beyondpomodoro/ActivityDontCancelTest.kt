package com.smaukay.beyondpomodoro


import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.smaukay.beyondpomodoro.sessiontype.SessionDatabase
import kotlinx.coroutines.runBlocking
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class ActivityDontCancelTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun clearDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dao = SessionDatabase.getInstance(context).sessionDao()
        runBlocking {
            dao.getSessions().forEach {
                dao.removeSession(it.sid)
            }
        }
    }

    @Test
    fun activityDontCancelTest() {
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
        
        val floatingActionButton2 = onView(
allOf(withId(R.id.newSessionTypeButton), withContentDescription("Create new session type"),
childAtPosition(
allOf(withId(R.id.sessionListConstraintLayout),
childAtPosition(
withId(R.id.nav_host_fragment_content_main),
0)),
1),
isDisplayed()))
        floatingActionButton2.perform(click())
        
        val materialButton2 = onView(
allOf(withId(android.R.id.button1), withText("No")))
        materialButton2.perform(scrollTo(), click())
        
        val recyclerView = onView(
allOf(withId(R.id.sessionListFragment),
childAtPosition(
withId(R.id.sessionListConstraintLayout),
0)))
        recyclerView.perform(actionOnItemAtPosition<ViewHolder>(0, ViewActionClickRecyclerViewItem()))
        
        val button = onView(
allOf(withId(R.id.button), withText("PAUSE"),
isDisplayed()))
        button.check(matches(isDisplayed()))
        
        val materialButton3 = onView(
allOf(withId(R.id.button), withText("Pause"),
isDisplayed()))
        materialButton3.perform(click())
        
        val materialButton4 = onView(
allOf(withId(R.id.button4), withText("End"),
isDisplayed()))
        materialButton4.perform(click())
        
        val materialButton5 = onView(
allOf(withId(android.R.id.button2), withText("Discard"),
))
        materialButton5.perform(scrollTo(), click())
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
