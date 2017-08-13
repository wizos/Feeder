package com.nononsenseapps.feeder.util

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HelpersKtTest {
    @Test
    @Throws(Exception::class)
    fun nullValue() {
        assertNull(getAbsoluteUrl(null))
    }

    @Test
    @Throws(Exception::class)
    fun absoluteUrl() {
        assertEquals("http://bob.com/img.jpg", getAbsoluteUrl("http://bob.com/img.jpg"))
    }

    @Test
    @Throws(Exception::class)
    fun noProtocolUrl() {

        assertEquals("http://bob.com/img.jpg", getAbsoluteUrl("//bob.com/img.jpg"))
    }
}
