package com.nononsenseapps.feeder.ui

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.nononsenseapps.feeder.R

class StarsHolder(private val activity: BaseActivity, v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
    val title: TextView = v.findViewById(R.id.feed_name)
    val unreadCount: TextView = v.findViewById(R.id.feed_unreadcount)

    init {
        title.setText("Stars TODO")
        v.setOnClickListener(this)
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    override fun onClick(v: View) {
        activity.drawerLayout?.closeDrawers()
        activity.onNavigationDrawerItemSelected(STARRED_ITEMS_ID, null, null, null)
    }
}
