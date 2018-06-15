package com.nononsenseapps.feeder.data

import android.arch.persistence.room.ColumnInfo
import org.joda.time.DateTime

data class FeedItemForList(
        val id: Long,
        @ColumnInfo(name = "plain_title")
        val plainTitle: String,
        @ColumnInfo(name = "plain_snippet")
        val plainSnippet: String,
        @ColumnInfo(name = "image_url")
        val imageUrl: String?,
        val link: String?,
        val enclosure: String?,
        val author: String? = null,
        @ColumnInfo(name = "pub_date")
        val pubDate: DateTime?,
        val unread: Boolean
)
