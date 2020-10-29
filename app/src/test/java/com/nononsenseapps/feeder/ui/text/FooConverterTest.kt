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
}
