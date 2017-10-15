package com.nononsenseapps.feeder.ui

import com.nononsenseapps.feeder.db.FeedSQL
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class FeedWrapperTest {

    @Test
    fun equalsAndHashcodeContractForTags() {
        val a = BaseActivity.FeedWrapper("mytag")
        val b = BaseActivity.FeedWrapper("mytag")

        assertTrue(a == b, "Expected equality")
        assertEquals(a.hashCode(), b.hashCode(), "Expected hashcodes to match")
    }

    @Test
    fun equalsAndHashcodeContractForItems() {
        // ID is the only thing which should matter here
        val ai = FeedSQL(id = 99, title = "Item A")
        val bi = FeedSQL(id = 99, title = "Item B")

        val a = BaseActivity.FeedWrapper(ai)
        val b = BaseActivity.FeedWrapper(bi)

        assertTrue(a == b, "Expected equality")
        assertEquals(a.hashCode(), b.hashCode(), "Expected hashcodes to match")
    }
}
