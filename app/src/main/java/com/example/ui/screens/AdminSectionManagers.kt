package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.EXTRA_OPEN_ADMIN_PANEL
import com.example.MainActivity
import com.example.R
import com.example.data.model.CloudCoverageDoc
import com.example.data.model.CloudEventDoc
import com.example.data.model.CloudNewsDoc
import com.example.data.model.CloudVideoDoc
import com.example.data.model.EventStatus
import com.example.ui.components.AlAnsariGoldCard
import com.example.ui.components.CrimsonPillBadge
import com.example.ui.components.GoldOutlineChip
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDarkBorder
import com.example.ui.theme.MutedGray
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite

const val ADMIN_DIRECT_LINK = "alansari://admin"
const val ADMIN_CHROME_INTENT_LINK =
    "intent://admin#Intent;scheme=alansari;package=com.aistudio.alansaritv.qzmpxk;end"

@Composable
private fun adminFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = GoldBright,
    unfocusedBorderColor = GoldDarkBorder,
    focusedLabelColor = GoldBright,
    unfocusedLabelColor = MutedGray,
    focusedTextColor = PureWhite,
    unfocusedTextColor = PureWhite,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminDirectLinkCard(
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    AlAnsariGoldCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Filled.Link,
                    contentDescription = null,
                    tint = GoldBright,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "الرابط المباشر للوحة التحكم",
                    style = MaterialTheme.typography.titleMedium.copy(color = GoldBright, fontWeight = FontWeight.Bold),
                )
            }
            CrimsonPillBadge(text = "خاص بك", horizontalPadding = 10.dp)
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "بعد تثبيت أحدث نسخة من التطبيق، يمكنك فتح لوحة التحكم مباشرة عبر تثبيت أيقونة لها على شاشة هاتفك، أو بالضغط مطولاً على أيقونة التطبيق واختيار «لوحة التحكم»، أو بنسخ رابط الفتح المباشر أدناه:",
            style = MaterialTheme.typography.bodyMedium.copy(color = MutedGray, fontSize = 12.5.sp),
        )

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ObsidianBlack.copy(alpha = 0.7f))
                .border(1.dp, GoldDarkBorder.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ADMIN_DIRECT_LINK,
                    style = MaterialTheme.typography.titleSmall.copy(color = GoldBright, fontWeight = FontWeight.Bold),
                    modifier = Modifier.testTag("admin_direct_link_text"),
                )
                Text(
                    text = "رابط المتصفح المباشر (Chrome): intent://admin#Intent;scheme=alansari;...",
                    style = MaterialTheme.typography.labelSmall.copy(color = MutedGray),
                )
            }
            IconButton(
                onClick = {
                    clipboard.setText(AnnotatedString(ADMIN_DIRECT_LINK))
                    onShowMessage("تم نسخ الرابط المباشر: $ADMIN_DIRECT_LINK")
                },
                modifier = Modifier.testTag("admin_copy_link_button"),
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = "نسخ الرابط", tint = GoldBright)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = {
                    clipboard.setText(AnnotatedString(ADMIN_CHROME_INTENT_LINK))
                    onShowMessage("تم نسخ رابط المتصفح المباشر (الصقه في Chrome لفتح اللوحة)")
                },
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("admin_copy_intent_link_button"),
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null, tint = GoldBright, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("نسخ رابط Chrome", color = GoldBright, fontSize = 12.5.sp)
            }

            OutlinedButton(
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "الرابط المباشر للوحة تحكم قناة الأنصاري:\n$ADMIN_DIRECT_LINK\nرابط متصفح Chrome على الهاتف:\n$ADMIN_CHROME_INTENT_LINK",
                        )
                    }
                    runCatching {
                        context.startActivity(Intent.createChooser(shareIntent, "حفظ أو مشاركة رابط لوحة التحكم"))
                    }.onFailure {
                        clipboard.setText(AnnotatedString(ADMIN_DIRECT_LINK))
                        onShowMessage("تم نسخ الرابط إلى الحافظة")
                    }
                },
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("admin_share_link_button"),
            ) {
                Icon(Icons.Filled.Share, contentDescription = null, tint = GoldBright, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("حفظ / إرسال الرابط", color = GoldBright, fontSize = 12.5.sp)
            }

            Button(
                onClick = {
                    if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
                        val launchIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(ADMIN_DIRECT_LINK),
                            context,
                            MainActivity::class.java,
                        ).apply {
                            putExtra(EXTRA_OPEN_ADMIN_PANEL, true)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        val shortcut = ShortcutInfoCompat.Builder(context, "alansari_admin_panel_shortcut")
                            .setShortLabel("إدارة الأنصاري")
                            .setLongLabel("لوحة تحكم قناة الأنصاري")
                            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
                            .setIntent(launchIntent)
                            .build()
                        val requested = ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
                        if (requested) {
                            onShowMessage("أكّد إضافة أيقونة \"إدارة الأنصاري\" إلى شاشة هاتفك")
                        } else {
                            onShowMessage("تعذر طلب الاختصار على هذا المشغّل، يمكنك الضغط مطولاً على أيقونة التطبيق واختيار لوحة التحكم")
                        }
                    } else {
                        clipboard.setText(AnnotatedString(ADMIN_DIRECT_LINK))
                        onShowMessage("المشغّل لا يدعم الاختصار التلقائي، تم نسخ الرابط $ADMIN_DIRECT_LINK")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = PureWhite),
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("admin_pin_shortcut_button"),
            ) {
                Icon(Icons.Filled.AddToHomeScreen, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("تثبيت أيقونة الإدارة بالشاشة", fontSize = 12.5.sp)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminEventsManager(
    events: List<CloudEventDoc>,
    isSaving: Boolean,
    onPublishEvent: (
        title: String,
        hijriDate: String,
        gregorianDate: String,
        timeLabel: String,
        location: String,
        status: String,
        description: String,
        onSuccess: () -> Unit,
    ) -> Unit,
    onUpdateEventStatus: (eventId: String, status: String) -> Unit,
    onDeleteEvent: (eventId: String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var hijriDate by remember { mutableStateOf("") }
    var gregorianDate by remember { mutableStateOf("") }
    var timeLabel by remember { mutableStateOf("08:00 مساءً") }
    var location by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("UPCOMING") }
    var description by remember { mutableStateOf("") }
    val colors = adminFieldColors()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AlAnsariGoldCard {
            Text(
                text = "إضافة فعالية أو مجلس جديد",
                style = MaterialTheme.typography.titleMedium.copy(color = GoldBright, fontWeight = FontWeight.Bold),
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("عنوان الفعالية أو المناسبة *") },
                singleLine = true,
                colors = colors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_event_title_input"),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = hijriDate,
                    onValueChange = { hijriDate = it },
                    label = { Text("التاريخ الهجري") },
                    placeholder = { Text("10 محرم 1448 هـ") },
                    singleLine = true,
                    colors = colors,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = gregorianDate,
                    onValueChange = { gregorianDate = it },
                    label = { Text("الميلادي") },
                    placeholder = { Text("2026-07-05") },
                    singleLine = true,
                    colors = colors,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = timeLabel,
                    onValueChange = { timeLabel = it },
                    label = { Text("الوقت") },
                    singleLine = true,
                    colors = colors,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("المكان / المحافظة *") },
                    singleLine = true,
                    colors = colors,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_event_location_input"),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("حالة الفعالية:", style = MaterialTheme.typography.labelLarge.copy(color = GoldBright))
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EventStatus.entries.forEach { entry ->
                    GoldOutlineChip(
                        text = entry.arabicLabel,
                        selected = status == entry.name,
                        onClick = { status = entry.name },
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("تفاصيل الفعالية والتغطية") },
                minLines = 2,
                colors = colors,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    onPublishEvent(title, hijriDate, gregorianDate, timeLabel, location, status, description) {
                        title = ""
                        location = ""
                        description = ""
                    }
                },
                enabled = !isSaving && title.isNotBlank() && location.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = PureWhite),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("admin_publish_event_button"),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = PureWhite, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("نشر الفعالية الآن", style = MaterialTheme.typography.labelLarge)
            }
        }

        Text(
            text = "الفعاليات المنشورة سحابياً (${events.size})",
            style = MaterialTheme.typography.titleMedium.copy(color = GoldBright),
        )
        if (events.isEmpty()) {
            AlAnsariGoldCard {
                Text("لم تنشر فعاليات مضافة من لوحة التحكم بعد.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            events.forEach { item ->
                AlAnsariGoldCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, style = MaterialTheme.typography.titleSmall.copy(color = GoldBright, fontWeight = FontWeight.Bold))
                            Text("${item.location} • ${item.timeLabel}", style = MaterialTheme.typography.bodyMedium.copy(color = MutedGray, fontSize = 12.sp))
                        }
                        IconButton(onClick = { onDeleteEvent(item.id) }) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = "حذف الفعالية", tint = CrimsonRed)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        EventStatus.entries.forEach { st ->
                            GoldOutlineChip(
                                text = st.arabicLabel,
                                selected = item.status == st.name,
                                onClick = { onUpdateEventStatus(item.id, st.name) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminNewsSection(
    newsList: List<CloudNewsDoc>,
    isSaving: Boolean,
    onPublishNews: (
        title: String,
        category: String,
        summary: String,
        fullBody: String,
        author: String,
        isBreaking: Boolean,
        onSuccess: () -> Unit,
    ) -> Unit,
    onDeleteNews: (newsId: String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("أخبار القناة") }
    var summary by remember { mutableStateOf("") }
    var fullBody by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("إدارة قناة الأنصاري") }
    var isBreaking by remember { mutableStateOf(false) }
    val colors = adminFieldColors()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AlAnsariGoldCard {
            Text(
                text = "نشر خبر أو بيان جديد",
                style = MaterialTheme.typography.titleMedium.copy(color = GoldBright, fontWeight = FontWeight.Bold),
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("عنوان الخبر *") },
                singleLine = true,
                colors = colors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_news_title_input"),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("التصنيف") },
                    singleLine = true,
                    colors = colors,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("المصدر / المحرر") },
                    singleLine = true,
                    colors = colors,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = summary,
                onValueChange = { summary = it },
                label = { Text("ملخص الخبر *") },
                minLines = 2,
                colors = colors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_news_summary_input"),
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = fullBody,
                onValueChange = { fullBody = it },
                label = { Text("نص الخبر الكامل (اختياري)") },
                minLines = 3,
                colors = colors,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("تصنيف كخبر عاجل", style = MaterialTheme.typography.bodyMedium.copy(color = PureWhite))
                Switch(
                    checked = isBreaking,
                    onCheckedChange = { isBreaking = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = CrimsonRed),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    onPublishNews(title, category, summary, fullBody, author, isBreaking) {
                        title = ""
                        summary = ""
                        fullBody = ""
                        isBreaking = false
                    }
                },
                enabled = !isSaving && title.isNotBlank() && summary.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = PureWhite),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("admin_publish_news_button"),
            ) {
                Icon(Icons.Filled.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("نشر الخبر الآن", style = MaterialTheme.typography.labelLarge)
            }
        }

        Text(
            text = "الأخبار المنشورة من لوحة التحكم (${newsList.size})",
            style = MaterialTheme.typography.titleMedium.copy(color = GoldBright),
        )
        if (newsList.isEmpty()) {
            AlAnsariGoldCard {
                Text("لا توجد أخبار مضافة سحابياً بعد.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            newsList.forEach { item ->
                AlAnsariGoldCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (item.breaking) CrimsonPillBadge(text = "عاجل", horizontalPadding = 8.dp)
                                Text(item.title, style = MaterialTheme.typography.titleSmall.copy(color = GoldBright, fontWeight = FontWeight.Bold))
                            }
                            Text(item.summary, style = MaterialTheme.typography.bodyMedium.copy(color = MutedGray, fontSize = 12.sp))
                        }
                        IconButton(onClick = { onDeleteNews(item.id) }) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = "حذف الخبر", tint = CrimsonRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminVideosSection(
    videos: List<CloudVideoDoc>,
    isSaving: Boolean,
    onPublishVideo: (
        title: String,
        category: String,
        presenter: String,
        duration: String,
        description: String,
        streamOrYoutubeUrl: String,
        onSuccess: () -> Unit,
    ) -> Unit,
    onDeleteVideo: (videoId: String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("تغطيات خاصة") }
    var presenter by remember { mutableStateOf("قناة الأنصاري للإنتاج الفني") }
    var duration by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val colors = adminFieldColors()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AlAnsariGoldCard {
            Text(
                text = "إضافة فيديو أو برنامج لمكتبة الفيديوهات",
                style = MaterialTheme.typography.titleMedium.copy(color = GoldBright, fontWeight = FontWeight.Bold),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "الصق رابط يوتيوب (يعمل تلقائياً داخل التطبيق) أو رابط فيديو مباشر (MP4 / HLS).",
                style = MaterialTheme.typography.bodyMedium.copy(color = MutedGray, fontSize = 12.sp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("عنوان الفيديو أو الحلقة *") },
                singleLine = true,
                colors = colors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_video_title_input"),
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("رابط يوتيوب أو رابط الفيديو المباشر *") },
                placeholder = { Text("https://youtu.be/... أو https://.../video.mp4") },
                singleLine = true,
                colors = colors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_video_url_input"),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("التصنيف") },
                    singleLine = true,
                    colors = colors,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("المدة (مثال: 15:20)") },
                    singleLine = true,
                    colors = colors,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = presenter,
                onValueChange = { presenter = it },
                label = { Text("الخطيب / المقدم / الجهة") },
                singleLine = true,
                colors = colors,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("وصف مختصر") },
                minLines = 2,
                colors = colors,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    onPublishVideo(title, category, presenter, duration, description, url) {
                        title = ""
                        url = ""
                        description = ""
                    }
                },
                enabled = !isSaving && title.isNotBlank() && url.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = PureWhite),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("admin_publish_video_button"),
            ) {
                Icon(Icons.Filled.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("إضافة ونشر الفيديو", style = MaterialTheme.typography.labelLarge)
            }
        }

        Text(
            text = "الفيديوهات المضافة سحابياً (${videos.size})",
            style = MaterialTheme.typography.titleMedium.copy(color = GoldBright),
        )
        if (videos.isEmpty()) {
            AlAnsariGoldCard {
                Text("لا توجد فيديوهات مضافة يدوياً بعد.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            videos.forEach { item ->
                AlAnsariGoldCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, style = MaterialTheme.typography.titleSmall.copy(color = GoldBright, fontWeight = FontWeight.Bold))
                            Text("${item.category} • ${item.presenter}", style = MaterialTheme.typography.bodyMedium.copy(color = MutedGray, fontSize = 12.sp))
                        }
                        IconButton(onClick = { onDeleteVideo(item.id) }) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = "حذف الفيديو", tint = CrimsonRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCoveragesSection(
    coverages: List<CloudCoverageDoc>,
    isSaving: Boolean,
    onPublishCoverage: (
        title: String,
        location: String,
        reporter: String,
        statusLabel: String,
        summary: String,
        videoUrl: String,
        isLiveField: Boolean,
        onSuccess: () -> Unit,
    ) -> Unit,
    onDeleteCoverage: (coverageId: String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var reporter by remember { mutableStateOf("وحدة البث الخارجي SNG1080") }
    var statusLabel by remember { mutableStateOf("تغطية ميدانية") }
    var videoUrl by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var isLiveField by remember { mutableStateOf(true) }
    val colors = adminFieldColors()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AlAnsariGoldCard {
            Text(
                text = "نشر تغطية ميدانية (SNG1080)",
                style = MaterialTheme.typography.titleMedium.copy(color = GoldBright, fontWeight = FontWeight.Bold),
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("عنوان التغطية الميدانية *") },
                singleLine = true,
                colors = colors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_coverage_title_input"),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("الموقع *") },
                    singleLine = true,
                    colors = colors,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_coverage_location_input"),
                )
                OutlinedTextField(
                    value = statusLabel,
                    onValueChange = { statusLabel = it },
                    label = { Text("الشارة") },
                    singleLine = true,
                    colors = colors,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = reporter,
                onValueChange = { reporter = it },
                label = { Text("المراسل / وحدة البث") },
                singleLine = true,
                colors = colors,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = videoUrl,
                onValueChange = { videoUrl = it },
                label = { Text("رابط الفيديو أو البث (اختياري)") },
                singleLine = true,
                colors = colors,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = summary,
                onValueChange = { summary = it },
                label = { Text("ملخص التغطية") },
                minLines = 2,
                colors = colors,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("شارة بث ميداني مباشر", style = MaterialTheme.typography.bodyMedium.copy(color = PureWhite))
                Switch(
                    checked = isLiveField,
                    onCheckedChange = { isLiveField = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = CrimsonRed),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    onPublishCoverage(title, location, reporter, statusLabel, summary, videoUrl, isLiveField) {
                        title = ""
                        location = ""
                        videoUrl = ""
                        summary = ""
                    }
                },
                enabled = !isSaving && title.isNotBlank() && location.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = PureWhite),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("admin_publish_coverage_button"),
            ) {
                Icon(Icons.Filled.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("نشر التغطية الميدانية", style = MaterialTheme.typography.labelLarge)
            }
        }

        Text(
            text = "التغطيات الميدانية المنشورة (${coverages.size})",
            style = MaterialTheme.typography.titleMedium.copy(color = GoldBright),
        )
        if (coverages.isEmpty()) {
            AlAnsariGoldCard {
                Text("لا توجد تغطيات ميدانية مضافة بعد.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            coverages.forEach { item ->
                AlAnsariGoldCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, style = MaterialTheme.typography.titleSmall.copy(color = GoldBright, fontWeight = FontWeight.Bold))
                            Text("${item.location} • ${item.reporter}", style = MaterialTheme.typography.bodyMedium.copy(color = MutedGray, fontSize = 12.sp))
                        }
                        IconButton(onClick = { onDeleteCoverage(item.id) }) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = "حذف التغطية", tint = CrimsonRed)
                        }
                    }
                }
            }
        }
    }
}
