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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AlAnsariGoldCard
import com.example.ui.components.AlAnsariSubTopBar
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDarkBorder
import com.example.ui.theme.MutedGray
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.viewmodel.AlAnsariUiState

@Composable
fun SourceSettingsScreen(
    uiState: AlAnsariUiState,
    onSaveLiveUrls: (String, String) -> Unit,
    onSaveYouTubeSource: (String, String) -> Unit,
    onSaveSocialAndSatellite: (String, String, String, String, String, String) -> Unit,
    onSyncCms: (String) -> Unit,
    onResetDefaults: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var ytHandle by remember(uiState.settings.youtubeHandle) { mutableStateOf(uiState.settings.youtubeHandle) }
    var ytChannelId by remember(uiState.settings.youtubeChannelId) { mutableStateOf(uiState.settings.youtubeChannelId) }
    var primaryLive by remember(uiState.settings.primaryLiveUrl) { mutableStateOf(uiState.settings.primaryLiveUrl) }
    var backupLive by remember(uiState.settings.backupLiveUrl) { mutableStateOf(uiState.settings.backupLiveUrl) }
    var cmsApiUrl by remember(uiState.settings.cmsApiBaseUrl) { mutableStateOf(uiState.settings.cmsApiBaseUrl) }
    var facebookUrl by remember(uiState.settings.facebookUrl) { mutableStateOf(uiState.settings.facebookUrl) }
    var youtubeUrl by remember(uiState.settings.youtubeUrl) { mutableStateOf(uiState.settings.youtubeUrl) }
    var tiktokUrl by remember(uiState.settings.tiktokUrl) { mutableStateOf(uiState.settings.tiktokUrl) }
    var telegramUrl by remember(uiState.settings.telegramUrl) { mutableStateOf(uiState.settings.telegramUrl) }
    var websiteUrl by remember(uiState.settings.websiteUrl) { mutableStateOf(uiState.settings.websiteUrl) }
    var frequency by remember(uiState.settings.satelliteFrequency) { mutableStateOf(uiState.settings.satelliteFrequency) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = GoldBright,
        unfocusedBorderColor = GoldDarkBorder,
        focusedLabelColor = GoldBright,
        unfocusedLabelColor = MutedGray,
        focusedTextColor = PureWhite,
        unfocusedTextColor = PureWhite,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlAnsariSubTopBar(
            title = "إدارة المصادر واليوتيوب",
            badgeText = "إعدادات البث والربط",
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
            // YouTube Source Card (@alansarie1)
            item {
                AlAnsariGoldCard {
                    Text(
                        text = "مصدر قناة يوتيوب الرسمية (الشعار والفيديوهات)",
                        style = MaterialTheme.typography.titleMedium.copy(color = GoldBright),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "يتم جلب الشعار الرسمي وأحدث المقاطع تلقائياً من قناة يوتيوب المحددة أدناه.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = ytHandle,
                        onValueChange = { ytHandle = it },
                        label = { Text("معرّف القناة (Handle)") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ytChannelId,
                        onValueChange = { ytChannelId = it },
                        label = { Text("معرّف RSS (Channel ID)") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onSaveYouTubeSource(ytHandle, ytChannelId) },
                        enabled = !uiState.isRefreshingYouTube,
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = PureWhite),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (uiState.isRefreshingYouTube) {
                            CircularProgressIndicator(color = PureWhite, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حفظ وتحديث من يوتيوب الآن", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            // Live HLS Stream Configuration
            item {
                AlAnsariGoldCard {
                    Text(
                        text = "مصادر البث المباشر (HLS .m3u8)",
                        style = MaterialTheme.typography.titleMedium.copy(color = GoldBright),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "أدخل رابط البث الرسمي لقناة الأنصاري هنا ليظهر مباشرة في شاشة البث المباشر.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = primaryLive,
                        onValueChange = { primaryLive = it },
                        label = { Text("رابط البث الرئيسي (.m3u8)") },
                        placeholder = { Text("https://stream.example.com/alansari/live.m3u8") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_primary_live_input"),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = backupLive,
                        onValueChange = { backupLive = it },
                        label = { Text("رابط البث الاحتياطي / الاختباري") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onSaveLiveUrls(primaryLive, backupLive) },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = PureWhite),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_save_live_button"),
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حفظ روابط البث المباشر", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            // Future Admin Dashboard / CMS API Integration
            item {
                AlAnsariGoldCard {
                    Text(
                        text = "ربط لوحة التحكم المستقبلية (CMS API)",
                        style = MaterialTheme.typography.titleMedium.copy(color = GoldBright),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "البنية جاهزة لاستقبال الأخبار والفيديوهات وروابط البث من خادم JSON على المسار api/v1/channel/bundle.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "الحالة: ${uiState.settings.lastSyncedTimestamp}",
                        style = MaterialTheme.typography.labelSmall.copy(color = GoldBright, fontSize = 12.sp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cmsApiUrl,
                        onValueChange = { cmsApiUrl = it },
                        label = { Text("رابط خادم لوحة التحكم (Base URL)") },
                        placeholder = { Text("https://cms.alansaritv.example.com/") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onSyncCms(cmsApiUrl) },
                        enabled = !uiState.isSyncingCms,
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = PureWhite),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (uiState.isSyncingCms) {
                            CircularProgressIndicator(
                                color = PureWhite,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(Icons.Filled.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مزامنة المحتوى من لوحة التحكم", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            // Social Links & Satellite Frequency
            item {
                AlAnsariGoldCard {
                    Text(
                        text = "الروابط الرسمية والتردد الفضائي",
                        style = MaterialTheme.typography.titleMedium.copy(color = GoldBright),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = frequency, onValueChange = { frequency = it }, label = { Text("معلومات البث / التردد") }, singleLine = true, colors = fieldColors, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = youtubeUrl, onValueChange = { youtubeUrl = it }, label = { Text("رابط يوتيوب") }, singleLine = true, colors = fieldColors, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = telegramUrl, onValueChange = { telegramUrl = it }, label = { Text("رابط تيليجرام") }, singleLine = true, colors = fieldColors, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = facebookUrl, onValueChange = { facebookUrl = it }, label = { Text("رابط فيسبوك") }, singleLine = true, colors = fieldColors, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = tiktokUrl, onValueChange = { tiktokUrl = it }, label = { Text("رابط تيك توك") }, singleLine = true, colors = fieldColors, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = websiteUrl, onValueChange = { websiteUrl = it }, label = { Text("رابط إنستغرام / الموقع") }, singleLine = true, colors = fieldColors, modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = {
                                onSaveSocialAndSatellite(
                                    facebookUrl,
                                    youtubeUrl,
                                    tiktokUrl,
                                    telegramUrl,
                                    websiteUrl,
                                    frequency,
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = PureWhite),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("حفظ الروابط", style = MaterialTheme.typography.labelLarge)
                        }
                        OutlinedButton(
                            onClick = onResetDefaults,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Filled.RestartAlt, contentDescription = null, tint = GoldBright, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("استعادة الافتراضي", color = GoldBright)
                        }
                    }
                }
            }
        }
    }
}
