package com.nononsenseapps.feeder.ui

import android.support.v4.view.GravityCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.FeedSQL
import com.nononsenseapps.feeder.util.GlideUtils
import com.nononsenseapps.feeder.util.PrefUtils

class FeedHolder(private val activity: BaseActivity, v: View) : RecyclerView.ViewHolder(v), View.OnClickListener, ViewTreeObserver.OnPreDrawListener {
    val unreadCount: TextView = v.findViewById(R.id.feed_unreadcount)
    val title: TextView = v.findViewById(R.id.feed_name)
    val icon: ImageView = v.findViewById(R.id.feed_icon)
    var item: FeedSQL? = null

    init {
        v.setOnClickListener(this)
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    override fun onClick(v: View) {
        if (activity.drawerLayout != null) {
            activity.drawerLayout!!.closeDrawer(GravityCompat.START)
        }

        activity.onNavigationDrawerItemSelected(item!!.id, item!!.displayTitle, item!!.url.toString(), item!!.tag)
    }

    override fun onPreDraw(): Boolean {
        try {
            GlideUtils.glide(activity, item!!.icon.toString(),
                    PrefUtils.shouldLoadImages(activity))
                    .centerCrop()
                    .error(R.drawable.ic_signal_wifi_off_white_24dp)
                    .into(icon)
        } catch (e: Throwable) {
            Log.d("FeedHolder", e.localizedMessage)
        }

        // Remove as listener
        itemView.viewTreeObserver.removeOnPreDrawListener(this)
        return true
    }
}
