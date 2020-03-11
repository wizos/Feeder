package com.nononsenseapps.feeder.db.room

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MigrationFrom11To12 {
    private val dbName = "testDb"

    @Rule
    @JvmField
    val testHelper: MigrationTestHelper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory())

    @Test
    fun migrate10to11() {
        var db = testHelper.createDatabase(dbName, 11)

        db.use {
            db.execSQL("""
            INSERT INTO feeds(id, title, url, custom_title, tag, notify, last_sync, response_hash)
            VALUES(1, 'feed', 'http://url', '', '', 0, 0, 666)
        """.trimIndent())
        }

        db = testHelper.runMigrationsAndValidate(dbName, 12, true, MIGRATION_11_12)

        db.query("""
            SELECT reading_time FROM feeds
        """.trimIndent())!!.use {
            assert(it.count == 1)
            assert(it.moveToFirst())
            assertEquals(0L, it.getLong(0))
        }
    }
}
