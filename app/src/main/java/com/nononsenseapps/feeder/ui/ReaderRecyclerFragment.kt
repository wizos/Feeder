package com.nononsenseapps.feeder.ui

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.ParcelableSpan
import android.text.Spannable
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.SubscriptSpan
import android.text.style.SuperscriptSpan
import android.text.style.TypefaceSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.map
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.base.KodeinAwareFragment
import com.nononsenseapps.feeder.db.room.FeedItemWithFeed
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.FeedItemViewModel
import com.nononsenseapps.feeder.model.SettingsViewModel
import com.nononsenseapps.feeder.model.cancelNotification
import com.nononsenseapps.feeder.ui.text.Big
import com.nononsenseapps.feeder.ui.text.BlackholeElement
import com.nononsenseapps.feeder.ui.text.BlockQuoteElement
import com.nononsenseapps.feeder.ui.text.Bold
import com.nononsenseapps.feeder.ui.text.Code
import com.nononsenseapps.feeder.ui.text.DisplayElement
import com.nononsenseapps.feeder.ui.text.Font
import com.nononsenseapps.feeder.ui.text.HeaderElement
import com.nononsenseapps.feeder.ui.text.Href
import com.nononsenseapps.feeder.ui.text.ImageElement
import com.nononsenseapps.feeder.ui.text.Italic
import com.nononsenseapps.feeder.ui.text.ListElement
import com.nononsenseapps.feeder.ui.text.Monospace
import com.nononsenseapps.feeder.ui.text.ParagraphTextElement
import com.nononsenseapps.feeder.ui.text.SensibleSpannableStringBuilder
import com.nononsenseapps.feeder.ui.text.Small
import com.nononsenseapps.feeder.ui.text.Sub
import com.nononsenseapps.feeder.ui.text.Super
import com.nononsenseapps.feeder.ui.text.TableElement
import com.nononsenseapps.feeder.ui.text.Underline
import com.nononsenseapps.feeder.ui.text.VideoElement
import com.nononsenseapps.feeder.util.PREF_VAL_OPEN_WITH_CUSTOM_TAB
import com.nononsenseapps.feeder.util.Prefs
import com.nononsenseapps.feeder.util.TabletUtils
import com.nononsenseapps.feeder.util.bundle
import com.nononsenseapps.feeder.util.openLinkInBrowser
import com.nononsenseapps.feeder.util.openLinkInCustomTab
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import org.kodein.di.generic.instance
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.*


