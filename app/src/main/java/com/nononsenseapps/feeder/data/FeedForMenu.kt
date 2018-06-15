package com.nononsenseapps.feeder.data

import android.arch.persistence.room.ColumnInfo

data class FeedForMenu(
        val id: Long,
        val title: String,
        @ColumnInfo(name = "custom_title")
        val customTitle: String,
        val url: String,
        val tag: String,
        @ColumnInfo(name = "unread_count")
        val unreadCount: Int
)
