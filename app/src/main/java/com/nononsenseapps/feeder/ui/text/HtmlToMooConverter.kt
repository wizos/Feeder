package com.nononsenseapps.feeder.ui.text

import android.annotation.SuppressLint
import android.app.Application
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.Editable
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.*
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.util.Prefs
import com.nononsenseapps.feeder.util.relativeLinkIntoAbsolute
import com.nononsenseapps.feeder.util.relativeLinkIntoAbsoluteUrlOrNull
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import org.xml.sax.*
import java.io.IOException
import java.io.StringReader
import java.net.URL
import java.util.*
import kotlin.math.roundToInt


private val headerTags = listOf("h1", "h2", "h3", "h4", "h5", "h6")
private val ignoredTags = listOf("style", "script")
private val literalFormattingTags = listOf("pre", "code")
private val textTags = listOf(
        "br",
        "p",
        "div",
        "strong",
        "b",
        "em",
        "i",
        "cite",
        "dfn",
        "big",
        "small",
        "font",
        "blockquote",
        "tt",
        "a",
        "u",
        "sup",
        "sub",
        "pre",
        "code",
        "ul",
        "ol",
        "li"
) + headerTags
private val HEADER_SIZES = floatArrayOf(1.5f, 1.4f, 1.3f, 1.2f, 1.1f, 1f)


