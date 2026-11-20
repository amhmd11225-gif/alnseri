package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.ChannelSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.channelDataStore: DataStore<Preferences> by preferencesDataStore(name = "al_ansari_channel_prefs")

class ChannelConfigStore(private val context: Context) {

    companion object {
        const val DEFAULT_YOUTUBE_CHANNEL_ID = "UCbZnqtO8B64mMhTzTXIxqUA"
        const val DEFAULT_YOUTUBE_HANDLE = "@alansarie1"

        const val OPEN_HLS_TEST_STREAM =
            "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8"
        const val OPEN_VOD_TEST_STREAM =
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"

        const val DEFAULT_FACEBOOK = "https://www.facebook.com/al.ansar.live"
        const val DEFAULT_YOUTUBE = "https://www.youtube.com/@alansarie1"
        const val DEFAULT_TIKTOK = "https://www.tiktok.com/@alansarie1"
        const val DEFAULT_TELEGRAM = "https://t.me/SNG1080"
        const val DEFAULT_INSTAGRAM = "https://www.instagram.com/alansarie1"
        const val DEFAULT_FREQUENCY = "وحدة البث الخارجي SNG1080 • يوتيوب @alansarie1"

        private val KEY_PRIMARY_LIVE_URL = stringPreferencesKey("primary_live_url")
        private val KEY_BACKUP_LIVE_URL = stringPreferencesKey("backup_live_url")
        private val KEY_CMS_API_BASE_URL = stringPreferencesKey("cms_api_base_url")
        private val KEY_LAST_SYNCED = stringPreferencesKey("last_synced")
        private val KEY_FACEBOOK_URL = stringPreferencesKey("facebook_url")
        private val KEY_YOUTUBE_URL = stringPreferencesKey("youtube_url")
        private val KEY_TIKTOK_URL = stringPreferencesKey("tiktok_url")
        private val KEY_TELEGRAM_URL = stringPreferencesKey("telegram_url")
        private val KEY_WEBSITE_URL = stringPreferencesKey("website_url")
        private val KEY_FREQUENCY = stringPreferencesKey("satellite_frequency")
        private val KEY_YOUTUBE_CHANNEL_ID = stringPreferencesKey("youtube_channel_id")
        private val KEY_YOUTUBE_HANDLE = stringPreferencesKey("youtube_handle")
        private val KEY_BOOKMARKED_VIDEOS = stringSetPreferencesKey("bookmarked_videos")
        private val KEY_BOOKMARKED_NEWS = stringSetPreferencesKey("bookmarked_news")
        private val KEY_CUSTOM_VIDEO_OVERRIDES = stringSetPreferencesKey("custom_video_urls")
        private val KEY_SAVED_REQUESTS = stringSetPreferencesKey("saved_coverage_requests")

        // Haqeebat Al-Mu'min local preferences
        private val KEY_TASBEEH_TOTAL = intPreferencesKey("haqeebat_tasbeeh_total")
        private val KEY_SELECTED_CITY_ID = stringPreferencesKey("haqeebat_selected_city_id")
        private val KEY_QURAN_BOOKMARK_SURAH = intPreferencesKey("haqeebat_quran_bookmark_surah")
        private val KEY_HIJRI_OFFSET = intPreferencesKey("haqeebat_hijri_offset")
        private val KEY_FAVORITE_PRAYERS = stringSetPreferencesKey("haqeebat_favorite_prayers")
    }

