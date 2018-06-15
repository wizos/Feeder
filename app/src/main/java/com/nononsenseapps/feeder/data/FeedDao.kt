package com.nononsenseapps.feeder.data

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface FeedDao {
    @Query("SELECT * FROM $FEEDS_TABLE_NAME WHERE id = :id")
    fun getFeedById(id: Long): LiveData<Feed>

    @Query("""
        SELECT id, title, custom_title, url, tag, unread_count FROM $FEEDS_TABLE_NAME
        LEFT JOIN (SELECT COUNT(1) AS unread_count, feed
         FROM $FEED_ITEMS_TABLE_NAME
         WHERE unread IS 1
         GROUP BY feed)
       ON $FEEDS_TABLE_NAME.id = feed
    """)
    fun getFeedsForMenu(): LiveData<List<FeedForMenu>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(feed: Feed): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(feeds: List<Feed>): List<Long>

    /*@Query("DELETE FROM $FEEDS_TABLE_NAME WHERE id = :id")
    fun delete(id: Long)*/

    @Delete
    fun delete(feed: Feed)

    @Delete
    fun delete(feeds: List<Feed>)
}
