package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LiveTv
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.data.auth.GoogleAuthHelper
import com.example.data.model.bookingStatusArabic
import com.example.ui.components.AlAnsariGoldCard
import com.example.ui.components.AlAnsariSubTopBar
import com.example.ui.components.CrimsonPillBadge
import com.example.ui.components.GoldOutlineChip
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDarkBorder
import com.example.ui.theme.MutedGray
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.viewmodel.AlAnsariUiState

const val BOOKING_WHATSAPP_NUMBER = "9647801705580"

fun buildBookingWhatsAppUrl(
    serviceType: String,
    senderName: String,
    phone: String,
    governorate: String,
    eventDate: String,
    topic: String,
    details: String,
    whatsAppDigits: String = BOOKING_WHATSAPP_NUMBER,
): String {
    val body = buildString {
        appendLine("السلام عليكم، أود طلب حجز تغطية وبث مباشر – قناة الأنصاري (SNG1080):")
        appendLine("• نوع الخدمة: ${serviceType.trim()}")
        appendLine("• الاسم / الجهة: ${senderName.trim()}")
        appendLine("• رقم الهاتف: ${phone.trim()}")
        appendLine("• المحافظة والمنطقة: ${governorate.trim()}")
        appendLine("• التاريخ والوقت: ${eventDate.trim()}")
        appendLine("• عنوان الفعالية: ${topic.trim()}")
        if (details.isNotBlank()) {
            appendLine("• ملاحظات إضافية: ${details.trim()}")
        }
    }.trim()
    return "https://wa.me/$whatsAppDigits?text=${Uri.encode(body)}"
}

