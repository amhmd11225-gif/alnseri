package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
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

enum class StreamProtocol {
    HLS,
    RTMP,
    SRT,
    RTSP,
    VOD_HTTP,
}

fun detectStreamProtocol(url: String): StreamProtocol {
    val clean = url.trim().lowercase()
    return when {
        clean.startsWith("srt://") -> StreamProtocol.SRT
        clean.startsWith("rtmp://") || clean.startsWith("rtmps://") -> StreamProtocol.RTMP
        clean.startsWith("rtsp://") || clean.startsWith("udp://") -> StreamProtocol.RTSP
        clean.contains(".m3u8") -> StreamProtocol.HLS
        else -> StreamProtocol.VOD_HTTP
    }
}

fun protocolBadgeLabel(protocol: StreamProtocol): String = when (protocol) {
    StreamProtocol.SRT -> "SRT مباشر"
    StreamProtocol.RTMP -> "RTMP مباشر"
    StreamProtocol.RTSP -> "RTSP مباشر"
    StreamProtocol.HLS -> "HLS HD"
    StreamProtocol.VOD_HTTP -> "بث مباشر"
}

data class ChannelSettings(
    val primaryLiveUrl: String,
    val backupLiveUrl: String,
    val liveTitle: String = "قناة الأنصاري للإنتاج الفني – البث المباشر",
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
    val homeAnnouncement: String = "",
    val latestAppVersion: String = "",
    val appUpdateUrl: String = "",
)

@Keep
@IgnoreExtraProperties
data class CloudChannelConfig(
    @DocumentId val id: String = "",
    val primaryLiveUrl: String = "",
    val backupLiveUrl: String = "",
    val liveTitle: String = "قناة الأنصاري للإنتاج الفني – البث المباشر",
    val youtubeHandle: String = "@alansarie1",
    val youtubeChannelId: String = "UCbZnqtO8B64mMhTzTXIxqUA",
    val facebookUrl: String = "",
    val telegramUrl: String = "",
    val tiktokUrl: String = "",
    val instagramUrl: String = "",
    val satelliteFrequency: String = "",
    val homeAnnouncement: String = "",
    val latestAppVersion: String = "",
    val appUpdateUrl: String = "",
    val updatedByUid: String = "",
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class CoverageBookingRequest(
    @DocumentId val id: String = "",
    val userId: String = "",
    val senderName: String = "",
    val phone: String = "",
    val governorate: String = "",
    val serviceType: String = "تغطية وبث مباشر",
    val eventDate: String = "",
    val topic: String = "",
    val details: String? = null,
    val status: String = "pending",
    val adminNote: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

fun bookingStatusArabic(status: String): String = when (status) {
    "confirmed" -> "مؤكد"
    "completed" -> "مكتمل"
    "declined" -> "معتذر عنه"
    else -> "قيد المراجعة"
}

data class CoverageRequestRecord(
    val id: String,
    val senderName: String,
    val governorate: String,
    val topic: String,
    val details: String,
    val createdAt: String,
)

@Keep
@IgnoreExtraProperties
data class CloudEventDoc(
    @DocumentId val id: String = "",
    val title: String = "",
    val hijriDate: String = "",
    val gregorianDate: String = "",
    val timeLabel: String = "",
    val location: String = "",
    val status: String = "UPCOMING",
    val description: String = "",
    val createdByUid: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    fun toChannelEvent(): ChannelEvent = ChannelEvent(
        id = id,
        title = title,
        hijriDate = hijriDate,
        gregorianDate = gregorianDate,
        timeLabel = timeLabel,
        location = location,
        status = enumValues<EventStatus>().firstOrNull { it.name == status } ?: EventStatus.UPCOMING,
        description = description,
        startTimeMillis = (createdAt?.toDate()?.time ?: System.currentTimeMillis()),
    )
}

@Keep
@IgnoreExtraProperties
data class CloudNewsDoc(
    @DocumentId val id: String = "",
    val title: String = "",
    val category: String = "أخبار القناة",
    val summary: String = "",
    val fullBody: String = "",
    val publishedAt: String = "الآن",
    val readingTime: String = "دقيقتان",
    val author: String = "إدارة قناة الأنصاري",
    val breaking: Boolean = false,
    val createdByUid: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    fun toNewsReport(): NewsReport = NewsReport(
        id = id,
        title = title,
        category = category,
        summary = summary,
        fullBody = fullBody,
        publishedAt = publishedAt,
        readingTime = readingTime,
        author = author,
        isBreaking = breaking,
    )
}

@Keep
@IgnoreExtraProperties
data class CloudVideoDoc(
    @DocumentId val id: String = "",
    val title: String = "",
    val category: String = "تغطيات خاصة",
    val duration: String = "فيديو",
    val publishedAt: String = "جديد",
    val description: String = "",
    val videoUrl: String = "",
    val presenter: String = "قناة الأنصاري للإنتاج الفني",
    val youtubeVideoId: String? = null,
    val youtubeWatchUrl: String? = null,
    val featured: Boolean = false,
    val createdByUid: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    fun toVideoProgram(): VideoProgram = VideoProgram(
        id = id,
        title = title,
        category = category,
        duration = duration,
        publishedAt = publishedAt,
        description = description,
        videoUrl = videoUrl,
        presenter = presenter,
        isFeatured = featured,
        isPreviewStream = false,
        youtubeVideoId = youtubeVideoId,
        youtubeWatchUrl = youtubeWatchUrl,
    )
}

@Keep
@IgnoreExtraProperties
data class CloudCoverageDoc(
    @DocumentId val id: String = "",
    val title: String = "",
    val location: String = "",
    val reporter: String = "وحدة البث الخارجي SNG1080",
    val timestampLabel: String = "تغطية حديثة",
    val statusLabel: String = "تغطية ميدانية",
    val summary: String = "",
    val videoUrl: String = "",
    val youtubeUrl: String? = null,
    val liveField: Boolean = false,
    val createdByUid: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    fun toFieldCoverage(): FieldCoverage = FieldCoverage(
        id = id,
        title = title,
        location = location,
        reporter = reporter,
        timestamp = timestampLabel,
        statusLabel = statusLabel,
        summary = summary,
        videoUrl = videoUrl,
        isLiveField = liveField,
        youtubeUrl = youtubeUrl,
    )
}
