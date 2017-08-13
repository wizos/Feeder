package com.nononsenseapps.feeder.model

import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FeedParserTest2 {
    @Rule
    @JvmField
    var tempFolder = TemporaryFolder()

    @Test
    @Throws(Exception::class)
    @Ignore
    fun relativeLinksAreMadeAbsoluteAtom() {

        val feed = FeedParser.parseFeed(atomRelative.byteInputStream())
        assertNotNull(feed)

        assertEquals("http://cowboyprogrammer.org/feed.atom", FeedParser.selfLink(feed))
    }

    @Test
    @Throws(Exception::class)
    @Ignore
    fun relativeLinksAreMadeAbsoluteAtomNoBase() {

        val feed = FeedParser.parseFeed(atomRelativeNoBase.byteInputStream())
        assertNotNull(feed)

        assertEquals("http://cowboyprogrammer.org/feed.atom", FeedParser.selfLink(feed))
    }

    @Test
    @Throws(Exception::class)
    fun thumbnailsWithNoProtocolGetsAProtocol() {

        val feed = FeedParser.parseFeed(atomMissingProtocol.byteInputStream())
        assertNotNull(feed)

        assertEquals("http://bob.com/img.jpg", FeedParser.thumbnail(feed.entries[0]))
    }
}

val atomMissingProtocol = """
<?xml version='1.0' encoding='UTF-8'?>
<feed xmlns='http://www.w3.org/2005/Atom' xml:base='http://cowboyprogrammer.org'>
  <id>http://cowboyprogrammer.org</id>
  <title>Relative links</title>
  <updated>2003-12-13T18:30:02Z</updated>
  <link rel="self" href="/feed.atom"/>
<entry>
    <id>http://localhost:1313/2016/09/reboot_machine_on_wrong_password/</id>
    <link href="http://localhost:1313/2016/09/reboot_machine_on_wrong_password/" rel="alternate" />
    <title>Rebooting on wrong password</title>
    <updated>2016-09-28T22:57:21+02:00</updated>
    <author>
      <name>Space Cowboy</name>
      <email>jonas@cowboyprogrammer.org</email>
    </author>

    <media:thumbnail xmlns:media="http://search.yahoo.com/mrss/" url="//bob.com/img.jpg" height="72" width="72"/>
    <summary>hello</summary>
    <content>hello</content>
  </entry>
</feed>
"""

val atomRelative = """
<?xml version='1.0' encoding='UTF-8'?>
<feed xmlns='http://www.w3.org/2005/Atom' xml:base='http://cowboyprogrammer.org'>
  <id>http://cowboyprogrammer.org</id>
  <title>Relative links</title>
  <updated>2003-12-13T18:30:02Z</updated>
  <link rel="self" href="/feed.atom"/>
</feed>
"""

val atomRelativeNoBase = """
<?xml version='1.0' encoding='UTF-8'?>
<feed xmlns='http://www.w3.org/2005/Atom'>
  <id>http://cowboyprogrammer.org</id>
  <title>Relative links</title>
  <updated>2003-12-13T18:30:02Z</updated>
  <link rel="self" href="/feed.atom"/>
</feed>
"""
