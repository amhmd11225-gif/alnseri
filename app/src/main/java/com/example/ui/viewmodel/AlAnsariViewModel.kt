package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChannelConfigStore
import com.example.data.model.ChannelEvent
import com.example.data.model.ChannelServiceItem
import com.example.data.model.ChannelSettings
import com.example.data.model.FieldCoverage
import com.example.data.model.LiveChannelSource
import com.example.data.model.NewsReport
import com.example.data.model.ProgramScheduleItem
import com.example.data.model.SocialLink
import com.example.data.model.VideoProgram
import com.example.data.repository.ChannelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AlAnsariUiState(
    val settings: ChannelSettings = ChannelSettings(
        primaryLiveUrl = "",
        backupLiveUrl = ChannelConfigStore.OPEN_HLS_TEST_STREAM,
        cmsApiBaseUrl = "",
        lastSyncedTimestamp = "مرتبط بقناة يوتيوب @alansarie1",
        facebookUrl = ChannelConfigStore.DEFAULT_FACEBOOK,
        youtubeUrl = ChannelConfigStore.DEFAULT_YOUTUBE,
        tiktokUrl = ChannelConfigStore.DEFAULT_TIKTOK,
        telegramUrl = ChannelConfigStore.DEFAULT_TELEGRAM,
        websiteUrl = ChannelConfigStore.DEFAULT_INSTAGRAM,
        satelliteFrequency = ChannelConfigStore.DEFAULT_FREQUENCY,
        youtubeChannelId = ChannelConfigStore.DEFAULT_YOUTUBE_CHANNEL_ID,
        youtubeHandle = ChannelConfigStore.DEFAULT_YOUTUBE_HANDLE,
    ),
    val channelTitle: String = "قناة الأنصاري للإنتاج الفني",
    val channelLogoUrl: String? = null,
    val youtubeLiveVideoId: String? = null,
    val isRefreshingYouTube: Boolean = false,
    val liveSources: List<LiveChannelSource> = emptyList(),
    val selectedLiveSourceId: String = "technical_preview",
    val scheduleItems: List<ProgramScheduleItem> = emptyList(),
    val videos: List<VideoProgram> = emptyList(),
    val fieldCoverages: List<FieldCoverage> = emptyList(),
    val events: List<ChannelEvent> = emptyList(),
    val newsReports: List<NewsReport> = emptyList(),
    val socialLinks: List<SocialLink> = emptyList(),
    val services: List<ChannelServiceItem> = emptyList(),
    val bookmarkedVideoIds: Set<String> = emptySet(),
    val bookmarkedNewsIds: Set<String> = emptySet(),
    val savedRequests: List<String> = emptyList(),
    val statusMessage: String? = null,
    val isSyncingCms: Boolean = false,
)

class AlAnsariViewModel(application: Application) : AndroidViewModel(application) {

    private val configStore = ChannelConfigStore(application.applicationContext)
    private val repository = ChannelRepository(configStore)

    private val selectedSourceIdFlow = MutableStateFlow("official_primary")
    private val statusMessageFlow = MutableStateFlow<String?>(null)
    private val isSyncingFlow = MutableStateFlow(false)
    private val isRefreshingYouTubeFlow = MutableStateFlow(false)
    private val channelLogoUrlFlow = MutableStateFlow<String?>(null)
    private val channelTitleFlow = MutableStateFlow("قناة الأنصاري للإنتاج الفني")
    private val youtubeLiveVideoIdFlow = MutableStateFlow<String?>(null)
    private val youtubeVideosFlow = MutableStateFlow<List<VideoProgram>?>(null)

    private val remoteVideosOverride = MutableStateFlow<List<VideoProgram>?>(null)
    private val remoteNewsOverride = MutableStateFlow<List<NewsReport>?>(null)
    private val remoteEventsOverride = MutableStateFlow<List<ChannelEvent>?>(null)
    private val remoteCoveragesOverride = MutableStateFlow<List<FieldCoverage>?>(null)

    private data class AuxiliaryState(
        val bookmarkedVideos: Set<String>,
        val bookmarkedNews: Set<String>,
        val savedRequests: List<String>,
        val selectedSourceId: String,
        val statusMessage: String?,
        val isSyncing: Boolean,
        val isRefreshingYouTube: Boolean,
        val logoUrl: String?,
        val channelTitle: String,
        val liveVideoId: String?,
        val ytVideos: List<VideoProgram>?,
        val overrides: Map<String, String>,
    )

