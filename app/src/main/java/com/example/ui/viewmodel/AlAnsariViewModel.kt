package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.GoogleAuthHelper
import com.example.data.auth.GoogleAuthHelper.authStateFlow
import com.example.data.local.ChannelConfigStore
import com.example.data.model.ChannelEvent
import com.example.data.model.ChannelServiceItem
import com.example.data.model.ChannelSettings
import com.example.data.model.CloudChannelConfig
import com.example.data.model.CloudCoverageDoc
import com.example.data.model.CloudEventDoc
import com.example.data.model.CloudNewsDoc
import com.example.data.model.CloudVideoDoc
import com.example.data.model.CoverageBookingRequest
import com.example.data.model.FieldCoverage
import com.example.data.model.LiveChannelSource
import com.example.data.model.NewsReport
import com.example.data.model.ProgramScheduleItem
import com.example.data.model.SocialLink
import com.example.data.model.SocialPlatform
import com.example.data.model.VideoProgram
import com.example.data.remote.FirestoreChannelRepository
import com.example.data.repository.ChannelRepository
import com.example.ui.screens.extractYouTubeVideoId
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HaqeebatPrefsState(
    val tasbeehTotal: Int = 0,
    val selectedCityId: String = "najaf",
    val quranBookmarkSurah: Int = 1,
    val hijriOffsetDays: Int = 0,
    val favoritePrayerIds: Set<String> = emptySet(),
)

