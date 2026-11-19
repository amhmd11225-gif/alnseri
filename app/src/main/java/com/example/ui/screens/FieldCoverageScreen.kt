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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FieldCoverage
import com.example.ui.components.AlAnsariGoldCard
import com.example.ui.components.AlAnsariMediaPlayer
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
fun FieldCoverageScreen(
    uiState: AlAnsariUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val regions = listOf("الكل", "سوق الشيوخ", "الجبايش", "ذي قار", "كربلاء المقدسة")
    var selectedRegion by remember { mutableStateOf("الكل") }
    var activeCoverage by remember(uiState.fieldCoverages) {
        mutableStateOf<FieldCoverage?>(uiState.fieldCoverages.firstOrNull())
    }

    val filtered = remember(uiState.fieldCoverages, selectedRegion) {
        if (selectedRegion == "الكل") uiState.fieldCoverages
        else uiState.fieldCoverages.filter { it.location == selectedRegion }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlAnsariSubTopBar(
            title = "التغطيات الميدانية",
            badgeText = "وحدة SNG1080 – ذي قار",
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
            if (activeCoverage != null) {
                item {
                    AlAnsariMediaPlayer(
                        streamUrl = activeCoverage!!.videoUrl,
                        title = activeCoverage!!.title,
                        isLiveStream = activeCoverage!!.isLiveField,
                    )
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(regions) { region ->
                        GoldOutlineChip(
                            text = region,
                            selected = selectedRegion == region,
                            onClick = { selectedRegion = region },
                            testTag = "coverage_region_$region",
                        )
                    }
                }
            }

            items(filtered, key = { it.id }) { coverage ->
                val isPlayingThis = activeCoverage?.id == coverage.id
                AlAnsariGoldCard(
                    onClick = { activeCoverage = coverage },
                    testTag = "coverage_card_${coverage.id}",
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (coverage.isLiveField) {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(CrimsonRed),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Videocam,
                                    contentDescription = null,
                                    tint = GoldBright,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            CrimsonPillBadge(text = coverage.statusLabel, horizontalPadding = 10.dp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = GoldBright,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = coverage.location,
                                style = MaterialTheme.typography.labelMedium.copy(color = GoldBright),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = coverage.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = if (isPlayingThis) GoldBright else PureWhite,
                        ),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = coverage.summary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "${coverage.reporter} • ${coverage.timestamp}",
                            style = MaterialTheme.typography.labelSmall.copy(color = MutedGray, fontSize = 12.sp),
                            modifier = Modifier.weight(1f),
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!coverage.youtubeUrl.isNullOrBlank()) {
                                IconButton(
                                    onClick = {
                                        runCatching {
                                            context.startActivity(
                                                Intent(Intent.ACTION_VIEW, Uri.parse(coverage.youtubeUrl)),
                                            )
                                        }
                                    },
                                    modifier = Modifier.size(48.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = "فتح في يوتيوب",
                                        tint = GoldBright,
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    val share = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "${coverage.title} (${coverage.location})\n${coverage.summary}")
                                    }
                                    context.startActivity(Intent.createChooser(share, "مشاركة التغطية"))
                                },
                                modifier = Modifier.size(48.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Share,
                                    contentDescription = "مشاركة",
                                    tint = PureWhite,
                                )
                            }
                            Button(
                                onClick = { activeCoverage = coverage },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isPlayingThis) GoldBright else CrimsonRed,
                                    contentColor = if (isPlayingThis) ObsidianBlack else PureWhite,
                                ),
                                shape = RoundedCornerShape(50),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isPlayingThis) "قيد التشغيل" else "مشاهدة",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (isPlayingThis) ObsidianBlack else PureWhite,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
