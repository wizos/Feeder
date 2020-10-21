package com.nononsenseapps.feeder.ui.text

import android.text.Spanned
import android.text.style.ParagraphStyle
import org.ccil.cowan.tagsoup.Parser
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.xml.sax.Attributes
import org.xml.sax.ContentHandler
import org.xml.sax.InputSource
import org.xml.sax.Locator
import org.xml.sax.SAXException
import java.io.IOException
import java.io.Reader
import java.net.URL

class FooConverter(
        private val source: Reader,
        private val siteUrl: URL,
        private val parser: Parser,
        override val kodein: Kodein
): KodeinAware, ContentHandler {
    fun convert() {
        parser.contentHandler = this
        try {
            parser.parse(InputSource(source))
        } catch (e: IOException) {
            // We are reading from a string. There should not be IO problems.
            throw RuntimeException(e)
        } catch (e: SAXException) {
            // TagSoup doesn't throw parse exceptions.
            throw RuntimeException(e)
        }

        TODO("Return something")
//        return spannableStringBuilder
    }

    override fun setDocumentLocator(locator: Locator?) {
    }

    override fun startDocument() {
    }

    override fun endDocument() {
    }

    override fun startPrefixMapping(prefix: String?, uri: String?) {
    }

    override fun endPrefixMapping(prefix: String?) {
    }

    override fun ignorableWhitespace(ch: CharArray?, start: Int, length: Int) {
    }

    override fun processingInstruction(target: String?, data: String?) {
    }

    override fun skippedEntity(name: String?) {
    }

    override fun startElement(uri: String, localName: String, qName: String, atts: Attributes) {
        TODO("Not yet implemented")
    }

    override fun endElement(uri: String, localName: String, qName: String) {
        TODO("Not yet implemented")
    }

    override fun characters(ch: CharArray, start: Int, length: Int) {
        TODO("Not yet implemented")
    }
}