data class AlAnsariUiState(
    val settings: ChannelSettings = ChannelSettings(
        primaryLiveUrl = "",
        backupLiveUrl = ChannelConfigStore.OPEN_HLS_TEST_STREAM,
        liveTitle = "قناة الأنصاري للإنتاج الفني – البث المباشر",
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
    val currentUser: FirebaseUser? = null,
    val isChannelOwner: Boolean = false,
    val myBookings: List<CoverageBookingRequest> = emptyList(),
    val adminBookings: List<CoverageBookingRequest> = emptyList(),
    val cloudEvents: List<CloudEventDoc> = emptyList(),
    val cloudNews: List<CloudNewsDoc> = emptyList(),
    val cloudVideos: List<CloudVideoDoc> = emptyList(),
    val cloudCoverages: List<CloudCoverageDoc> = emptyList(),
    val isSavingCloud: Boolean = false,
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
    val haqeebat: HaqeebatPrefsState = HaqeebatPrefsState(),
    val statusMessage: String? = null,
    val isSyncingCms: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class AlAnsariViewModel(
    private val repository: ChannelRepository,
    private val firestoreRepository: FirestoreChannelRepository,
) : ViewModel() {

    private companion object {
        const val TAG = "AlAnsariVM"
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }

    private val configStore = repository.configStore
    private val auth = Firebase.auth

    private val selectedSourceIdFlow = MutableStateFlow("official_primary")
    private val statusMessageFlow = MutableStateFlow<String?>(null)
    private val isSyncingFlow = MutableStateFlow(false)
    private val isSavingCloudFlow = MutableStateFlow(false)
    private val isRefreshingYouTubeFlow = MutableStateFlow(false)
    private val channelLogoUrlFlow = MutableStateFlow<String?>(null)
    private val channelTitleFlow = MutableStateFlow("قناة الأنصاري للإنتاج الفني")
    private val youtubeLiveVideoIdFlow = MutableStateFlow<String?>(null)
    private val youtubeVideosFlow = MutableStateFlow<List<VideoProgram>?>(null)

    private val remoteVideosOverride = MutableStateFlow<List<VideoProgram>?>(null)
    private val remoteNewsOverride = MutableStateFlow<List<NewsReport>?>(null)
    private val remoteEventsOverride = MutableStateFlow<List<ChannelEvent>?>(null)
    private val remoteCoveragesOverride = MutableStateFlow<List<FieldCoverage>?>(null)

    private val authUserFlow = auth.authStateFlow()

    private val cloudConfigFlow = firestoreRepository.observeLiveChannelConfig()
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            Log.w(TAG, "Error observing cloud channel config", firestoreError ?: error)
            emit(null)
        }

    private val myBookingsFlow = authUserFlow.flatMapLatest { user ->
        if (user == null) {
            flowOf(emptyList())
        } else {
            firestoreRepository.observeMyBookings(user.uid)
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    Log.w(TAG, "Error observing user bookings", firestoreError ?: error)
                    emit(emptyList())
                }
        }
    }

    private val adminBookingsFlow = authUserFlow.flatMapLatest { user ->
        if (!GoogleAuthHelper.isVerifiedChannelOwner(user)) {
            flowOf(emptyList())
        } else {
            firestoreRepository.observeAllBookingsForOwner()
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    Log.w(TAG, "Error observing admin bookings", firestoreError ?: error)
                    emit(emptyList())
                }
        }
    }

    private data class AuxiliaryState(
        val bookmarkedVideos: Set<String>,
        val bookmarkedNews: Set<String>,
        val selectedSourceId: String,
        val statusMessage: String?,
        val isSyncing: Boolean,
        val isSavingCloud: Boolean,
        val isRefreshingYouTube: Boolean,
        val logoUrl: String?,
        val channelTitle: String,
        val liveVideoId: String?,
        val ytVideos: List<VideoProgram>?,
        val currentUser: FirebaseUser?,
        val myBookings: List<CoverageBookingRequest>,
        val adminBookings: List<CoverageBookingRequest>,
    )

    private val auxiliaryFlow = combine(
        repository.bookmarkedVideosFlow,
        repository.bookmarkedNewsFlow,
        selectedSourceIdFlow,
        statusMessageFlow,
        isSyncingFlow,
        isSavingCloudFlow,
        isRefreshingYouTubeFlow,
        channelLogoUrlFlow,
        channelTitleFlow,
        youtubeLiveVideoIdFlow,
        youtubeVideosFlow,
        authUserFlow,
        myBookingsFlow,
        adminBookingsFlow,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        AuxiliaryState(
            bookmarkedVideos = values[0] as Set<String>,
            bookmarkedNews = values[1] as Set<String>,
            selectedSourceId = values[2] as String,
            statusMessage = values[3] as String?,
            isSyncing = values[4] as Boolean,
            isSavingCloud = values[5] as Boolean,
            isRefreshingYouTube = values[6] as Boolean,
            logoUrl = values[7] as String?,
            channelTitle = values[8] as String,
            liveVideoId = values[9] as String?,
            ytVideos = values[10] as List<VideoProgram>?,
            currentUser = values[11] as FirebaseUser?,
            myBookings = values[12] as List<CoverageBookingRequest>,
            adminBookings = values[13] as List<CoverageBookingRequest>,
        )
    }

    private data class CloudSectionsState(
        val events: List<CloudEventDoc> = emptyList(),
        val news: List<CloudNewsDoc> = emptyList(),
        val videos: List<CloudVideoDoc> = emptyList(),
        val coverages: List<CloudCoverageDoc> = emptyList(),
    )

    private val cloudSectionsFlow = combine(
        firestoreRepository.observeCloudEvents().catch { err ->
            if (err is CancellationException) throw err
            Log.w(TAG, "Error observing cloud events", err)
            emit(emptyList())
        },
        firestoreRepository.observeCloudNews().catch { err ->
            if (err is CancellationException) throw err
            Log.w(TAG, "Error observing cloud news", err)
            emit(emptyList())
        },
        firestoreRepository.observeCloudVideos().catch { err ->
            if (err is CancellationException) throw err
            Log.w(TAG, "Error observing cloud videos", err)
            emit(emptyList())
        },
        firestoreRepository.observeCloudCoverages().catch { err ->
            if (err is CancellationException) throw err
            Log.w(TAG, "Error observing cloud coverages", err)
            emit(emptyList())
        },
    ) { events, news, videos, coverages ->
        CloudSectionsState(events = events, news = news, videos = videos, coverages = coverages)
    }

    private val haqeebatPrefsFlow = combine(
        configStore.tasbeehTotalFlow,
        configStore.selectedCityIdFlow,
        configStore.quranBookmarkSurahFlow,
        configStore.hijriOffsetFlow,
        configStore.favoritePrayersFlow,
    ) { tasbeehTotal, cityId, bookmarkSurah, hijriOffset, favorites ->
        HaqeebatPrefsState(
            tasbeehTotal = tasbeehTotal,
            selectedCityId = cityId,
            quranBookmarkSurah = bookmarkSurah,
            hijriOffsetDays = hijriOffset,
            favoritePrayerIds = favorites,
        )
    }

    private val haqeebatAndCloudFlow = combine(
        haqeebatPrefsFlow,
        cloudSectionsFlow,
    ) { haqeebatState, cloudSections ->
        haqeebatState to cloudSections
    }

    val uiState: StateFlow<AlAnsariUiState> = combine(
        repository.settingsFlow,
        cloudConfigFlow,
        repository.savedRequestsFlow,
        auxiliaryFlow,
        haqeebatAndCloudFlow,
    ) { localSettings, cloudConfig, savedRequests, aux, (haqeebatState, cloudSections) ->
        val effectiveSettings = if (cloudConfig != null) {
            localSettings.copy(
                primaryLiveUrl = cloudConfig.primaryLiveUrl,
                backupLiveUrl = cloudConfig.backupLiveUrl.ifBlank { localSettings.backupLiveUrl },
                liveTitle = cloudConfig.liveTitle.ifBlank { localSettings.liveTitle },
                youtubeHandle = cloudConfig.youtubeHandle.ifBlank { localSettings.youtubeHandle },
                youtubeChannelId = cloudConfig.youtubeChannelId.ifBlank { localSettings.youtubeChannelId },
                facebookUrl = cloudConfig.facebookUrl.ifBlank { localSettings.facebookUrl },
                telegramUrl = cloudConfig.telegramUrl.ifBlank { localSettings.telegramUrl },
                tiktokUrl = cloudConfig.tiktokUrl.ifBlank { localSettings.tiktokUrl },
                websiteUrl = cloudConfig.instagramUrl.ifBlank { localSettings.websiteUrl },
                youtubeUrl = "https://www.youtube.com/${cloudConfig.youtubeHandle.ifBlank { localSettings.youtubeHandle }}",
                satelliteFrequency = cloudConfig.satelliteFrequency.ifBlank { localSettings.satelliteFrequency },
                homeAnnouncement = cloudConfig.homeAnnouncement,
                latestAppVersion = cloudConfig.latestAppVersion,
                appUpdateUrl = cloudConfig.appUpdateUrl,
            )
        } else {
            localSettings
        }

        val liveSources = repository.buildLiveSources(effectiveSettings)
        val resolvedSelectedId = if (liveSources.any { it.id == aux.selectedSourceId }) {
            aux.selectedSourceId
        } else {
            liveSources.firstOrNull()?.id ?: "technical_preview"
        }

        val effectiveSocialLinks = listOf(
            SocialLink(
                id = "yt",
                platform = SocialPlatform.YOUTUBE,
                title = "يوتيوب قناة الأنصاري",
                handle = effectiveSettings.youtubeHandle,
                url = effectiveSettings.youtubeUrl,
                followersLabel = "أكثر من 180 ألف مشترك • البث والأرشيف",
            ),
            SocialLink(
                id = "tg",
                platform = SocialPlatform.TELEGRAM,
                title = "تيليجرام البث والأخبار",
                handle = "@SNG1080",
                url = effectiveSettings.telegramUrl,
                followersLabel = "تغطيات SNG1080 والإعلانات الفورية",
            ),
            SocialLink(
                id = "fb",
                platform = SocialPlatform.FACEBOOK,
                title = "فيسبوك الرسمي",
                handle = "al.ansar.live",
                url = effectiveSettings.facebookUrl,
                followersLabel = "البث المباشر والتغطيات اليومية",
            ),
            SocialLink(
                id = "tt",
                platform = SocialPlatform.TIKTOK,
                title = "تيك توك",
                handle = effectiveSettings.youtubeHandle,
                url = effectiveSettings.tiktokUrl,
                followersLabel = "مقاطع قصيرة ولقطات ميدانية",
            ),
            SocialLink(
                id = "ig",
                platform = SocialPlatform.INSTAGRAM,
                title = "إنستغرام",
                handle = effectiveSettings.youtubeHandle,
                url = effectiveSettings.websiteUrl,
                followersLabel = "أبرز الصور والفعاليات",
            ),
        )

        val baseVideos = remoteVideosOverride.value
            ?: aux.ytVideos?.takeIf { it.isNotEmpty() }
            ?: repository.defaultVideos

        val mergedVideos = (cloudSections.videos.map { it.toVideoProgram() } + baseVideos).distinctBy { it.id }
        val mergedEvents = (cloudSections.events.map { it.toChannelEvent() } + (remoteEventsOverride.value ?: repository.events)).distinctBy { it.id }
        val mergedNews = (cloudSections.news.map { it.toNewsReport() } + (remoteNewsOverride.value ?: repository.newsReports)).distinctBy { it.id }
        val mergedCoverages = (cloudSections.coverages.map { it.toFieldCoverage() } + (remoteCoveragesOverride.value ?: repository.fieldCoverages)).distinctBy { it.id }

        AlAnsariUiState(
            settings = effectiveSettings,
            currentUser = aux.currentUser,
            isChannelOwner = GoogleAuthHelper.isVerifiedChannelOwner(aux.currentUser),
            myBookings = aux.myBookings,
            adminBookings = aux.adminBookings,
            cloudEvents = cloudSections.events,
            cloudNews = cloudSections.news,
            cloudVideos = cloudSections.videos,
            cloudCoverages = cloudSections.coverages,
            isSavingCloud = aux.isSavingCloud,
            channelTitle = aux.channelTitle,
            channelLogoUrl = aux.logoUrl,
            youtubeLiveVideoId = aux.liveVideoId,
            isRefreshingYouTube = aux.isRefreshingYouTube,
            liveSources = liveSources,
            selectedLiveSourceId = resolvedSelectedId,
            scheduleItems = repository.scheduleItems,
            videos = mergedVideos,
            fieldCoverages = mergedCoverages,
            events = mergedEvents,
            newsReports = mergedNews,
            socialLinks = effectiveSocialLinks,
            services = repository.servicesList,
            bookmarkedVideoIds = aux.bookmarkedVideos,
            bookmarkedNewsIds = aux.bookmarkedNews,
            savedRequests = savedRequests,
            haqeebat = haqeebatState,
            statusMessage = aux.statusMessage,
            isSyncingCms = aux.isSyncing,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
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

    fun selectLiveSource(sourceId: String) {
        selectedSourceIdFlow.value = sourceId
    }

    fun publishCloudChannelConfig(
        primaryLiveUrl: String,
        backupLiveUrl: String,
        liveTitle: String,
        youtubeHandle: String,
        youtubeChannelId: String,
        facebookUrl: String,
        telegramUrl: String,
        tiktokUrl: String,
        instagramUrl: String,
        satelliteFrequency: String,
        homeAnnouncement: String = "",
        latestAppVersion: String = "",
        appUpdateUrl: String = "",
    ) {
        viewModelScope.launch {
            isSavingCloudFlow.value = true
            val config = CloudChannelConfig(
                primaryLiveUrl = primaryLiveUrl,
                backupLiveUrl = backupLiveUrl,
                liveTitle = liveTitle,
                youtubeHandle = youtubeHandle,
                youtubeChannelId = youtubeChannelId,
                facebookUrl = facebookUrl,
                telegramUrl = telegramUrl,
                tiktokUrl = tiktokUrl,
                instagramUrl = instagramUrl,
                satelliteFrequency = satelliteFrequency,
                homeAnnouncement = homeAnnouncement,
                latestAppVersion = latestAppVersion,
                appUpdateUrl = appUpdateUrl,
            )
            val result = firestoreRepository.publishChannelConfig(config)
            isSavingCloudFlow.value = false
            result.onSuccess {
                configStore.updateLiveUrls(primaryLiveUrl, backupLiveUrl)
                configStore.updateYouTubeSource(youtubeHandle, youtubeChannelId)
                configStore.updateSocialAndSatellite(
                    facebook = facebookUrl,
                    youtube = "https://www.youtube.com/${youtubeHandle.trim()}",
                    tiktok = tiktokUrl,
                    telegram = telegramUrl,
                    website = instagramUrl,
                    frequency = satelliteFrequency,
                )
                if (primaryLiveUrl.isNotBlank()) {
                    selectedSourceIdFlow.value = "official_primary"
                } else {
                    selectedSourceIdFlow.value = "technical_preview"
                }
                statusMessageFlow.value = "تم نشر رابط البث وإعدادات القناة لجميع المشاهدين"
            }.onFailure { err ->
                statusMessageFlow.value = err.localizedMessage ?: "تعذر حفظ الإعدادات السحابية"
            }
        }
    }

    fun submitCloudCoverageBooking(
        senderName: String,
        phone: String,
        governorate: String,
        serviceType: String,
        eventDate: String,
        topic: String,
        details: String,
        onSubmitted: () -> Unit = {},
    ) {
        viewModelScope.launch {
            isSavingCloudFlow.value = true
            val result = firestoreRepository.submitCoverageBooking(
                senderName = senderName,
                phone = phone,
                governorate = governorate,
                serviceType = serviceType,
                eventDate = eventDate,
                topic = topic,
                details = details,
            )
            isSavingCloudFlow.value = false
            result.onSuccess {
                configStore.addCoverageRequest("${System.currentTimeMillis()} — $senderName ($governorate): $topic")
                statusMessageFlow.value = "تم إرسال طلب حجز التغطية والبث المباشر بنجاح"
                onSubmitted()
            }.onFailure { err ->
                statusMessageFlow.value = err.localizedMessage ?: "تعذر إرسال الطلب، تأكد من الحقول والاتصال"
            }
        }
    }

    fun updateBookingStatus(
        requestId: String,
        newStatus: String,
        adminNote: String? = null,
    ) {
        viewModelScope.launch {
            isSavingCloudFlow.value = true
            val result = firestoreRepository.updateBookingStatus(
                requestId = requestId,
                newStatus = newStatus,
                adminNote = adminNote,
            )
            isSavingCloudFlow.value = false
            result.onSuccess {
                statusMessageFlow.value = "تم تحديث حالة طلب الحجز بنجاح"
            }.onFailure { err ->
                statusMessageFlow.value = err.localizedMessage ?: "تعذر تحديث حالة الطلب"
            }
        }
    }

    fun publishChannelEvent(
        title: String,
        hijriDate: String,
        gregorianDate: String,
        timeLabel: String,
        location: String,
        status: String,
        description: String,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            isSavingCloudFlow.value = true
            val result = firestoreRepository.publishChannelEvent(
                title = title,
                hijriDate = hijriDate,
                gregorianDate = gregorianDate,
                timeLabel = timeLabel,
                location = location,
                status = status,
                description = description,
            )
            isSavingCloudFlow.value = false
            result.onSuccess {
                statusMessageFlow.value = "تمت إضافة الفعالية ونشرها في التطبيق بنجاح"
                onSuccess()
            }.onFailure { err ->
                statusMessageFlow.value = err.localizedMessage ?: "تعذر نشر الفعالية"
            }
        }
    }

    fun updateCloudEventStatus(eventId: String, newStatus: String) {
        viewModelScope.launch {
            isSavingCloudFlow.value = true
            val result = firestoreRepository.updateCloudEventStatus(eventId, newStatus)
            isSavingCloudFlow.value = false
            result.onSuccess {
                statusMessageFlow.value = "تم تحديث حالة الفعالية"
            }.onFailure { err ->
                statusMessageFlow.value = err.localizedMessage ?: "تعذر تحديث حالة الفعالية"
            }
        }
    }

    fun deleteCloudEvent(eventId: String) {
        viewModelScope.launch {
            isSavingCloudFlow.value = true
            val result = firestoreRepository.deleteCloudEvent(eventId)
            isSavingCloudFlow.value = false
            result.onSuccess {
                statusMessageFlow.value = "تم حذف الفعالية من التطبيق"
            }.onFailure { err ->
                statusMessageFlow.value = err.localizedMessage ?: "تعذر حذف الفعالية"
            }
        }
    }

    fun publishChannelNews(
        title: String,
        category: String,
        summary: String,
        fullBody: String,
        author: String,
        isBreaking: Boolean,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            isSavingCloudFlow.value = true
            val result = firestoreRepository.publishChannelNews(
                title = title,
                category = category,
                summary = summary,
                fullBody = fullBody,
                author = author,
                isBreaking = isBreaking,
            )
            isSavingCloudFlow.value = false
            result.onSuccess {
                statusMessageFlow.value = "تم نشر الخبر في قسم الأخبار بنجاح"
                onSuccess()
            }.onFailure { err ->
                statusMessageFlow.value = err.localizedMessage ?: "تعذر نشر الخبر"
            }
        }
    }

    fun deleteCloudNews(newsId: String) {
        viewModelScope.launch {
            isSavingCloudFlow.value = true
            val result = firestoreRepository.deleteCloudNews(newsId)
            isSavingCloudFlow.value = false
            result.onSuccess {
                statusMessageFlow.value = "تم حذف الخبر بنجاح"
            }.onFailure { err ->
                statusMessageFlow.value = err.localizedMessage ?: "تعذر حذف الخبر"
            }
        }
    }

    fun publishChannelVideo(
        title: String,
        category: String,
        presenter: String,
        duration: String,
        description: String,
        streamOrYoutubeUrl: String,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            isSavingCloudFlow.value = true
            val ytId = extractYouTubeVideoId(streamOrYoutubeUrl)
            val result = firestoreRepository.publishChannelVideo(
                title = title,
                category = category,
                presenter = presenter,
                duration = duration,
                description = description,
                streamOrYoutubeUrl = streamOrYoutubeUrl,
                youtubeVideoId = ytId,
            )
            isSavingCloudFlow.value = false
            result.onSuccess {
                statusMessageFlow.value = "تمت إضافة الفيديو إلى مكتبة البرامج بنجاح"
                onSuccess()
            }.onFailure { err ->
                statusMessageFlow.value = err.localizedMessage ?: "تعذر إضافة الفيديو"
            }
        }
    }

    fun deleteCloudVideo(videoId: String) {
        viewModelScope.launch {
            isSavingCloudFlow.value = true
            val result = firestoreRepository.deleteCloudVideo(videoId)
            isSavingCloudFlow.value = false
            result.onSuccess {
                statusMessageFlow.value = "تم حذف الفيديو بنجاح"
            }.onFailure { err ->
                statusMessageFlow.value = err.localizedMessage ?: "تعذر حذف الفيديو"
            }
        }
    }

    fun publishChannelCoverage(
        title: String,
        location: String,
        reporter: String,
        statusLabel: String,
        summary: String,
        videoUrl: String,
        isLiveField: Boolean,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            isSavingCloudFlow.value = true
            val trimmedUrl = videoUrl.trim()
            val ytId = extractYouTubeVideoId(trimmedUrl)
            val result = firestoreRepository.publishChannelCoverage(
                title = title,
                location = location,
                reporter = reporter,
                statusLabel = statusLabel,
                summary = summary,
                videoUrl = if (ytId != null) "" else trimmedUrl,
                youtubeUrl = if (ytId != null) trimmedUrl else null,
                isLiveField = isLiveField,
            )
            isSavingCloudFlow.value = false
            result.onSuccess {
                statusMessageFlow.value = "تم نشر التغطية الميدانية بنجاح"
                onSuccess()
            }.onFailure { err ->
                statusMessageFlow.value = err.localizedMessage ?: "تعذر نشر التغطية"
            }
        }
    }

    fun deleteCloudCoverage(coverageId: String) {
        viewModelScope.launch {
            isSavingCloudFlow.value = true
            val result = firestoreRepository.deleteCloudCoverage(coverageId)
            isSavingCloudFlow.value = false
            result.onSuccess {
                statusMessageFlow.value = "تم حذف التغطية بنجاح"
            }.onFailure { err ->
                statusMessageFlow.value = err.localizedMessage ?: "تعذر حذف التغطية"
            }
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

    fun incrementTasbeeh(delta: Int = 1) {
        viewModelScope.launch {
            configStore.incrementTasbeehTotal(delta)
        }
    }

    fun resetTasbeeh() {
        viewModelScope.launch {
            configStore.resetTasbeehTotal()
            statusMessageFlow.value = "تم تصفير عدّاد التسبيح الإجمالي"
        }
    }

    fun selectHaqeebatCity(cityId: String) {
        viewModelScope.launch {
            configStore.setSelectedCityId(cityId)
        }
    }

    fun setQuranBookmark(surahNumber: Int) {
        viewModelScope.launch {
            configStore.setQuranBookmarkSurah(surahNumber)
            statusMessageFlow.value = "تم تثبيت علامة الختمة عند سورة رقم $surahNumber"
        }
    }

    fun adjustHijriOffset(delta: Int, currentOffset: Int) {
        viewModelScope.launch {
            configStore.setHijriOffset((currentOffset + delta).coerceIn(-2, 2))
        }
    }

    fun toggleFavoritePrayer(id: String) {
        viewModelScope.launch {
            configStore.toggleFavoritePrayer(id)
        }
    }

    fun showStatusMessage(message: String) {
        statusMessageFlow.value = message
    }

    fun clearStatusMessage() {
        statusMessageFlow.value = null
    }
}
