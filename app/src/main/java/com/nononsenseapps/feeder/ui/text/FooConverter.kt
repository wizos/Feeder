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

    fun convert(): List<DisplayElement> {
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

        return displayElements
    }

    override fun setDocumentLocator(locator: Locator?) {
    }

    override fun startDocument() {
        // Reset state
        displayElements.clear()
        displayElements.add(ParagraphTextElement())
    }

    override fun endDocument() {
        displayElements.lastOrNull()?.close()
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

    /**
     * Called in the rare case that an open tag is left when document is closed
     */
    open fun close() {}

    /**
     * Returns true if there is visible content - false otherwise
     */
    abstract val isVisible: Boolean
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

    override val isVisible: Boolean = false

}

data class ImageElement(
        val src: String?,
        val width: Int?,
        val height: Int?,
        val alt: String?,
        val link: String?,
        val nextTextElement: ParagraphTextElement
) : DisplayElement() {
    constructor(
            attributes: Attributes,
            nextTextElement:
            ParagraphTextElement,
            link: String?
    ) : this(
            src = attributes.getValue("", "src"),
            width = attributes.getValue("", "width")?.toIntOrNull(),
            height = attributes.getValue("", "height")?.toIntOrNull(),
            alt = attributes.getValue("", "alt"),
            link = link,
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

    override val isVisible: Boolean = src?.isNotBlank() == true
}


open class ParagraphTextElement(
        initialPlaceholders: List<Placeholder> = emptyList(),
        open val nextTextElement: ParagraphTextElement? = null
) : DisplayElement() {
    protected var spannableStringBuilder = SensibleSpannableStringBuilder()

    private val nextIndex: Int
        get() = spannableStringBuilder.length

    private val codeTextBgColor: Int
        get() = TODO()

    private val urlClickListener: UrlClickListener?
        get() = TODO()

    private val siteUrl: URL
        get() = TODO()

    protected val placeholders = mutableListOf<Placeholder>().apply {
        addAll(initialPlaceholders)
    }

    override fun close() {
        while (placeholders.isNotEmpty()) {
            end(placeholders.last().javaClass)
        }
    }

    override val isVisible: Boolean
        get() = spannableStringBuilder.isNotBlank()

    private fun closeTagsAndReturnFreshCopy(): ParagraphTextElement {
        val initialPlaceholders = mutableListOf<Placeholder>().also { list ->
            list.addAll(placeholders.map { it.copyWithZeroStartIndex() })
        }

        this.close()

        return ParagraphTextElement(
                initialPlaceholders = initialPlaceholders,
                nextTextElement = nextTextElement
        )
    }

    protected open fun startParagraph(): ParagraphTextElement? {
        return closeTagsAndReturnFreshCopy()
    }

    private fun endParagraph(): ParagraphTextElement? {
        return when {
            nextTextElement != null -> {
                close()
                nextTextElement
            }
            else -> closeTagsAndReturnFreshCopy()
        }
    }

    protected open fun startFormattedParagraph(): FormattedTextElement? {
        val initialPlaceholders = mutableListOf<Placeholder>().also { list ->
            list.addAll(placeholders.map { it.copyWithZeroStartIndex() })
        }

        return FormattedTextElement(
                nextTextElement = closeTagsAndReturnFreshCopy(),
                initialPlaceholders = initialPlaceholders
        )
    }

    override fun startTag(tag: String, attributes: Attributes): DisplayElement? {
        when (tag) {
            "strong", "b" -> start(Bold(nextIndex))
            "em", "cite", "dfn", "i" -> start(Italic(nextIndex))
            "big" -> start(Big(nextIndex))
            "small" -> start(Small(nextIndex))
            "font" -> start(Font(attributes, nextIndex))
            "tt" -> start(Monospace(nextIndex))
            "a" -> start(Href(attributes, siteUrl, urlClickListener, nextIndex))
            "u" -> start(Underline(nextIndex))
            "sup" -> start(Super(nextIndex))
            "sub" -> start(Sub(nextIndex))
            "code" -> start(Code(codeTextBgColor, nextIndex))
        }

        return when (tag) {
            "img" -> ImageElement(
                    attributes,
                    link = placeholders.filterIsInstance<Href>().lastOrNull()?.absUrl,
                    nextTextElement = closeTagsAndReturnFreshCopy()
            )
            "p", "div" -> startParagraph()
            "blockquote" -> TODO("blockquote - indented paragraph with a possible 'cite' source URL in attributes")
            "h1", "h2", "h3", "h4", "h5", "h6" -> TODO("header")
            "ul" -> TODO("unorderd list")
            "ol" -> TODO("ordered list")
            "pre" -> startFormattedParagraph()
            "iframe" -> TODO("iframe")
            "table" -> TODO("table")
            "style", "script" -> BlackholeElement(nextTextElement = closeTagsAndReturnFreshCopy())
            "li" -> TODO("probably ignore list items here then?")
            "tr", "td", "th" -> TODO("probably ignore table items here then?")
            else -> {
                // keep going
                null
            }
        }
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

        return when (tag) {
            "p", "div" -> endParagraph()
            else -> null
        }
    }

    override fun characters(chars: CharArray, start: Int, length: Int) {
        for (index in start until (start + length)) {
            val char = chars[index]
            val prev = spannableStringBuilder.lastOrNull()

            when {
                char.isWhitespace() && prev?.isWhitespace() == false -> {
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

    private fun <T : Placeholder> end(klass: Class<T>) {
        placeholders.filterIsInstance(klass)
                .lastOrNull()
                ?.let { placeholder ->
                    placeholders.remove(placeholder)

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

    private inline fun <reified T : Placeholder> end() = end(T::class.java)

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }

    override fun toString(): String {
        return "PARAGRAPH('${spannableStringBuilder.toString().take(15)}')"
    }
}

class FormattedTextElement(
        override val nextTextElement: ParagraphTextElement,
        initialPlaceholders: List<Placeholder>
) : ParagraphTextElement(initialPlaceholders, nextTextElement) {

    override fun startParagraph(): ParagraphTextElement? {
        val initialPlaceholders = mutableListOf<Placeholder>().also { list ->
            list.addAll(placeholders.map { it.copyWithZeroStartIndex() })
        }

        this.close()

        return ParagraphTextElement(
                initialPlaceholders = initialPlaceholders,
                nextTextElement = FormattedTextElement(
                        initialPlaceholders = initialPlaceholders,
                        nextTextElement = nextTextElement
                )
        )
    }

    override fun startFormattedParagraph(): FormattedTextElement? {
        val initialPlaceholders = mutableListOf<Placeholder>().also { list ->
            list.addAll(placeholders.map { it.copyWithZeroStartIndex() })
        }

        this.close()

        return FormattedTextElement(
                initialPlaceholders = initialPlaceholders,
                nextTextElement = FormattedTextElement(
                        initialPlaceholders = initialPlaceholders,
                        nextTextElement = nextTextElement
                )
        )
    }

    override fun endTag(tag: String): DisplayElement? {
        return when (tag) {
            "pre" -> {
                close()
                nextTextElement
            }
            else -> super.endTag(tag)
        }
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

    override fun toString(): String {
        return "FORMATTED('${spannableStringBuilder.toString().take(15)}')"
    }
}

sealed class Placeholder {
    abstract fun actualSpans(): Iterable<ParcelableSpan>
    abstract val startIndex: Int
    abstract fun copyWithZeroStartIndex(): Placeholder
}

data class Bold(override val startIndex: Int) : Placeholder() {
    override fun actualSpans(): Iterable<ParcelableSpan> = listOf(StyleSpan(Typeface.BOLD))
    override fun copyWithZeroStartIndex() = copy(startIndex = 0)
}

data class Italic(override val startIndex: Int) : Placeholder() {
    override fun actualSpans(): Iterable<ParcelableSpan> = listOf(StyleSpan(Typeface.ITALIC))
    override fun copyWithZeroStartIndex() = copy(startIndex = 0)
}

data class Underline(override val startIndex: Int) : Placeholder() {
    override fun actualSpans(): Iterable<ParcelableSpan> = listOf(UnderlineSpan())
    override fun copyWithZeroStartIndex() = copy(startIndex = 0)
}

data class Big(override val startIndex: Int) : Placeholder() {
    override fun actualSpans(): Iterable<ParcelableSpan> = listOf(RelativeSizeSpan(1.25f))
    override fun copyWithZeroStartIndex() = copy(startIndex = 0)
}

data class Small(override val startIndex: Int) : Placeholder() {
    override fun actualSpans(): Iterable<ParcelableSpan> = listOf(RelativeSizeSpan(0.8f))
    override fun copyWithZeroStartIndex() = copy(startIndex = 0)
}

data class Monospace(override val startIndex: Int) : Placeholder() {
    override fun actualSpans(): Iterable<ParcelableSpan> = listOf(TypefaceSpan("monospace"))
    override fun copyWithZeroStartIndex() = copy(startIndex = 0)
}

data class Super(override val startIndex: Int) : Placeholder() {
    override fun actualSpans(): Iterable<ParcelableSpan> = listOf(SuperscriptSpan())
    override fun copyWithZeroStartIndex() = copy(startIndex = 0)
}

data class Sub(override val startIndex: Int) : Placeholder() {
    override fun actualSpans(): Iterable<ParcelableSpan> = listOf(SubscriptSpan())
    override fun copyWithZeroStartIndex() = copy(startIndex = 0)
}

data class Code(val codeTextColor: Int, override val startIndex: Int) : Placeholder() {
    override fun actualSpans(): Iterable<ParcelableSpan> = listOf(
            TypefaceSpan("monospace"),
            RelativeSizeSpan(0.8f),
            BackgroundColorSpan(codeTextColor)
    )

    override fun copyWithZeroStartIndex() = copy(startIndex = 0)
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

    override fun copyWithZeroStartIndex() = copy(startIndex = 0)
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

    override fun copyWithZeroStartIndex() = copy(startIndex = 0)
}