    private val auxiliaryFlow = combine(
        repository.bookmarkedVideosFlow,
        repository.bookmarkedNewsFlow,
        repository.savedRequestsFlow,
        selectedSourceIdFlow,
        statusMessageFlow,
        isSyncingFlow,
        isRefreshingYouTubeFlow,
        channelLogoUrlFlow,
        channelTitleFlow,
        youtubeLiveVideoIdFlow,
        youtubeVideosFlow,
        repository.customVideoOverridesFlow(),
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        AuxiliaryState(
            bookmarkedVideos = values[0] as Set<String>,
            bookmarkedNews = values[1] as Set<String>,
            savedRequests = values[2] as List<String>,
            selectedSourceId = values[3] as String,
            statusMessage = values[4] as String?,
            isSyncing = values[5] as Boolean,
            isRefreshingYouTube = values[6] as Boolean,
            logoUrl = values[7] as String?,
            channelTitle = values[8] as String,
            liveVideoId = values[9] as String?,
            ytVideos = values[10] as List<VideoProgram>?,
            overrides = values[11] as Map<String, String>,
        )
    }

    val uiState: StateFlow<AlAnsariUiState> = combine(
        repository.settingsFlow,
        repository.liveSourcesFlow(),
        repository.socialLinksFlow(),
        auxiliaryFlow,
    ) { settings, liveSources, socialLinks, aux ->
        val resolvedSelectedId = if (liveSources.any { it.id == aux.selectedSourceId }) {
            aux.selectedSourceId
        } else {
            liveSources.firstOrNull()?.id ?: "technical_preview"
        }

        val baseVideos = remoteVideosOverride.value
            ?: aux.ytVideos?.takeIf { it.isNotEmpty() }
            ?: repository.defaultVideos

        val finalVideos = baseVideos.map { video ->
            val customUrl = aux.overrides[video.id]
            if (!customUrl.isNullOrBlank()) {
                video.copy(videoUrl = customUrl, isPreviewStream = false)
            } else {
                video
            }
        }

        AlAnsariUiState(
            settings = settings,
            channelTitle = aux.channelTitle,
            channelLogoUrl = aux.logoUrl,
            youtubeLiveVideoId = aux.liveVideoId,
            isRefreshingYouTube = aux.isRefreshingYouTube,
            liveSources = liveSources,
            selectedLiveSourceId = resolvedSelectedId,
            scheduleItems = repository.scheduleItems,
            videos = finalVideos,
            fieldCoverages = remoteCoveragesOverride.value ?: repository.fieldCoverages,
            events = remoteEventsOverride.value ?: repository.events,
            newsReports = remoteNewsOverride.value ?: repository.newsReports,
            socialLinks = socialLinks,
            services = repository.servicesList,
            bookmarkedVideoIds = aux.bookmarkedVideos,
            bookmarkedNewsIds = aux.bookmarkedNews,
            savedRequests = aux.savedRequests,
            statusMessage = aux.statusMessage,
            isSyncingCms = aux.isSyncing,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AlAnsariUiState(
            liveSources = repository.buildLiveSources(AlAnsariUiState().settings),
            scheduleItems = repository.scheduleItems,
            videos = repository.defaultVideos,
            fieldCoverages = repository.fieldCoverages,
            events = repository.events,
            newsReports = repository.newsReports,
            services = repository.servicesList,
        ),
    )

    init {
        refreshFromYouTube(showToast = false)
    }

    fun refreshFromYouTube(showToast: Boolean = true) {
        viewModelScope.launch {
            isRefreshingYouTubeFlow.value = true
            val currentSettings = repository.settingsFlow.first()
            val result = repository.fetchYouTubeChannelSnapshot(
                channelId = currentSettings.youtubeChannelId,
                handle = currentSettings.youtubeHandle,
            )
            isRefreshingYouTubeFlow.value = false
            result.onSuccess { snapshot ->
                if (!snapshot.logoUrl.isNullOrBlank()) {
                    channelLogoUrlFlow.value = snapshot.logoUrl
                }
                if (snapshot.title.isNotBlank()) {
                    channelTitleFlow.value = snapshot.title
                }
                youtubeLiveVideoIdFlow.value = snapshot.liveVideoId
                if (snapshot.videos.isNotEmpty()) {
                    youtubeVideosFlow.value = snapshot.videos
                }
                if (showToast) {
                    statusMessageFlow.value = "تم تحديث الشعار ومقاطع القناة من ${snapshot.handle}"
                }
            }.onFailure {
                if (showToast) {
                    statusMessageFlow.value = "تعذر التحديث من يوتيوب حالياً، يتم عرض المحتوى المحفوظ"
                }
            }
        }
    }

    fun saveYouTubeSource(handle: String, channelId: String) {
        viewModelScope.launch {
            configStore.updateYouTubeSource(handle, channelId)
            refreshFromYouTube(showToast = true)
        }
    }

    fun selectLiveSource(sourceId: String) {
        selectedSourceIdFlow.value = sourceId
    }

    fun saveLiveStreamUrls(primaryUrl: String, backupUrl: String) {
        viewModelScope.launch {
            configStore.updateLiveUrls(primaryUrl, backupUrl)
            if (primaryUrl.isNotBlank()) {
                selectedSourceIdFlow.value = "official_primary"
            }
            statusMessageFlow.value = "تم حفظ روابط البث المباشر بنجاح"
        }
    }

    fun updateVideoStreamUrl(videoId: String, customUrl: String) {
        viewModelScope.launch {
            configStore.setCustomVideoUrl(videoId, customUrl)
            statusMessageFlow.value = "تم تحديث رابط الفيديو بنجاح"
        }
    }

    fun saveSocialAndSatellite(
        facebook: String,
        youtube: String,
        tiktok: String,
        telegram: String,
        website: String,
        frequency: String,
    ) {
        viewModelScope.launch {
            configStore.updateSocialAndSatellite(
                facebook = facebook,
                youtube = youtube,
                tiktok = tiktok,
                telegram = telegram,
                website = website,
                frequency = frequency,
            )
            statusMessageFlow.value = "تم تحديث روابط التواصل والتردد"
        }
    }

    fun syncFromCmsDashboard(baseUrl: String) {
        if (baseUrl.isBlank()) {
            statusMessageFlow.value = "يرجى إدخال رابط خادم لوحة التحكم (CMS API) أولاً"
            return
        }
        viewModelScope.launch {
            isSyncingFlow.value = true
            val result = repository.syncWithRemoteDashboard(baseUrl)
            isSyncingFlow.value = false
            result.onSuccess { bundle ->
                if (bundle.videos.isNotEmpty()) remoteVideosOverride.value = bundle.videos
                if (bundle.news.isNotEmpty()) remoteNewsOverride.value = bundle.news
                if (bundle.events.isNotEmpty()) remoteEventsOverride.value = bundle.events
                if (bundle.coverages.isNotEmpty()) remoteCoveragesOverride.value = bundle.coverages
                statusMessageFlow.value = "تمت المزامنة مع لوحة التحكم بنجاح"
            }.onFailure { err ->
                statusMessageFlow.value = "تعذر الاتصال بلوحة التحكم: ${err.localizedMessage ?: "تحقق من الرابط"}"
            }
        }
    }

    fun toggleVideoBookmark(videoId: String) {
        viewModelScope.launch {
            configStore.toggleVideoBookmark(videoId)
        }
    }

    fun toggleNewsBookmark(newsId: String) {
        viewModelScope.launch {
            configStore.toggleNewsBookmark(newsId)
        }
    }

    fun submitCoverageRequest(name: String, governorate: String, topic: String, details: String) {
        viewModelScope.launch {
            val formatted = "${System.currentTimeMillis()} — $name ($governorate): $topic"
            configStore.addCoverageRequest(formatted)
            statusMessageFlow.value = "تم تسجيل طلب التغطية الميدانية بنجاح"
        }
    }

    fun resetAllSources() {
        viewModelScope.launch {
            configStore.resetAllDefaults()
            remoteVideosOverride.value = null
            remoteNewsOverride.value = null
            remoteEventsOverride.value = null
            remoteCoveragesOverride.value = null
            selectedSourceIdFlow.value = "technical_preview"
            refreshFromYouTube(showToast = false)
            statusMessageFlow.value = "تمت استعادة الإعدادات الافتراضية"
        }
    }

    fun clearStatusMessage() {
        statusMessageFlow.value = null
    }
}
