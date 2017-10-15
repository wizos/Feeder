package com.nononsenseapps.feeder.db

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FeedSQLTest {
    @Test
    fun equalsContract() {
        val a = FeedSQL(id = 99, title = "A")
        val b = FeedSQL(id = 99, title = "B")

        assertTrue(a == b, "Expected equality")
        assertEquals(a.hashCode(), b.hashCode(), "Expected hashcodes to match")
    }
}
