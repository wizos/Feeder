package com.nononsenseapps.feeder.ui

import android.content.Context
import android.os.Build.VERSION_CODES.JELLY_BEAN_MR2
import android.os.Build.VERSION_CODES.KITKAT
import androidx.test.InstrumentationRegistry
import androidx.test.InstrumentationRegistry.getInstrumentation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.util.PrefUtils
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class FeedOverflowMenuTests {
    @Rule
    @JvmField
    var activityRule: ActivityTestRule<FeedActivity> = ActivityTestRule(FeedActivity::class.java)

    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())!!

    private val targetContext: Context
        get() = getInstrumentation().targetContext

    @Before
    fun keepNavDrawerClosed() {
        PrefUtils.markWelcomeDone(targetContext)
    }

    @Test
    fun clickingAddFeedOpensDialogWithSearchBox() {
        openActionBarOverflowOrOptionsMenu(targetContext)
        onView(withText(R.string.add_feed)).perform(click())

        onView(withId(R.id.search_view)).check(matches(isDisplayed()))
    }

    @Test
    fun clickingSettingsOpensSettings() {
        openActionBarOverflowOrOptionsMenu(targetContext)
        onView(withText(R.string.action_settings)).perform(click())

        onView(withText(R.string.theme)).check(matches(isDisplayed()))
    }

    @Test
    fun clickingExportOpensFilePicker() {
        openActionBarOverflowOrOptionsMenu(targetContext)
        onView(withText(R.string.export_feeds_to_opml)).perform(click())

        assertTrue("File picker should be visible",
                device.findObject(UiSelector().text(DEFAULT_OPML_FILENAME)).exists())
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    fun clickingImportOpensFilePickerOnKitKat() {
        openActionBarOverflowOrOptionsMenu(targetContext)
        onView(withText(R.string.import_feeds_from_opml)).perform(click())

        // By default, the Android filepicker shows file in the Recent view
        assertTrue("File picker should be visible",
                device.findObject(UiSelector().text("Recent")).exists())
    }

    @Test
    @SdkSuppress(maxSdkVersion = JELLY_BEAN_MR2)
    fun clickingImportOpensFilePickerOnJellyBean() {
        openActionBarOverflowOrOptionsMenu(targetContext)
        onView(withText(R.string.import_feeds_from_opml)).perform(click())

        assertTrue("File picker should be visible",
                device.findObject(UiSelector().text("/storage/sdcard")).exists())
    }
}
