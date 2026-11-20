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

    // Haqeebat Al-Mu'min routes
    const val QURAN = "quran"
    const val DUAS = "duas"
    const val ZIYARAT = "ziyarat"
    const val PRAYER_TIMES = "prayer_times"
    const val QIBLA = "qibla"
    const val TASBEEH = "tasbeeh"
    const val HIJRI_CALENDAR = "hijri_calendar"
    const val ISTIKHARA_FIQH = "istikhara_fiqh"

    fun videoPlayer(videoId: String): String = "video_player/$videoId"
    fun newsDetail(newsId: String): String = "news_detail/$newsId"
}
