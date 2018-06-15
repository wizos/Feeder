package com.nononsenseapps.feeder.data

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class FeedDaoTest {
    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: FeederDatabase

    private val feed = Feed(id = 1, url = "cowboyprogrammer.org")

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
        assertNull(database.feedDao().getFeedById(9).awaitValue())
    }

    @Test
    fun insertAndGetFeedWithId() {
        database.feedDao().insert(feed.copy(id = 1))

        database.feedDao().getFeedById(feed.id).awaitValue().let {
            assertEquals(feed, it)
        }
    }

    @Test
    fun insertAndGetFeedNoId() {
        database.feedDao().insert(feed.copy(id = 0))

        database.feedDao().getFeedById(feed.id).awaitValue().let {
            assertEquals(feed, it)
        }
    }

    @Test
    fun insertIdConflict() {
        val second = feed.copy(url = "bob.org")
        database.feedDao().insert(feed)
        database.feedDao().insert(second)

        database.feedDao().getFeedById(1).awaitValue().let {
            assertEquals(second, it)
        }
    }

    @Test
    fun insertUrlConflict() {
        val second = feed.copy(id = 0, title = "second")
        database.feedDao().insert(feed.copy(id = 0, title = "first"))
        database.feedDao().insert(second)

        database.feedDao().getFeedById(1).awaitValue().let {
            assertNull(it)
        }

        database.feedDao().getFeedById(2).awaitValue().let {
            assertEquals(second.copy(id = 2), it)
        }
    }

    @Test
    fun deleteAndGetFeed() {
        database.feedDao().insert(feed)
        database.feedDao().delete(feed)

        database.feedDao().getFeedById(feed.id).awaitValue().let {
            assertNull(it)
        }
    }

    @Test
    fun deleteAndGetFeeds() {
        database.feedDao().insert(feed)
        database.feedDao().delete(listOf(feed))

        database.feedDao().getFeedById(feed.id).awaitValue().let {
            assertNull(it)
        }
    }
}

fun <T> LiveData<T>.awaitValue(): T? {
    val latch = CountDownLatch(1)
    var data: T? = null
    observeForever(object : Observer<T?> {
        override fun onChanged(t: T?) {
            data = t
            latch.countDown()
            removeObserver(this)
        }

    })
    latch.await(2, TimeUnit.SECONDS)
    return data
}
