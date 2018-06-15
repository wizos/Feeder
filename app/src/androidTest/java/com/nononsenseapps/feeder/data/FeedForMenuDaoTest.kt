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
class FeedForMenuDaoTest {
    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: FeederDatabase

    private val feed1 = Feed(id = 1, url = "cowboyprogrammer.org")
    private val feed2 = Feed(id = 2, url = "nononsenseapps.com")

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
        database.feedDao().getFeedsForMenu().awaitValue().let {
            assertEquals(emptyList(), it)
        }
    }

    @Test
    fun noFeedItemsMeanZeroCount() {
        database.feedDao().insert(feed1)

        database.feedDao().getFeedsForMenu().awaitValue().let {
            assertEquals(listOf(feedForMenu(feed1)), it)
        }
    }

    @Test
    fun feedWithReadItemsMeanZeroCount() {
        database.feedDao().insert(feed1)
        database.feedItemDao().insert(feedItem(feed1, unread = false))
        database.feedItemDao().insert(feedItem(feed1, unread = false))

        database.feedDao().getFeedsForMenu().awaitValue().let {
            assertEquals(listOf(feedForMenu(feed1)), it)
        }
    }

    @Test
    fun feedWithUnreadItems() {
        database.feedDao().insert(feed1)
        database.feedItemDao().insert(feedItem(feed1))
        database.feedItemDao().insert(feedItem(feed1))

        database.feedDao().getFeedsForMenu().awaitValue().let {
            assertEquals(listOf(feedForMenu(feed1, unreadCount = 2)), it)
        }
    }

    @Test
    fun feedsWithUnreadItems() {
        database.feedDao().insert(feed1)
        database.feedItemDao().insert(feedItem(feed1))
        database.feedItemDao().insert(feedItem(feed1))
        database.feedItemDao().insert(feedItem(feed1, unread = false))
        database.feedItemDao().insert(feedItem(feed1, unread = false))


        database.feedDao().insert(feed2)
        database.feedItemDao().insert(feedItem(feed2))
        database.feedItemDao().insert(feedItem(feed2))
        database.feedItemDao().insert(feedItem(feed2))
        database.feedItemDao().insert(feedItem(feed2, unread = false))

        database.feedDao().getFeedsForMenu().awaitValue().let {
            assertEquals(listOf(feedForMenu(feed1, unreadCount = 2),
                    feedForMenu(feed2, unreadCount = 3)), it)
        }
    }

    private fun feedForMenu(feed: Feed, unreadCount: Int = 0) =
            FeedForMenu(id = feed.id,
                    title = feed.title,
                    customTitle = feed.customTitle,
                    url = feed.url,
                    tag = feed.tag,
                    unreadCount = unreadCount)

    private fun feedItem(feed: Feed, unread: Boolean = true) =
            FeedItem(guid = UUID.randomUUID().toString(),
                    unread = unread,
                    feed = feed.id)
}