class HtmlToMooConverter(
        private val source: String,
        private val baseUrl: URL,
        override val kodein: Kodein
) : KodeinAware, ContentHandler by ContentHandlerEmptyImpl {
    private val context: Application by instance()
    private val prefs: Prefs by instance()
    private val reader: XMLReader by instance()

    @Suppress("DEPRECATION")
    private val codeTextBgColor = if (prefs.isNightMode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.resources.getColor(R.color.code_text_bg_night, context.theme)
        } else {
            context.resources.getColor(R.color.code_text_bg_night)
        }
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.resources.getColor(R.color.code_text_bg_day, context.theme)
        } else {
            context.resources.getColor(R.color.code_text_bg_day)
        }
    }
    @Suppress("DEPRECATION")
    private val accentColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        context.resources.getColor(R.color.accent, context.theme)
    } else {
        context.resources.getColor(R.color.accent)
    }
    private val quoteGapWidth = context.resources.getDimension(R.dimen.reader_quote_gap_width).roundToInt()
    private val quoteStripeWidth = context.resources.getDimension(R.dimen.reader_quote_stripe_width).roundToInt()
    private val urlClickListener by instance<UrlClickListener2>()

    private val moos = mutableListOf<Moo>()
    private val tagStack = Stack<Tag>()

    private var mooInProgress: Moo? = null


    fun convert(): List<Moo> {
        reader.contentHandler = this

        try {
            reader.parse(InputSource(StringReader(source)))
        } catch (e: IOException) {
            // We are reading from a string. There should not be IO problems.
            throw RuntimeException(e)
        } catch (e: SAXException) {
            // TagSoup doesn't throw parse exceptions.
            throw RuntimeException(e)
        }

        finalizeMoo()
        return moos
    }


    override fun characters(ch: CharArray, start: Int, length: Int) {
        // Ignore characters in ignored tags
        if (tagStack.any { it.ignored }) {
            return
        }

        val literalFormatting = tagStack.any { it.literalSubFormatting }

        val textMoo = ensureTextMooInProgress()

        /*
         * Ignore whitespace that immediately follows other whitespace;
         * newlines count as spaces.
         */
        (start until length)
                .asSequence()
                .map { ch[it] }
                .forEach {
                    if (!literalFormatting && it.isWhitespace()) {
                        val prev: Char = textMoo.lastOrNull() ?: ' '

                        if (!prev.isWhitespace()) {
                            textMoo.append(' ')
                        }
                    } else {
                        textMoo.append(it)
                    }
                }
    }

    @SuppressLint("DefaultLocale")
    override fun startElement(uri: String?, tagName: String, qName: String?, attributes: Attributes) =
            startTag(Tag(
                    name = tagName.toLowerCase(),
                    attributes = attributes,
                    ignored = tagName in ignoredTags,
                    literalSubFormatting = tagName in literalFormattingTags))

    @SuppressLint("DefaultLocale")
    override fun endElement(uri: String?, tagName: String, qName: String?) =
            endTag(tagName.toLowerCase())

    override fun endDocument() {
        finalizeMoo()
    }

    private fun startTag(tag: Tag) {
        when (tag.name) {
            "img" -> startImg(tag)
            "iframe" -> startVideo(tag)
            in textTags -> ensureTextMooInProgress().startTag(tag, baseUrl = baseUrl)
            "table" -> beginTableMoo()
            "td" -> (mooInProgress as? Table)?.startTd(tag)
            "th" -> (mooInProgress as? Table)?.startTh(tag)
            "tr" -> (mooInProgress as? Table)?.startTr(tag)
        }

        // Add this last. order matters
        tagStack.push(tag)
    }

    private fun endTag(tagName: String) {
        when {
            tagStack.isEmpty() -> return
            tagStack.peek().name != tagName -> return
        }

        // Pop it
        val tag = tagStack.pop()

        // Handle tag specific things
        when (tag.name) {
            in textTags -> ensureTextMooInProgress().endTag(tag)
            "table" -> finalizeMoo()
            "td" -> (mooInProgress as? Table)?.endTd(tag)
            "th" -> (mooInProgress as? Table)?.endTh(tag)
            "tr" -> (mooInProgress as? Table)?.endTr(tag)
        }
    }

    private fun startVideo(tag: Tag) {
        // Parse information. If there's no src, do nothing
        val video: Video = getVideo(tag.attributes.getValue("", "src")) ?: return

        // Finalize object of what is being built - unless it's a table, in which case we ignore
        if (mooInProgress is Table) {
            return
        }

        finalizeMoo()

        // Create video and add to list
        val videoMoo = VideoMoo(
                video = video
        )
        moos.add(videoMoo)
    }

    private fun startImg(tag: Tag) {
        // If there's no source, then don't do anything
        val src: URL = relativeLinkIntoAbsoluteUrlOrNull(baseUrl, tag.getValue("", "src")) ?: return

        // Finalize object of what is being built - unless it's a table, in which case we ignore
        if (mooInProgress is Table) {
            return
        }

        finalizeMoo()

        // Create image, add to list
        val image = Image(
                width = tag.getValue("", "width")?.toIntOrNull(),
                height = tag.getValue("", "height")?.toIntOrNull(),
                label = tag.getValue("", "alt"),
                src = src
        )
        moos.add(image)
    }

    // Must NOT modify the stack
    private fun finalizeMoo(): Moo? {
        mooInProgress?.let {
            it.finalize()
            moos.add(it)
        }

        return mooInProgress.also {
            mooInProgress = null
        }
    }

    private fun beginTableMoo(): Table {
        when {
            mooInProgress != null -> finalizeMoo()
        }

        return Table().also {
            mooInProgress = it
        }
    }

    private fun ensureTextMooInProgress(): Text {
        when {
            mooInProgress is Text -> return mooInProgress as Text
            mooInProgress != null -> finalizeMoo()
        }

        val text = Text(
                codeTextBgColor = codeTextBgColor,
                accentColor = accentColor,
                quoteGapWidth = quoteGapWidth,
                quoteStripeWidth = quoteStripeWidth,
                urlClickListener = urlClickListener
        )
        tagStack.asSequence()
                .forEach {
                    text.startTag(it, baseUrl = baseUrl)
                }

        return text.also {
            mooInProgress = it
        }
    }
}

data class Tag(
        val name: String,
        val attributes: Attributes,
        val ignored: Boolean = false,
        val literalSubFormatting: Boolean = false
) : Attributes by attributes

sealed class Moo {
    abstract fun finalize()
}

data class Image(
        val width: Int?,
        val height: Int?,
        val src: URL,
        val label: String?
) : Moo() {
    override fun finalize() {
    }
}

