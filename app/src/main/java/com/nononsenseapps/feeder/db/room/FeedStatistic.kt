package com.nononsenseapps.feeder.db.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nononsenseapps.feeder.db.COL_DAILY_READING_TIME
import com.nononsenseapps.feeder.db.COL_FEEDID
import com.nononsenseapps.feeder.db.COL_ID
import org.threeten.bp.Duration

@Entity(tableName = "feed_statistics",
        indices = [Index(value = [COL_FEEDID])],
        foreignKeys = [ForeignKey(entity = Feed::class,
                parentColumns = [COL_ID],
                childColumns = [COL_FEEDID],
                onDelete = ForeignKey.CASCADE)])
data class FeedStatistic @Ignore constructor(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = COL_ID) var id: Long = ID_UNSET,
        @ColumnInfo(name = COL_FEEDID) var feedId: Long = ID_UNSET,
        @ColumnInfo(name = COL_DAILY_READING_TIME, typeAffinity = ColumnInfo.INTEGER, defaultValue = "0") var dailyReadingTime: Duration = Duration.ZERO,
        @ColumnInfo(name = "drt_ring_buffer", typeAffinity = ColumnInfo.TEXT, defaultValue = "'0,0,0,0,0,0,0,0'") var dailyReadingTimeRingBuffer: DurationRingBuffer = DurationRingBuffer.ofSize(STATISTICS_BUFFER_DAYS),
        // Daily reading time Simple moving median
        @ColumnInfo(name = "drt_smm", typeAffinity = ColumnInfo.INTEGER, defaultValue = "0") var dailyReadingTimeSMM: Duration = Duration.ZERO
) {
    constructor() : this(id = ID_UNSET)
}

const val STATISTICS_BUFFER_DAYS = 7

data class DurationRingBuffer(
        private var index: Int = 0,
        private val buffer: Array<Duration>
) {
    fun addAndBumpIndex(element: Duration) {
        buffer[index] = element
        index = (index + 1) % buffer.size
    }

    fun getMedian(): Duration {
        val sorted = buffer.sortedArray()
        return if (buffer.size % 2 == 0) {
            Duration.ofSeconds((sorted[buffer.size / 2 - 1].seconds + sorted[buffer.size / 2].seconds) / 2)
        } else {
            sorted[buffer.size / 2]
        }
    }

    override fun toString(): String {
        return buffer.map { it.seconds }.joinToString(separator = ",", prefix = "$index,")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DurationRingBuffer

        if (index != other.index) return false
        if (!buffer.contentEquals(other.buffer)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + buffer.contentHashCode()
        return result
    }

    companion object {
        fun ofSize(length: Int) =
                DurationRingBuffer(0, Array(length.coerceAtLeast(1)) { Duration.ZERO })
    }
}
