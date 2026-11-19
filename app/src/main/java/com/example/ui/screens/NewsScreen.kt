package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AlAnsariGoldCard
import com.example.ui.components.AlAnsariSubTopBar
import com.example.ui.components.CrimsonPillBadge
import com.example.ui.components.GoldOutlineChip
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.GoldBright
import com.example.ui.theme.MutedGray
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.viewmodel.AlAnsariUiState

@Composable
fun NewsScreen(
    uiState: AlAnsariUiState,
    onNewsClick: (String) -> Unit,
    onToggleBookmark: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categories = listOf("الكل", "أخبار القناة", "تقارير ميدانية", "ثقافية", "بيانات رسمية")
    var selectedCategory by remember { mutableStateOf("الكل") }

    val breakingNews = uiState.newsReports.firstOrNull { it.isBreaking }
    val filteredNews = remember(uiState.newsReports, selectedCategory) {
        if (selectedCategory == "الكل") uiState.newsReports
        else uiState.newsReports.filter { it.category == selectedCategory }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlAnsariSubTopBar(
            title = "الأخبار والتقارير",
            badgeText = "غرفة الأخبار",
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
            if (breakingNews != null) {
                item {
                    AlAnsariGoldCard(
                        onClick = { onNewsClick(breakingNews.id) },
                        testTag = "news_breaking_banner",
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Campaign,
                                contentDescription = "خبر عاجل",
                                tint = CrimsonRed,
                                modifier = Modifier.size(22.dp),
                            )
                            CrimsonPillBadge(text = "خبر هام", horizontalPadding = 10.dp)
                            Text(
                                text = breakingNews.publishedAt,
                                style = MaterialTheme.typography.labelSmall.copy(color = GoldBright),
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = breakingNews.title,
                            style = MaterialTheme.typography.titleMedium.copy(color = GoldBright),
                        )
                    }
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        GoldOutlineChip(
                            text = cat,
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            testTag = "news_filter_$cat",
                        )
                    }
                }
            }

            items(filteredNews, key = { it.id }) { report ->
                val isBookmarked = uiState.bookmarkedNewsIds.contains(report.id)
                AlAnsariGoldCard(
                    onClick = { onNewsClick(report.id) },
                    testTag = "news_card_${report.id}",
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        CrimsonPillBadge(text = report.category, horizontalPadding = 10.dp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${report.publishedAt} • ${report.readingTime}",
                                style = MaterialTheme.typography.labelSmall.copy(color = MutedGray),
                            )
                            IconButton(
                                onClick = { onToggleBookmark(report.id) },
                                modifier = Modifier.size(48.dp),
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                    contentDescription = "حفظ الخبر",
                                    tint = if (isBookmarked) GoldBright else MutedGray,
                                )
                            }
                        }
                    }

                    Text(
                        text = report.title,
                        style = MaterialTheme.typography.titleMedium.copy(color = PureWhite),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = report.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "بقلم: ${report.author} • اضغط لقراءة التقرير كاملاً",
                        style = MaterialTheme.typography.labelMedium.copy(color = GoldBright, fontSize = 12.sp),
                    )
                }
            }
        }
    }
}
