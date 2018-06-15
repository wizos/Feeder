package com.nononsenseapps.feeder.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.nononsenseapps.feeder.coroutines.BackgroundUI
import com.nononsenseapps.feeder.data.Feed
import com.nononsenseapps.feeder.data.FeederDatabase
import kotlinx.coroutines.experimental.launch

class FeedViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = FeederDatabase.getInstance(application).feedDao()

    val feedsForMenu = dao.getFeedsForMenu()

    fun feed(id: Long) = dao.getFeedById(id)

    fun insert(feed: Feed) = launch(BackgroundUI) {
        dao.insert(feed)
    }

    fun delete(feed: Feed) = launch(BackgroundUI) {
        dao.delete(feed)
    }
}
