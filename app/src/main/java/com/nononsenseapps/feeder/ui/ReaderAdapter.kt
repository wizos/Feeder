package com.nononsenseapps.feeder.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.text.toSpanned
import androidx.recyclerview.widget.RecyclerView
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.ui.text.*
import com.nononsenseapps.feeder.util.GlideUtils
import com.nononsenseapps.feeder.views.LinkedTextView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class ReaderAdapter : RecyclerView.Adapter<ReaderViewHolder>() {
    private var data: List<Moo> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReaderViewHolder =
            when (MooType.values()[viewType]) {
                MooType.TEXT -> ReaderTextViewHolder(parent)
                MooType.IMAGE -> ReaderImageViewHolder(parent)
                MooType.VIDEO -> ReaderVideoViewHolder(parent)
                MooType.TABLE -> TODO("not implemented")
            }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ReaderViewHolder, position: Int) {
        when (holder) {
            is ReaderTextViewHolder -> holder.setText(data[position] as Text)
            is ReaderImageViewHolder -> holder.setImage(data[position] as Image)
            is ReaderVideoViewHolder -> holder.setVideo(data[position] as VideoMoo)
        }
    }


    override fun getItemViewType(position: Int): Int = getItemViewEnumType(position).ordinal

    private fun getItemViewEnumType(position: Int): MooType = when (data[position]) {
        is Image -> MooType.IMAGE
        is VideoMoo -> MooType.VIDEO
        is Table -> MooType.TABLE
        is Text -> MooType.TEXT
    }

    fun setData(data: List<Moo>) {
        this.data = data
        notifyDataSetChanged()
    }
}

enum class MooType {
    TEXT,
    IMAGE,
    VIDEO,
    TABLE
}

sealed class ReaderViewHolder(parent: ViewGroup, @LayoutRes layout: Int) :
        RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(layout, parent, false))

class ReaderTextViewHolder(parent: ViewGroup) : ReaderViewHolder(parent, R.layout.reader_text_moo) {
    val view = itemView as LinkedTextView

    fun setText(text: Text) {
        view.text = text.builder.toSpanned()
    }
}

class ReaderImageViewHolder(parent: ViewGroup) : ReaderViewHolder(parent, R.layout.reader_image_moo) {
    val imageView = itemView.findViewById<ImageView>(R.id.image)
    val labelView = itemView.findViewById<TextView>(R.id.label)

    fun setImage(image: Image) {
        // TODO errors
        GlideUtils.glide(
                imageView.context,
                image.src.toString(),
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

        labelView.text = image.label
        labelView.visibility = if (image.label?.isNotBlank() == true) View.VISIBLE else View.GONE
    }
}

class ReaderVideoViewHolder(parent: ViewGroup) : ReaderViewHolder(parent, R.layout.reader_video_moo) {
    val player = itemView as YouTubePlayerView


    fun setVideo(video: VideoMoo) {
        // TODO add lifecycle observer
        player.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo(videoId = video.video.videoId, startSeconds = 0.0f)
            }
        })

        //webView.loadUrl(video.video.link)
        Log.d("JONAS", "Setting ${video.video}")
    }
}