@FlowPreview
class ReaderRecyclerFragment : KodeinAwareFragment() {
    private val dateTimeFormat =
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.SHORT)
                    .withLocale(Locale.getDefault())

    private var _id: Long = ID_UNSET

    // All content contained in RssItem
    private var rssItem: FeedItemWithFeed? = null
    private lateinit var titleTextView: TextView

    private val viewModel: FeedItemViewModel by instance(arg = this)
    // Important to get the activity bound view model here hence no arg specified
    private val settingsViewModel: SettingsViewModel by instance()
    private val prefs: Prefs by instance()

    init {
        lifecycleScope.launchWhenStarted {
            try {
                if (prefs.shouldPreloadCustomTab) {
                    val warmer = CustomTabsWarmer(context)
                    warmer.preLoad {
                        rssItem?.link?.let { Uri.parse(it) }
                    }
                }
            } catch (e: Exception) {
                // Don't let this crash
                Log.e("ReaderFragment", "Couldn't preload ${rssItem?.link}", e)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { arguments ->
            _id = arguments.getLong(ARG_ID, ID_UNSET)
        }

        if (_id > ID_UNSET) {
            val itemId = _id
            val appContext = context?.applicationContext
            appContext?.let {
                lifecycleScope.launchWhenResumed {
                    viewModel.markAsReadAndNotified(_id)
                    cancelNotification(it, itemId)
                }
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val theLayout = if (TabletUtils.isTablet(activity)) {
            R.layout.fragment_reader_tablet
        } else {
            R.layout.fragment_reader
        }
        val rootView = inflater.inflate(theLayout, container, false)

        titleTextView = rootView.findViewById(R.id.story_title)
        val recyclerView = rootView.findViewById<RecyclerView>(android.R.id.list)
        val authorTextView = rootView.findViewById<TextView>(R.id.story_author)
        val feedTitleTextView = rootView.findViewById<TextView>(R.id.story_feedtitle)

        lifecycleScope.launchWhenCreated {
            viewModel.getLiveItem(_id).observe(this@ReaderRecyclerFragment, androidx.lifecycle.Observer {
                rssItem = it

                rssItem?.let { rssItem ->
                    titleTextView.text = rssItem.plainTitle

                    rssItem.feedId?.let { feedId ->
                        feedTitleTextView.setOnClickListener {
                            findNavController().navigate(R.id.action_readerFragment_to_feedFragment, bundle {
                                putLong(ARG_FEED_ID, feedId)
                            })
                        }
                    }

                    feedTitleTextView.text = rssItem.feedDisplayTitle

                    rssItem.pubDate.let { pubDate ->
                        rssItem.author.let { author ->
                            when {
                                author == null && pubDate != null ->
                                    authorTextView.text = getString(R.string.on_date,
                                        pubDate.format(dateTimeFormat))
                                author != null && pubDate != null ->
                                    authorTextView.text = getString(R.string.by_author_on_date,
                                        // Must wrap author in unicode marks to ensure it formats
                                        // correctly in RTL
                                        unicodeWrap(author),
                                        pubDate.format(dateTimeFormat))
                                else -> authorTextView.visibility = View.GONE
                            }
                        }
                    }

                    // Update state of notification toggle
                    activity?.invalidateOptionsMenu()
                }
            })

            val adapter = FooItemAdapter(requireContext())

            viewModel.getLiveFooRecyclerThins(
                _id
            ).map { displayElements ->
                // Convert to UI items
                displayElements.map(::convertToListItem)
            }.observe(
                this@ReaderRecyclerFragment,
                androidx.lifecycle.Observer {items ->
                    // Set adapter items
                    adapter.setData(items)
                }
            )
        }
        return rootView
    }

    private fun convertToListItem(displayElement: DisplayElement): UiItem =
        when (displayElement) {
            is BlackholeElement -> TODO()
            is ImageElement -> TODO()
            is ParagraphTextElement -> TextItem(displayElement)
            is TableElement -> TODO()
            is ListElement -> TODO()
            is BlockQuoteElement -> TODO()
            is HeaderElement -> TODO()
            is VideoElement -> TODO()
        }

    override fun onSaveInstanceState(outState: Bundle) {
        rssItem?.storeInBundle(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.reader, menu)

        // Set intent
        rssItem?.let { rssItem ->
            // Show/Hide buttons
            menu.findItem(R.id.action_open_enclosure).isVisible = rssItem.enclosureLink != null
            menu.findItem(R.id.action_open_in_webview).isVisible = rssItem.link != null
            menu.findItem(R.id.action_open_in_browser).isVisible = rssItem.link != null
            // Add filename to tooltip
            if (rssItem.enclosureLink != null) {
                val filename = rssItem.enclosureFilename
                if (filename != null) {
                    menu.findItem(R.id.action_open_enclosure).title = filename
                }

            }
        }

        // Don't forget super call here
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_open_in_webview -> {
                // Open in web view or custom tab
                context?.let { context ->
                    rssItem?.let { rssItem ->
                        rssItem.link?.let { link ->
                            when (prefs.openLinksWith) {
                                PREF_VAL_OPEN_WITH_CUSTOM_TAB -> {
                                    openLinkInCustomTab(context, link, rssItem.id)
                                }
                                else -> {
                                    findNavController().navigate(
                                            R.id.action_readerFragment_to_readerWebViewFragment,
                                            bundle {
                                                putString(ARG_URL, link)
                                                putString(ARG_ENCLOSURE, rssItem.enclosureLink)
                                                putLong(ARG_ID, _id)
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
                true
            }
            R.id.action_open_in_browser -> {
                val link = rssItem?.link
                if (link != null) {
                    context?.let { context ->
                        openLinkInBrowser(context, link)
                    }
                }

                true
            }
            R.id.action_open_enclosure -> {
                val link = rssItem?.enclosureLink
                if (link != null) {
                    context?.let { context ->
                        openLinkInBrowser(context, link)
                    }
                }

                true
            }
            R.id.action_mark_as_unread -> {
                lifecycleScope.launch {
                    viewModel.markAsRead(_id, unread = true)
                }
                true
            }
            R.id.action_share -> {
                rssItem?.link?.let { link ->
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "text/plain"
                    shareIntent.putExtra(Intent.EXTRA_TEXT, link)

                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
                }
                true
            }
            else -> super.onOptionsItemSelected(menuItem)
        }
    }
}

enum class FooItemType {
    HEADER,
    TEXT,
    IMAGE,
    BLOCKQUOTE,
    TABLE,
    LIST,
    VIDEO
}

fun parseFooItemInt(i: Int): FooItemType =
    when (i) {
        FooItemType.HEADER.ordinal -> FooItemType.HEADER
        FooItemType.TEXT.ordinal -> FooItemType.TEXT
        FooItemType.IMAGE.ordinal -> FooItemType.IMAGE
        FooItemType.BLOCKQUOTE.ordinal -> FooItemType.BLOCKQUOTE
        FooItemType.TABLE.ordinal -> FooItemType.TABLE
        FooItemType.LIST.ordinal -> FooItemType.LIST
        FooItemType.VIDEO.ordinal -> FooItemType.VIDEO
        else -> TODO("Better error")
    }

sealed class UiItem {
    abstract val type: Int
}

class TextItem(element: ParagraphTextElement): UiItem() {
    val spanBuilder: SensibleSpannableStringBuilder

    init {
        spanBuilder = SensibleSpannableStringBuilder(
            text = element.getText()
        )

        element.spans.forEach { span ->
            val spanType: ParcelableSpan = when (span) {
                is Bold -> StyleSpan(Typeface.BOLD)
                is Italic -> StyleSpan(Typeface.ITALIC)
                is Underline -> UnderlineSpan()
                is Big -> RelativeSizeSpan(1.25f)
                is Small -> RelativeSizeSpan(0.8f)
                is Monospace -> TypefaceSpan("monospace")
                is Super -> SuperscriptSpan()
                is Sub -> SubscriptSpan()
                is Code -> TODO()
                is Font -> TODO()
                is Href -> TODO()
            }
            spanBuilder.setSpan(
                spanType,
                span.startIndex,
                span.endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override val type = FooItemType.TEXT.ordinal

}
