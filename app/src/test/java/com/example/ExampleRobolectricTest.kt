package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ChannelConfigStore
import com.example.data.model.ChannelSettings
import com.example.data.repository.ChannelRepository
import com.example.ui.components.AlAnsariBottomBar
import com.example.ui.navigation.AlAnsariRoutes
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.AlAnsariTheme
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
    fun `home screen renders all seven sections and routes on click`() {
        val navigatedRoutes = mutableListOf<String>()

        composeTestRule.setContent {
            AlAnsariTheme {
                HomeScreen(onNavigate = { navigatedRoutes.add(it) })
            }
        }
        composeTestRule.waitForIdle()

        val expectedTags = listOf(
            "home_card_live",
            "home_card_coverage",
            "home_card_videos",
            "home_card_news",
            "home_card_events",
            "home_card_social",
            "home_card_services",
        )

        expectedTags.forEach { tag ->
            composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
            composeTestRule.onNodeWithTag(tag).performClick()
        }

        assertEquals(
            listOf(
                AlAnsariRoutes.LIVE,
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
    fun `repository prioritizes configured official live stream url`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ChannelRepository(ChannelConfigStore(context))
        val settings = ChannelSettings(
            primaryLiveUrl = "https://cdn.example.com/alansari/live.m3u8",
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
        assertEquals("https://cdn.example.com/alansari/live.m3u8", sources.first().streamUrl)
        assertEquals("@alansarie1", ChannelConfigStore.DEFAULT_YOUTUBE_HANDLE)
        assertEquals("UCbZnqtO8B64mMhTzTXIxqUA", ChannelConfigStore.DEFAULT_YOUTUBE_CHANNEL_ID)
    }
}
