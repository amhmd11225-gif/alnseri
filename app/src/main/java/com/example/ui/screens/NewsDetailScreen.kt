package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AlAnsariGoldCard
import com.example.ui.components.AlAnsariSubTopBar
import com.example.ui.components.CrimsonPillBadge
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDarkBorder
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftWhite
import com.example.ui.viewmodel.AlAnsariUiState

@Composable
fun NewsDetailScreen(
    newsId: String,
    uiState: AlAnsariUiState,
    onToggleBookmark: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val report = uiState.newsReports.firstOrNull { it.id == newsId } ?: uiState.newsReports.firstOrNull()
    val isBookmarked = report != null && uiState.bookmarkedNewsIds.contains(report.id)
    var largeText by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlAnsariSubTopBar(
            title = "تفاصيل التقرير",
            badgeText = report?.category ?: "أخبار",
            onBackClick = onBackClick,
            trailingAction = {
                if (report != null) {
                    Row {
                        IconButton(
                            onClick = { largeText = !largeText },
                            modifier = Modifier.size(48.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FormatSize,
                                contentDescription = "تغيير حجم الخط",
                                tint = GoldBright,
                            )
                        }
                        IconButton(
                            onClick = { onToggleBookmark(report.id) },
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
                                val share = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "${report.title}\n\n${report.summary}\n— قناة الأنصاري الفضائية")
                                }
                                context.startActivity(Intent.createChooser(share, "مشاركة الخبر"))
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

        if (report != null) {
            Column(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
            ) {
                AlAnsariGoldCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CrimsonPillBadge(text = report.category)
                        Text(
                            text = "${report.publishedAt} • ${report.readingTime}",
                            style = MaterialTheme.typography.labelMedium.copy(color = GoldBright),
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = report.title,
                        style = MaterialTheme.typography.headlineMedium.copy(color = PureWhite),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "المصدر: ${report.author}",
                        style = MaterialTheme.typography.labelMedium.copy(color = GoldBright),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = GoldDarkBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = report.fullBody,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = SoftWhite,
                            fontSize = if (largeText) 19.sp else 16.sp,
                            lineHeight = if (largeText) 30.sp else 26.sp,
                        ),
                    )
                }
            }
        }
    }
}
