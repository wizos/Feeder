package com.nononsenseapps.feeder.ui.text

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.ccil.cowan.tagsoup.Parser
import org.junit.Test
import org.junit.runner.RunWith
import org.kodein.di.Kodein
import java.net.URL
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class FooConverterTest {
    private val kodein = Kodein.lazy {

    }
    private val siteUrl = URL("https://foo.com")

    private val parser = Parser().also {
        it.setProperty(Parser.schemaProperty, schema)
    }

    @Test
    fun converterStructureIsSound() {
        val content = """
            <html><head></head><body>
            <div/>
            <div>
            hello
            </div>
            </body></html>
        """.trimIndent()

        val converter = FooConverter(
                content.reader(),
                siteUrl,
                parser,
                kodein
        )

        val rawResult = converter.convert()

        assertEquals(5, rawResult.size)

        assertEquals(1, rawResult.filter { it.isVisible }.size)
    }
}
