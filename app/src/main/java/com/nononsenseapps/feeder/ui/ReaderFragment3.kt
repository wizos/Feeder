package com.nononsenseapps.feeder.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.ShareActionProvider
import androidx.core.text.toSpanned
import androidx.core.view.MenuItemCompat
import androidx.navigation.fragment.findNavController
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.base.CoroutineScopedKodeinAwareFragment
import com.nononsenseapps.feeder.db.room.FeedItemDao
import com.nononsenseapps.feeder.db.room.FeedItemWithFeed
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.FeedItemViewModel
import com.nononsenseapps.feeder.model.cancelNotification
import com.nononsenseapps.feeder.ui.text.*
import com.nononsenseapps.feeder.util.GlideUtils
import com.nononsenseapps.feeder.util.TabletUtils
import com.nononsenseapps.feeder.util.bundle
import com.nononsenseapps.feeder.util.openLinkInBrowser
import com.nononsenseapps.feeder.views.LinkedTextView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.joda.time.format.DateTimeFormat
import org.kodein.di.generic.instance
import java.util.*

class ReaderFragment3 : CoroutineScopedKodeinAwareFragment() {
    private val dateTimeFormat = DateTimeFormat.forStyle("FM").withLocale(Locale.getDefault())

    private var _id: Long = ID_UNSET
    // All content contained in RssItem
    private var rssItem: FeedItemWithFeed? = null

    private val feedItemDao: FeedItemDao by instance()
    private val viewModel: FeedItemViewModel by instance(arg = this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { arguments ->
            _id = arguments.getLong(ARG_ID, ID_UNSET)
        }

        if (_id > ID_UNSET) {
            val itemId = _id
            val appContext = context?.applicationContext
            appContext?.let {
                launch(Dispatchers.Default) {
                    feedItemDao.markAsReadAndNotified(itemId)
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
            R.layout.fragment_reader3
        }
        val rootView = inflater.inflate(theLayout, container, false)
        val constraintLayout = rootView.findViewById<LinearLayout>(R.id.constraint_layout)

        val urlClickListener = object : UrlClickListener2 {
            override fun accept(url: String) {
                rssItem?.feedId?.let { feedId ->
                    findNavController().navigate(R.id.action_readerFragment_to_feedFragment, bundle {
                        putLong(ARG_FEED_ID, feedId)
                    })
                }
            }
        }

        viewModel.getLiveMoo(_id, urlClickListener).observe(this, androidx.lifecycle.Observer {
            // Update state of notification toggle
            activity?.invalidateOptionsMenu()

            // TODO updates?

            // TODO gets two updates
            constraintLayout.removeAllViews()

//            val constraints = ConstraintSet().also { it.clone(constraintLayout) }

            val views = getMooViews(it, constraintLayout)

            views.forEach {
                constraintLayout.addView(it)
            }
//
//            var previous: View? = null
//            views.forEachIndexed { index, v ->
//                constraints.connect(v.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
//                constraints.connect(v.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
//
//                if (index == 0) {
//                    constraints.connect(v.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
//                } else {
//                    previous?.let { previous ->
//                        constraints.connect(v.id, ConstraintSet.TOP, previous.id, ConstraintSet.BOTTOM)
//                    }
//                }
//
//                if(index == views.lastIndex) {
//                    constraints.connect(v.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
//                }
//
//                previous = v
//            }

//            constraints.createVerticalChain(
//                    ConstraintSet.PARENT_ID,
//                    ConstraintSet.TOP,
//                    ConstraintSet.PARENT_ID,
//                    ConstraintSet.BOTTOM,
//                    views.map { it.id }.toIntArray(),
//                    null,
//                    ConstraintSet.CHAIN_PACKED
//            )

//            constraints.applyTo(constraintLayout)
        })

        return rootView
    }

    private fun getMooViews(moos: List<Moo>, parent: ViewGroup): List<View> {
        return moos.mapIndexed { index, moo ->
            Log.d("JONAS", "Moo: $moo")
            when (moo) {
                is Image -> moo.toView(parent)
                is VideoMoo -> moo.toView(parent)
                is Table -> moo.toView(parent)
                is Text -> moo.toView(parent)
            }.also {
                it.id = 100_000_000 + index
            }
        }
    }

    private fun Image.toView(parent: ViewGroup): View {
        val view = LayoutInflater.from(context).inflate(R.layout.reader_image_moo, parent, false)
        val imageView = view.findViewById<ImageView>(R.id.image)
        val labelView = view.findViewById<TextView>(R.id.label)

        GlideUtils.glide(
                imageView.context,
                src.toString(),
                true // todo Prefs
        ).fitCenter()
                .error(
                        R.drawable.placeholder_image_list_night_64dp
                        // TODO
                        /*when (prefs.isNightMode) {
                            true -> R.drawable.placeholder_image_list_night_64dp
                            false -> R.drawable.placeholder_image_list_day_64dp
                        }*/
                )
                .into(imageView)

        labelView.text = label
        labelView.visibility = if (label?.isNotBlank() == true) View.VISIBLE else View.GONE

        return view
    }

    private fun VideoMoo.toView(parent: ViewGroup): View {
        val player = LayoutInflater.from(context).inflate(R.layout.reader_video_moo, parent, false) as YouTubePlayerView

        // TODO add lifecycle observer
        player.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo(videoId = video.videoId, startSeconds = 0.0f)
            }
        })

        return player
    }

    private fun Table.toView(parent: ViewGroup): View {
        TODO()
    }

    private fun Text.toView(parent: ViewGroup): View {
        val view = LayoutInflater.from(context).inflate(R.layout.reader_text_moo, parent, false) as LinkedTextView

        view.text = builder.toSpanned()

        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        rssItem?.storeInBundle(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.reader, menu)

        // Locate MenuItem with ShareActionProvider
        val shareItem = menu.findItem(R.id.action_share)

        // Fetch and store ShareActionProvider
        val shareActionProvider = MenuItemCompat.getActionProvider(shareItem) as ShareActionProvider

        // Set intent
        rssItem?.let { rssItem ->

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, rssItem.link)
            shareActionProvider.setShareIntent(shareIntent)

            // Show/Hide enclosure
            menu.findItem(R.id.action_open_enclosure).isVisible = rssItem.enclosureLink != null
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
                // Open in web view
                rssItem?.let { rssItem ->
                    rssItem.link?.let { link ->
                        findNavController().navigate(
                                R.id.action_readerFragment_to_readerWebViewFragment,
                                bundle {
                                    putString(ARG_URL, link)
                                    putString(ARG_ENCLOSURE, rssItem.enclosureLink)
                                }
                        )
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
                launch(Dispatchers.Default) {
                    feedItemDao.markAsRead(_id, unread = true)
                }
                true
            }
            else -> super.onOptionsItemSelected(menuItem)
        }
    }
}
