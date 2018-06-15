package com.nononsenseapps.feeder.data

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import com.nononsenseapps.feeder.db.DATABASE_NAME
import com.nononsenseapps.feeder.db.DATABASE_VERSION
import com.nononsenseapps.feeder.util.also


@Database(entities = [Feed::class, FeedItem::class], version = DATABASE_VERSION)
@TypeConverters(value = [DateTimeConverter::class])
abstract class FeederDatabase : RoomDatabase() {
    abstract fun feedDao(): FeedDao
    abstract fun feedItemDao(): FeedItemDao

    companion object {

        @Volatile
        private var INSTANCE: FeederDatabase? = null

        @Suppress("unused")
        fun getInstance(context: Context): FeederDatabase =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
                }

        private fun buildDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext,
                        FeederDatabase::class.java, DATABASE_NAME)
                        .build()
    }
}
