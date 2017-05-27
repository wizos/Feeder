package com.nononsenseapps.feeder.ui

import com.nononsenseapps.feeder.BuildConfig
import com.nononsenseapps.feeder.util.PrefUtils
import kotlinx.android.synthetic.main.include_activity_feed_main.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric.setupActivity
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class FeedActivityTest {

    @Test
    fun clickingNightModeShouldTogglePrefs() {
        val activity = setupActivity(FeedActivity::class.java)
        assertFalse(PrefUtils.isNightMode(activity), "Should start with false")
        activity.nightcheck.performClick()
        assertTrue(PrefUtils.isNightMode(activity), "Should toggle value")
        activity.nightcheck.performClick()
        assertFalse(PrefUtils.isNightMode(activity), "Should toggle value again")
    }
}
