package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LiveChannelSource(
    val id: String,
    val title: String,
    val subtitle: String,
    val qualityLabel: String,
    val streamUrl: String,
    val isConfiguredOfficial: Boolean = false,
    val isAudioOnly: Boolean = false,
    val currentProgram: String = "",
)

@JsonClass(generateAdapter = true)
data class ProgramScheduleItem(
    val id: String,
    val time: String,
    val title: String,
    val presenter: String,
    val category: String,
    val isLiveNow: Boolean = false,
)

@JsonClass(generateAdapter = true)
data class VideoProgram(
    val id: String,
    val title: String,
    val category: String,
    val duration: String,
    val publishedAt: String,
    val description: String,
    val videoUrl: String,
    val thumbnailUrl: String? = null,
    val presenter: String = "قناة الأنصاري للإنتاج الفني",
    val isFeatured: Boolean = false,
    val isPreviewStream: Boolean = true,
    val youtubeVideoId: String? = null,
    val youtubeWatchUrl: String? = null,
)

@JsonClass(generateAdapter = true)
data class FieldCoverage(
    val id: String,
    val title: String,
    val location: String,
    val reporter: String,
    val timestamp: String,
    val statusLabel: String,
    val summary: String,
    val videoUrl: String,
    val isLiveField: Boolean = false,
    val youtubeUrl: String? = null,
)

@JsonClass(generateAdapter = true)
data class ChannelEvent(
    val id: String,
    val title: String,
    val hijriDate: String,
    val gregorianDate: String,
    val timeLabel: String,
    val location: String,
    val status: EventStatus,
    val description: String,
    val startTimeMillis: Long,
)

enum class EventStatus(val arabicLabel: String) {
    ONGOING("جارية الآن"),
    UPCOMING("قريباً"),
    ARCHIVED("تغطية مؤرشفة"),
}

@JsonClass(generateAdapter = true)
data class NewsReport(
    val id: String,
    val title: String,
    val category: String,
    val summary: String,
    val fullBody: String,
    val publishedAt: String,
    val readingTime: String,
    val author: String,
    val isBreaking: Boolean = false,
)

enum class SocialPlatform {
    FACEBOOK,
    YOUTUBE,
    TIKTOK,
    TELEGRAM,
    INSTAGRAM,
    X_PLATFORM,
    WEBSITE,
}

@JsonClass(generateAdapter = true)
data class SocialLink(
    val id: String,
    val platform: SocialPlatform,
    val title: String,
    val handle: String,
    val url: String,
    val followersLabel: String,
)

data class ChannelServiceItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val actionLabel: String,
    val routeTarget: String,
)

data class ChannelSettings(
    val primaryLiveUrl: String,
    val backupLiveUrl: String,
    val cmsApiBaseUrl: String,
    val lastSyncedTimestamp: String,
    val facebookUrl: String,
    val youtubeUrl: String,
    val tiktokUrl: String,
    val telegramUrl: String,
    val websiteUrl: String,
    val satelliteFrequency: String,
    val youtubeChannelId: String,
    val youtubeHandle: String,
)

data class CoverageRequestRecord(
    val id: String,
    val senderName: String,
    val governorate: String,
    val topic: String,
    val details: String,
    val createdAt: String,
)
