package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object YouTubeStreamResolver {

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .build()
    }

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    /**
     * Resolves a direct playable HLS/MP4 stream URL for a public YouTube video
     * without requiring a Google/YouTube login. Returns null if resolution fails
     * so the UI can cleanly fall back to the privacy-enhanced embed player.
     */
    suspend fun resolveDirectStreamUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        val cleanId = videoId.trim().replace(Regex("[^a-zA-Z0-9_-]"), "")
        if (cleanId.isBlank()) return@withContext null

        withTimeoutOrNull(10_000L) {
            tryInnerTubeIos(cleanId)
                ?: tryInnerTubeAndroidVr(cleanId)
                ?: tryInnerTubeTvEmbedded(cleanId)
                ?: tryPipedFallback(cleanId)
        }
    }

    private fun tryInnerTubeIos(videoId: String): String? {
        val body = JSONObject().apply {
            put("videoId", videoId)
            put("contentCheckOk", true)
            put("racyCheckOk", true)
            put(
                "context",
                JSONObject().put(
                    "client",
                    JSONObject().apply {
                        put("clientName", "IOS")
                        put("clientVersion", "19.45.4")
                        put("deviceMake", "Apple")
                        put("deviceModel", "iPhone16,2")
                        put("osName", "iOS")
                        put("osVersion", "18.1.0.22B83")
                        put("hl", "ar")
                        put("gl", "IQ")
                    },
                ),
            )
        }
        return callInnerTube(
            payload = body,
            userAgent = "com.google.ios.youtube/19.45.4 (iPhone16,2; U; CPU iOS 18_1_0 like Mac OS X;)",
            clientNameHeader = "5",
            clientVersionHeader = "19.45.4",
        )
    }

    private fun tryInnerTubeAndroidVr(videoId: String): String? {
        val body = JSONObject().apply {
            put("videoId", videoId)
            put("contentCheckOk", true)
            put("racyCheckOk", true)
            put(
                "context",
                JSONObject().put(
                    "client",
                    JSONObject().apply {
                        put("clientName", "ANDROID_VR")
                        put("clientVersion", "1.60.19")
                        put("deviceMake", "Oculus")
                        put("deviceModel", "Quest 3")
                        put("androidSdkVersion", 34)
                        put("osName", "Android")
                        put("osVersion", "14")
                        put("hl", "ar")
                        put("gl", "IQ")
                    },
                ),
            )
        }
        return callInnerTube(
            payload = body,
            userAgent = "com.google.android.apps.youtube.vr.oculus/1.60.19 (Linux; U; Android 14; Quest 3 Build/UKQ1.231207.002) gzip",
            clientNameHeader = "28",
            clientVersionHeader = "1.60.19",
        )
    }

    private fun tryInnerTubeTvEmbedded(videoId: String): String? {
        val body = JSONObject().apply {
            put("videoId", videoId)
            put("contentCheckOk", true)
            put("racyCheckOk", true)
            put(
                "context",
                JSONObject().apply {
                    put(
                        "client",
                        JSONObject().apply {
                            put("clientName", "TVHTML5_SIMPLY_EMBEDDED_PLAYER")
                            put("clientVersion", "2.0")
                            put("hl", "ar")
                            put("gl", "IQ")
                        },
                    )
                    put(
                        "thirdParty",
                        JSONObject().put("embedUrl", "https://www.youtube.com/"),
                    )
                },
            )
        }
        return callInnerTube(
            payload = body,
            userAgent = "Mozilla/5.0 (ChromiumStylePlatform) Cobalt/Version",
            clientNameHeader = "85",
            clientVersionHeader = "2.0",
        )
    }

    private fun callInnerTube(
        payload: JSONObject,
        userAgent: String,
        clientNameHeader: String,
        clientVersionHeader: String,
    ): String? {
        return runCatching {
            val request = Request.Builder()
                .url("https://www.youtube.com/youtubei/v1/player?prettyPrint=false")
                .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
                .header("User-Agent", userAgent)
                .header("X-YouTube-Client-Name", clientNameHeader)
                .header("X-YouTube-Client-Version", clientVersionHeader)
                .header("Origin", "https://www.youtube.com")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val raw = response.body?.string() ?: return null
                val json = JSONObject(raw)
                val status = json.optJSONObject("playabilityStatus")?.optString("status").orEmpty()
                if (status != "OK") return null

                val streamingData = json.optJSONObject("streamingData") ?: return null
                val hlsUrl = streamingData.optString("hlsManifestUrl")
                if (hlsUrl.startsWith("http")) {
                    return hlsUrl
                }

                val formats = streamingData.optJSONArray("formats") ?: return null
                var bestUrl: String? = null
                var bestHeight = -1
                for (i in 0 until formats.length()) {
                    val item = formats.optJSONObject(i) ?: continue
                    val directUrl = item.optString("url")
                    if (!directUrl.startsWith("http")) continue
                    val mime = item.optString("mimeType")
                    if (!mime.contains("mp4", ignoreCase = true)) continue
                    val height = item.optInt("height", 0)
                    if (height > bestHeight) {
                        bestHeight = height
                        bestUrl = directUrl
                    }
                }
                bestUrl
            }
        }.getOrNull()
    }

    private fun tryPipedFallback(videoId: String): String? {
        val hosts = listOf(
            "https://pipedapi.kavin.rocks",
            "https://pipedapi.adminforge.de",
            "https://api.piped.private.coffee",
        )
        for (base in hosts) {
            val resolved = runCatching {
                val request = Request.Builder()
                    .url("$base/streams/$videoId")
                    .header("User-Agent", "AlAnsariTV/1.0 (Android)")
                    .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@runCatching null
                    val raw = response.body?.string() ?: return@runCatching null
                    val json = JSONObject(raw)
                    val hls = json.optString("hls")
                    if (hls.startsWith("http")) return@runCatching hls
                    val streams = json.optJSONArray("videoStreams") ?: return@runCatching null
                    for (i in 0 until streams.length()) {
                        val item = streams.optJSONObject(i) ?: continue
                        val videoOnly = item.optBoolean("videoOnly", true)
                        val url = item.optString("url")
                        if (!videoOnly && url.startsWith("http")) {
                            return@runCatching url
                        }
                    }
                    null
                }
            }.getOrNull()
            if (!resolved.isNullOrBlank()) return resolved
        }
        return null
    }
}
