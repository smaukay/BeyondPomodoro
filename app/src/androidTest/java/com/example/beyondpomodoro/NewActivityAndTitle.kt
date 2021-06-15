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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class NewActivityAndTitle {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun newActivityAndTitle() {
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
allOf(withId(R.id.home_layout),
childAtPosition(
withId(R.id.nav_host_fragment_content_main),
0)),
2),
isDisplayed()))
        appCompatEditText.perform(replaceText("tag1, tag2"), closeSoftKeyboard())
        
        val appCompatEditText2 = onView(
allOf(withId(R.id.editTextTags), withText("tag1, tag2"),
childAtPosition(
allOf(withId(R.id.home_layout),
childAtPosition(
withId(R.id.nav_host_fragment_content_main),
0)),
2),
isDisplayed()))
        appCompatEditText2.perform(pressImeActionButton())
        
        val materialButton = onView(
allOf(withId(R.id.button), withText("Start"),
childAtPosition(
allOf(withId(R.id.home_layout),
childAtPosition(
withId(R.id.nav_host_fragment_content_main),
0)),
4),
isDisplayed()))
        materialButton.perform(click())
        
        val materialButton2 = onView(
allOf(withId(R.id.button), withText("Pause"),
childAtPosition(
allOf(withId(R.id.home_layout),
childAtPosition(
withId(R.id.nav_host_fragment_content_main),
0)),
4),
isDisplayed()))
        materialButton2.perform(click())
        
        val materialButton3 = onView(
allOf(withId(R.id.button4), withText("End"),
childAtPosition(
allOf(withId(R.id.home_layout),
childAtPosition(
withId(R.id.nav_host_fragment_content_main),
0)),
3),
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
        
        val materialButton5 = onView(
allOf(withId(R.id.button2), withText("Start"),
childAtPosition(
childAtPosition(
withId(R.id.nav_host_fragment_content_main),
0),
1),
isDisplayed()))
        materialButton5.perform(click())
        
        val materialButton6 = onView(
allOf(withId(R.id.button2), withText("Pause"),
childAtPosition(
childAtPosition(
withId(R.id.nav_host_fragment_content_main),
0),
1),
isDisplayed()))
        materialButton6.perform(click())
        
        val materialButton7 = onView(
allOf(withId(R.id.button3), withText("End"),
childAtPosition(
childAtPosition(
withId(R.id.nav_host_fragment_content_main),
0),
2),
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
            println("DEBUG: Running Dbtest")
            val context = ApplicationProvider.getApplicationContext<Context>()
            val dao = SessionDatabase.getInstance(context).sessionDao()
            println("DEBUG: ${dao.getSessions()}")
            val session = dao.getLatestSession()
            println("DEBUG: checkDb $session")
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