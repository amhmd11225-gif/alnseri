package com.example.data.remote

import android.util.Xml
import com.example.data.model.VideoProgram
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader

data class YouTubeChannelSnapshot(
    val channelId: String,
    val handle: String,
    val title: String,
    val description: String,
    val logoUrl: String?,
    val liveVideoId: String?,
    val videos: List<VideoProgram>,
)

class YouTubeChannelFetcher(
    private val okHttpClient: OkHttpClient,
) {
    suspend fun fetchChannel(
        channelId: String,
        handle: String,
    ): Result<YouTubeChannelSnapshot> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanHandle = handle.trim().let { if (it.startsWith("@")) it else "@$it" }
            val channelUrl = "https://www.youtube.com/$cleanHandle"
            val html = fetchText(channelUrl).orEmpty()

            val resolvedChannelId = extractChannelId(html) ?: channelId
            val logoUrl = extractMetaContent(html, "og:image")
                ?: extractMetaContent(html, "twitter:image")
            val channelTitle = extractMetaContent(html, "og:title")
                ?.takeIf { it.isNotBlank() }
                ?: "قناة الانصاري للانتاج الفني"
            val channelDesc = extractMetaContent(html, "og:description").orEmpty()

            val liveHtml = fetchText("$channelUrl/live").orEmpty()
            val liveVideoId = extractLiveVideoId(liveHtml)

            val feedUrl = "https://www.youtube.com/feeds/videos.xml?channel_id=$resolvedChannelId"
            val feedXml = fetchText(feedUrl).orEmpty()
            val videos = if (feedXml.isNotBlank()) parseAtomFeed(feedXml) else emptyList()

            YouTubeChannelSnapshot(
                channelId = resolvedChannelId,
                handle = cleanHandle,
                title = channelTitle,
                description = channelDesc,
                logoUrl = logoUrl,
                liveVideoId = liveVideoId,
                videos = videos,
            )
        }
    }

    private fun fetchText(url: String): String? {
        val request = Request.Builder()
            .url(url)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36",
            )
            .header("Accept-Language", "ar,en-US;q=0.9,en;q=0.8")
            .build()
        return okHttpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) response.body?.string() else null
        }
    }

    private fun extractMetaContent(html: String, propertyOrName: String): String? {
        val patterns = listOf(
            Regex("""<meta[^>]+(?:property|name)=["']$propertyOrName["'][^>]+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE),
            Regex("""<meta[^>]+content=["']([^"']+)["'][^>]+(?:property|name)=["']$propertyOrName["']""", RegexOption.IGNORE_CASE),
        )
        for (regex in patterns) {
            val match = regex.find(html)?.groupValues?.getOrNull(1)
            if (!match.isNullOrBlank()) return match
        }
        return null
    }

    private fun extractChannelId(html: String): String? {
        val regex = Regex("""(?:channel_id=|externalId["']\s*:\s*["'])(UC[a-zA-Z0-9_-]{20,})""")
        return regex.find(html)?.groupValues?.getOrNull(1)
    }

    private fun extractLiveVideoId(html: String): String? {
        if (!html.contains("\"isLive\":true") && !html.contains("\"isLiveNow\":true")) {
            return null
        }
        val canonicalRegex = Regex("""<link[^>]+rel=["']canonical["'][^>]+href=["']https://www\.youtube\.com/watch\?v=([a-zA-Z0-9_-]{11})["']""")
        return canonicalRegex.find(html)?.groupValues?.getOrNull(1)
    }

    private fun parseAtomFeed(xml: String): List<VideoProgram> {
        val list = mutableListOf<VideoProgram>()
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(StringReader(xml))

        var inEntry = false
        var videoId = ""
        var title = ""
        var published = ""
        var description = ""
        var thumbnail = ""
        var author = "قناة الأنصاري للإنتاج الفني"
        var currentTag = ""

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = parser.name.orEmpty()
                    if (currentTag == "entry") {
                        inEntry = true
                        videoId = ""
                        title = ""
                        published = ""
                        description = ""
                        thumbnail = ""
                    } else if (inEntry && currentTag == "media:thumbnail") {
                        thumbnail = parser.getAttributeValue(null, "url").orEmpty()
                    }
                }
                XmlPullParser.TEXT -> {
                    val text = parser.text.orEmpty().trim()
                    if (inEntry && text.isNotEmpty()) {
                        when (currentTag) {
                            "yt:videoId" -> videoId = text
                            "title" -> title = text
                            "published" -> published = formatIsoDate(text)
                            "media:description" -> description = text
                            "name" -> author = text
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "entry" && inEntry) {
                        if (videoId.isNotBlank() && title.isNotBlank()) {
                            val resolvedThumb = thumbnail.ifBlank {
                                "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
                            }
                            list.add(
                                VideoProgram(
                                    id = "yt_$videoId",
                                    title = title,
                                    category = classifyArabicVideo(title),
                                    duration = "يوتيوب",
                                    publishedAt = published.ifBlank { "حديث" },
                                    description = description.ifBlank { "من إصدارات قناة الأنصاري للإنتاج الفني (@alansarie1)." },
                                    videoUrl = "",
                                    thumbnailUrl = resolvedThumb,
                                    presenter = author,
                                    isFeatured = list.isEmpty(),
                                    isPreviewStream = false,
                                    youtubeVideoId = videoId,
                                    youtubeWatchUrl = "https://www.youtube.com/watch?v=$videoId",
                                ),
                            )
                        }
                        inEntry = false
                    }
                    currentTag = ""
                }
            }
            eventType = parser.next()
        }
        return list
    }

    private fun formatIsoDate(iso: String): String {
        return iso.substringBefore("T").ifBlank { iso }
    }

    private fun classifyArabicVideo(title: String): String {
        return when {
            title.contains("مباشر") || title.contains("تغطية") || title.contains("موكب") -> "تغطيات خاصة"
            title.contains("الشيخ") || title.contains("محاضرة") || title.contains("مجلس") -> "مجالس ومحاضرات"
            title.contains("تشابيه") || title.contains("واقعة") -> "تشابيه ومسرح"
            title.contains("قصيدة") || title.contains("لطمية") || title.contains("الرادود") -> "قصائد ومراثي"
            else -> "برامج وإصدارات"
        }
    }
}
