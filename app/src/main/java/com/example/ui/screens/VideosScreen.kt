package com.example.ui.screens

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.VideoProgram
import com.example.ui.components.AlAnsariGoldCard
import com.example.ui.components.AlAnsariSubTopBar
import com.example.ui.components.CrimsonPillBadge
import com.example.ui.components.GoldOutlineChip
import com.example.ui.theme.CardSurfaceTop
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CrimsonRedDark
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDarkBorder
import com.example.ui.theme.MutedGray
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.viewmodel.AlAnsariUiState

@Composable
fun VideosScreen(
    uiState: AlAnsariUiState,
    onVideoClick: (String) -> Unit,
    onToggleBookmark: (String) -> Unit,
    onRefreshYouTube: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categories = remember(uiState.videos) {
        listOf("الكل") + uiState.videos.map { it.category }.distinct()
    }
    var selectedCategory by remember { mutableStateOf("الكل") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredVideos = remember(uiState.videos, selectedCategory, searchQuery) {
        uiState.videos.filter { video ->
            val matchesCat = selectedCategory == "الكل" || video.category == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                video.title.contains(searchQuery, ignoreCase = true) ||
                video.presenter.contains(searchQuery, ignoreCase = true)
            matchesCat && matchesQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlAnsariSubTopBar(
            title = "الفيديوهات والبرامج",
            badgeText = "قناة ${uiState.settings.youtubeHandle}",
            onBackClick = onBackClick,
            trailingAction = {
                IconButton(
                    onClick = onRefreshYouTube,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("videos_refresh_youtube"),
                ) {
                    if (uiState.isRefreshingYouTube) {
                        CircularProgressIndicator(
                            color = GoldBright,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "تحديث من يوتيوب",
                            tint = GoldBright,
                        )
                    }
                }
            },
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
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ابحث في إصدارات ${uiState.settings.youtubeHandle}...", color = MutedGray) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "بحث",
                            tint = GoldBright,
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldBright,
                        unfocusedBorderColor = GoldDarkBorder,
                        focusedContainerColor = CardSurfaceTop,
                        unfocusedContainerColor = CardSurfaceTop,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("videos_search_input"),
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(categories) { category ->
                        GoldOutlineChip(
                            text = category,
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            testTag = "video_filter_$category",
                        )
                    }
                }
            }

            if (filteredVideos.isEmpty()) {
                item {
                    AlAnsariGoldCard {
                        Text(
                            text = "لا توجد برامج مطابقة لبحثك حالياً.",
                            style = MaterialTheme.typography.bodyLarge.copy(color = GoldBright),
                        )
                    }
                }
            } else {
                items(filteredVideos, key = { it.id }) { video ->
                    VideoProgramCard(
                        video = video,
                        isBookmarked = uiState.bookmarkedVideoIds.contains(video.id),
                        onClick = { onVideoClick(video.id) },
                        onToggleBookmark = { onToggleBookmark(video.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoProgramCard(
    video: VideoProgram,
    isBookmarked: Boolean,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit,
) {
    AlAnsariGoldCard(
        onClick = onClick,
        testTag = "video_card_${video.id}",
        contentPadding = PaddingValues(12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF1D1710), CrimsonRedDark.copy(alpha = 0.72f), ObsidianBlack),
                    ),
                )
                .border(0.8.dp, GoldDarkBorder, RoundedCornerShape(12.dp)),
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
                        .background(Color.Black.copy(alpha = 0.28f)),
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(CrimsonRed.copy(alpha = 0.92f))
                    .border(1.5.dp, GoldBright, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "تشغيل ${video.title}",
                    tint = PureWhite,
                    modifier = Modifier.size(30.dp),
                )
            }

            CrimsonPillBadge(
                text = video.category,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp),
                horizontalPadding = 10.dp,
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.80f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text = video.duration,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = GoldBright,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium.copy(color = PureWhite),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${video.presenter} • ${video.publishedAt}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                )
            }
            IconButton(
                onClick = onToggleBookmark,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = if (isBookmarked) "إزالة من المحفوظات" else "حفظ الفيديو",
                    tint = if (isBookmarked) GoldBright else MutedGray,
                )
            }
        }
    }
}
