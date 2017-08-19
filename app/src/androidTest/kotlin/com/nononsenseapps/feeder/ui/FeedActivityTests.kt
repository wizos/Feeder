package com.nononsenseapps.feeder.ui


import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.espresso.Espresso.onData
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
import com.nononsenseapps.feeder.R
import com.rometools.rome.feed.synd.SyndFeed
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
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
        Thread.sleep(500L)

        checkedTextView = onView(allOf(withId(R.id.notifycheck)))
        checkedTextView.check(matches(allOf(isDisplayed(), isChecked())))
    }

    fun addFeed(url: String = "cowboyprogrammer.org/index.xml", tag: String = "") {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)

        val addFeedMenuItem = onView(allOf(withId(R.id.title), withText("Add feed"), isDisplayed()))
        addFeedMenuItem.perform(click())

        val urlEditText = onView(allOf(withId(R.id.search_view), isDisplayed()))
        urlEditText.perform(replaceText(url), closeSoftKeyboard())
        urlEditText.perform(pressImeActionButton())

        Thread.sleep(1000L)

        onData(`is`(instanceOf(SyndFeed::class.java)))
                .inAdapterView(withId(R.id.results_listview))
                .perform(click())

        val tagEditText = onView(allOf(withId(R.id.feed_tag), isDisplayed()))
        tagEditText.perform(replaceText(tag), closeSoftKeyboard())
        tagEditText.perform(pressImeActionButton())

        val addButton = onView(withId(R.id.add_button))
        addButton.perform(scrollTo(), click())
    }
}
