package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.BuildConfig
import com.example.R
import com.example.ui.components.AlAnsari3DTileCard
import com.example.ui.components.AlAnsariGoldCard
import com.example.ui.components.CrimsonPillBadge
import com.example.ui.components.DuaHands3DIcon
import com.example.ui.components.EventsCalendarIcon
import com.example.ui.components.FieldCoverageCameraIcon
import com.example.ui.components.HijriCrescentCalendar3DIcon
import com.example.ui.components.HomeSectionHeader
import com.example.ui.components.IstikharaQuranStar3DIcon
import com.example.ui.components.LiveBroadcastTvIcon
import com.example.ui.components.NewsNewspaperIcon
import com.example.ui.components.PrayerTimesCrescent3DIcon
import com.example.ui.components.QiblaCompass3DIcon
import com.example.ui.components.QuranBook3DIcon
import com.example.ui.components.ServicesCommunityIcon
import com.example.ui.components.SocialBadgesRow
import com.example.ui.components.TasbeehBeads3DIcon
import com.example.ui.components.VideosProgramsIcon
import com.example.ui.components.ZiyaratDome3DIcon
import com.example.ui.navigation.AlAnsariRoutes
import com.example.ui.theme.CardSurfaceTop
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CrimsonRedBright
import com.example.ui.theme.CrimsonRedDark
import com.example.ui.theme.ElevatedDark
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSpecular
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    channelLogoUrl: String? = null,
    channelTitle: String = "قناة الأنصاري للإنتاج الفني",
    channelHandle: String = "@alansarie1",
    liveTitle: String = "",
    homeAnnouncement: String = "",
    latestAppVersion: String = "",
    appUpdateUrl: String = "",
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .statusBarsPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top Hero Channel Logo synced from https://www.youtube.com/@alansarie1 with warm gold aura
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(GoldGlow, Color.Transparent),
                                ),
                            ),
                    )
                    if (!channelLogoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = channelLogoUrl,
                            contentDescription = channelTitle,
                            placeholder = painterResource(id = R.drawable.logo_al_ansari),
                            error = painterResource(id = R.drawable.logo_al_ansari),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.5.dp,
                                    brush = Brush.linearGradient(listOf(GoldSpecular, GoldBright, GoldPrimary)),
                                    shape = CircleShape,
                                )
                                .testTag("home_youtube_logo"),
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.logo_al_ansari),
                            contentDescription = channelTitle,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth(0.84f)
                                .height(116.dp),
                        )
                    }
                }
                Text(
                    text = "$channelTitle • $channelHandle",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = GoldBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            val hasAppUpdate = latestAppVersion.isNotBlank() &&
                latestAppVersion.trim() != BuildConfig.VERSION_NAME &&
                appUpdateUrl.isNotBlank()

            if (hasAppUpdate) {
                AlAnsariGoldCard(
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "home_app_update_card",
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.SystemUpdate, contentDescription = null, tint = GoldBright, modifier = Modifier.size(26.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "تتوفر نسخة جديدة ($latestAppVersion)",
                                    style = MaterialTheme.typography.titleSmall.copy(color = GoldBright, fontWeight = FontWeight.Bold),
                                )
                                if (homeAnnouncement.isNotBlank()) {
                                    Text(
                                        text = homeAnnouncement,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(appUpdateUrl.trim())))
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = PureWhite),
                            shape = RoundedCornerShape(50),
                        ) {
                            Text("تحديث", fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            } else if (homeAnnouncement.isNotBlank()) {
                AlAnsariGoldCard(
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "home_live_announcement_card",
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.NotificationsActive, contentDescription = "إعلان القناة", tint = GoldBright, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = homeAnnouncement,
                            style = MaterialTheme.typography.bodyMedium.copy(color = PureWhite, fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Featured Full-Width 3D Live Broadcast Card
            AlAnsariGoldCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigate(AlAnsariRoutes.LIVE) },
                testTag = "home_card_live",
                cornerRadius = 18.dp,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = stringResource(R.string.live_title),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 21.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PureWhite,
                            ),
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        CrimsonPillBadge(
                            text = liveTitle.ifBlank { stringResource(R.string.live_subtitle) },
                            horizontalPadding = 16.dp,
                            verticalPadding = 3.dp,
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    LiveBroadcastTvIcon(size = 52.dp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Prominent Direct Booking CTA Card on the Home Screen
            AlAnsariGoldCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigate(AlAnsariRoutes.BOOKING_REQUEST) },
                testTag = "home_card_booking",
                cornerRadius = 18.dp,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.booking_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 17.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GoldBright,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        CrimsonPillBadge(
                            text = stringResource(R.string.booking_subtitle),
                            horizontalPadding = 14.dp,
                            verticalPadding = 3.dp,
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(ElevatedDark, CardSurfaceTop),
                                ),
                            )
                            .border(1.4.dp, GoldBright, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Campaign,
                            contentDescription = stringResource(R.string.booking_title),
                            tint = GoldSpecular,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            HomeSectionHeader(
                title = "أقسام قناة الأنصاري",
                badge = "بث وتغطيات",
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Channel Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AlAnsari3DTileCard(
                    title = stringResource(R.string.coverage_title),
                    subtitle = stringResource(R.string.coverage_subtitle),
                    testTag = "home_card_coverage",
                    onClick = { onNavigate(AlAnsariRoutes.FIELD_COVERAGE) },
                    modifier = Modifier.weight(1f),
                    icon = { FieldCoverageCameraIcon() },
                )
                AlAnsari3DTileCard(
                    title = stringResource(R.string.videos_title),
                    subtitle = stringResource(R.string.videos_subtitle),
                    testTag = "home_card_videos",
                    onClick = { onNavigate(AlAnsariRoutes.VIDEOS) },
                    modifier = Modifier.weight(1f),
                    icon = { VideosProgramsIcon() },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Channel Row 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AlAnsari3DTileCard(
                    title = stringResource(R.string.news_title),
                    subtitle = stringResource(R.string.news_subtitle),
                    testTag = "home_card_news",
                    onClick = { onNavigate(AlAnsariRoutes.NEWS) },
                    modifier = Modifier.weight(1f),
                    icon = { NewsNewspaperIcon() },
                )
                AlAnsari3DTileCard(
                    title = stringResource(R.string.events_title),
                    subtitle = stringResource(R.string.events_subtitle),
                    testTag = "home_card_events",
                    onClick = { onNavigate(AlAnsariRoutes.EVENTS) },
                    modifier = Modifier.weight(1f),
                    icon = { EventsCalendarIcon() },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Channel Row 3
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AlAnsari3DTileCard(
                    title = stringResource(R.string.social_title),
                    subtitle = stringResource(R.string.social_subtitle),
                    testTag = "home_card_social",
                    onClick = { onNavigate(AlAnsariRoutes.SOCIAL) },
                    modifier = Modifier.weight(1f),
                    icon = { SocialBadgesRow() },
                )
                AlAnsari3DTileCard(
                    title = stringResource(R.string.services_title),
                    subtitle = stringResource(R.string.services_subtitle),
                    testTag = "home_card_services",
                    onClick = { onNavigate(AlAnsariRoutes.MORE) },
                    modifier = Modifier.weight(1f),
                    icon = { ServicesCommunityIcon() },
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Haqeebat Al-Mu'min Full 3D Grid Section
            HomeSectionHeader(
                title = stringResource(R.string.haqeebat_section_title),
                badge = "8 خدمات إيمانية",
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Haqeebat Row 1: القرآن الكريم + الأدعية
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AlAnsari3DTileCard(
                    title = stringResource(R.string.quran_title),
                    subtitle = stringResource(R.string.quran_subtitle),
                    testTag = "home_card_quran",
                    onClick = { onNavigate(AlAnsariRoutes.QURAN) },
                    modifier = Modifier.weight(1f),
                    badgeColorTop = EmeraldAccent,
                    badgeColorBottom = Color(0xFF10613B),
                    icon = { QuranBook3DIcon() },
                )
                AlAnsari3DTileCard(
                    title = stringResource(R.string.duas_title),
                    subtitle = stringResource(R.string.duas_subtitle),
                    testTag = "home_card_duas",
                    onClick = { onNavigate(AlAnsariRoutes.DUAS) },
                    modifier = Modifier.weight(1f),
                    badgeColorTop = CrimsonRedBright,
                    badgeColorBottom = CrimsonRedDark,
                    icon = { DuaHands3DIcon() },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Haqeebat Row 2: الزيارات + أوقات الصلاة
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AlAnsari3DTileCard(
                    title = stringResource(R.string.ziyarat_title),
                    subtitle = stringResource(R.string.ziyarat_subtitle),
                    testTag = "home_card_ziyarat",
                    onClick = { onNavigate(AlAnsariRoutes.ZIYARAT) },
                    modifier = Modifier.weight(1f),
                    badgeColorTop = CrimsonRedBright,
                    badgeColorBottom = CrimsonRedDark,
                    icon = { ZiyaratDome3DIcon() },
                )
                AlAnsari3DTileCard(
                    title = stringResource(R.string.prayer_times_title),
                    subtitle = stringResource(R.string.prayer_times_subtitle),
                    testTag = "home_card_prayer_times",
                    onClick = { onNavigate(AlAnsariRoutes.PRAYER_TIMES) },
                    modifier = Modifier.weight(1f),
                    badgeColorTop = EmeraldAccent,
                    badgeColorBottom = Color(0xFF10613B),
                    icon = { PrayerTimesCrescent3DIcon() },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Haqeebat Row 3: اتجاه القبلة + المسبحة
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AlAnsari3DTileCard(
                    title = stringResource(R.string.qibla_title),
                    subtitle = stringResource(R.string.qibla_subtitle),
                    testTag = "home_card_qibla",
                    onClick = { onNavigate(AlAnsariRoutes.QIBLA) },
                    modifier = Modifier.weight(1f),
                    badgeColorTop = CrimsonRed,
                    badgeColorBottom = CrimsonRedDark,
                    icon = { QiblaCompass3DIcon() },
                )
                AlAnsari3DTileCard(
                    title = stringResource(R.string.tasbeeh_title),
                    subtitle = stringResource(R.string.tasbeeh_subtitle),
                    testTag = "home_card_tasbeeh",
                    onClick = { onNavigate(AlAnsariRoutes.TASBEEH) },
                    modifier = Modifier.weight(1f),
                    badgeColorTop = EmeraldAccent,
                    badgeColorBottom = Color(0xFF10613B),
                    icon = { TasbeehBeads3DIcon() },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Haqeebat Row 4: التقويم الهجري + الاستخارة والمسائل
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AlAnsari3DTileCard(
                    title = stringResource(R.string.hijri_title),
                    subtitle = stringResource(R.string.hijri_subtitle),
                    testTag = "home_card_hijri_calendar",
                    onClick = { onNavigate(AlAnsariRoutes.HIJRI_CALENDAR) },
                    modifier = Modifier.weight(1f),
                    badgeColorTop = CrimsonRedBright,
                    badgeColorBottom = CrimsonRedDark,
                    icon = { HijriCrescentCalendar3DIcon() },
                )
                AlAnsari3DTileCard(
                    title = stringResource(R.string.istikhara_title),
                    subtitle = stringResource(R.string.istikhara_subtitle),
                    testTag = "home_card_istikhara",
                    onClick = { onNavigate(AlAnsariRoutes.ISTIKHARA_FIQH) },
                    modifier = Modifier.weight(1f),
                    badgeColorTop = EmeraldAccent,
                    badgeColorBottom = Color(0xFF10613B),
                    icon = { IstikharaQuranStar3DIcon() },
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

