package com.nononsenseapps.feeder.model

import com.nononsenseapps.feeder.db.room.Feed
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.URL
import java.net.URLEncoder

class RssLocalSyncKtUnitTest {

    @Test
    fun urlToFetchIsFeedWhenNoExtractFull() {
        val feed = Feed(title = "foo", tag = "bar", url = URL("http://foobar"), extractFullText = false)
        assertEquals(feed.url, urlToFetch(feed, URL("http://meh")))
    }

    @Test
    fun urlToFetchIsApiCallWhenExtractFull() {
        val feed = Feed(title = "foo", tag = "bar", url = URL("http://foobar"), extractFullText = true)
        val api = URL("http://api")
        assertEquals(URL("http://api?url=${URLEncoder.encode(feed.url.toString(), "UTF-8")}&max=10&links=preserve"),
                urlToFetch(feed, api))
    }
}
