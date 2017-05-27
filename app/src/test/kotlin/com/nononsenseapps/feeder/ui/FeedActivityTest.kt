package com.nononsenseapps.feeder.ui

import com.nononsenseapps.feeder.BuildConfig
import com.nononsenseapps.feeder.R
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric.setupActivity
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.fail

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class FeedActivityTest {



    @Test
    fun clickingNightModeShouldToggleBackground() {
        val activity = setupActivity(FeedActivity::class.java)
        activity.findViewById(R.id.nightcheck).performClick()
        fail("I did run it!")
    }
}
