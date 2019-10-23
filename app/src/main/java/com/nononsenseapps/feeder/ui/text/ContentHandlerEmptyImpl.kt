package com.nononsenseapps.feeder.ui.text

import org.xml.sax.Attributes
import org.xml.sax.ContentHandler
import org.xml.sax.Locator

object ContentHandlerEmptyImpl: ContentHandler {
    override fun endElement(uri: String?, localName: String?, qName: String?) {
    }

    override fun processingInstruction(target: String?, data: String?) {
    }

    override fun startPrefixMapping(prefix: String?, uri: String?) {
    }

    override fun ignorableWhitespace(ch: CharArray?, start: Int, length: Int) {
    }

    override fun characters(ch: CharArray?, start: Int, length: Int) {
    }

    override fun endDocument() {
    }

    override fun startElement(uri: String?, localName: String?, qName: String?, atts: Attributes?) {
    }

    override fun skippedEntity(name: String?) {
    }

    override fun setDocumentLocator(locator: Locator?) {
    }

    override fun endPrefixMapping(prefix: String?) {
    }

    override fun startDocument() {
    }
}
