package com.nononsenseapps.feeder.data

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

const val FEEDS_TABLE_NAME = "Feed"

@Entity(tableName = FEEDS_TABLE_NAME,
        indices = [(Index(value = ["url"], unique = true))])
data class Feed(
        @PrimaryKey(autoGenerate = true)
        val id: Long = 0,
        val title: String = "",
        @ColumnInfo(name = "custom_title")
        val customTitle: String = "",
        val url: String,
        val tag: String = "",
        val notify: Boolean = false,
        @ColumnInfo(name = "image_url")
        val imageUrl: String? = null
)
