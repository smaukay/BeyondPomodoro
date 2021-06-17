package com.example.beyondpomodoro


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
import com.example.beyondpomodoro.sessiontype.SessionDatabase
import kotlinx.coroutines.runBlocking
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class TagsColourConsistencyTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    val context = ApplicationProvider.getApplicationContext<Context>()
    val dao = SessionDatabase.getInstance(context).tagsDao()
    @Before
    fun clearDb() {
        runBlocking {
            SessionDatabase.getInstance(context).clearAllTables()
        }
    }
    @Test
    fun tagsColourConsistencyTest() {
        val floatingActionButton = onView(allOf(withId(R.id.newSessionTypeButton), withContentDescription("Create new session type"), isDisplayed()))
        floatingActionButton.perform(click())
        
        val appCompatEditText = onView(
            allOf(withId(R.id.editTextTags),
                isDisplayed()))
        appCompatEditText.perform(replaceText("tag1, tag2"), closeSoftKeyboard())
        
        val appCompatEditText2 = onView(
allOf(withId(R.id.editTextTags), withText("tag1, tag2"),
isDisplayed()))
        appCompatEditText2.perform(pressImeActionButton())
        
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
        
        val appCompatImageButton = onView(
allOf(withContentDescription("Navigate up"),
isDisplayed()))
        appCompatImageButton.perform(click())
        
        val appCompatImageButton2 = onView(
allOf(withContentDescription("Open navigation drawer"),
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
        
        val recyclerView = onView(
allOf(withId(R.id.sessionListFragment),
childAtPosition(
withId(R.id.sessionListConstraintLayout),
0)))
        recyclerView.perform(actionOnItemAtPosition<ViewHolder>(0, ViewActionClickRecyclerViewItem()))
        
        val tag1 = onView(
allOf(withText("tag1"),
withParent(allOf(withId(R.id.recipient_group_FL),
withParent(withId(R.id.flexboxHolder)))),
isDisplayed()))
        tag1.check(matches(isDisplayed()))
        val tag2 = onView(
            allOf(withText("tag2"),
                withParent(allOf(withId(R.id.recipient_group_FL),
                    withParent(withId(R.id.flexboxHolder)))),
                isDisplayed()))
        tag2.check(matches(isDisplayed()))
        val expectedColourTag1 = runBlocking {
            dao.getTagColour("tag1")
        }
        tag1.check(matches(withBgColor(
            expectedColourTag1
        )))
        val expectedColourTag2 = runBlocking {
            dao.getTagColour("tag2")
        }
        tag2.check(matches(withBgColor(
            expectedColourTag2
        )))

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
