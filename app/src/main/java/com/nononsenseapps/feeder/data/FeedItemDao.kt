package com.nononsenseapps.feeder.data

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface FeedItemDao {
    @Query("SELECT * FROM $FEED_ITEMS_TABLE_NAME WHERE id = :id")
    fun getFeedItemById(id: Long): LiveData<FeedItem>

    @Query("""
        SELECT id, plain_title, plain_snippet, image_url, link, enclosure, author, pub_date, unread
        FROM $FEED_ITEMS_TABLE_NAME
        ORDER BY pub_date DESC
        """)
    fun getFeedItemsForList(): LiveData<List<FeedItemForList>>

    @Query("""
        SELECT id, plain_title, plain_snippet, image_url, link, enclosure, author, pub_date, unread
        FROM $FEED_ITEMS_TABLE_NAME WHERE unread = 1
        ORDER BY pub_date DESC
        """)
    fun getUnreadFeedItemsForList(): LiveData<List<FeedItemForList>>

    @Query("""
        SELECT id, plain_title, plain_snippet, image_url, link, enclosure, author, pub_date, unread
        FROM $FEED_ITEMS_TABLE_NAME WHERE feed = :feedId
        ORDER BY pub_date DESC
        """)
    fun getFeedItemsForList(feedId: Long): LiveData<List<FeedItemForList>>

    @Query("""
        SELECT id, plain_title, plain_snippet, image_url, link, enclosure, author, pub_date, unread
        FROM $FEED_ITEMS_TABLE_NAME WHERE feed = :feedId AND unread = 1
        ORDER BY pub_date DESC
        """)
    fun getUnreadFeedItemsForList(feedId: Long): LiveData<List<FeedItemForList>>

    @Query("""
        SELECT $FEED_ITEMS_TABLE_NAME.id, plain_title, plain_snippet, $FEED_ITEMS_TABLE_NAME.image_url, link, enclosure, author, pub_date, unread
        FROM $FEED_ITEMS_TABLE_NAME LEFT JOIN $FEEDS_TABLE_NAME ON $FEED_ITEMS_TABLE_NAME.feed = $FEEDS_TABLE_NAME.id
        WHERE tag = :tag
        ORDER BY pub_date DESC
        """)
    fun getFeedItemsForList(tag: String): LiveData<List<FeedItemForList>>

    @Query("""
        SELECT $FEED_ITEMS_TABLE_NAME.id, plain_title, plain_snippet, $FEED_ITEMS_TABLE_NAME.image_url, link, enclosure, author, pub_date, unread
        FROM $FEED_ITEMS_TABLE_NAME LEFT JOIN $FEEDS_TABLE_NAME ON $FEED_ITEMS_TABLE_NAME.feed = $FEEDS_TABLE_NAME.id
        WHERE $FEEDS_TABLE_NAME.tag = :tag AND $FEED_ITEMS_TABLE_NAME.unread = 1
        ORDER BY pub_date DESC
        """)
    fun getUnreadFeedItemsForList(tag: String): LiveData<List<FeedItemForList>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(feed: FeedItem): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(feeds: List<FeedItem>): List<Long>

    //@Query("DELETE FROM $FEED_ITEMS_TABLE_NAME WHERE id = :id")
    //fun delete(id: Long)

    @Delete
    fun delete(feedItem: FeedItem)

    @Delete
    fun delete(feedItems: List<FeedItem>)
}
