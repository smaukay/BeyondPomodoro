package com.smaukay.beyondpomodoro


import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
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
class ActivityRunningConsistencyTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun clearDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = SessionDatabase.getInstance(context)
        val dao = db.sessionDao()
        runBlocking {
            db.clearAllTables()
        }
    }
    @Test
    fun activityRunningConsistencyTest() {
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
allOf(withId(R.id.editTextTags),
childAtPosition(
allOf(withId(R.id.recipient_group_FL),
childAtPosition(
withId(R.id.flexboxHolder),
0)),
0),
isDisplayed()))
        appCompatEditText.perform(replaceText("tag1, tag2"), closeSoftKeyboard())
        
        val appCompatEditText2 = onView(
allOf(withId(R.id.editTextTags), withText("tag1, tag2"),
childAtPosition(
allOf(withId(R.id.recipient_group_FL),
childAtPosition(
withId(R.id.flexboxHolder),
0)),
0),
isDisplayed()))
        appCompatEditText2.perform(pressImeActionButton())
        
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
        
        val materialButton2 = onView(
allOf(withId(R.id.button), withText("Pause"),
childAtPosition(
allOf(withId(R.id.home_layout),
childAtPosition(
withId(R.id.nav_host_fragment_content_main),
0)),
3),
isDisplayed()))
        materialButton2.perform(click())
        
        val materialButton3 = onView(
allOf(withId(R.id.button4), withText("End"),
childAtPosition(
allOf(withId(R.id.home_layout),
childAtPosition(
withId(R.id.nav_host_fragment_content_main),
0)),
2),
isDisplayed()))
        materialButton3.perform(click())
        
        val materialButton4 = onView(
allOf(withId(android.R.id.button2), withText("Discard"),
childAtPosition(
childAtPosition(
withClassName(`is`("android.widget.ScrollView")),
0),
2)))
        materialButton4.perform(scrollTo(), click())
        
        val appCompatImageButton = onView(
allOf(withContentDescription("Navigate up"),
childAtPosition(
allOf(withId(R.id.toolbar),
childAtPosition(
withClassName(`is`("com.google.android.material.appbar.AppBarLayout")),
0)),
1),
isDisplayed()))
        appCompatImageButton.perform(click())
        
        val appCompatImageButton2 = onView(
allOf(withContentDescription("Open navigation drawer"),
childAtPosition(
allOf(withId(R.id.toolbar),
childAtPosition(
withClassName(`is`("com.google.android.material.appbar.AppBarLayout")),
0)),
1),
isDisplayed()))
        appCompatImageButton2.perform(click())
        
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
        appCompatEditText3.perform(replaceText("activity 1"), closeSoftKeyboard())
        
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
        
        val appCompatEditText4 = onView(
allOf(withId(R.id.editTextTags),
childAtPosition(
allOf(withId(R.id.recipient_group_FL),
childAtPosition(
withId(R.id.flexboxHolder),
0)),
0),
isDisplayed()))
        appCompatEditText4.perform(replaceText("tag3, tag4"), closeSoftKeyboard())
        
        val appCompatEditText5 = onView(
allOf(withId(R.id.editTextTags), withText("tag3, tag4"),
childAtPosition(
allOf(withId(R.id.recipient_group_FL),
childAtPosition(
withId(R.id.flexboxHolder),
0)),
0),
isDisplayed()))
        appCompatEditText5.perform(pressImeActionButton())
        
        val materialButton5 = onView(
allOf(withId(R.id.button), withText("Start"),
childAtPosition(
allOf(withId(R.id.home_layout),
childAtPosition(
withId(R.id.nav_host_fragment_content_main),
0)),
3),
isDisplayed()))
        materialButton5.perform(click())
        
        val materialButton6 = onView(
allOf(withId(R.id.button), withText("Pause"),
childAtPosition(
allOf(withId(R.id.home_layout),
childAtPosition(
withId(R.id.nav_host_fragment_content_main),
0)),
3),
isDisplayed()))
        materialButton6.perform(click())
        
        val materialButton7 = onView(
allOf(withId(R.id.button4), withText("End"),
childAtPosition(
allOf(withId(R.id.home_layout),
childAtPosition(
withId(R.id.nav_host_fragment_content_main),
0)),
2),
isDisplayed()))
        materialButton7.perform(click())
        
        val materialButton8 = onView(
allOf(withId(android.R.id.button2), withText("Discard"),
childAtPosition(
childAtPosition(
withClassName(`is`("android.widget.ScrollView")),
0),
2)))
        materialButton8.perform(scrollTo(), click())
        
        val appCompatImageButton3 = onView(
allOf(withContentDescription("Navigate up"),
childAtPosition(
allOf(withId(R.id.toolbar),
childAtPosition(
withClassName(`is`("com.google.android.material.appbar.AppBarLayout")),
0)),
1),
isDisplayed()))
        appCompatImageButton3.perform(click())
        
        val appCompatImageButton4 = onView(
allOf(withContentDescription("Open navigation drawer"),
childAtPosition(
allOf(withId(R.id.toolbar),
childAtPosition(
withClassName(`is`("com.google.android.material.appbar.AppBarLayout")),
0)),
1),
isDisplayed()))
        appCompatImageButton4.perform(click())
        
        val navigationMenuItemView2 = onView(
allOf(withId(R.id.sessionInfoFragment),
childAtPosition(
allOf(withId(R.id.design_navigation_view),
childAtPosition(
withId(R.id.nav_view),
0)),
1),
isDisplayed()))
        navigationMenuItemView2.perform(click())
        
        val appCompatEditText6 = onView(
allOf(withId(R.id.editTextSessionInfoTitle),
childAtPosition(
allOf(withId(R.id.linearLayout2),
childAtPosition(
withId(R.id.sessionListFragment),
0)),
0),
isDisplayed()))
        appCompatEditText6.perform(replaceText("activity 2"), closeSoftKeyboard())
        
        val recyclerView = onView(
allOf(withId(R.id.sessionListFragment),
childAtPosition(
withId(R.id.sessionListConstraintLayout),
0)))
        recyclerView.perform(actionOnItemAtPosition<ViewHolder>(1, ViewActionClickRecyclerViewItem()))
        
        val materialButton9 = onView(
allOf(withId(R.id.button), withText("Start"),
childAtPosition(
allOf(withId(R.id.home_layout),
childAtPosition(
withId(R.id.nav_host_fragment_content_main),
0)),
3),
isDisplayed()))
        materialButton9.perform(click())
        
        val appCompatImageButton5 = onView(
allOf(withContentDescription("Open navigation drawer"),
childAtPosition(
allOf(withId(R.id.toolbar),
childAtPosition(
withClassName(`is`("com.google.android.material.appbar.AppBarLayout")),
0)),
1),
isDisplayed()))
        appCompatImageButton5.perform(click())
        
        val navigationMenuItemView3 = onView(
allOf(withId(R.id.sessionInfoFragment),
childAtPosition(
allOf(withId(R.id.design_navigation_view),
childAtPosition(
withId(R.id.nav_view),
0)),
1),
isDisplayed()))
        navigationMenuItemView3.perform(click())
        
        val recyclerView2 = onView(
allOf(withId(R.id.sessionListFragment),
childAtPosition(
withId(R.id.sessionListConstraintLayout),
0)))
        recyclerView2.perform(actionOnItemAtPosition<ViewHolder>(1, ViewActionClickRecyclerViewItem()))

        val materialButton10 = onView(
allOf(withText("No")
))
        materialButton10.perform(scrollTo(), click())
        
        val recyclerView3 = onView(
allOf(withId(R.id.sessionListFragment),
childAtPosition(
withId(R.id.sessionListConstraintLayout),
0)))
        recyclerView3.perform(actionOnItemAtPosition<ViewHolder>(0, ViewActionClickRecyclerViewItem()))
        
        val textView = onView(
allOf(withId(R.id.activityName), withText("activity 1"),
withParent(allOf(withId(R.id.home_layout),
withParent(withId(R.id.nav_host_fragment_content_main)))),
isDisplayed()))
        textView.check(matches(withText("activity 1")))

        clickDisplayedNotification("Session running")
        focusOnApp()

        val textView2 = onView(
allOf(withId(R.id.activityName), withText("activity 1"),
withParent(allOf(withId(R.id.home_layout),
withParent(withId(R.id.nav_host_fragment_content_main)))),
isDisplayed()))
        textView2.check(matches(withText("activity 1")))

        onView(
            allOf(withId(R.id.button), withText("Pause"),
                childAtPosition(
                    allOf(withId(R.id.home_layout),
                        childAtPosition(
                            withId(R.id.nav_host_fragment_content_main),
                            0)),
                    3),
                isDisplayed())).perform(click())

        onView(
            allOf(withId(R.id.button4), withText("End"),
                childAtPosition(
                    allOf(withId(R.id.home_layout),
                        childAtPosition(
                            withId(R.id.nav_host_fragment_content_main),
                            0)),
                    2),
                isDisplayed())).perform(click())

        onView(
            allOf(withId(android.R.id.button2), withText("Discard"),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("android.widget.ScrollView")),
                        0),
                    2))).perform(scrollTo(), click())
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
