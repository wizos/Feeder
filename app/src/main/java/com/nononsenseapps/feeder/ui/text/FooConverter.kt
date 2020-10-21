package com.nononsenseapps.feeder.ui.text

import android.content.res.Resources
import android.graphics.Typeface
import android.text.ParcelableSpan
import android.text.Spannable
import android.text.style.BackgroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.SubscriptSpan
import android.text.style.SuperscriptSpan
import android.text.style.TextAppearanceSpan
import android.text.style.TypefaceSpan
import android.text.style.UnderlineSpan
import com.nononsenseapps.feeder.util.relativeLinkIntoAbsolute
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
) : KodeinAware, ContentHandler {
    private val displayElements = mutableListOf<DisplayElement>()
    private val lastElement: DisplayElement
        get() = displayElements.last()

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
        // Reset state
        displayElements.clear()
        displayElements.add(ParagraphTextElement())
    }

    override fun endDocument() {
        TODO("Handle end?")
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
        lastElement.startTag(localName.toLowerCase(), atts)?.let { nextElement ->
            displayElements.add(nextElement)
        }
    }

    override fun endElement(uri: String, localName: String, qName: String) {
        lastElement.endTag(localName.toLowerCase())?.let { nextElement ->
            displayElements.add(nextElement)
        }
    }

    override fun characters(ch: CharArray, start: Int, length: Int) {
        lastElement.characters(ch, start, length)
    }
}

sealed class DisplayElement {
    /**
     * Returns an element when a different element should be pushed to the list
     */
    abstract fun startTag(tag: String, attributes: Attributes): DisplayElement?


    /**
     * Returns an element when a different element should be pushed to the list
     */
    abstract fun endTag(tag: String): DisplayElement?

    /**
     * Characters to add to the buffer if the element handles text
     */
    abstract fun characters(chars: CharArray, start: Int, length: Int)
}

class BlackholeElement(
        val nextTextElement: ParagraphTextElement
) : DisplayElement() {
    private var ignoreDepth = 0

    override fun startTag(tag: String, attributes: Attributes): DisplayElement? {
        // All nested tags goes to the black hole
        ignoreDepth += 1

        return null
    }

    override fun endTag(tag: String): DisplayElement? {
        return when {
            // End of the black holed tag
            ignoreDepth == 0 -> nextTextElement
            ignoreDepth > 0 -> {
                ignoreDepth -= 1
                null
            }
            else -> {
                error("ignore depth is less than 0: $ignoreDepth. This is a programmer error.")
            }
        }
    }

    override fun characters(chars: CharArray, start: Int, length: Int) {
        // Black hole
    }

}

data class ImageElement(
        val src: String?,
        val width: Int?,
        val height: Int?,
        val alt: String?,
        val nextTextElement: ParagraphTextElement
) : DisplayElement() {
    constructor(attributes: Attributes, nextTextElement: ParagraphTextElement) : this(
            src = attributes.getValue("", "src"),
            width = attributes.getValue("", "width")?.toIntOrNull(),
            height = attributes.getValue("", "height")?.toIntOrNull(),
            alt = attributes.getValue("", "alt"),
            nextTextElement = nextTextElement
    )

    private var ignoreDepth = 0

    override fun startTag(tag: String, attributes: Attributes): DisplayElement? {
        // All nested tags goes to the black hole
        ignoreDepth += 1

        return null
    }

    override fun endTag(tag: String): DisplayElement? {
        return when {
            // End of the black holed tag
            ignoreDepth == 0 -> nextTextElement
            ignoreDepth > 0 -> {
                ignoreDepth -= 1
                null
            }
            else -> {
                error("ignore depth is less than 0: $ignoreDepth. This is a programmer error.")
            }
        }
    }

    override fun characters(chars: CharArray, start: Int, length: Int) {
        // Black hole
    }
}


