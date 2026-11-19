package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.AlAnsariGoldCard
import com.example.ui.components.AlAnsariMediaPlayer
import com.example.ui.components.AlAnsariSubTopBar
import com.example.ui.components.CrimsonPillBadge
import com.example.ui.theme.CardSurfaceTop
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CrimsonRedDark
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDarkBorder
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.viewmodel.AlAnsariUiState

@Composable
fun VideoPlayerScreen(
    videoId: String,
    uiState: AlAnsariUiState,
    onSelectVideo: (String) -> Unit,
    onToggleBookmark: (String) -> Unit,
    onUpdateVideoUrl: (String, String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val video = uiState.videos.firstOrNull { it.id == videoId } ?: uiState.videos.firstOrNull()
    val isBookmarked = video != null && uiState.bookmarkedVideoIds.contains(video.id)
    var showEditUrlDialog by remember { mutableStateOf(false) }

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
                            onClick = { showEditUrlDialog = true },
                            modifier = Modifier.size(48.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "تعديل رابط البث المباشر للفيديو",
                                tint = GoldBright,
                            )
                        }
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
                        val watchUrl = video.youtubeWatchUrl ?: uiState.settings.youtubeUrl
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF1D1710), CrimsonRedDark.copy(alpha = 0.7f), ObsidianBlack),
                                    ),
                                )
                                .border(1.2.dp, GoldPrimary, RoundedCornerShape(16.dp)),
                        ) {
                            if (!video.thumbnailUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = video.thumbnailUrl,
                                    contentDescription = video.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.56f)),
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Button(
                                    onClick = {
                                        runCatching {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(watchUrl)))
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CrimsonRed,
                                        contentColor = PureWhite,
                                    ),
                                    shape = RoundedCornerShape(50),
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "تشغيل من قناة يوتيوب (${uiState.settings.youtubeHandle})",
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { showEditUrlDialog = true },
                                    shape = RoundedCornerShape(50),
                                ) {
                                    Text(
                                        text = "أو إدخال رابط .mp4 / .m3u8 للتشغيل الداخلي",
                                        style = MaterialTheme.typography.labelMedium.copy(color = GoldBright),
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
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
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = GoldBright,
                                modifier = Modifier.size(28.dp),
                            )
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

    if (showEditUrlDialog && video != null) {
        var urlInput by remember(video.id) { mutableStateOf(video.videoUrl) }
        AlertDialog(
            onDismissRequest = { showEditUrlDialog = false },
            containerColor = CardSurfaceTop,
            title = {
                Text("رابط التشغيل المباشر للفيديو", style = MaterialTheme.typography.titleMedium.copy(color = GoldBright))
            },
            text = {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("رابط مباشر (.mp4 أو .m3u8)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldBright,
                        unfocusedBorderColor = GoldDarkBorder,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateVideoUrl(video.id, urlInput)
                        showEditUrlDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                ) {
                    Text("حفظ", color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditUrlDialog = false }) {
                    Text("إلغاء", color = GoldBright)
                }
            },
        )
    }
}
