package com.nononsenseapps.feeder.data

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.persistence.room.Room
import android.database.sqlite.SQLiteConstraintException
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class FeedItemDaoTest {
    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: FeederDatabase

    private val feed = Feed(id = 1, url = "cowboyprogrammer.org")
    private val feedItem = FeedItem(id = 1, feed = feed.id, guid = "cowboy1")

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                FeederDatabase::class.java)
                // allowing main thread queries, just for testing
                .allowMainThreadQueries()
                .build()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun nothingReturnedForNonExistingId() {
        assertNull(database.feedItemDao().getFeedItemById(1).awaitValue())
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertFeedItemWithoutFeedShouldFail() {
        database.feedItemDao().insert(feedItem)
    }

    @Test
    fun insertFeedItemWithFeed() {
        database.feedDao().insert(feed)
        database.feedItemDao().insert(feedItem)

        database.feedItemDao().getFeedItemById(1).awaitValue().let {
            assertEquals(feedItem, it)
        }
    }

    @Test
    fun insertFeedItemWithPubDate() {
        val feedItem = feedItem.copy(pubDate = DateTime.now())

        database.feedDao().insert(feed)
        database.feedItemDao().insert(feedItem)

        database.feedItemDao().getFeedItemById(1).awaitValue().let {
            assertEquals(feedItem.pubDate.toString(), it?.pubDate?.toString())
        }
    }

    @Test
    fun insertFeedItemWithoutId() {
        database.feedDao().insert(feed)
        database.feedItemDao().insert(feedItem.copy(id = 0))

        database.feedItemDao().getFeedItemById(1).awaitValue().let {
            assertEquals(feedItem, it)
        }
    }

    @Test
    fun conflictingGuidIsHandled() {
        database.feedDao().insert(feed)
        database.feedItemDao().insert(feedItem.copy(id = 0, title = "first", guid = "bob"))
        database.feedItemDao().insert(feedItem.copy(id = 0, title = "second", guid = "bob"))

        database.feedItemDao().getFeedItemById(1).awaitValue().let {
            assertEquals("first", it?.title)
        }

        database.feedItemDao().getFeedItemById(2).awaitValue().let {
            assertNull(it)
        }
    }

    @Test
    fun guidDoNotConflictAcrossFeeds() {
        val first = feedItem.copy(title = "first", guid = "bob", feed = 1)
        val second = feedItem.copy(id = 2, title = "second", guid = "bob", feed = 2)

        database.feedDao().insert(feed)
        database.feedDao().insert(feed.copy(id = 2, url = "whoopie"))
        database.feedItemDao().insert(first)
        database.feedItemDao().insert(second)

        database.feedItemDao().getFeedItemById(1).awaitValue().let {
            assertEquals(first, it)
        }

        database.feedItemDao().getFeedItemById(2).awaitValue().let {
            assertEquals(second, it)
        }
    }

    @Test
    fun deletingFeedDeletesFeedItems() {
        database.feedDao().insert(feed)
        database.feedItemDao().insert(feedItem)

        database.feedDao().delete(feed)

        database.feedItemDao().getFeedItemById(1).awaitValue().let {
            assertNull(it)
        }
    }

    @Test
    fun deleteFeedItem() {
        database.feedDao().insert(feed)
        database.feedItemDao().insert(feedItem)

        database.feedItemDao().delete(feedItem)

        database.feedItemDao().getFeedItemById(1).awaitValue().let {
            assertNull(it)
        }
    }

    @Test
    fun deleteFeedItems() {
        database.feedDao().insert(feed)
        database.feedItemDao().insert(feedItem)

        database.feedItemDao().delete(listOf(feedItem))

        database.feedItemDao().getFeedItemById(1).awaitValue().let {
            assertNull(it)
        }
    }
}
