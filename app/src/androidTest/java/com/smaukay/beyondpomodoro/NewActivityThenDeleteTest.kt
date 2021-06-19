package com.smaukay.beyondpomodoro


import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.smaukay.beyondpomodoro.sessiontype.SessionDatabase
import kotlinx.coroutines.runBlocking
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class NewActivityThenDeleteTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun newActivityThenDeleteTest() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dao = SessionDatabase.getInstance(context).sessionDao()
        val numActivitys = runBlocking {
            dao.getSessions().size
        }

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
        
        val recyclerView = onView(
allOf(withId(R.id.sessionListFragment),
childAtPosition(
withId(R.id.sessionListConstraintLayout),
0)))
        recyclerView.perform(
            actionOnItemAtPosition<ViewHolder>(0,
            ViewActionLongClickRecyclerViewItem()
                )
            )
        
        val materialButton = onView(
allOf(withId(android.R.id.button2), withText("Yes"),
childAtPosition(
childAtPosition(
withClassName(`is`("android.widget.ScrollView")),
0),
2)))
        materialButton.perform(scrollTo(), click())

        runBlocking {
            assertThat(dao.getSessions().size, equalTo(numActivitys))
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
