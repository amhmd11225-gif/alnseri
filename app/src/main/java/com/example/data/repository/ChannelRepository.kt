package com.example.data.repository

import com.example.data.local.ChannelConfigStore
import com.example.data.model.ChannelEvent
import com.example.data.model.ChannelServiceItem
import com.example.data.model.ChannelSettings
import com.example.data.model.EventStatus
import com.example.data.model.FieldCoverage
import com.example.data.model.LiveChannelSource
import com.example.data.model.NewsReport
import com.example.data.model.ProgramScheduleItem
import com.example.data.model.SocialLink
import com.example.data.model.SocialPlatform
import com.example.data.model.VideoProgram
import com.example.data.model.detectStreamProtocol
import com.example.data.model.protocolBadgeLabel
import com.example.data.remote.AlAnsariApiService
import com.example.data.remote.DashboardBundleResponse
import com.example.data.remote.YouTubeChannelFetcher
import com.example.data.remote.YouTubeChannelSnapshot
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ChannelRepository(
    val configStore: ChannelConfigStore,
) {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val youTubeFetcher = YouTubeChannelFetcher(okHttpClient)

    val settingsFlow: Flow<ChannelSettings> = configStore.settingsFlow
    val bookmarkedVideosFlow: Flow<Set<String>> = configStore.bookmarkedVideosFlow
    val bookmarkedNewsFlow: Flow<Set<String>> = configStore.bookmarkedNewsFlow
    val savedRequestsFlow: Flow<List<String>> = configStore.savedRequestsFlow

    fun liveSourcesFlow(): Flow<List<LiveChannelSource>> =
        configStore.settingsFlow.combine(configStore.customVideoUrlMapFlow) { settings, _ ->
            buildLiveSources(settings)
        }

    fun customVideoOverridesFlow(): Flow<Map<String, String>> = configStore.customVideoUrlMapFlow

    fun socialLinksFlow(): Flow<List<SocialLink>> =
        configStore.settingsFlow.combine(configStore.bookmarkedNewsFlow) { settings, _ ->
            listOf(
                SocialLink(
                    id = "yt",
                    platform = SocialPlatform.YOUTUBE,
                    title = "يوتيوب قناة الأنصاري",
                    handle = settings.youtubeHandle,
                    url = settings.youtubeUrl,
                    followersLabel = "أكثر من 180 ألف مشترك • البث والأرشيف",
                ),
                SocialLink(
                    id = "tg",
                    platform = SocialPlatform.TELEGRAM,
                    title = "تيليجرام البث والأخبار",
                    handle = "@SNG1080",
                    url = settings.telegramUrl,
                    followersLabel = "تغطيات SNG1080 والإعلانات الفورية",
                ),
                SocialLink(
                    id = "fb",
                    platform = SocialPlatform.FACEBOOK,
                    title = "فيسبوك الرسمي",
                    handle = "al.ansar.live",
                    url = settings.facebookUrl,
                    followersLabel = "البث المباشر والتغطيات اليومية",
                ),
                SocialLink(
                    id = "tt",
                    platform = SocialPlatform.TIKTOK,
                    title = "تيك توك",
                    handle = settings.youtubeHandle,
                    url = settings.tiktokUrl,
                    followersLabel = "مقاطع قصيرة ولقطات ميدانية",
                ),
                SocialLink(
                    id = "ig",
                    platform = SocialPlatform.INSTAGRAM,
                    title = "إنستغرام",
                    handle = settings.youtubeHandle,
                    url = settings.websiteUrl,
                    followersLabel = "أبرز الصور والفعاليات",
                ),
            )
        }

    suspend fun fetchYouTubeChannelSnapshot(
        channelId: String,
        handle: String,
    ): Result<YouTubeChannelSnapshot> {
        return youTubeFetcher.fetchChannel(channelId = channelId, handle = handle)
    }

    suspend fun syncWithRemoteDashboard(baseUrl: String): Result<DashboardBundleResponse> {
        return runCatching {
            val normalized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            val retrofit = Retrofit.Builder()
                .baseUrl(normalized)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            val api = retrofit.create(AlAnsariApiService::class.java)
            val bundle = api.fetchChannelBundle()
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            configStore.updateCmsApiUrl(normalized, "تمت المزامنة: $timestamp")
            bundle.liveSources.firstOrNull()?.let { primary ->
                configStore.updateLiveUrls(
                    primaryUrl = primary.streamUrl,
                    backupUrl = bundle.liveSources.getOrNull(1)?.streamUrl ?: ChannelConfigStore.OPEN_HLS_TEST_STREAM,
                )
            }
            bundle
        }
    }

    fun buildLiveSources(settings: ChannelSettings): List<LiveChannelSource> {
        val list = mutableListOf<LiveChannelSource>()
        if (settings.primaryLiveUrl.isNotBlank()) {
            val protocol = detectStreamProtocol(settings.primaryLiveUrl)
            val protocolLabel = protocolBadgeLabel(protocol)
            list.add(
                LiveChannelSource(
                    id = "official_primary",
                    title = settings.liveTitle.ifBlank { "قناة الأنصاري للإنتاج الفني – البث المباشر" },
                    subtitle = "البث الرسمي المباشر لقناة الأنصاري ($protocolLabel)",
                    qualityLabel = protocolLabel,
                    streamUrl = settings.primaryLiveUrl,
                    isConfiguredOfficial = true,
                    currentProgram = "البث المباشر للفعاليات والمواكب",
                )
            )
        }
        val backupUrl = settings.backupLiveUrl.ifBlank { ChannelConfigStore.OPEN_HLS_TEST_STREAM }
        val backupProtocolLabel = protocolBadgeLabel(detectStreamProtocol(backupUrl))
        list.add(
            LiveChannelSource(
                id = "technical_preview",
                title = if (settings.primaryLiveUrl.isBlank()) {
                    "البث التجريبي لقناة الأنصاري"
                } else {
                    "البث الاحتياطي للقناة"
                },
                subtitle = if (settings.primaryLiveUrl.isBlank()) {
                    "جاري عرض البث التجريبي ريثما يبدأ البث الرسمي المباشر"
                } else {
                    "مسار بث بديل ($backupProtocolLabel) لضمان استمرارية المشاهدة"
                },
                qualityLabel = backupProtocolLabel,
                streamUrl = backupUrl,
                isConfiguredOfficial = false,
                currentProgram = "القناة الفضائية – البث المتكيف",
            )
        )
        return list
    }

    val scheduleItems: List<ProgramScheduleItem> = listOf(
        ProgramScheduleItem("s1", "04:00 عصراً", "تغطية المواكب الحسينية – سوق الشيوخ", "وحدة البث الخارجي SNG1080", "تغطيات خاصة", false),
        ProgramScheduleItem("s2", "06:30 مساءً", "المجلس الحسيني المركزي والمحاضرات الدينية", "الشيخ حازم الباوي ونخبة الخطباء", "مجالس ومحاضرات", true),
        ProgramScheduleItem("s3", "08:30 مساءً", "موكب شهيد الجمعة الحسيني – النقل المباشر", "فريق قناة الأنصاري (@alansarie1)", "تغطيات خاصة", false),
        ProgramScheduleItem("s4", "10:00 مساءً", "مراسم التشابيه والملاحم التاريخية", "قسم الإنتاج الفني", "تشابيه ومسرح", false),
    )

    val defaultVideos: List<VideoProgram> = listOf(
        VideoProgram(
            id = "v1",
            title = "تغطية خاصة: مسيرة موكب شهيد الجمعة الحسيني – سوق الشيوخ",
            category = "تغطيات خاصة",
            duration = "يوتيوب",
            publishedAt = "من إصدارات @alansarie1",
            description = "تغطية ميدانية شاملة لمراسم العزاء والمواكب الحسينية في قضاء سوق الشيوخ بمحافظة ذي قار من إنتاج قناة الأنصاري للإنتاج الفني.",
            videoUrl = "",
            presenter = "قناة الأنصاري للإنتاج الفني",
            isFeatured = true,
            isPreviewStream = false,
            youtubeWatchUrl = "https://www.youtube.com/@alansarie1",
        ),
        VideoProgram(
            id = "v2",
            title = "مجالس ومحاضرات الشيخ حازم الباوي – الليالي الفاطمية",
            category = "مجالس ومحاضرات",
            duration = "يوتيوب",
            publishedAt = "من إصدارات @alansarie1",
            description = "محاضرة دينية وتوجيهية من سلسلة المجالس التي تنقلها عدسة قناة الأنصاري للإنتاج الفني.",
            videoUrl = "",
            presenter = "الشيخ حازم الباوي",
            isPreviewStream = false,
            youtubeWatchUrl = "https://www.youtube.com/@alansarie1",
        ),
        VideoProgram(
            id = "v3",
            title = "تشابيه واقعة الطف والمشاهد التاريخية الكبرى",
            category = "تشابيه ومسرح",
            duration = "يوتيوب",
            publishedAt = "من إصدارات @alansarie1",
            description = "توثيق مرئي احترافي لمراسم التشابيه والأعمال المسرحية الحسينية في محافظة ذي قار.",
            videoUrl = "",
            presenter = "فريق الإنتاج الفني",
            isPreviewStream = false,
            youtubeWatchUrl = "https://www.youtube.com/@alansarie1",
        ),
        VideoProgram(
            id = "v4",
            title = "موكب العقيلة والمسيرات الأربعينية نحو كربلاء المقدسة",
            category = "تغطيات خاصة",
            duration = "يوتيوب",
            publishedAt = "من إصدارات @alansarie1",
            description = "مواكبة مستمرة لحركة الزائرين والخدمات الحسينية على طريق المحافظات الجنوبية نحو كربلاء.",
            videoUrl = "",
            presenter = "وحدة البث الخارجي SNG1080",
            isPreviewStream = false,
            youtubeWatchUrl = "https://www.youtube.com/@alansarie1",
        ),
        VideoProgram(
            id = "v5",
            title = "قصائد ومراثي حسينية – قضاء الجبايش وسوق الشيوخ",
            category = "قصائد ومراثي",
            duration = "يوتيوب",
            publishedAt = "من إصدارات @alansarie1",
            description = "مختارات من القصائد والمجالس المنبرية المسجلة بعدسة قناة الأنصاري للإنتاج الفني.",
            videoUrl = "",
            presenter = "قناة الأنصاري للإنتاج الفني",
            isPreviewStream = false,
            youtubeWatchUrl = "https://www.youtube.com/@alansarie1",
        ),
    )

    val fieldCoverages: List<FieldCoverage> = listOf(
        FieldCoverage(
            id = "fc1",
            title = "التغطية الميدانية لمراسم موكب شهيد الجمعة الحسيني",
            location = "سوق الشيوخ",
            reporter = "وحدة البث الخارجي SNG1080",
            timestamp = "بث متواصل",
            statusLabel = "بث ميداني",
            summary = "نقل وتوثيق فعاليات المواكب الحسينية والمجالس التربوية في قضاء سوق الشيوخ بمحافظة ذي قار.",
            videoUrl = ChannelConfigStore.OPEN_HLS_TEST_STREAM,
            isLiveField = true,
            youtubeUrl = "https://www.youtube.com/@alansarie1/streams",
        ),
        FieldCoverage(
            id = "fc2",
            title = "جولة ميدانية وتوثيق الفعاليات الدينية والثقافية",
            location = "الجبايش",
            reporter = "فريق قناة الأنصاري – ذي قار",
            timestamp = "تغطية حديثة",
            statusLabel = "تقرير ميداني",
            summary = "رصد للمجالس الحسينية والمهرجانات الإنشادية والشعرية في قضاء الجبايش.",
            videoUrl = ChannelConfigStore.OPEN_VOD_TEST_STREAM,
            isLiveField = false,
            youtubeUrl = "https://www.youtube.com/@alansarie1",
        ),
        FieldCoverage(
            id = "fc3",
            title = "تغطية مسيرة الأربعين والمواكب الخدمية على الطريق",
            location = "كربلاء المقدسة",
            reporter = "موفدو قناة الأنصاري",
            timestamp = "أرشيف الزيارات",
            statusLabel = "تغطية خاصة",
            summary = "مواكبة حشود الزائرين والخدمات الميدانية من ذي قار وصولاً إلى العتبات المقدسة في كربلاء.",
            videoUrl = ChannelConfigStore.OPEN_VOD_TEST_STREAM,
            isLiveField = false,
            youtubeUrl = "https://www.youtube.com/@alansarie1",
        ),
        FieldCoverage(
            id = "fc4",
            title = "توثيق التشابيه والندوات الثقافية المركزية",
            location = "ذي قار",
            reporter = "قسم الإنتاج الفني",
            timestamp = "تغطية مصورة",
            statusLabel = "تقرير مصور",
            summary = "توثيق احترافي للأعمال المسرحية والتشابيه الحسينية بمشاركة الكوادر الفنية المحلية.",
            videoUrl = ChannelConfigStore.OPEN_VOD_TEST_STREAM,
            isLiveField = false,
            youtubeUrl = "https://www.youtube.com/@alansarie1",
        ),
    )

    val events: List<ChannelEvent> = listOf(
        ChannelEvent(
            id = "ev1",
            title = "المجلس الحسيني الأسبوعي لموكب شهيد الجمعة",
            hijriDate = "كل ليلة جمعة",
            gregorianDate = "البث المباشر @alansarie1",
            timeLabel = "07:30 مساءً بتوقيت بغداد",
            location = "قضاء سوق الشيوخ – محافظة ذي قار",
            status = EventStatus.ONGOING,
            description = "تغطية مباشرة عبر قناة الأنصاري للإنتاج الفني على يوتيوب ومنصات التواصل الرسمية.",
            startTimeMillis = System.currentTimeMillis() + 86_400_000L,
        ),
        ChannelEvent(
            id = "ev2",
            title = "موسم المجالس والمحاضرات الدينية والليالي الفاطمية",
            hijriDate = "جمادى الآخرة 1448 هـ",
            gregorianDate = "نوفمبر – ديسمبر 2026",
            timeLabel = "08:00 مساءً",
            location = "سوق الشيوخ والجبايش – ذي قار",
            status = EventStatus.UPCOMING,
            description = "سلسلة محاضرات ومجالس عزاء بمشاركة خطباء المنبر الحسيني والرواديد.",
            startTimeMillis = System.currentTimeMillis() + 5 * 86_400_000L,
        ),
        ChannelEvent(
            id = "ev3",
            title = "مهرجان القصيدة الحسينية والشعر الشعبي والفصيح",
            hijriDate = "رجب 1448 هـ",
            gregorianDate = "ديسمبر 2026",
            timeLabel = "06:30 مساءً",
            location = "المركز الثقافي – ذي قار",
            status = EventStatus.UPCOMING,
            description = "نقل وتوثيق فعاليات المهرجان الشعري السنوي عبر كوادر قناة الأنصاري للإنتاج الفني.",
            startTimeMillis = System.currentTimeMillis() + 14 * 86_400_000L,
        ),
        ChannelEvent(
            id = "ev4",
            title = "أرشيف تشابيه واقعة الطف ومسيرة الأربعين",
            hijriDate = "محرم وصفر 1448 هـ",
            gregorianDate = "توثيق 2026",
            timeLabel = "مؤرشف على @alansarie1",
            location = "ذي قار وكربلاء المقدسة",
            status = EventStatus.ARCHIVED,
            description = "الأرشيف الكامل للتغطيات الميدانية والمجالس والمواكب على قناة اليوتيوب الرسمية.",
            startTimeMillis = System.currentTimeMillis() - 10 * 86_400_000L,
        ),
    )

    val newsReports: List<NewsReport> = listOf(
        NewsReport(
            id = "n1",
            title = "قناة الأنصاري للإنتاج الفني (@alansarie1) تتجاوز 180 ألف مشترك مع تحديث التغطيات المباشرة",
            category = "أخبار القناة",
            summary = "تواصل كوادر قناة الأنصاري للإنتاج الفني توثيق وبث الفعاليات الدينية والمواكب الحسينية في ذي قار وسوق الشيوخ والجبايش بدقة عالية.",
            fullBody = "تواصل قناة الأنصاري للإنتاج الفني مسيرتها الإعلامية في توثيق ونقل الشعائر والمجالس الحسينية والمهرجانات الثقافية في محافظة ذي قار وأقضية سوق الشيوخ والجبايش، وصولاً إلى مسيرات الأربعين في كربلاء المقدسة.\n\nيرتبط هذا التطبيق مباشرة بالقناة الرسمية (@alansarie1) لعرض أحدث الإصدارات المرئية والشعار الرسمي، مع دعم البث المباشر ومصادر التطوير المستقبلية عبر لوحة التحكم المركزية.",
            publishedAt = "حديث",
            readingTime = "دقيقتان",
            author = "إدارة قناة الأنصاري (@alansarie1)",
            isBreaking = true,
        ),
        NewsReport(
            id = "n2",
            title = "تجهيز وحدة البث الخارجي SNG1080 لتغطية المجالس والزيارات في ذي قار",
            category = "تقارير ميدانية",
            summary = "استعدادات فنية ولوجستية لنقل المجالس والمحاضرات المباشرة بجودة عالية عبر يوتيوب وتيليجرام وفيسبوك.",
            fullBody = "أتمت الفرق الهندسية والتصويرية في قناة الأنصاري للإنتاج الفني تجهيز وحدات البث الخارجي (SNG1080) لتأمين النقل المباشر لمجالس العزاء والندوات الدينية في سوق الشيوخ والجبايش والناصرية.\n\nويمكن للمشاهدين متابعة التغطيات الفورية عبر قناة اليوتيوب @alansarie1 أو قناة التيليجرام الرسمية t.me/SNG1080.",
            publishedAt = "اليوم",
            readingTime = "3 دقائق",
            author = "فريق البث الخارجي SNG1080",
            isBreaking = true,
        ),
        NewsReport(
            id = "n3",
            title = "توثيق الأعمال المسرحية وتشابيه واقعة الطف في أرشيف القناة",
            category = "ثقافية",
            summary = "إتاحة باقة واسعة من التغطيات الدرامية والتاريخية والمحاضرات التربوية للمشاهدين.",
            fullBody = "يعمل قسم الإنتاج والمونتاج في قناة الأنصاري على أرشفة وتقديم أعمال التشابيه والقصائد المنبرية ومحاضرات الخطباء بصورة منظمة تتيح للمشاهد الوصول السريع إليها في أي وقت.",
            publishedAt = "أمس",
            readingTime = "دقيقتان",
            author = "قسم الإنتاج الفني",
            isBreaking = false,
        ),
    )

    val servicesList: List<ChannelServiceItem> = listOf(
        ChannelServiceItem(
            id = "srv_booking",
            title = "طلب حجز تغطية وبث مباشر",
            subtitle = "احجز فريق التصوير ووحدة البث الخارجي SNG1080 لتغطية المواكب والمجالس",
            actionLabel = "حجز الآن",
            routeTarget = "booking_request",
        ),
        ChannelServiceItem(
            id = "srv_saved",
            title = "المحفوظات والمفضلة",
            subtitle = "تصفح الفيديوهات والتغطيات والأخبار التي قمت بحفظها",
            actionLabel = "عرض المحفوظات",
            routeTarget = "saved_items",
        ),
        ChannelServiceItem(
            id = "srv_schedule",
            title = "مواعيد البث والفعاليات",
            subtitle = "جدول المجالس والمواكب والبث المباشر لقناة الأنصاري",
            actionLabel = "الجدول",
            routeTarget = "live",
        ),
    )
}
