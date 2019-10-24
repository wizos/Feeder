package com.nononsenseapps.feeder.ui.text

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.junit.Test
import org.junit.runner.RunWith
import org.kodein.di.Kodein
import org.kodein.di.android.closestKodein
import java.net.URL
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@MediumTest
class HtmlToMooConverterTest {
    private val parentKodein by closestKodein(ApplicationProvider.getApplicationContext() as Context)
    private val kodein by Kodein.lazy {
        extend(parentKodein)
    }
    private val urlClickListener = object : UrlClickListener2 {
        override fun accept(url: String) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
                urlClickListener,
                kodein
        )

        val moos = converter.convert()

        assertEquals(1, moos.size)
        assertEquals("""Text and 

a paragraph with bold""",
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
            </div>""".trimIndent()

        val converter = HtmlToMooConverter(
                source,
                baseUrl,
                urlClickListener,
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
                "And even more text ",
                (moos[2] as Text).builder.toString()
        )
    }
}
