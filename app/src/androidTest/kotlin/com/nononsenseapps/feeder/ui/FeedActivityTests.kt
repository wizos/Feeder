package com.nononsenseapps.feeder.ui


import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.closeSoftKeyboard
import android.support.test.espresso.action.ViewActions.pressImeActionButton
import android.support.test.espresso.action.ViewActions.replaceText
import android.support.test.espresso.action.ViewActions.scrollTo
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isChecked
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.isNotChecked
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.view.View
import android.view.ViewGroup
import com.nononsenseapps.feeder.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeedActivityTests {
    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule<FeedActivity>(FeedActivity::class.java)

    @Test
    @Throws(Exception::class)
    fun notifyAllFeedsWorks() {
        addFeed()
        Thread.sleep(500L)

        var checkedTextView = onView(allOf(withId(R.id.notifycheck), isNotChecked()))

        checkedTextView.perform(click())
        Thread.sleep(100L)

        checkedTextView = onView(allOf(withId(R.id.notifycheck), isChecked()))
        checkedTextView.check(matches(allOf(isDisplayed(), isChecked())))
    }

    fun addFeed(url: String = "cowboyprogrammer.org/index.xml", tag: String = "") {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)

        val addFeedMenuItem = onView(allOf(withId(R.id.title), withText("Add feed"), isDisplayed()))
        addFeedMenuItem.perform(click())

        val urlEditText = onView(allOf(withId(R.id.search_view), isDisplayed()))
        urlEditText.perform(replaceText(url), closeSoftKeyboard())
        urlEditText.perform(pressImeActionButton())

        val result = onView(childAtPosition(withId(R.id.results_listview), 0))
        result.perform(click())

        val tagEditText = onView(allOf(withId(R.id.feed_tag), isDisplayed()))
        tagEditText.perform(replaceText(tag), closeSoftKeyboard())
        tagEditText.perform(pressImeActionButton())

        val addButton = onView(withId(R.id.add_button))
        addButton.perform(scrollTo(), click())
    }

    private fun childAtPosition(parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