open class ParagraphTextElement(
        initialPlaceholders: List<Placeholder> = emptyList()
) : DisplayElement() {
    private val spannableStringBuilder = SensibleSpannableStringBuilder()

    private val nextIndex: Int
        get() = spannableStringBuilder.length

    private val codeTextBgColor: Int
        get() = TODO()

    private val urlClickListener: UrlClickListener?
        get() = TODO()

    private val siteUrl: URL
        get() = TODO()

    private val placeholders = mutableListOf<Placeholder>().apply {
        addAll(initialPlaceholders)
    }

    private fun closeTagsAndReturnFreshCopy(): ParagraphTextElement {
        TODO()
    }

    override fun startTag(tag: String, attributes: Attributes): DisplayElement? {
        val result = when (tag) {
            "img" -> ImageElement(attributes, closeTagsAndReturnFreshCopy())
            "p", "div" -> closeTagsAndReturnFreshCopy()
            "blockquote" -> TODO("blockquote - type of formatted")
            "h1", "h2", "h3", "h4", "h5", "h6" -> TODO("header")
            "strong", "b" -> {
                start(Bold(nextIndex))
                null
            }
            "em", "cite", "dfn", "i" -> {
                start(Italic(nextIndex))
                null
            }
            "big" -> {
                start(Big(nextIndex))
                null
            }
            "small" -> {
                start(Small(nextIndex))
                null
            }
            "font" -> {
                start(Font(attributes, nextIndex))
                null
            }
            "tt" -> {
                start(Monospace(nextIndex))
                null
            }
            "a" -> {
                start(Href(attributes, siteUrl, urlClickListener, nextIndex))
                null
            }
            "u" -> {
                start(Underline(nextIndex))
                null
            }
            "sup" -> {
                start(Super(nextIndex))
                null
            }
            "sub" -> {
                start(Sub(nextIndex))
                null
            }
            "code" -> {
                start(Code(codeTextBgColor, nextIndex))
                null
            }
            "ul" -> TODO("unorderd list")
            "ol" -> TODO("ordered list")
            "pre" -> TODO("pre formatted")
            "iframe" -> TODO("iframe")
            "table" -> TODO("table")
            "style", "script" -> BlackholeElement(nextTextElement = closeTagsAndReturnFreshCopy())
            "li" -> TODO("probably ignore list items here then?")
            "tr", "td", "th" -> TODO("probably ignore table items here then?")
            else -> {
                // Ignore the tag and just keep going
                null
            }
        }

        return result
    }

    override fun endTag(tag: String): DisplayElement? {
        when (tag) {
            "br" -> handleBr()
            "strong", "b" -> end<Bold>()
            "em", "cite", "dfn", "i" -> end<Italic>()
            "big" -> end<Big>()
            "small" -> end<Small>()
            "font" -> end<Font>()
            "tt" -> end<Monospace>()
            "a" -> end<Href>()
            "u" -> end<Underline>()
            "sup" -> end<Super>()
            "sub" -> end<Sub>()
            "code" -> end<Code>()
        }

        return null
    }

    override fun characters(chars: CharArray, start: Int, length: Int) {
        for (index in start until (start + length)) {
            val char = chars[index]
            val prev = spannableStringBuilder.lastOrNull()

            when {
                char.isWhitespace() && prev?.isWhitespace() != true -> {
                    spannableStringBuilder.append(' ')
                }
                !char.isWhitespace() -> {
                    spannableStringBuilder.append(char)
                }
            }
        }
    }

    private fun handleBr() {
        spannableStringBuilder.lastOrNull()?.let { last ->
            if (last != '\n') {
                spannableStringBuilder.append('\n')
            }
        }
    }


    private fun start(placeholder: Placeholder) {
        placeholders.add(placeholder)
    }

    private inline fun <reified T : Placeholder> end() {
        placeholders.filterIsInstance<T>()
                .lastOrNull()
                ?.let { placeholder ->
                    if (placeholder.startIndex < nextIndex) {
                        for (realSpans in placeholder.actualSpans()) {
                            spannableStringBuilder.setSpan(
                                    realSpans,
                                    placeholder.startIndex,
                                    nextIndex,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                }
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }
}

class FormattedTextElement : ParagraphTextElement() {
    private val spannableStringBuilder = SensibleSpannableStringBuilder()

    private fun closeTagsAndReturnFreshCopy(): ParagraphTextElement {
        TODO()
    }

    override fun startTag(tag: String, attributes: Attributes): DisplayElement? {
        val result = when (tag) {
            "img" -> ImageElement(attributes, closeTagsAndReturnFreshCopy())
            else -> null
        }

        TODO()

        return result
    }

    override fun endTag(tag: String): DisplayElement? {
        TODO("Not yet implemented")
    }

    override fun characters(chars: CharArray, start: Int, length: Int) {
        for (index in start until (start + length)) {
            val char = chars[index]
            spannableStringBuilder.append(char)
        }
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }
}

sealed class Placeholder {
    abstract fun actualSpans(): Iterable<ParcelableSpan>
    abstract val startIndex: Int
}

data class Bold(override val startIndex: Int) : Placeholder() {
    override fun actualSpans(): Iterable<ParcelableSpan> = listOf(StyleSpan(Typeface.BOLD))
}

data class Italic(override val startIndex: Int) : Placeholder() {
    override fun actualSpans(): Iterable<ParcelableSpan> = listOf(StyleSpan(Typeface.ITALIC))
}

data class Underline(override val startIndex: Int) : Placeholder() {
    override fun actualSpans(): Iterable<ParcelableSpan> = listOf(UnderlineSpan())
}

data class Big(override val startIndex: Int) : Placeholder() {
    override fun actualSpans(): Iterable<ParcelableSpan> = listOf(RelativeSizeSpan(1.25f))
}

data class Small(override val startIndex: Int) : Placeholder() {
    override fun actualSpans(): Iterable<ParcelableSpan> = listOf(RelativeSizeSpan(0.8f))
}

data class Monospace(override val startIndex: Int) : Placeholder() {
    override fun actualSpans(): Iterable<ParcelableSpan> = listOf(TypefaceSpan("monospace"))
}

data class Super(override val startIndex: Int) : Placeholder() {
    override fun actualSpans(): Iterable<ParcelableSpan> = listOf(SuperscriptSpan())
}

data class Sub(override val startIndex: Int) : Placeholder() {
    override fun actualSpans(): Iterable<ParcelableSpan> = listOf(SubscriptSpan())
}

data class Code(val codeTextColor: Int, override val startIndex: Int) : Placeholder() {
    override fun actualSpans(): Iterable<ParcelableSpan> = listOf(
            TypefaceSpan("monospace"),
            RelativeSizeSpan(0.8f),
            BackgroundColorSpan(codeTextColor)
    )
}

data class Font(val color: String?, val face: String?, override val startIndex: Int) : Placeholder() {
    constructor(attributes: Attributes, startIndex: Int) : this(
            color = attributes.getValue("", "color"),
            face = attributes.getValue("", "face"),
            startIndex = startIndex
    )

    override fun actualSpans(): Iterable<ParcelableSpan> {
        return sequence<ParcelableSpan> {
            if (color?.isNotBlank() == true) {
                if (color.startsWith("@")) {
                    val res = Resources.getSystem()
                    val name = color.substring(1)
                    val colorRes = res.getIdentifier(name, "color", "android")
                    if (colorRes != 0) {
                        @Suppress("DEPRECATION")
                        val colors = res.getColorStateList(colorRes)
                        yield(TextAppearanceSpan(null, 0, 0, colors, null))
                    }
                }
            }

            if (face?.isNotBlank() == true) {
                yield(TypefaceSpan(face))
            }
        }.asIterable()
    }
}

data class Href(
        val absUrl: String?,
        val urlClickListener: UrlClickListener?,
        override val startIndex: Int
) : Placeholder() {
    constructor(
            attributes: Attributes,
            siteUrl: URL,
            urlClickListener: UrlClickListener?,
            startIndex: Int
    ) : this(
            absUrl = attributes.getValue("", "href")?.let { relativeLinkIntoAbsolute(siteUrl, it) },
            urlClickListener = urlClickListener,
            startIndex = startIndex
    )

    override fun actualSpans(): Iterable<ParcelableSpan> =
            if (absUrl != null) {
                listOf(URLSpanWithListener(absUrl, urlClickListener))
            } else {
                emptyList()
            }
}