private fun launchWhatsAppWithUrl(
    context: Context,
    url: String,
    onFailureMessage: (String) -> Unit,
) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }.onFailure {
        onFailureMessage("تعذر فتح تطبيق واتساب على هذا الجهاز، تأكد من تثبيت واتساب")
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookingCoverageScreen(
    uiState: AlAnsariUiState,
    onSubmitBooking: (
        senderName: String,
        phone: String,
        governorate: String,
        serviceType: String,
        eventDate: String,
        topic: String,
        details: String,
        onSubmitted: () -> Unit,
    ) -> Unit,
    onShowMessage: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val serviceTypes = remember {
        listOf(
            "تغطية وبث مباشر",
            "بث مباشر فقط SNG1080",
            "تصوير وتوثيق فقط",
        )
    }

    var selectedServiceType by remember { mutableStateOf(serviceTypes.first()) }
    var senderName by remember(uiState.currentUser?.displayName) {
        mutableStateOf(uiState.currentUser?.displayName.orEmpty())
    }
    var phone by remember { mutableStateOf("") }
    var governorate by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var isSigningIn by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = GoldBright,
        unfocusedBorderColor = GoldDarkBorder,
        focusedLabelColor = GoldBright,
        unfocusedLabelColor = MutedGray,
        focusedTextColor = PureWhite,
        unfocusedTextColor = PureWhite,
    )

    fun validateFields(): Boolean {
        if (senderName.trim().length < 2) {
            onShowMessage("يرجى كتابة الاسم أو اسم الجهة المنظمة")
            return false
        }
        if (phone.trim().length < 5) {
            onShowMessage("يرجى كتابة رقم هاتف للتواصل")
            return false
        }
        if (governorate.trim().length < 2) {
            onShowMessage("يرجى كتابة المحافظة والمنطقة")
            return false
        }
        if (eventDate.trim().length < 2) {
            onShowMessage("يرجى تحديد تاريخ وموعد الفعالية")
            return false
        }
        if (topic.trim().length < 2) {
            onShowMessage("يرجى كتابة عنوان الفعالية أو المجلس")
            return false
        }
        return true
    }

    fun openWhatsAppWithCurrentDetails() {
        if (!validateFields()) return
        val waUrl = buildBookingWhatsAppUrl(
            serviceType = selectedServiceType,
            senderName = senderName,
            phone = phone,
            governorate = governorate,
            eventDate = eventDate,
            topic = topic,
            details = details,
        )
        launchWhatsAppWithUrl(context, waUrl, onShowMessage)
    }

    fun submitNow() {
        if (!validateFields()) return

        val waUrlSnapshot = buildBookingWhatsAppUrl(
            serviceType = selectedServiceType,
            senderName = senderName,
            phone = phone,
            governorate = governorate,
            eventDate = eventDate,
            topic = topic,
            details = details,
        )

        val doSend = {
            onSubmitBooking(
                senderName,
                phone,
                governorate,
                selectedServiceType,
                eventDate,
                topic,
                details,
            ) {
                phone = ""
                governorate = ""
                eventDate = ""
                topic = ""
                details = ""
                launchWhatsAppWithUrl(context, waUrlSnapshot, onShowMessage)
            }
        }

        if (uiState.currentUser == null) {
            isSigningIn = true
            GoogleAuthHelper.onGoogleSignInClicked(
                context = context,
                credentialManager = credentialManager,
                scope = coroutineScope,
                onAuthSuccess = {
                    isSigningIn = false
                    doSend()
                },
                onAuthError = { msg ->
                    isSigningIn = false
                    onShowMessage(msg)
                },
                onAuthCancelled = { _ ->
                    isSigningIn = false
                    onShowMessage("يتطلب توثيق الطلب تأكيد Google، أو يمكنك الإرسال لواتساب مباشرة")
                },
            )
        } else {
            doSend()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlAnsariSubTopBar(
            title = "طلب حجز تغطية وبث مباشر",
            badgeText = "وحدة SNG1080 الميدانية",
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
            // Hero Explanation Card
            item {
                AlAnsariGoldCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LiveTv,
                            contentDescription = null,
                            tint = GoldBright,
                            modifier = Modifier.size(30.dp),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "خدمة النقل المباشر والتغطيات الميدانية",
                                style = MaterialTheme.typography.titleMedium.copy(color = GoldBright),
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "احجز فريق التصوير أو وحدة البث الفضائي المباشر لتغطية المجالس الحسينية، المؤتمرات، والفعاليات الرسمية في جميع المحافظات.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            // Account verification status card
            item {
                AlAnsariGoldCard {
                    if (uiState.currentUser != null) {
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
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = GoldBright,
                                    modifier = Modifier.size(22.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "مسجل بالحساب: ${uiState.currentUser.email ?: uiState.currentUser.displayName.orEmpty()}",
                                    style = MaterialTheme.typography.labelLarge.copy(color = GoldBright),
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "سجّل الدخول بحساب Google لتوثيق طلبك ومتابعة حالة الموافقة مباشرة من إدارة القناة.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            OutlinedButton(
                                onClick = {
                                    isSigningIn = true
                                    GoogleAuthHelper.onGoogleSignInClicked(
                                        context = context,
                                        credentialManager = credentialManager,
                                        scope = coroutineScope,
                                        onAuthSuccess = {
                                            isSigningIn = false
                                            onShowMessage("تم تسجيل الدخول بنجاح، يمكنك الآن إرسال الطلب")
                                        },
                                        onAuthError = { msg ->
                                            isSigningIn = false
                                            onShowMessage(msg)
                                        },
                                        onAuthCancelled = { reason ->
                                            isSigningIn = false
                                            onShowMessage(reason)
                                        },
                                    )
                                },
                                enabled = !isSigningIn,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("booking_google_signin_button"),
                            ) {
                                if (isSigningIn) {
                                    CircularProgressIndicator(
                                        color = GoldBright,
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.AccountCircle,
                                        contentDescription = null,
                                        tint = GoldBright,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isSigningIn) "جاري فتح نافذة Google..." else "تسجيل الدخول بحساب Google",
                                    color = GoldBright,
                                )
                            }
                        }
                    }
                }
            }

            // Booking Form Card
            item {
                AlAnsariGoldCard {
                    Text(
                        text = "استمارة حجز التغطية",
                        style = MaterialTheme.typography.titleMedium.copy(color = GoldBright),
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "نوع الخدمة المطلوبة",
                        style = MaterialTheme.typography.labelLarge.copy(color = PureWhite),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        serviceTypes.forEach { option ->
                            GoldOutlineChip(
                                text = option,
                                selected = selectedServiceType == option,
                                onClick = { selectedServiceType = option },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = senderName,
                        onValueChange = { senderName = it },
                        label = { Text("الاسم الكامل أو اسم الجهة / الموكب") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("booking_name_input"),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف / واتساب للتواصل") },
                        placeholder = { Text("07XXXXXXXXX") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("booking_phone_input"),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = governorate,
                        onValueChange = { governorate = it },
                        label = { Text("المحافظة والمنطقة") },
                        placeholder = { Text("مثال: النجف الأشرف / حي الحسين") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = eventDate,
                        onValueChange = { eventDate = it },
                        label = { Text("التاريخ والوقت المقترح") },
                        placeholder = { Text("مثال: الجمعة 8:00 مساءً") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("عنوان الفعالية أو المجلس") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text("ملاحظات وتفاصيل إضافية (اختياري)") },
                        maxLines = 4,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { submitNow() },
                        enabled = !uiState.isSavingCloud && !isSigningIn,
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = PureWhite),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("booking_submit_button"),
                    ) {
                        if (uiState.isSavingCloud) {
                            CircularProgressIndicator(
                                color = PureWhite,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.currentUser == null) {
                                "حفظ الطلب والانتقال إلى واتساب"
                            } else {
                                "إرسال الحجز والانتقال إلى واتساب"
                            },
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { openWhatsAppWithCurrentDetails() },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = PureWhite),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("booking_whatsapp_button"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "واتساب الحجز",
                            modifier = Modifier.size(19.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إرسال المعلومات لواتساب مباشرة (+964 780 170 5580)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            val tgUrl = uiState.settings.telegramUrl.ifBlank { "https://t.me/SNG1080" }
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(tgUrl)))
                            }
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("أو التنسيق عبر تيليجرام @SNG1080", color = GoldBright)
                    }
                }
            }

            // User's Previous Bookings
            if (uiState.myBookings.isNotEmpty()) {
                item {
                    Text(
                        text = "طلباتي السابقة",
                        style = MaterialTheme.typography.titleMedium.copy(color = GoldBright),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                items(uiState.myBookings, key = { it.id }) { booking ->
                    AlAnsariGoldCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = booking.topic,
                                style = MaterialTheme.typography.titleSmall.copy(color = PureWhite, fontWeight = FontWeight.Bold),
                                modifier = Modifier.weight(1f),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            CrimsonPillBadge(text = bookingStatusArabic(booking.status), horizontalPadding = 10.dp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${booking.serviceType} • ${booking.governorate} • ${booking.eventDate}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = GoldBright, fontSize = 12.5.sp),
                        )
                        if (!booking.adminNote.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "رد الإدارة: ${booking.adminNote}",
                                style = MaterialTheme.typography.bodyMedium.copy(color = PureWhite),
                            )
                        }
                    }
                }
            }
        }
    }
}
