package com.nononsenseapps.feeder.db.room

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.ZonedDateTime

class ConvertersTest {
    @Test
    fun zeroIs1970() {
        assertEquals(Instant.EPOCH,
                Converters().instantFromLong(0))
    }

    @Test
    fun negativeLongGivesValidDate() {
        assertEquals(Instant.ofEpochMilli(-1),
                Converters().instantFromLong(-1))
    }

    @Test
    fun noLongGivesNullDate() {
        assertNull(Converters().instantFromLong(null))
    }

    @Test
    fun noDateTimeGivesNull() {
        assertNull(Converters().longFromInstant(null))
    }

    @Test
    fun instantGivesLong() {
        assertEquals(1514768461000,
                Converters().longFromInstant(ZonedDateTime.parse("2018-01-01T01:01:01Z").toInstant()))
    }

    @Test
    fun durationGivesLongOfSeconds() {
        assertEquals(6L,
                Converters().longFromDuration(Duration.ofSeconds(6)))
    }

    @Test
    fun ringBufferToString() {
        assertEquals("1,0,4,7",
                Converters().stringFromRingBuffer(
                        DurationRingBuffer(
                                1,
                                arrayOf(Duration.ZERO, Duration.ofSeconds(4), Duration.ofSeconds(7))
                        )
                )
        )
    }

    @Test
    fun ringBufferFromString() {
        assertEquals(
                DurationRingBuffer(
                        1,
                        arrayOf(Duration.ZERO, Duration.ofSeconds(4), Duration.ofSeconds(7))
                ),
                Converters().ringBufferFromString("1,0,4,7")
        )
    }
}
