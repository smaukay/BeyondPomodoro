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
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class TagsSaveDBTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)
    val context = ApplicationProvider.getApplicationContext<Context>()
    val dao = SessionDatabase.getInstance(context).sessionDao()
    @Before
    fun clearDb() {
        runBlocking {
            dao.getSessions().forEach {
                dao.removeSession(it.sid)
            }
        }
    }

    @Test
    fun tagsSaveDBTest() {
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
            withId(R.id.editTextTags))
        appCompatEditText.perform(replaceText("tag1, tag2"),
            closeSoftKeyboard(),
            pressImeActionButton()
        )

        val materialButton = onView(
            allOf(
                withId(R.id.button), withText("Start"),
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

        runBlocking {
            println("DEBUG: sessions in db: ${dao.getSessions()}")
            println("DEBUG: latest session tags is ${dao.getLatestSession().tags}")
            assertThat(dao.getLatestSession().tags, equalTo(setOf<String>("tag1", "tag2")))
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