    val settingsFlow: Flow<ChannelSettings> = context.channelDataStore.data.map { prefs ->
        ChannelSettings(
            primaryLiveUrl = prefs[KEY_PRIMARY_LIVE_URL] ?: "",
            backupLiveUrl = prefs[KEY_BACKUP_LIVE_URL] ?: OPEN_HLS_TEST_STREAM,
            cmsApiBaseUrl = prefs[KEY_CMS_API_BASE_URL] ?: "",
            lastSyncedTimestamp = prefs[KEY_LAST_SYNCED] ?: "مرتبط بقناة يوتيوب @alansarie1",
            facebookUrl = prefs[KEY_FACEBOOK_URL] ?: DEFAULT_FACEBOOK,
            youtubeUrl = prefs[KEY_YOUTUBE_URL] ?: DEFAULT_YOUTUBE,
            tiktokUrl = prefs[KEY_TIKTOK_URL] ?: DEFAULT_TIKTOK,
            telegramUrl = prefs[KEY_TELEGRAM_URL] ?: DEFAULT_TELEGRAM,
            websiteUrl = prefs[KEY_WEBSITE_URL] ?: DEFAULT_INSTAGRAM,
            satelliteFrequency = prefs[KEY_FREQUENCY] ?: DEFAULT_FREQUENCY,
            youtubeChannelId = prefs[KEY_YOUTUBE_CHANNEL_ID] ?: DEFAULT_YOUTUBE_CHANNEL_ID,
            youtubeHandle = prefs[KEY_YOUTUBE_HANDLE] ?: DEFAULT_YOUTUBE_HANDLE,
        )
    }

    val bookmarkedVideosFlow: Flow<Set<String>> = context.channelDataStore.data.map { prefs ->
        prefs[KEY_BOOKMARKED_VIDEOS] ?: emptySet()
    }

    val bookmarkedNewsFlow: Flow<Set<String>> = context.channelDataStore.data.map { prefs ->
        prefs[KEY_BOOKMARKED_NEWS] ?: emptySet()
    }

    val customVideoUrlMapFlow: Flow<Map<String, String>> = context.channelDataStore.data.map { prefs ->
        (prefs[KEY_CUSTOM_VIDEO_OVERRIDES] ?: emptySet()).mapNotNull { entry ->
            val idx = entry.indexOf('|')
            if (idx > 0) entry.substring(0, idx) to entry.substring(idx + 1) else null
        }.toMap()
    }

    val savedRequestsFlow: Flow<List<String>> = context.channelDataStore.data.map { prefs ->
        (prefs[KEY_SAVED_REQUESTS] ?: emptySet()).toList().sortedDescending()
    }

    val tasbeehTotalFlow: Flow<Int> = context.channelDataStore.data.map { prefs ->
        prefs[KEY_TASBEEH_TOTAL] ?: 0
    }

    val selectedCityIdFlow: Flow<String> = context.channelDataStore.data.map { prefs ->
        prefs[KEY_SELECTED_CITY_ID] ?: "najaf"
    }

    val quranBookmarkSurahFlow: Flow<Int> = context.channelDataStore.data.map { prefs ->
        prefs[KEY_QURAN_BOOKMARK_SURAH] ?: 1
    }

    val hijriOffsetFlow: Flow<Int> = context.channelDataStore.data.map { prefs ->
        prefs[KEY_HIJRI_OFFSET] ?: 0
    }

    val favoritePrayersFlow: Flow<Set<String>> = context.channelDataStore.data.map { prefs ->
        prefs[KEY_FAVORITE_PRAYERS] ?: emptySet()
    }

    suspend fun incrementTasbeehTotal(delta: Int = 1) {
        context.channelDataStore.edit { prefs ->
            val current = prefs[KEY_TASBEEH_TOTAL] ?: 0
            prefs[KEY_TASBEEH_TOTAL] = (current + delta).coerceAtLeast(0)
        }
    }

    suspend fun resetTasbeehTotal() {
        context.channelDataStore.edit { prefs ->
            prefs[KEY_TASBEEH_TOTAL] = 0
        }
    }

    suspend fun setSelectedCityId(cityId: String) {
        context.channelDataStore.edit { prefs ->
            prefs[KEY_SELECTED_CITY_ID] = cityId
        }
    }

    suspend fun setQuranBookmarkSurah(surahNumber: Int) {
        context.channelDataStore.edit { prefs ->
            prefs[KEY_QURAN_BOOKMARK_SURAH] = surahNumber
        }
    }

    suspend fun setHijriOffset(offset: Int) {
        context.channelDataStore.edit { prefs ->
            prefs[KEY_HIJRI_OFFSET] = offset.coerceIn(-2, 2)
        }
    }

    suspend fun toggleFavoritePrayer(id: String) {
        context.channelDataStore.edit { prefs ->
            val current = (prefs[KEY_FAVORITE_PRAYERS] ?: emptySet()).toMutableSet()
            if (current.contains(id)) current.remove(id) else current.add(id)
            prefs[KEY_FAVORITE_PRAYERS] = current
        }
    }