data class VideoMoo(
        val video: Video
) : Moo() {
    override fun finalize() {
    }
}

class Table : Moo() {
    override fun finalize() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    var state: Any? = null

    fun startTd(tag: Tag) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun startTh(tag: Tag) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun startTr(tag: Tag) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun endTd(tag: Tag?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun endTh(tag: Tag?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun endTr(tag: Tag?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

class Text(
        private val codeTextBgColor: Int,
        private val accentColor: Int,
        private val quoteGapWidth: Int,
        private val quoteStripeWidth: Int,
        private val urlClickListener: UrlClickListener2,
        val builder: SpannableStringBuilder = SpannableStringBuilder()
) : Moo(), Editable by builder {
    override fun finalize() {
        // Fix flags and range for paragraph-type markup.
        val obj = builder.getAllSpansWithType<ParagraphStyle>()
        for (anObj in obj) {
            val start = builder.getSpanStart(anObj)
            var end = builder.getSpanEnd(anObj)

            // If the last line of the range is blank, back off by one.
            if (end - 2 >= 0) {
                if (builder[end - 1] == '\n' && builder[end - 2] == '\n') {
                    end--
                }
            }

            if (end == start) {
                builder.removeSpan(anObj)
            }
        }
    }

    fun startTag(tag: Tag, baseUrl: URL) {
        when (tag.name) {
            "br" -> lineBreak()
            "p" -> paragraph()
            "div" -> paragraph()
            "strong" -> startBold()
            "b" -> startBold()
            "em" -> startItalic()
            "i" -> startItalic()
            "cite" -> startItalic()
            "dfn" -> startItalic()
            "big" -> startBig()
            "small" -> startSmall()
            "font" -> startFont(tag)
            "blockquote" -> startBlockQuote()
            "tt" -> startMonoSpace()
            "a" -> startLink(tag, baseUrl = baseUrl)
            "u" -> startUnderline()
            "sup" -> startSuper()
            "sub" -> startSub()
            in headerTags -> startHeader(tag)
            "pre" -> startPre()
            "code" -> startCode()
            "ul" -> startUl(tag)
            "ol" -> startOl(tag)
            "li" -> startLi(tag)
        }
    }

    fun endTag(tag: Tag) {
        when (tag.name) {
            "br" -> lineBreak()
            "p" -> paragraph()
            "div" -> paragraph()
            "strong" -> endBold()
            "b" -> endBold()
            "em" -> endItalic()
            "i" -> endItalic()
            "cite" -> endItalic()
            "dfn" -> endItalic()
            "big" -> endBig()
            "small" -> endSmall()
            "font" -> endFont(tag)
            "blockquote" -> endBlockQuote()
            "tt" -> endMonoSpace()
            "a" -> endLink(tag)
            "u" -> endUnderline()
            "sup" -> endSuper()
            "sub" -> endSub()
            in headerTags -> endHeader(tag)
            "pre" -> endPre()
            "code" -> endCode()
            "ul" -> endUl(tag)
            "ol" -> endOl(tag)
            "li" -> endLi(tag)
        }
    }

    fun lineBreak() {
        if (builder.isNotBlank() && builder.last() != '\n') {
            builder.append("\n")
        }
    }

    fun paragraph() {
        if (builder.isNotBlank()) {
            val len = builder.length
            // Make sure it has spaces before and after
            if (len >= 1 && builder[len - 1] == '\n') {
                if (len >= 2 && builder[len - 2] != '\n') {
                    builder.append("\n")
                }
            } else if (len != 0) {
                builder.append("\n\n")
            }
        }
    }

    fun startSpan(mark: Any) = builder.setSpan(mark, builder.length, builder.length, Spannable.SPAN_MARK_MARK)

    inline fun <reified T> endSpan(replacement: Any) {
        val len = builder.length

        val obj = builder.getLastSpanWithType<T>()
        val where = builder.getSpanStart(obj)

        builder.removeSpan(obj)

        if (where != len) {
            builder.setSpan(replacement, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun startBold() = startSpan(Bold)

    fun startItalic() = startSpan(Italic)

    fun startBig() = startSpan(Big)

    fun startSmall() = startSpan(Small)

    fun startFont(tag: Tag) {
        val color: String? = tag.getValue("", "color")
        val face: String? = tag.getValue("", "face")

        val len = builder.length
        // Use empty string to prevent null pointer errors. empty string will be ignored in endFont.
        builder.setSpan(Font(color ?: "", face), len, len, Spannable.SPAN_MARK_MARK)
    }

    fun startBlockQuote() {
        // Ensure spacing
        paragraph()
        startSpan(Blockquote)
    }

    fun startMonoSpace() = startSpan(Monospace)

    fun startLink(tag: Tag, baseUrl: URL) {
        var href: String? = tag.getValue("", "href")

        if (href != null) {
            // Yes, this was an observed null pointer exception
            href = relativeLinkIntoAbsolute(baseUrl, href)
        }

        val len = builder.length
        builder.setSpan(Href(href), len, len, Spannable.SPAN_MARK_MARK)
    }

    fun startUnderline() = startSpan(Underline)

    fun startSuper() = startSpan(Super)

    fun startSub() = startSpan(Sub)

    fun startHeader(tag: Tag) {
        // Ensure spacing
        paragraph()
        startSpan(Header(tag.name.lastOrNull()?.toInt() ?: 1))
    }

    fun startPre() {
        // Ensure spacing
        paragraph()
        startSpan(Pre)
    }

    fun startCode() = startSpan(Code)

    private fun startLi(tag: Tag) {
        // Get type of list
        val list: Listing? = builder.getLastSpanWithType<Listing>()

        if (list?.ordered == true) {
            // Numbered
            // Add number in bold
            startBold()
            builder.append("${list.number}. ")
            list.number += 1
            endBold()
            // Then do a leading margin
            startSpan(CountBullet())
        } else {
            // Bullet
            startSpan(Bullet())
        }
    }

    private fun startOl(tag: Tag) {
        lineBreak()

        // Remember list type
        startSpan(Listing(true))
    }

    private fun startUl(tag: Tag) {
        lineBreak()

        // Remember list type
        startSpan(Listing(false))
    }

    private fun endLi(tag: Tag) {
        val len = builder.length
        val obj = builder.getLastSpanWithType<Bullet>()
        val where = builder.getSpanStart(obj)

        builder.removeSpan(obj)

        if (where != len) {
            val offset = 60
            val span: LeadingMarginSpan = if (obj is CountBullet) {
                // Numbered
                LeadingMarginSpan.Standard(offset, offset)
            } else {
                // Bullet points
                BulletSpan(offset, Color.GRAY)
            }

            builder.setSpan(span, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        // Add newline
        builder.append("\n")
    }

    private fun endOl(tag: Tag) {
        val obj = builder.getLastSpanWithType<Listing>()
        builder.removeSpan(obj)
    }

    private fun endUl(tag: Tag) {
        val obj = builder.getLastSpanWithType<Listing>()
        builder.removeSpan(obj)
    }

    fun endBold() = endSpan<Bold>(StyleSpan(Typeface.BOLD))

    fun endItalic() = endSpan<Italic>(StyleSpan(Typeface.ITALIC))

    fun endBig() = endSpan<Big>(RelativeSizeSpan(1.25f))

    fun endSmall() = endSpan<Small>(RelativeSizeSpan(0.8f))

    fun endFont(tag: Tag?) {
        val len = builder.length
        val obj = builder.getLastSpanWithType<Font>()
        val where = builder.getSpanStart(obj)

        builder.removeSpan(obj)

        if (where != len) {
            val f: Font? = obj

            if (f?.color?.isNotEmpty() == true) {
                if (f.color.startsWith("@")) {
                    val res = Resources.getSystem()
                    val name = f.color.substring(1)
                    val colorRes = res.getIdentifier(name, "color", "android")
                    if (colorRes != 0) {
                        @Suppress("DEPRECATION")
                        val colors = res.getColorStateList(colorRes)
                        builder.setSpan(
                                TextAppearanceSpan(null, 0, 0, colors, null),
                                where, len,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }

            f?.face?.let {
                builder.setSpan(TypefaceSpan(it), where, len,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    fun endBlockQuote() {
        // Don't want end newlines inside block=
        while (builder.isNotEmpty() && builder.last() == '\n') {
            builder.delete(builder.length - 1, builder.length)
        }

        val len = builder.length
        val obj = builder.getLastSpanWithType<Blockquote>()
        val where = builder.getSpanStart(obj)

        builder.removeSpan(obj)

        if (where != len) {
            // Set quote span
            builder.setSpan(
                    MyQuoteSpan(
                            accentColor,
                            quoteGapWidth,
                            quoteStripeWidth),
                    where,
                    len,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            // Be slightly smaller
            builder.setSpan(
                    RelativeSizeSpan(0.8f),
                    where,
                    len,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        paragraph()
    }

    fun endMonoSpace() = endSpan<Monospace>(TypefaceSpan("monospace"))

    fun endLink(tag: Tag?) {
        val len = builder.length
        val obj = builder.getLastSpanWithType<Href>()
        val where = builder.getSpanStart(obj)

        builder.removeSpan(obj)

        if (where != len) {
            obj?.href?.let { link ->
                builder.setSpan(
                        URLSpanWithListener2(link, urlClickListener),
                        where,
                        len,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    fun endUnderline() = endSpan<Underline>(UnderlineSpan())

    fun endSuper() = endSpan<Super>(SuperscriptSpan())

    fun endSub() = endSpan<Sub>(SubscriptSpan())

    fun endHeader(tag: Tag?) {
        var len = builder.length
        val obj = builder.getLastSpanWithType<Header>()

        val where = builder.getSpanStart(obj)

        builder.removeSpan(obj)

        // Back off not to change only the text, not the blank line.
        while (len > where && builder[len - 1] == '\n') {
            len--
        }

        if (where != len) {
            obj?.level?.let {
                builder.setSpan(RelativeSizeSpan(HEADER_SIZES[it]), where,
                        len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            builder.setSpan(StyleSpan(Typeface.BOLD), where, len,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun endPre() {
        // yes, take len before appending
        val len = builder.length

        val obj = builder.getLastSpanWithType<Pre>()
        val where = builder.getSpanStart(obj)

        builder.removeSpan(obj)

        if (where != len) {
            // Should make sure text does not wrap.
            // No easy solution exists for this
            builder.setSpan(
                    AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL),
                    where,
                    len,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // and end with two newlines
        paragraph()
    }

    fun endCode() {
        val len = builder.length
        val obj = builder.getLastSpanWithType<Code>()
        val where = builder.getSpanStart(obj)

        builder.removeSpan(obj)

        if (where != len) {
            // Want it to be monospace
            builder.setSpan(TypefaceSpan("monospace"), where, len,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            // Be slightly smaller
            builder.setSpan(RelativeSizeSpan(0.8f), where, len,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            // And have background color
            builder.setSpan(BackgroundColorSpan(codeTextBgColor), where, len,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private object Bold

    private object Italic

    private object Underline

    private object Big

    private object Small

    private object Monospace

    private object Blockquote

    private object Super

    private object Sub

    private class Listing(var ordered: Boolean, var number: Int = 1)

    private open class Bullet

    private class CountBullet : Bullet()

    private object Pre

    private object Code

    private class Font(var color: String, var face: String?)

    private class Href(var href: String?)

    private class Header(var level: Int)
}

fun SpannableStringBuilder.getAllSpans(): Sequence<Any?> =
        getSpans(0, length, Object::class.java).asSequence()

inline fun <reified T> SpannableStringBuilder.getAllSpansWithType(): Sequence<T> =
        getAllSpans().filterIsInstance(T::class.java)

inline fun <reified T> SpannableStringBuilder.getLastSpanWithType(): T? =
        getAllSpansWithType<T>().lastOrNull()


interface UrlClickListener2 {
    fun accept(url: String)
}
