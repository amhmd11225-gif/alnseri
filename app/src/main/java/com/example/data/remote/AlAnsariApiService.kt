package com.example.data.remote

import com.example.data.model.ChannelEvent
import com.example.data.model.FieldCoverage
import com.example.data.model.LiveChannelSource
import com.example.data.model.NewsReport
import com.example.data.model.SocialLink
import com.example.data.model.VideoProgram
import com.squareup.moshi.JsonClass
import retrofit2.http.GET

@JsonClass(generateAdapter = true)
data class DashboardBundleResponse(
    val liveSources: List<LiveChannelSource> = emptyList(),
    val videos: List<VideoProgram> = emptyList(),
    val coverages: List<FieldCoverage> = emptyList(),
    val events: List<ChannelEvent> = emptyList(),
    val news: List<NewsReport> = emptyList(),
    val socialLinks: List<SocialLink> = emptyList(),
)

/**
 * Future-ready CMS / Admin Dashboard API contract.
 * When a backend dashboard URL is configured in Stream & CMS Settings,
 * the repository can fetch live JSON updates from these endpoints.
 */
interface AlAnsariApiService {
    @GET("api/v1/channel/bundle")
    suspend fun fetchChannelBundle(): DashboardBundleResponse

    @GET("api/v1/live-streams")
    suspend fun fetchLiveStreams(): List<LiveChannelSource>

    @GET("api/v1/videos")
    suspend fun fetchVideos(): List<VideoProgram>

    @GET("api/v1/coverages")
    suspend fun fetchCoverages(): List<FieldCoverage>

    @GET("api/v1/events")
    suspend fun fetchEvents(): List<ChannelEvent>

    @GET("api/v1/news")
    suspend fun fetchNews(): List<NewsReport>
}
