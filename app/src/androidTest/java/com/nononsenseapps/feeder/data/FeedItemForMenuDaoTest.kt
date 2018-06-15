package com.nononsenseapps.feeder.data

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class FeedItemForMenuDaoTest {
    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: FeederDatabase

    private val feed1 = Feed(id = 1, url = "cowboyprogrammer.org")
    private val feed2 = Feed(id = 2, url = "nononsenseapps.com", tag = "foo")
    private val feed3 = Feed(id = 3, url = "bambam.org", tag = "foo")

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
    fun nothingReturnedWhenEmpty() {
        database.feedItemDao().getFeedItemsForList().awaitValue().let {
            assertEquals(emptyList(), it)
        }
    }

    @Test
    fun allItems() {
        insertTestData()

        database.feedItemDao().getFeedItemsForList().awaitValue().let {
            assertEquals(12, it?.size)
        }
    }

    @Test
    fun allUnreadItems() {
        insertTestData()

        database.feedItemDao().getUnreadFeedItemsForList().awaitValue().let {
            assertEquals(6, it?.size)
        }
    }

    @Test
    fun unreadItemsInFeed() {
        insertTestData()

        database.feedItemDao().getUnreadFeedItemsForList(feedId = 1).awaitValue().let {
            assertEquals(1, it?.size)
        }

        database.feedItemDao().getUnreadFeedItemsForList(feedId = 2).awaitValue().let {
            assertEquals(2, it?.size)
        }

        database.feedItemDao().getUnreadFeedItemsForList(feedId = 3).awaitValue().let {
            assertEquals(3, it?.size)
        }
    }

    @Test
    fun itemsInFeed() {
        insertTestData()

        database.feedItemDao().getFeedItemsForList(feedId = 1).awaitValue().let {
            assertEquals(2, it?.size)
        }

        database.feedItemDao().getFeedItemsForList(feedId = 2).awaitValue().let {
            assertEquals(4, it?.size)
        }

        database.feedItemDao().getFeedItemsForList(feedId = 3).awaitValue().let {
            assertEquals(6, it?.size)
        }
    }

    @Test
    fun itemsInTag() {
        insertTestData()

        database.feedItemDao().getFeedItemsForList(tag = feed1.tag).awaitValue().let {
            assertEquals(2, it?.size)
        }

        database.feedItemDao().getFeedItemsForList(tag = feed2.tag).awaitValue().let {
            assertEquals(10, it?.size)
        }
    }

    @Test
    fun unreadItemsInTag() {
        insertTestData()

        database.feedItemDao().getUnreadFeedItemsForList(tag = feed1.tag).awaitValue().let {
            assertEquals(1, it?.size)
        }

        database.feedItemDao().getUnreadFeedItemsForList(tag = feed2.tag).awaitValue().let {
            assertEquals(5, it?.size)
        }
    }

    private fun insertTestData() {
        database.feedDao().insert(listOf(feed1, feed2, feed3))

        feed1.let {
            database.feedItemDao().insert(listOf(feedItem(it),
                    feedItem(it, unread = false)))
        }

        feed2.let {
            database.feedItemDao().insert(listOf(feedItem(it),
                    feedItem(it),
                    feedItem(it, unread = false),
                    feedItem(it, unread = false)))

        }

        feed3.let {
            database.feedItemDao().insert(listOf(feedItem(it),
                    feedItem(it),
                    feedItem(it),
                    feedItem(it, unread = false),
                    feedItem(it, unread = false),
                    feedItem(it, unread = false)))
        }
    }

    private fun feedItem(feed: Feed, unread: Boolean = true) =
            FeedItem(guid = UUID.randomUUID().toString(),
                    unread = unread,
                    feed = feed.id)
}
