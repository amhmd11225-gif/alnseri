package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SocialLink
import com.example.data.model.SocialPlatform
import com.example.ui.components.AlAnsariGoldCard
import com.example.ui.components.AlAnsariSubTopBar
import com.example.ui.components.SocialBadgesRow
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.GoldBright
import com.example.ui.theme.MutedGray
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SocialFacebookBlue
import com.example.ui.theme.SocialInstagramPink
import com.example.ui.theme.SocialTelegramCyan
import com.example.ui.theme.SocialTikTokDark
import com.example.ui.theme.SocialYouTubeRed
import com.example.ui.viewmodel.AlAnsariUiState

@Composable
fun SocialLinksScreen(
    uiState: AlAnsariUiState,
    onShowMessage: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlAnsariSubTopBar(
            title = "مواقع التواصل الرسمية",
            badgeText = "المنصات المعتمدة",
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
            item {
                AlAnsariGoldCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تابع قناة الأنصاري الفضائية",
                                style = MaterialTheme.typography.titleMedium.copy(color = GoldBright),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "تابع جميع الحسابات والمنصات الرسمية المعتمدة لقناة الأنصاري الفضائية.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        SocialBadgesRow()
                    }
                }
            }

            items(uiState.socialLinks, key = { it.id }) { link ->
                SocialPlatformCard(
                    link = link,
                    onOpen = {
                        val safeUrl = if (link.url.startsWith("http")) link.url else "https://${link.url}"
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl)))
                        }.onFailure {
                            onShowMessage("تعذر فتح الرابط: $safeUrl")
                        }
                    },
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText(link.title, link.url))
                        onShowMessage("تم نسخ رابط ${link.title}")
                    },
                )
            }
        }
    }
}

@Composable
private fun SocialPlatformCard(
    link: SocialLink,
    onOpen: () -> Unit,
    onCopy: () -> Unit,
) {
    val badgeColor = when (link.platform) {
        SocialPlatform.FACEBOOK -> SocialFacebookBlue
        SocialPlatform.YOUTUBE -> SocialYouTubeRed
        SocialPlatform.TIKTOK -> SocialTikTokDark
        SocialPlatform.TELEGRAM -> SocialTelegramCyan
        SocialPlatform.INSTAGRAM -> SocialInstagramPink
        SocialPlatform.X_PLATFORM -> Color(0xFF1F1F1F)
        SocialPlatform.WEBSITE -> CrimsonRed
    }

    AlAnsariGoldCard(
        onClick = onOpen,
        testTag = "social_card_${link.id}",
        contentPadding = PaddingValues(14.dp),
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
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeColor)
                        .border(1.dp, GoldBright.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    when (link.platform) {
                        SocialPlatform.FACEBOOK -> Text("f", color = PureWhite, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                        SocialPlatform.YOUTUBE -> Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = PureWhite)
                        SocialPlatform.TIKTOK -> Text("♪", color = PureWhite, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        SocialPlatform.TELEGRAM -> Icon(Icons.Filled.Send, contentDescription = null, tint = PureWhite, modifier = Modifier.size(20.dp))
                        SocialPlatform.INSTAGRAM -> Text("IG", color = PureWhite, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                        SocialPlatform.X_PLATFORM -> Text("X", color = PureWhite, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        SocialPlatform.WEBSITE -> Icon(Icons.Filled.Language, contentDescription = null, tint = PureWhite, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = link.title,
                        style = MaterialTheme.typography.titleMedium.copy(color = PureWhite),
                    )
                    Text(
                        text = link.handle,
                        style = MaterialTheme.typography.labelMedium.copy(color = GoldBright),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = link.followersLabel,
                        style = MaterialTheme.typography.bodyMedium.copy(color = MutedGray, fontSize = 12.sp),
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "نسخ الرابط",
                        tint = GoldBright,
                    )
                }
                Button(
                    onClick = onOpen,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = PureWhite),
                    shape = RoundedCornerShape(50),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("فتح", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
