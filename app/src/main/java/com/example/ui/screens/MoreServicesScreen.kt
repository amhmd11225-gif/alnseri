package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AlAnsariGoldCard
import com.example.ui.components.AlAnsariSubTopBar
import com.example.ui.components.CrimsonPillBadge
import com.example.ui.navigation.AlAnsariRoutes
import com.example.ui.theme.CardSurfaceTop
import com.example.ui.theme.GoldBright
import com.example.ui.theme.MutedGray
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.viewmodel.AlAnsariUiState

@Composable
fun MoreServicesScreen(
    uiState: AlAnsariUiState,
    onNavigate: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showSavedDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlAnsariSubTopBar(
            title = "المزيد والخدمات",
            badgeText = "جميع خدمات قناة الأنصاري",
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
            // Satellite Frequency Card
            item {
                AlAnsariGoldCard {
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
                                imageVector = Icons.Filled.SatelliteAlt,
                                contentDescription = "تردد القناة",
                                tint = GoldBright,
                                modifier = Modifier.size(30.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "تردد قناة الأنصاري الفضائية",
                                    style = MaterialTheme.typography.titleMedium.copy(color = GoldBright),
                                )
                                Text(
                                    text = uiState.settings.satelliteFrequency,
                                    style = MaterialTheme.typography.titleSmall.copy(color = PureWhite),
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(
                                    ClipData.newPlainText("تردد قناة الأنصاري", uiState.settings.satelliteFrequency),
                                )
                                onShowMessage("تم نسخ تردد القناة")
                            },
                            modifier = Modifier.size(48.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "نسخ التردد",
                                tint = GoldBright,
                            )
                        }
                    }
                }
            }

            // Public Services List
            items(uiState.services, key = { it.id }) { service ->
                val icon = when (service.id) {
                    "srv_booking" -> Icons.Filled.Campaign
                    "srv_coverage" -> Icons.Filled.SettingsInputAntenna
                    "srv_saved" -> Icons.Filled.Bookmark
                    else -> Icons.Filled.LiveTv
                }
                AlAnsariGoldCard(
                    onClick = {
                        when (service.routeTarget) {
                            "booking_request", "request_coverage" -> onNavigate(AlAnsariRoutes.BOOKING_REQUEST)
                            "saved_items" -> showSavedDialog = true
                            else -> onNavigate(service.routeTarget)
                        }
                    },
                    testTag = "service_card_${service.id}",
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
                                imageVector = icon,
                                contentDescription = service.title,
                                tint = GoldBright,
                                modifier = Modifier.size(28.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = service.title,
                                    style = MaterialTheme.typography.titleMedium.copy(color = PureWhite),
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = service.subtitle,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        CrimsonPillBadge(text = service.actionLabel, horizontalPadding = 12.dp)
                    }
                }
            }

            // Quick shortcuts to all sections
            item {
                Text(
                    text = "الوصول السريع للأقسام",
                    style = MaterialTheme.typography.titleMedium.copy(color = GoldBright),
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AlAnsariGoldCard(
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AlAnsariRoutes.FIELD_COVERAGE) },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.SettingsInputAntenna, contentDescription = null, tint = GoldBright)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("التغطيات الميدانية", style = MaterialTheme.typography.titleSmall)
                        }
                    }
                    AlAnsariGoldCard(
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AlAnsariRoutes.NEWS) },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Newspaper, contentDescription = null, tint = GoldBright)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("الأخبار والتقارير", style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AlAnsariGoldCard(
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AlAnsariRoutes.EVENTS) },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.VideoLibrary, contentDescription = null, tint = GoldBright)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("الفعاليات", style = MaterialTheme.typography.titleSmall)
                        }
                    }
                    AlAnsariGoldCard(
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AlAnsariRoutes.SOCIAL) },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Share, contentDescription = null, tint = GoldBright)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("التواصل الرسمي", style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }
            }

            // Share App Card
            item {
                AlAnsariGoldCard(
                    onClick = {
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "تطبيق قناة الأنصاري الفضائية – البث المباشر والبرامج والتغطيات الميدانية.",
                            )
                        }
                        context.startActivity(Intent.createChooser(share, "مشاركة التطبيق"))
                    },
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                text = "عن تطبيق قناة الأنصاري – Al Ansari TV",
                                style = MaterialTheme.typography.titleSmall.copy(color = GoldBright),
                            )
                            Text(
                                text = "الإصدار 1.0 • مشاركة التطبيق مع الأهل والأصدقاء",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MutedGray, fontSize = 12.sp),
                            )
                        }
                        Icon(Icons.Filled.Share, contentDescription = "مشاركة التطبيق", tint = GoldBright)
                    }
                }
            }

            // Discreet Owner-Only Isolated Portal Link at Bottom
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onNavigate(AlAnsariRoutes.ADMIN_PANEL) },
                        modifier = Modifier.testTag("admin_portal_entry"),
                    ) {
                        Icon(
                            imageVector = if (uiState.isChannelOwner) Icons.Filled.AdminPanelSettings else Icons.Filled.Lock,
                            contentDescription = null,
                            tint = MutedGray,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "بوابة إدارة القناة (للمسؤول فقط)",
                            style = MaterialTheme.typography.labelMedium.copy(color = MutedGray),
                        )
                    }
                }
            }
        }
    }

    if (showSavedDialog) {
        SavedItemsDialog(
            uiState = uiState,
            onDismiss = { showSavedDialog = false },
            onOpenVideo = { id ->
                showSavedDialog = false
                onNavigate(AlAnsariRoutes.videoPlayer(id))
            },
            onOpenNews = { id ->
                showSavedDialog = false
                onNavigate(AlAnsariRoutes.newsDetail(id))
            },
        )
    }
}

@Composable
private fun SavedItemsDialog(
    uiState: AlAnsariUiState,
    onDismiss: () -> Unit,
    onOpenVideo: (String) -> Unit,
    onOpenNews: (String) -> Unit,
) {
    val savedVideos = uiState.videos.filter { uiState.bookmarkedVideoIds.contains(it.id) }
    val savedNews = uiState.newsReports.filter { uiState.bookmarkedNewsIds.contains(it.id) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurfaceTop,
        title = {
            Text("المحفوظات والمفضلة", style = MaterialTheme.typography.titleMedium.copy(color = GoldBright))
        },
        text = {
            if (savedVideos.isEmpty() && savedNews.isEmpty()) {
                Text(
                    text = "لا توجد عناصر محفوظة بعد. اضغط على أيقونة الحفظ داخل الفيديوهات أو الأخبار لإضافتها هنا.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = PureWhite),
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    savedVideos.forEach { video ->
                        AlAnsariGoldCard(onClick = { onOpenVideo(video.id) }, contentPadding = PaddingValues(10.dp)) {
                            Text("فيديو: ${video.title}", style = MaterialTheme.typography.labelLarge.copy(color = GoldBright))
                        }
                    }
                    savedNews.forEach { news ->
                        AlAnsariGoldCard(onClick = { onOpenNews(news.id) }, contentPadding = PaddingValues(10.dp)) {
                            Text("خبر: ${news.title}", style = MaterialTheme.typography.labelLarge.copy(color = PureWhite))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = GoldBright)
            }
        },
    )
}
