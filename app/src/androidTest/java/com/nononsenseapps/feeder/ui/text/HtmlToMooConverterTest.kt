package com.nononsenseapps.feeder.ui.text

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.ccil.cowan.tagsoup.Parser
import org.junit.Test
import org.junit.runner.RunWith
import org.kodein.di.Kodein
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton
import org.xml.sax.XMLReader
import java.net.URL
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@MediumTest
class HtmlToMooConverterTest {
    private val parentKodein by closestKodein(ApplicationProvider.getApplicationContext() as Context)
    private val kodein by Kodein.lazy {
        extend(parentKodein)
        bind<XMLReader>() with singleton {
            val parser = Parser()
            try {
                parser.setProperty(Parser.schemaProperty, schema)
            } catch (e: org.xml.sax.SAXNotRecognizedException) {
                // Should not happen.
                throw RuntimeException(e)
            } catch (e: org.xml.sax.SAXNotSupportedException) {
                throw RuntimeException(e)
            }
            parser
        }
        bind<UrlClickListener2>() with singleton {
            object : UrlClickListener2 {
                override fun accept(url: String) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

            }
        }
    }

    @Test
    fun asdf() {
        val baseUrl = URL("http://moo")
        val source = """
            Text and <p>a paragraph with <b>bold</b></p>
        """.trimIndent()

        val converter = HtmlToMooConverter(
                source,
                baseUrl,
                kodein
        )

        val moos = converter.convert()

        assertEquals(1, moos.size)
        assertEquals("""Text and 

a paragraph with bold

""",
                (moos.first() as Text).builder.toString())
    }

    @Test
    fun imagessfasx() {
        val baseUrl = URL("http://moo")
        val source = """
            <div><div>
            Some initial text
            <img src="http://foo" alt="meh"/>
            And even more text
            </div>
            </div>
        """.trimIndent()

        val converter = HtmlToMooConverter(
                source,
                baseUrl,
                kodein
        )

        val moos = converter.convert()

        assertEquals(3, moos.size)
        assertEquals(
                "Some initial text ",
                (moos[0] as Text).builder.toString()
        )
        assertEquals(
                Image(
                        width = null,
                        height = null,
                        src = URL("http://foo"),
                        label = "meh"
                ),
                moos[1]
        )
        assertEquals(
                "And even more text \n\n",
                (moos[2] as Text).builder.toString()
        )
    }
}