    suspend fun updateLiveUrls(primaryUrl: String, backupUrl: String) {
        context.channelDataStore.edit { prefs ->
            prefs[KEY_PRIMARY_LIVE_URL] = primaryUrl.trim()
            prefs[KEY_BACKUP_LIVE_URL] = backupUrl.trim().ifBlank { OPEN_HLS_TEST_STREAM }
        }
    }

    suspend fun updateYouTubeSource(handle: String, channelId: String) {
        context.channelDataStore.edit { prefs ->
            prefs[KEY_YOUTUBE_HANDLE] = handle.trim().ifBlank { DEFAULT_YOUTUBE_HANDLE }
            prefs[KEY_YOUTUBE_CHANNEL_ID] = channelId.trim().ifBlank { DEFAULT_YOUTUBE_CHANNEL_ID }
            prefs[KEY_YOUTUBE_URL] = "https://www.youtube.com/${prefs[KEY_YOUTUBE_HANDLE]}"
        }
    }

    suspend fun updateCmsApiUrl(baseUrl: String, syncedLabel: String? = null) {
        context.channelDataStore.edit { prefs ->
            prefs[KEY_CMS_API_BASE_URL] = baseUrl.trim()
            if (syncedLabel != null) {
                prefs[KEY_LAST_SYNCED] = syncedLabel
            }
        }
    }

    suspend fun updateSocialAndSatellite(
        facebook: String,
        youtube: String,
        tiktok: String,
        telegram: String,
        website: String,
        frequency: String,
    ) {
        context.channelDataStore.edit { prefs ->
            prefs[KEY_FACEBOOK_URL] = facebook.trim().ifBlank { DEFAULT_FACEBOOK }
            prefs[KEY_YOUTUBE_URL] = youtube.trim().ifBlank { DEFAULT_YOUTUBE }
            prefs[KEY_TIKTOK_URL] = tiktok.trim().ifBlank { DEFAULT_TIKTOK }
            prefs[KEY_TELEGRAM_URL] = telegram.trim().ifBlank { DEFAULT_TELEGRAM }
            prefs[KEY_WEBSITE_URL] = website.trim().ifBlank { DEFAULT_INSTAGRAM }
            prefs[KEY_FREQUENCY] = frequency.trim().ifBlank { DEFAULT_FREQUENCY }
        }
    }

    suspend fun setCustomVideoUrl(videoId: String, newUrl: String) {
        context.channelDataStore.edit { prefs ->
            val current = (prefs[KEY_CUSTOM_VIDEO_OVERRIDES] ?: emptySet())
                .filterNot { it.startsWith("$videoId|") }
                .toMutableSet()
            if (newUrl.isNotBlank()) {
                current.add("$videoId|${newUrl.trim()}")
            }
            prefs[KEY_CUSTOM_VIDEO_OVERRIDES] = current
        }
    }

    suspend fun toggleVideoBookmark(videoId: String) {
        context.channelDataStore.edit { prefs ->
            val current = (prefs[KEY_BOOKMARKED_VIDEOS] ?: emptySet()).toMutableSet()
            if (current.contains(videoId)) current.remove(videoId) else current.add(videoId)
            prefs[KEY_BOOKMARKED_VIDEOS] = current
        }
    }

    suspend fun toggleNewsBookmark(newsId: String) {
        context.channelDataStore.edit { prefs ->
            val current = (prefs[KEY_BOOKMARKED_NEWS] ?: emptySet()).toMutableSet()
            if (current.contains(newsId)) current.remove(newsId) else current.add(newsId)
            prefs[KEY_BOOKMARKED_NEWS] = current
        }
    }

    suspend fun addCoverageRequest(formattedRecord: String) {
        context.channelDataStore.edit { prefs ->
            val current = (prefs[KEY_SAVED_REQUESTS] ?: emptySet()).toMutableSet()
            current.add(formattedRecord)
            prefs[KEY_SAVED_REQUESTS] = current
        }
    }

    suspend fun resetAllDefaults() {
        context.channelDataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
