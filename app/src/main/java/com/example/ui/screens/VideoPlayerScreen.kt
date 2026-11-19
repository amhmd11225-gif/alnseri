package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.AlAnsariGoldCard
import com.example.ui.components.AlAnsariMediaPlayer
import com.example.ui.components.AlAnsariSubTopBar
import com.example.ui.components.AlAnsariYouTubePlayer
import com.example.ui.components.CrimsonPillBadge
import com.example.ui.theme.GoldBright
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.viewmodel.AlAnsariUiState

@Composable
fun VideoPlayerScreen(
    videoId: String,
    uiState: AlAnsariUiState,
    onSelectVideo: (String) -> Unit,
    onToggleBookmark: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val video = uiState.videos.firstOrNull { it.id == videoId } ?: uiState.videos.firstOrNull()
    val isBookmarked = video != null && uiState.bookmarkedVideoIds.contains(video.id)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlAnsariSubTopBar(
            title = video?.title ?: "مشغل الفيديو",
            badgeText = video?.category ?: "البرامج",
            onBackClick = onBackClick,
            trailingAction = {
                if (video != null) {
                    Row {
                        IconButton(
                            onClick = { onToggleBookmark(video.id) },
                            modifier = Modifier.size(48.dp),
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = "حفظ",
                                tint = GoldBright,
                            )
                        }
                        IconButton(
                            onClick = {
                                val targetLink = video.videoUrl.ifBlank {
                                    video.youtubeWatchUrl ?: uiState.settings.youtubeUrl
                                }
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "${video.title}\n${uiState.channelTitle}\n$targetLink")
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "مشاركة الإصدار"))
                            },
                            modifier = Modifier.size(48.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "مشاركة",
                                tint = PureWhite,
                            )
                        }
                    }
                }
            },
        )

        if (video != null) {
            val resolvedYtId = remember(video.id, video.youtubeVideoId, video.youtubeWatchUrl) {
                video.youtubeVideoId?.takeIf { it.isNotBlank() }
                    ?: extractYouTubeVideoId(video.youtubeWatchUrl)
            }
            val watchUrl = video.youtubeWatchUrl ?: uiState.settings.youtubeUrl

            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    if (video.videoUrl.isNotBlank()) {
                        AlAnsariMediaPlayer(
                            streamUrl = video.videoUrl,
                            title = video.title,
                            isLiveStream = false,
                        )
                    } else {
                        AlAnsariYouTubePlayer(
                            videoId = resolvedYtId,
                            fallbackWebUrl = watchUrl,
                            title = video.title,
                        )
                    }
                }

                item {
                    AlAnsariGoldCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CrimsonPillBadge(text = video.category)
                            Text(
                                text = "${video.duration} • ${video.publishedAt}",
                                style = MaterialTheme.typography.labelMedium.copy(color = GoldBright),
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = video.title,
                            style = MaterialTheme.typography.titleLarge.copy(color = PureWhite),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = video.presenter,
                            style = MaterialTheme.typography.labelMedium.copy(color = GoldBright),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = video.description,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(watchUrl)))
                                }
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint = GoldBright,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "فتح الفيديو على قناة يوتيوب (${uiState.settings.youtubeHandle})",
                                color = GoldBright,
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "إصدارات أخرى من ${uiState.settings.youtubeHandle}",
                        style = MaterialTheme.typography.titleMedium.copy(color = GoldBright),
                    )
                }

                items(uiState.videos.filter { it.id != video.id }, key = { it.id }) { related ->
                    AlAnsariGoldCard(
                        onClick = { onSelectVideo(related.id) },
                        contentPadding = PaddingValues(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (!related.thumbnailUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = related.thumbnailUrl,
                                    contentDescription = related.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(width = 78.dp, height = 48.dp),
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = GoldBright,
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = related.title,
                                    style = MaterialTheme.typography.titleSmall.copy(color = PureWhite),
                                )
                                Text(
                                    text = "${related.category} • ${related.publishedAt}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun extractYouTubeVideoId(url: String?): String? {
    if (url.isNullOrBlank()) return null
    val patterns = listOf(
        Regex("""(?:v=|/)([a-zA-Z0-9_-]{11})(?:[?&/#]|$)"""),
        Regex("""youtu\.be/([a-zA-Z0-9_-]{11})"""),
        Regex("""embed/([a-zA-Z0-9_-]{11})"""),
        Regex("""shorts/([a-zA-Z0-9_-]{11})"""),
    )
    for (regex in patterns) {
        val found = regex.find(url)?.groupValues?.getOrNull(1)
        if (!found.isNullOrBlank()) return found
    }
    return null
}
