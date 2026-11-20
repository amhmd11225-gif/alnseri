package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.haqeebat.PrayerTimesCalculator
import com.example.data.local.ChannelConfigStore
import com.example.data.model.ChannelSettings
import com.example.data.model.StreamProtocol
import com.example.data.model.VideoProgram
import com.example.data.model.detectStreamProtocol
import com.example.data.model.protocolBadgeLabel
import com.example.data.repository.ChannelRepository
import com.example.ui.components.AlAnsariBottomBar
import com.example.ui.navigation.AlAnsariRoutes
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.screens.extractYouTubeVideoId
import com.example.ui.theme.AlAnsariTheme
import com.example.ui.viewmodel.AlAnsariUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp-xxhdpi")
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("قناة الأنصاري", appName)
    }

    @Test
    fun `home screen renders all seven sections plus booking cta and routes on click`() {
        val navigatedRoutes = mutableListOf<String>()

        composeTestRule.setContent {
            AlAnsariTheme {
                HomeScreen(onNavigate = { navigatedRoutes.add(it) })
            }
        }
        composeTestRule.waitForIdle()

        val expectedTags = listOf(
            "home_card_live",
            "home_card_booking",
            "home_card_coverage",
            "home_card_videos",
            "home_card_news",
            "home_card_events",
            "home_card_social",
            "home_card_services",
        )

        expectedTags.forEach { tag ->
            composeTestRule.onNodeWithTag(tag).performScrollTo()
            composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
            composeTestRule.onNodeWithTag(tag).performClick()
        }

        assertEquals(
            listOf(
                AlAnsariRoutes.LIVE,
                AlAnsariRoutes.BOOKING_REQUEST,
                AlAnsariRoutes.FIELD_COVERAGE,
                AlAnsariRoutes.VIDEOS,
                AlAnsariRoutes.NEWS,
                AlAnsariRoutes.EVENTS,
                AlAnsariRoutes.SOCIAL,
                AlAnsariRoutes.MORE,
            ),
            navigatedRoutes,
        )
    }

    @Test
    fun `bottom navigation bar displays all four tabs`() {
        var selectedRoute = AlAnsariRoutes.HOME
        composeTestRule.setContent {
            AlAnsariTheme {
                AlAnsariBottomBar(
                    currentRoute = selectedRoute,
                    onNavigate = { selectedRoute = it },
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("bottom_nav_home").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_nav_videos").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_nav_live").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_nav_more").assertIsDisplayed()

        composeTestRule.onNodeWithTag("bottom_nav_live").performClick()
        assertEquals(AlAnsariRoutes.LIVE, selectedRoute)
    }

    @Test
    fun `stream protocol detector classifies hls rtmp and srt accurately`() {
        assertEquals(StreamProtocol.SRT, detectStreamProtocol("srt://live.alansari.iq:9000?streamid=live"))
        assertEquals(StreamProtocol.RTMP, detectStreamProtocol("rtmp://live.alansari.iq/app/stream"))
        assertEquals(StreamProtocol.RTMP, detectStreamProtocol("rtmps://live.alansari.iq/app/stream"))
        assertEquals(StreamProtocol.HLS, detectStreamProtocol("https://cdn.example.com/alansari/live.m3u8"))
        assertEquals("SRT مباشر", protocolBadgeLabel(StreamProtocol.SRT))
        assertEquals("RTMP مباشر", protocolBadgeLabel(StreamProtocol.RTMP))
    }

    @Test
    fun `youtube video id extractor parses watch short embed and shorts urls`() {
        assertEquals("dQw4w9WgXcQ", extractYouTubeVideoId("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", extractYouTubeVideoId("https://youtu.be/dQw4w9WgXcQ?si=abc"))
        assertEquals("dQw4w9WgXcQ", extractYouTubeVideoId("https://www.youtube.com/live/dQw4w9WgXcQ"))
    }

    @Test
    fun `video player screen renders in-app youtube player for youtube channel videos`() {
        val sampleVideo = VideoProgram(
            id = "yt_abc12345678",
            title = "مباشر المحاضرة الدينية",
            category = "مجالس ومحاضرات",
            duration = "يوتيوب",
            publishedAt = "2026-11-19",
            description = "بث مباشر",
            videoUrl = "",
            youtubeVideoId = "abc12345678",
            youtubeWatchUrl = "https://www.youtube.com/watch?v=abc12345678",
        )
        composeTestRule.setContent {
            AlAnsariTheme {
                VideoPlayerScreen(
                    videoId = sampleVideo.id,
                    uiState = AlAnsariUiState(videos = listOf(sampleVideo)),
                    onSelectVideo = {},
                    onToggleBookmark = {},
                    onBackClick = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("youtube_inapp_player").assertIsDisplayed()
    }

    @Test
    fun `repository prioritizes configured rtmp or srt live stream url`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ChannelRepository(ChannelConfigStore(context))
        val settings = ChannelSettings(
            primaryLiveUrl = "srt://live.alansari.iq:9000?mode=caller",
            backupLiveUrl = ChannelConfigStore.OPEN_HLS_TEST_STREAM,
            cmsApiBaseUrl = "",
            lastSyncedTimestamp = "",
            facebookUrl = "",
            youtubeUrl = "",
            tiktokUrl = "",
            telegramUrl = "",
            websiteUrl = "",
            satelliteFrequency = "",
            youtubeChannelId = ChannelConfigStore.DEFAULT_YOUTUBE_CHANNEL_ID,
            youtubeHandle = ChannelConfigStore.DEFAULT_YOUTUBE_HANDLE,
        )
        val sources = repo.buildLiveSources(settings)
        assertEquals(2, sources.size)
        assertTrue(sources.first().isConfiguredOfficial)
        assertEquals("srt://live.alansari.iq:9000?mode=caller", sources.first().streamUrl)
        assertEquals("SRT مباشر", sources.first().qualityLabel)
        assertEquals("@alansarie1", ChannelConfigStore.DEFAULT_YOUTUBE_HANDLE)
        assertEquals("UCbZnqtO8B64mMhTzTXIxqUA", ChannelConfigStore.DEFAULT_YOUTUBE_CHANNEL_ID)
    }

    @Test
    fun `home screen renders all eight haqeebat almumin 3d tiles and routes on click`() {
        val navigatedRoutes = mutableListOf<String>()

        composeTestRule.setContent {
            AlAnsariTheme {
                HomeScreen(onNavigate = { navigatedRoutes.add(it) })
            }
        }
        composeTestRule.waitForIdle()

        val haqeebatTags = listOf(
            "home_card_quran",
            "home_card_duas",
            "home_card_ziyarat",
            "home_card_prayer_times",
            "home_card_qibla",
            "home_card_tasbeeh",
            "home_card_hijri_calendar",
            "home_card_istikhara",
        )

        haqeebatTags.forEach { tag ->
            composeTestRule.onNodeWithTag(tag).performScrollTo()
            composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
            composeTestRule.onNodeWithTag(tag).performClick()
        }

        assertEquals(
            listOf(
                AlAnsariRoutes.QURAN,
                AlAnsariRoutes.DUAS,
                AlAnsariRoutes.ZIYARAT,
                AlAnsariRoutes.PRAYER_TIMES,
                AlAnsariRoutes.QIBLA,
                AlAnsariRoutes.TASBEEH,
                AlAnsariRoutes.HIJRI_CALENDAR,
                AlAnsariRoutes.ISTIKHARA_FIQH,
            ),
            navigatedRoutes,
        )
    }

    @Test
    fun `prayer times and qibla calculator produce valid iraqi astronomical schedule`() {
        val najaf = PrayerTimesCalculator.cityById("najaf")
        val qiblaDeg = PrayerTimesCalculator.calculateQiblaBearing(najaf.lat, najaf.lon)
        assertTrue(qiblaDeg in 190f..215f)

        val schedule = PrayerTimesCalculator.calculateForDate(najaf)
        assertTrue(schedule.imsaakMinutes < schedule.fajrMinutes)
        assertTrue(schedule.fajrMinutes < schedule.sunriseMinutes)
        assertTrue(schedule.sunriseMinutes < schedule.dhuhrMinutes)
        assertTrue(schedule.dhuhrMinutes < schedule.asrMinutes)
        assertTrue(schedule.asrMinutes < schedule.sunsetMinutes)
        assertTrue(schedule.sunsetMinutes <= schedule.maghribMinutes)
        assertTrue(schedule.maghribMinutes < schedule.ishaMinutes)
    }
}
