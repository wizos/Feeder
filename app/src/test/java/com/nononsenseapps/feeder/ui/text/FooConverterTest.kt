package com.nononsenseapps.feeder.ui.text

import org.ccil.cowan.tagsoup.Parser
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FooConverterTest {
    private val parser = Parser().also {
        it.setProperty(Parser.schemaProperty, schema)
    }

    @Test
    fun converterStructureIsSound() {
        val content = """
            <div/>
            <div>
            hello
            <img>
            </div>
        """.trimIndent()

        val converter = FooConverter(
                content.reader(),
                parser
        )

        val rawResult = converter.convert()

        assertEquals(
                7,
                rawResult.size,
                "$rawResult"
        )

        val filtered = rawResult.filter { it.isVisible }

        assertEquals(
                1,
                filtered.size,
                "$filtered"
        )
    }

    @Test
    fun converterStructureIsSoundFormatted() {
        val content = """
            <pre></pre>
            <pre> </pre>
            <pre> hello </pre>
        """.trimIndent()

        val converter = FooConverter(
                content.reader(),
                parser
        )

        val rawResult = converter.convert()

        assertEquals(
                7,
                rawResult.size,
                "$rawResult"
        )

        val filtered = rawResult.filter { it.isVisible }

        assertEquals(
                1,
                filtered.size,
                "$filtered"
        )
    }

    @Test
    fun tableElementNotVisible() {
        val content = """
            <table>
              <tr><th> </th><th> </th></tr>
              <tr><td> </td><td> </td></tr>
              <tr><td> </td><td> </td></tr>
            </table>
        """.trimIndent()

        val converter = FooConverter(
                content.reader(),
                parser
        )

        val table = converter.convert().filterIsInstance(TableElement::class.java).single()

        assertFalse { table.isVisible }
    }

    @Test
    fun tableElementWorks() {
        val content = """
            <table>
              <tr><th>car</th><th>price</th></tr>
              <tr><td>volvo</td><td>100</td></tr>
              <tr><td>tesla</td><td>99</td></tr>
            </table>
        """.trimIndent()

        val converter = FooConverter(
                content.reader(),
                parser
        )

        val table = converter.convert().filterIsInstance(TableElement::class.java).single()

        assertEquals(listOf("car", "price"), table.getRows().first())
        assertEquals(listOf("volvo", "100"), table.getRows()[1])
        assertEquals(listOf("tesla", "99"), table.getRows().last())

        assertTrue { table.isVisible }
    }

    @Test
    fun blockquoteIsNotVisible() {
        val content = """
            <blockquote>
            </blockquote>
        """.trimIndent()

        val converter = FooConverter(
                content.reader(),
                parser
        )

        val quote = converter.convert().filterIsInstance(BlockQuoteElement::class.java).single()

        assertFalse{
            quote.isVisible
        }

        assertNull(quote.citeRelativeUrl)
    }

    @Test
    fun blockquoteWorks() {
        val content = """
            <blockquote cite="google.com">
            Hi, my name  is Cory,   Cory Carson.
            </blockquote>
        """.trimIndent()

        val converter = FooConverter(
                content.reader(),
                parser
        )

        val quote = converter.convert().filterIsInstance(BlockQuoteElement::class.java).single()

        assertTrue{
            quote.isVisible
        }

        assertEquals("google.com", quote.citeRelativeUrl)
        assertEquals("Hi, my name is Cory, Cory Carson.", quote.getText().trim())
    }

    @Test
    fun headerIsNotVisible() {
        val content = """
            <h3>
            </h3>
        """.trimIndent()

        val converter = FooConverter(
                content.reader(),
                parser
        )

        val header = converter.convert().filterIsInstance(HeaderElement::class.java).single()

        assertFalse{
            header.isVisible
        }
    }

    @Test
    fun headerWorks() {
        val content = """
            <h1>big1</h1>
            <h2>big2</h2>
            <h3>big3</h3>
            <h4>big4</h4>
            <h5>big5</h5>
            <h6>big6</h6>
        """.trimIndent()

        val converter = FooConverter(
                content.reader(),
                parser
        )

        val headers = converter.convert().filterIsInstance(HeaderElement::class.java)

        assertEquals(6, headers.size)

        headers.forEachIndexed { index, headerElement ->
            assertTrue{
                headerElement.isVisible
            }

            assertEquals("big${index + 1}", headerElement.getText())
        }
    }

    @Test
    fun orderedListIsNotVisible() {
        val content = """
            <ol>
            <li></li>
            </ol>
        """.trimIndent()

        val converter = FooConverter(
                content.reader(),
                parser
        )

        val list = converter.convert().filterIsInstance(ListElement::class.java).single()

        assertFalse{
            list.isVisible
        }
    }

    @Test
    fun orderedListWorks() {
        val content = """
            <ol>
            <li>one</li>
            <li>two</li>
            </ol>
        """.trimIndent()

        val converter = FooConverter(
                content.reader(),
                parser
        )

        val list = converter.convert().filterIsInstance(ListElement::class.java).single()

        assertEquals(2, list.getRows().size)
        assertTrue { list.ordered }

        assertEquals("one", list.getRows().first())
        assertEquals("two", list.getRows().last())
    }

    @Test
    fun unorderedListIsNotVisible() {
        val content = """
            <ul>
            <li></li>
            </ul>
        """.trimIndent()

        val converter = FooConverter(
                content.reader(),
                parser
        )

        val list = converter.convert().filterIsInstance(ListElement::class.java).single()

        assertFalse{
            list.isVisible
        }
    }

    @Test
    fun unorderedListWorks() {
        val content = """
            <ul>
            <li>one</li>
            <li>two</li>
            </ul>
        """.trimIndent()

        val converter = FooConverter(
                content.reader(),
                parser
        )

        val list = converter.convert().filterIsInstance(ListElement::class.java).single()

        assertEquals(2, list.getRows().size)
        assertFalse { list.ordered }

        assertEquals("one", list.getRows().first())
        assertEquals("two", list.getRows().last())
    }

    @Test
    fun iframeWithNoContentIgnored() {
        val content = """
            <iframe/>
        """.trimIndent()

        val converter = FooConverter(
                content.reader(),
                parser
        )

        val list = converter.convert()

        assertEquals(1, list.size)
        assertTrue { list.first() is ParagraphTextElement }
    }

    @Test
    fun iframeWithNoVideoIsIgnored() {
        val content = """
            <iframe src="google.com"/>
        """.trimIndent()

        val converter = FooConverter(
                content.reader(),
                parser
        )

        val list = converter.convert()

        assertEquals(1, list.size)
        assertTrue { list.first() is ParagraphTextElement }
    }

    @Test
    fun iframeWithVideoWorks() {
        val content = """
            <iframe src="http://www.youtube.com/embed/cjxnVO9RpaQ/theoretical_crap"/>
        """.trimIndent()

        val converter = FooConverter(
                content.reader(),
                parser
        )

        val videoElement = converter.convert().filterIsInstance(VideoElement::class.java).single()

        assertEquals(
                "http://www.youtube.com/embed/cjxnVO9RpaQ/theoretical_crap",
                videoElement.video.src
        )
    }
}
