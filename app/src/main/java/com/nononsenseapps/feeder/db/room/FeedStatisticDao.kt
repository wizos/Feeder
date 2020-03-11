package com.nononsenseapps.feeder.db.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.Duration

@Dao
interface FeedStatisticDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStat(feedStatistic: FeedStatistic): Long

    @Update
    suspend fun updateStat(feedStatistic: FeedStatistic)

    @Query("UPDATE feed_statistics SET daily_reading_time = daily_reading_time + :time WHERE id IS :id")
    suspend fun addReadingTimeToFeed(id: Long, time: Duration)

    @Query("""
        SELECT *
        FROM feed_statistics
        """)
    fun getStatistics(): Flow<FeedStatistic>
}

suspend fun FeedStatisticDao.upsertStat(feedStatistic: FeedStatistic): Long =
        when (feedStatistic.id > ID_UNSET) {
            true -> {
                updateStat(feedStatistic)
                feedStatistic.id
            }
            false -> {
                insertStat(feedStatistic)
            }
        }
