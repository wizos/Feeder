package com.nononsenseapps.feeder.ui

import android.content.Context
import androidx.test.InstrumentationRegistry
import androidx.test.InstrumentationRegistry.getInstrumentation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import androidx.test.uiautomator.UiDevice
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.util.PrefUtils
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class FeedEmptyViewTests {
    @Rule
    @JvmField
    var activityRule: ActivityTestRule<FeedActivity> = ActivityTestRule(FeedActivity::class.java)

    private val targetContext: Context
        get() = getInstrumentation().targetContext

    @Before
    fun keepNavDrawerClosed() {
        PrefUtils.markWelcomeDone(targetContext)
    }

    @Test
    fun clickingOpenOpensNavdrawer() {
        onView(withId(R.id.empty_open_feeds)).perform(click())

        onView(withId(R.id.navdrawer)).check(matches(isDisplayed()))
    }

    @Test
    fun clickingAddOpensSearchBox() {
        onView(allOf(withId(R.id.empty_add_feed), isDisplayed())).perform(click())

        onView(withId(R.id.search_view)).check(matches(isDisplayed()))
    }
}