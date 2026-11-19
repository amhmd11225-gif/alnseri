package com.example.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.AlAnsariGoldCard
import com.example.ui.components.CrimsonPillBadge
import com.example.ui.components.EventsCalendarIcon
import com.example.ui.components.FieldCoverageCameraIcon
import com.example.ui.components.LiveBroadcastTvIcon
import com.example.ui.components.NewsNewspaperIcon
import com.example.ui.components.ServicesCommunityIcon
import com.example.ui.components.SocialBadgesRow
import com.example.ui.components.VideosProgramsIcon
import com.example.ui.navigation.AlAnsariRoutes
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    channelLogoUrl: String? = null,
    channelTitle: String = "قناة الأنصاري للإنتاج الفني",
    channelHandle: String = "@alansarie1",
) {
    val scrollState = rememberScrollState()

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
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top Hero Channel Logo synced from https://www.youtube.com/@alansarie1 with warm gold aura
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(174.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(142.dp),
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
                                .size(126.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.5.dp,
                                    brush = Brush.linearGradient(listOf(GoldBright, GoldPrimary)),
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
                                .height(138.dp),
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

            Spacer(modifier = Modifier.height(4.dp))

            // Section 1: Featured Full-Width Live Broadcast Card
            AlAnsariGoldCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigate(AlAnsariRoutes.LIVE) },
                testTag = "home_card_live",
                cornerRadius = 16.dp,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
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
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PureWhite,
                            ),
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        CrimsonPillBadge(
                            text = stringResource(R.string.live_subtitle),
                            horizontalPadding = 16.dp,
                            verticalPadding = 2.5.dp,
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    LiveBroadcastTvIcon(size = 56.dp)
                }
            }

            Spacer(modifier = Modifier.height(9.dp))

            // Row 1: Right = Field Coverage, Left = Videos & Programs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                HomeGridCard(
                    title = stringResource(R.string.coverage_title),
                    subtitle = stringResource(R.string.coverage_subtitle),
                    testTag = "home_card_coverage",
                    onClick = { onNavigate(AlAnsariRoutes.FIELD_COVERAGE) },
                    modifier = Modifier.weight(1f),
                    icon = { FieldCoverageCameraIcon() },
                )
                HomeGridCard(
                    title = stringResource(R.string.videos_title),
                    subtitle = stringResource(R.string.videos_subtitle),
                    testTag = "home_card_videos",
                    onClick = { onNavigate(AlAnsariRoutes.VIDEOS) },
                    modifier = Modifier.weight(1f),
                    icon = { VideosProgramsIcon() },
                )
            }

            Spacer(modifier = Modifier.height(9.dp))

            // Row 2: Right = News & Reports, Left = Events & Occasions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                HomeGridCard(
                    title = stringResource(R.string.news_title),
                    subtitle = stringResource(R.string.news_subtitle),
                    testTag = "home_card_news",
                    onClick = { onNavigate(AlAnsariRoutes.NEWS) },
                    modifier = Modifier.weight(1f),
                    icon = { NewsNewspaperIcon() },
                )
                HomeGridCard(
                    title = stringResource(R.string.events_title),
                    subtitle = stringResource(R.string.events_subtitle),
                    testTag = "home_card_events",
                    onClick = { onNavigate(AlAnsariRoutes.EVENTS) },
                    modifier = Modifier.weight(1f),
                    icon = { EventsCalendarIcon() },
                )
            }

            Spacer(modifier = Modifier.height(9.dp))

            // Row 3: Right = Official Social Media, Left = All Channel Services
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                HomeGridCard(
                    title = stringResource(R.string.social_title),
                    subtitle = stringResource(R.string.social_subtitle),
                    testTag = "home_card_social",
                    onClick = { onNavigate(AlAnsariRoutes.SOCIAL) },
                    modifier = Modifier.weight(1f),
                    icon = {
                        Box(
                            modifier = Modifier.height(40.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            SocialBadgesRow()
                        }
                    },
                )
                HomeGridCard(
                    title = stringResource(R.string.services_title),
                    subtitle = stringResource(R.string.services_subtitle),
                    testTag = "home_card_services",
                    onClick = { onNavigate(AlAnsariRoutes.MORE) },
                    modifier = Modifier.weight(1f),
                    icon = { ServicesCommunityIcon() },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HomeGridCard(
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    AlAnsariGoldCard(
        modifier = modifier.height(116.dp),
        onClick = onClick,
        testTag = testTag,
        cornerRadius = 16.dp,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 9.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier.height(40.dp),
                contentAlignment = Alignment.Center,
            ) {
                icon()
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PureWhite,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            CrimsonPillBadge(
                text = subtitle,
                horizontalPadding = 12.dp,
                verticalPadding = 2.dp,
            )
        }
    }
}
