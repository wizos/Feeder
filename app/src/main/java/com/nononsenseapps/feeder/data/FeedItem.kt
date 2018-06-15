package com.nononsenseapps.feeder.data

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import org.joda.time.DateTime

const val FEED_ITEMS_TABLE_NAME = "FeedItem"

@Entity(tableName = FEED_ITEMS_TABLE_NAME,
        foreignKeys = [ForeignKey(
                entity = Feed::class,
                parentColumns = ["id"],
                childColumns = ["feed"],
                onDelete = ForeignKey.CASCADE
        )],
        indices = [Index(
                value = ["guid", "feed"],
                unique = true
        ), Index(
                value = ["feed"]
        )])
data class FeedItem(
        @PrimaryKey(autoGenerate = true)
        val id: Long = 0,
        val guid: String = "",
        val title: String = "",
        val description: String = "",
        @ColumnInfo(name = "plain_title")
        val plainTitle: String = "",
        @ColumnInfo(name = "plain_snippet")
        val plainSnippet: String = "",
        @ColumnInfo(name = "image_url")
        val imageUrl: String? = null,
        val link: String? = null,
        val enclosure: String? = null,
        val author: String? = null,
        @ColumnInfo(name = "pub_date")
        val pubDate: DateTime? = null,
        val unread: Boolean = true,
        val notified: Boolean = false,
        val feed: Long
)
