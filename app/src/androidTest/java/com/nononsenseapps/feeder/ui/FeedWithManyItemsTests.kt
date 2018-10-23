package com.nononsenseapps.feeder.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import androidx.test.InstrumentationRegistry.getInstrumentation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.URI_FEEDS
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.util.PrefUtils
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL

@RunWith(AndroidJUnit4::class)
@LargeTest
class FeedWithManyItemsTests {
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

        db.runInTransaction {
            IntRange(1, 500).map {
                FeedItem(
                        feedId = feedId,
                        guid = "guid$it",
                        title = "title $it",
                        description = "description $it",
                        plainTitle = "plain title $it",
                        plainSnippet = "plain snippet $it",
                        author = "John Doe",
                        unread = true
                )
            }.forEach {
                db.feedItemDao().insertFeedItem(it)
            }
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(URI_FEEDS, "$feedId"))

        activityRule.launchActivity(intent)
    }

    @Test
    fun clickingCheckAllClearsTheList() {
        onView(withId(R.id.empty_open_feeds)).check(matches(not(isDisplayed())))
        val list = activityRule.activity.findViewById<RecyclerView>(android.R.id.list)!!


        onView(withId(R.id.checkall_button)).perform(click())

        getInstrumentation().waitForIdleSync()

        onView(withId(R.id.empty_open_feeds)).check(matches(isDisplayed()))
        assertEquals(0, list.adapter?.itemCount)
    }

}
