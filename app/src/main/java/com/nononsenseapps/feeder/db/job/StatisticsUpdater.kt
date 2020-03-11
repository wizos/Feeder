package com.nononsenseapps.feeder.db.job

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.nononsenseapps.feeder.db.room.FeedStatisticDao
import kotlinx.coroutines.flow.collect
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import org.threeten.bp.Duration
import java.util.concurrent.TimeUnit

const val UNIQUE_WORK_STATS_NAME = "cron_statistics"

fun configurePeriodicStatisticsUpdate(context: Context) {
    val kodein by closestKodein(context)
    val workManager: WorkManager by kodein.instance()

    val request = PeriodicWorkRequestBuilder<StatisticsUpdater>(1, TimeUnit.DAYS)
            .addTag("statistics")
            .setInitialDelay(1, TimeUnit.DAYS)
            .build()

    workManager.enqueueUniquePeriodicWork(
            UNIQUE_WORK_STATS_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
    )
}

class StatisticsUpdater(
        val context: Context,
        workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KodeinAware {
    override val kodein: Kodein by closestKodein(context)
    val dao: FeedStatisticDao by instance()

    override suspend fun doWork(): Result {
        dao.getStatistics().collect { stat ->
            // Update daily statistics
            stat.dailyReadingTimeRingBuffer.addAndBumpIndex(stat.dailyReadingTime)
            stat.dailyReadingTimeSMM = stat.dailyReadingTimeRingBuffer.getMedian()
            stat.dailyReadingTime = Duration.ZERO

            dao.updateStat(stat)
        }

        return Result.success()
    }
}
