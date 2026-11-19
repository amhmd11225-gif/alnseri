package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.data.auth.GoogleAuthHelper
import com.example.data.model.CoverageBookingRequest
import com.example.data.model.bookingStatusArabic
import com.example.data.model.detectStreamProtocol
import com.example.data.model.protocolBadgeLabel
import com.example.ui.components.AlAnsariGoldCard
import com.example.ui.components.AlAnsariSubTopBar
import com.example.ui.components.CrimsonPillBadge
import com.example.ui.components.GoldOutlineChip
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDarkBorder
import com.example.ui.theme.MutedGray
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.viewmodel.AlAnsariUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminPanelScreen(
    uiState: AlAnsariUiState,
    onPublishCloudConfig: (
        primaryLiveUrl: String,
        backupLiveUrl: String,
        liveTitle: String,
        youtubeHandle: String,
        youtubeChannelId: String,
        facebookUrl: String,
        telegramUrl: String,
        tiktokUrl: String,
        instagramUrl: String,
        satelliteFrequency: String,
    ) -> Unit,
    onUpdateBookingStatus: (requestId: String, newStatus: String, adminNote: String?) -> Unit,
    onRefreshYouTube: () -> Unit,
    onSyncCms: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlAnsariSubTopBar(
            title = "لوحة تحكم مالك القناة",
            badgeText = "إدارة البث والطلبات",
            onBackClick = onBackClick,
            trailingAction = {
                if (uiState.currentUser != null) {
                    TextButton(
                        onClick = {
                            GoogleAuthHelper.signOut(credentialManager, coroutineScope) {
                                onShowMessage("تم تسجيل الخروج من لوحة الإدارة")
                            }
                        },
                        modifier = Modifier.testTag("admin_signout_button"),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "خروج", tint = GoldBright, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("خروج", color = GoldBright)
                    }
                }
            },
        )

        when {
            uiState.currentUser == null -> {
                Box(
                    modifier = Modifier
                        .widthIn(max = 560.dp)
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    AlAnsariGoldCard {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = GoldBright,
                                modifier = Modifier.size(48.dp),
                            )
                            Text(
                                text = "بوابة إدارة البث المباشر",
                                style = MaterialTheme.typography.titleLarge.copy(color = GoldBright, fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = "هذه اللوحة معزولة ومحمية لمالك قناة الأنصاري فقط. يرجى تسجيل الدخول بحساب Google المصرّح به للتحكم بروابط البث (HLS / RTMP / SRT) وإدارة طلبات الحجز.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                            )
                            Button(
                                onClick = {
                                    GoogleAuthHelper.onGoogleSignInClicked(
                                        context = context,
                                        credentialManager = credentialManager,
                                        scope = coroutineScope,
                                        onAuthSuccess = { onShowMessage("تم تسجيل الدخول بنجاح") },
                                        onAuthError = { msg -> onShowMessage(msg) },
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = PureWhite),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("admin_google_signin_button"),
                            ) {
                                Icon(Icons.Filled.AccountCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("تسجيل الدخول بحساب Google", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            !uiState.isChannelOwner -> {
                Box(
                    modifier = Modifier
                        .widthIn(max = 560.dp)
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    AlAnsariGoldCard {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Block,
                                contentDescription = null,
                                tint = CrimsonRed,
                                modifier = Modifier.size(48.dp),
                            )
                            Text(
                                text = "غير مصرّح بالدخول",
                                style = MaterialTheme.typography.titleLarge.copy(color = CrimsonRed, fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = "أنت مسجّل بالحساب:\n${uiState.currentUser.email.orEmpty()}\n\nالتحكم ببث القناة محصور حصرياً بحساب مالك القناة المعتمد (${GoogleAuthHelper.CHANNEL_OWNER_EMAIL}).",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                            )
                            OutlinedButton(
                                onClick = {
                                    GoogleAuthHelper.signOut(credentialManager, coroutineScope) {
                                        onShowMessage("تم تسجيل الخروج، يمكنك الدخول بحساب المالك")
                                    }
                                },
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("تسجيل الخروج وتبديل الحساب", color = GoldBright)
                            }
                        }
                    }
                }
            }

            else -> {
                AdminOwnerDashboardContent(
                    uiState = uiState,
                    onPublishCloudConfig = onPublishCloudConfig,
                    onUpdateBookingStatus = onUpdateBookingStatus,
                    onRefreshYouTube = onRefreshYouTube,
                    onSyncCms = onSyncCms,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminOwnerDashboardContent(
    uiState: AlAnsariUiState,
    onPublishCloudConfig: (String, String, String, String, String, String, String, String, String, String) -> Unit,
    onUpdateBookingStatus: (String, String, String?) -> Unit,
    onRefreshYouTube: () -> Unit,
    onSyncCms: (String) -> Unit,
) {
    var primaryLive by remember(uiState.settings.primaryLiveUrl) { mutableStateOf(uiState.settings.primaryLiveUrl) }
    var backupLive by remember(uiState.settings.backupLiveUrl) { mutableStateOf(uiState.settings.backupLiveUrl) }
    var liveTitle by remember(uiState.settings.liveTitle) { mutableStateOf(uiState.settings.liveTitle) }
    var ytHandle by remember(uiState.settings.youtubeHandle) { mutableStateOf(uiState.settings.youtubeHandle) }
    var ytChannelId by remember(uiState.settings.youtubeChannelId) { mutableStateOf(uiState.settings.youtubeChannelId) }
    var facebookUrl by remember(uiState.settings.facebookUrl) { mutableStateOf(uiState.settings.facebookUrl) }
    var telegramUrl by remember(uiState.settings.telegramUrl) { mutableStateOf(uiState.settings.telegramUrl) }
    var tiktokUrl by remember(uiState.settings.tiktokUrl) { mutableStateOf(uiState.settings.tiktokUrl) }
    var instagramUrl by remember(uiState.settings.websiteUrl) { mutableStateOf(uiState.settings.websiteUrl) }
    var frequency by remember(uiState.settings.satelliteFrequency) { mutableStateOf(uiState.settings.satelliteFrequency) }
    var cmsApiUrl by remember(uiState.settings.cmsApiBaseUrl) { mutableStateOf(uiState.settings.cmsApiBaseUrl) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = GoldBright,
        unfocusedBorderColor = GoldDarkBorder,
        focusedLabelColor = GoldBright,
        unfocusedLabelColor = MutedGray,
        focusedTextColor = PureWhite,
        unfocusedTextColor = PureWhite,
    )

    val detectedProtocolLabel = protocolBadgeLabel(detectStreamProtocol(primaryLive))

    fun publishAll(overridePrimary: String = primaryLive) {
        onPublishCloudConfig(
            overridePrimary,
            backupLive,
            liveTitle,
            ytHandle,
            ytChannelId,
            facebookUrl,
            telegramUrl,
            tiktokUrl,
            instagramUrl,
            frequency,
        )
    }

    LazyColumn(
        modifier = Modifier
            .widthIn(max = 680.dp)
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Owner Verified Header
        item {
            AlAnsariGoldCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = GoldBright, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("أهلاً بك يا مدير قناة الأنصاري", style = MaterialTheme.typography.titleSmall.copy(color = GoldBright, fontWeight = FontWeight.Bold))
                            Text(
                                text = uiState.currentUser?.email.orEmpty(),
                                style = MaterialTheme.typography.labelMedium.copy(color = PureWhite),
                            )
                        }
                    }
                    CrimsonPillBadge(text = "صلاحية كاملة", horizontalPadding = 10.dp)
                }
            }
        }

        // 1. Live Broadcast Stream Control (HLS / RTMP / SRT)
        item {
            AlAnsariGoldCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "التحكم الفوري برابط البث المباشر",
                        style = MaterialTheme.typography.titleMedium.copy(color = GoldBright),
                    )
                    if (primaryLive.isNotBlank()) {
                        CrimsonPillBadge(text = detectedProtocolLabel, horizontalPadding = 10.dp)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "أي رابط تنشره هنا يظهر ويشغل البث مباشرة عند جميع المشاهدين. يدعم البروتوكولات: HLS (.m3u8) و RTMP (rtmp://) و SRT (srt://).",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GoldOutlineChip(
                        text = "HLS (.m3u8)",
                        selected = primaryLive.contains(".m3u8", ignoreCase = true),
                        onClick = {
                            if (primaryLive.isBlank()) primaryLive = "https://"
                        },
                    )
                    GoldOutlineChip(
                        text = "RTMP (rtmp://)",
                        selected = primaryLive.startsWith("rtmp", ignoreCase = true),
                        onClick = {
                            if (primaryLive.isBlank() || primaryLive.startsWith("http") || primaryLive.startsWith("srt")) {
                                primaryLive = "rtmp://"
                            }
                        },
                    )
                    GoldOutlineChip(
                        text = "SRT (srt://)",
                        selected = primaryLive.startsWith("srt://", ignoreCase = true),
                        onClick = {
                            if (primaryLive.isBlank() || primaryLive.startsWith("http") || primaryLive.startsWith("rtmp")) {
                                primaryLive = "srt://"
                            }
                        },
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = liveTitle,
                    onValueChange = { liveTitle = it },
                    label = { Text("عنوان البث الظاهر للمشاهدين") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = primaryLive,
                    onValueChange = { primaryLive = it },
                    label = { Text("رابط البث الرئيسي (m3u8 / rtmp:// / srt://)") },
                    placeholder = { Text("srt://server:9000?streamid=... أو rtmp://... أو https://.../live.m3u8") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_primary_live_input"),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = backupLive,
                    onValueChange = { backupLive = it },
                    label = { Text("رابط البث الاحتياطي") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { publishAll() },
                    enabled = !uiState.isSavingCloud,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = PureWhite),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("admin_publish_live_button"),
                ) {
                    if (uiState.isSavingCloud) {
                        CircularProgressIndicator(color = PureWhite, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.CloudDone, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("حفظ ونشر البث لجميع المشاهدين", style = MaterialTheme.typography.labelLarge)
                }

                if (primaryLive.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            primaryLive = ""
                            publishAll(overridePrimary = "")
                        },
                        enabled = !uiState.isSavingCloud,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Filled.StopCircle, contentDescription = null, tint = GoldBright, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مسح الرابط الرئيسي (تشغيل الاحتياطي)", color = GoldBright)
                    }
                }
            }
        }

        // 2. Incoming Coverage Bookings Inbox
        item {
            Text(
                text = "طلبات حجز التغطيات الواردة (${uiState.adminBookings.size})",
                style = MaterialTheme.typography.titleMedium.copy(color = GoldBright),
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        if (uiState.adminBookings.isEmpty()) {
            item {
                AlAnsariGoldCard {
                    Text(
                        text = "لا توجد طلبات حجز جديدة حالياً.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        } else {
            items(uiState.adminBookings, key = { it.id }) { booking ->
                AdminBookingItemCard(
                    booking = booking,
                    isSaving = uiState.isSavingCloud,
                    onUpdateStatus = onUpdateBookingStatus,
                )
            }
        }

        // 3. YouTube & Social Settings
        item {
            AlAnsariGoldCard {
                Text(
                    text = "إعدادات يوتيوب والروابط الرسمية والتردد",
                    style = MaterialTheme.typography.titleMedium.copy(color = GoldBright),
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = ytHandle, onValueChange = { ytHandle = it }, label = { Text("معرّف يوتيوب (@alansarie1)") }, singleLine = true, colors = fieldColors, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = ytChannelId, onValueChange = { ytChannelId = it }, label = { Text("Channel ID") }, singleLine = true, colors = fieldColors, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = frequency, onValueChange = { frequency = it }, label = { Text("معلومات البث / التردد") }, singleLine = true, colors = fieldColors, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = telegramUrl, onValueChange = { telegramUrl = it }, label = { Text("رابط تيليجرام") }, singleLine = true, colors = fieldColors, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = facebookUrl, onValueChange = { facebookUrl = it }, label = { Text("رابط فيسبوك") }, singleLine = true, colors = fieldColors, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = tiktokUrl, onValueChange = { tiktokUrl = it }, label = { Text("رابط تيك توك") }, singleLine = true, colors = fieldColors, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = instagramUrl, onValueChange = { instagramUrl = it }, label = { Text("رابط إنستغرام") }, singleLine = true, colors = fieldColors, modifier = Modifier.fillMaxWidth())
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = { publishAll() },
                        enabled = !uiState.isSavingCloud,
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = PureWhite),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("نشر الروابط", style = MaterialTheme.typography.labelLarge)
                    }
                    OutlinedButton(
                        onClick = onRefreshYouTube,
                        enabled = !uiState.isRefreshingYouTube,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, tint = GoldBright, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تحديث يوتيوب", color = GoldBright)
                    }
                }
            }
        }

        // 4. Optional CMS JSON Sync
        item {
            AlAnsariGoldCard {
                Text(
                    text = "مزامنة أرشيف إضافي (CMS JSON)",
                    style = MaterialTheme.typography.titleMedium.copy(color = GoldBright),
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = cmsApiUrl,
                    onValueChange = { cmsApiUrl = it },
                    label = { Text("رابط خادم CMS (اختياري)") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onSyncCms(cmsApiUrl) },
                    enabled = !uiState.isSyncingCms,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.CloudSync, contentDescription = null, tint = GoldBright, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مزامنة من CMS الآن", color = GoldBright)
                }
            }
        }
    }
}

@Composable
private fun AdminBookingItemCard(
    booking: CoverageBookingRequest,
    isSaving: Boolean,
    onUpdateStatus: (String, String, String?) -> Unit,
) {
    val context = LocalContext.current
    var noteInput by remember(booking.id, booking.adminNote) { mutableStateOf(booking.adminNote.orEmpty()) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = GoldBright,
        unfocusedBorderColor = GoldDarkBorder,
        focusedLabelColor = GoldBright,
        unfocusedLabelColor = MutedGray,
        focusedTextColor = PureWhite,
        unfocusedTextColor = PureWhite,
    )

    AlAnsariGoldCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = booking.topic,
                style = MaterialTheme.typography.titleSmall.copy(color = GoldBright, fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            CrimsonPillBadge(text = bookingStatusArabic(booking.status), horizontalPadding = 10.dp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "الجهة: ${booking.senderName} • النوع: ${booking.serviceType}",
            style = MaterialTheme.typography.bodyMedium.copy(color = PureWhite),
        )
        Text(
            text = "المكان: ${booking.governorate} • الموعد: ${booking.eventDate}",
            style = MaterialTheme.typography.bodyMedium.copy(color = MutedGray, fontSize = 12.5.sp),
        )
        if (!booking.details.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "التفاصيل: ${booking.details}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedButton(
            onClick = {
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${booking.phone}")))
                }
            },
            shape = RoundedCornerShape(50),
        ) {
            Icon(Icons.Filled.Phone, contentDescription = "اتصال", tint = GoldBright, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("اتصال: ${booking.phone}", color = GoldBright)
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = noteInput,
            onValueChange = { noteInput = it },
            label = { Text("ملاحظة / رد للعميل") },
            singleLine = true,
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Button(
                onClick = { onUpdateStatus(booking.id, "confirmed", noteInput) },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = PureWhite),
                shape = RoundedCornerShape(50),
                modifier = Modifier.weight(1f),
            ) {
                Text("تأكيد", fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = { onUpdateStatus(booking.id, "completed", noteInput) },
                enabled = !isSaving,
                shape = RoundedCornerShape(50),
                modifier = Modifier.weight(1f),
            ) {
                Text("مكتمل", color = GoldBright, fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = { onUpdateStatus(booking.id, "declined", noteInput) },
                enabled = !isSaving,
                shape = RoundedCornerShape(50),
                modifier = Modifier.weight(1f),
            ) {
                Text("اعتذار", color = MutedGray, fontSize = 12.sp)
            }
        }
    }
}
