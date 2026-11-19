package com.example.ui.navigation

object AlAnsariRoutes {
    const val HOME = "home"
    const val VIDEOS = "videos"
    const val LIVE = "live"
    const val MORE = "more"
    const val FIELD_COVERAGE = "field_coverage"
    const val EVENTS = "events"
    const val NEWS = "news"
    const val SOCIAL = "social"
    const val BOOKING_REQUEST = "booking_request"
    const val ADMIN_PANEL = "admin_panel"
    const val VIDEO_PLAYER = "video_player/{videoId}"
    const val NEWS_DETAIL = "news_detail/{newsId}"

    fun videoPlayer(videoId: String): String = "video_player/$videoId"
    fun newsDetail(newsId: String): String = "news_detail/$newsId"
}
