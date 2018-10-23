package com.nononsenseapps.feeder.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.InstrumentationRegistry.getInstrumentation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.URI_FEEDS
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.util.PrefUtils
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL

@RunWith(AndroidJUnit4::class)
@LargeTest
class FeedOverflowMenuWithFeedTests {
    @Rule
    @JvmField
    var activityRule: ActivityTestRule<FeedActivity> = ActivityTestRule(FeedActivity::class.java, false, false)

    private val targetContext: Context
        get() = getInstrumentation().targetContext

    private val db = AppDatabase.getInstance(targetContext)

    @Before
    fun keepNavDrawerClosed() {
        PrefUtils.markWelcomeDone(targetContext)
    }

    @After
    fun clearDb() {
        db.feedDao().deleteAll()
    }

    @Before
    fun openSpecificFeed() {
        val feedId = db.feedDao().insertFeed(Feed(
                title = "testFeed",
                url = URL("http://testfeed"),
                tag = "testTag"
        ))

        val intent = Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(URI_FEEDS, "$feedId"))

        activityRule.launchActivity(intent)
    }

    @Test
    fun clickingEditFeedOpensEdtiorWithFilledFields() {
        openActionBarOverflowOrOptionsMenu(targetContext)
        onView(withText(R.string.edit_feed)).perform(click())

        onView(allOf(withId(R.id.feed_title), withText("testFeed"))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.feed_url), withText("http://testfeed"))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.feed_tag), withText("testTag"))).check(matches(isDisplayed()))
    }

}