package com.nononsenseapps.feeder.db.room

import org.junit.Test
import org.threeten.bp.Duration
import kotlin.test.assertEquals

class DurationRingBufferTest {

    @Test
    fun addAndBumpDoesntOverflow() {
        val buffer = DurationRingBuffer.ofSize(2)
        buffer.addAndBumpIndex(Duration.ofSeconds(1))
        buffer.addAndBumpIndex(Duration.ofSeconds(2))
        buffer.addAndBumpIndex(Duration.ofSeconds(3))
        buffer.addAndBumpIndex(Duration.ofSeconds(4))

        assertEquals("0,3,4", buffer.toString())
    }

    @Test
    fun getMedianOnMinimum() {
        assertEquals(Duration.ofSeconds(0), DurationRingBuffer.ofSize(1).getMedian())
    }

    @Test
    fun getMedian2GivesAverageOfMid() {
        val buffer = DurationRingBuffer.ofSize(2)
        buffer.addAndBumpIndex(Duration.ofSeconds(2))
        buffer.addAndBumpIndex(Duration.ofSeconds(4))
        assertEquals(Duration.ofSeconds(3), buffer.getMedian())
    }

    @Test
    fun getMedian3() {
        val buffer = DurationRingBuffer.ofSize(3)
        buffer.addAndBumpIndex(Duration.ofSeconds(4))
        buffer.addAndBumpIndex(Duration.ofSeconds(1))
        buffer.addAndBumpIndex(Duration.ofSeconds(99))
        assertEquals(Duration.ofSeconds(4), buffer.getMedian())
    }
}
