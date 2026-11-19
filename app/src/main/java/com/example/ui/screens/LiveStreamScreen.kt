package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AlAnsariGoldCard
import com.example.ui.components.AlAnsariMediaPlayer
import com.example.ui.components.AlAnsariSubTopBar
import com.example.ui.components.AlAnsariYouTubePlayer
import com.example.ui.components.CrimsonPillBadge
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.GoldBright
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.viewmodel.AlAnsariUiState

@Composable
fun LiveStreamScreen(
    uiState: AlAnsariUiState,
    onSelectSource: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activeSource = uiState.liveSources.firstOrNull { it.id == uiState.selectedLiveSourceId }
        ?: uiState.liveSources.firstOrNull()
    var playYouTubeLiveInApp by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlAnsariSubTopBar(
            title = "البث المباشر",
            badgeText = uiState.settings.youtubeHandle,
            onBackClick = onBackClick,
        )

        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                if (playYouTubeLiveInApp && !uiState.youtubeLiveVideoId.isNullOrBlank()) {
                    AlAnsariYouTubePlayer(
                        videoId = uiState.youtubeLiveVideoId,
                        fallbackWebUrl = "https://www.youtube.com/${uiState.settings.youtubeHandle}/live",
                        title = "البث المباشر على يوتيوب (${uiState.settings.youtubeHandle})",
                    )
                } else {
                    AlAnsariMediaPlayer(
                        streamUrl = activeSource?.streamUrl.orEmpty(),
                        title = activeSource?.title ?: uiState.settings.liveTitle,
                        isLiveStream = true,
                    )
                }
            }

            // Direct YouTube Live card linked to @alansarie1/live
            item {
                val hasActiveYtLive = !uiState.youtubeLiveVideoId.isNullOrBlank()
                val liveYtUrl = if (hasActiveYtLive) {
                    "https://www.youtube.com/watch?v=${uiState.youtubeLiveVideoId}"
                } else {
                    "https://www.youtube.com/${uiState.settings.youtubeHandle}/live"
                }
                AlAnsariGoldCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (hasActiveYtLive) CrimsonRed else GoldBright),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (hasActiveYtLive) {
                                        "يوجد بث مباشر حالي على يوتيوب!"
                                    } else {
                                        "البث المباشر عبر يوتيوب (${uiState.settings.youtubeHandle})"
                                    },
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = GoldBright,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "للمشاهدة المباشرة للمجالس والمواكب على قناة ${uiState.settings.youtubeHandle}.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (hasActiveYtLive) {
                                    playYouTubeLiveInApp = !playYouTubeLiveInApp
                                } else {
                                    runCatching {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(liveYtUrl)))
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = PureWhite),
                            shape = RoundedCornerShape(50),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when {
                                    hasActiveYtLive && playYouTubeLiveInApp -> "مشغل القناة"
                                    hasActiveYtLive -> "تشغيل بالداخل"
                                    else -> "يوتيوب مباشر"
                                },
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
            }

            // Clean Viewer-Only Active Broadcast Info Card
            item {
                AlAnsariGoldCard(
                    contentPadding = PaddingValues(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeSource?.title ?: uiState.settings.liveTitle,
                                style = MaterialTheme.typography.titleMedium.copy(color = GoldBright),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activeSource?.subtitle.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        CrimsonPillBadge(
                            text = activeSource?.qualityLabel ?: "بث مباشر",
                            horizontalPadding = 12.dp,
                        )
                    }
                }
            }

            item {
                Text(
                    text = "جودات ومصادر البث المتاحة",
                    style = MaterialTheme.typography.titleMedium.copy(color = GoldBright),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            items(uiState.liveSources, key = { it.id }) { source ->
                val isSelected = !playYouTubeLiveInApp && source.id == activeSource?.id
                AlAnsariGoldCard(
                    onClick = {
                        playYouTubeLiveInApp = false
                        onSelectSource(source.id)
                    },
                    testTag = "live_source_${source.id}",
                    contentPadding = PaddingValues(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Filled.RadioButtonChecked else Icons.Filled.LiveTv,
                                contentDescription = source.title,
                                tint = if (isSelected) CrimsonRed else GoldBright,
                                modifier = Modifier.size(26.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = source.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = if (isSelected) GoldBright else PureWhite,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                )
                                Text(
                                    text = source.subtitle,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        CrimsonPillBadge(
                            text = source.qualityLabel,
                            horizontalPadding = 10.dp,
                        )
                    }
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = GoldBright,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "دليل التغطيات والبرامج اليومي",
                        style = MaterialTheme.typography.titleMedium.copy(color = GoldBright),
                    )
                }
            }

            items(uiState.scheduleItems, key = { it.id }) { item ->
                AlAnsariGoldCard(
                    contentPadding = PaddingValues(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.time,
                                    style = MaterialTheme.typography.labelMedium.copy(color = GoldBright),
                                )
                                if (item.isLiveNow) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(CrimsonRed),
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "يُعرض الآن",
                                        style = MaterialTheme.typography.labelSmall.copy(color = CrimsonRed),
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall.copy(color = PureWhite),
                            )
                            Text(
                                text = item.presenter,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                            )
                        }
                        CrimsonPillBadge(text = item.category, horizontalPadding = 10.dp)
                    }
                }
            }
        }
    }
}
