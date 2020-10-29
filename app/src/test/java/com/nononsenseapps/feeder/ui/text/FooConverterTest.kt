package com.nononsenseapps.feeder.ui.text

import org.ccil.cowan.tagsoup.Parser
import org.junit.Test
import kotlin.test.assertEquals

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
}
