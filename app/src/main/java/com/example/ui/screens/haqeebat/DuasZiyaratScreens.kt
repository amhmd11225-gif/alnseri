package com.example.ui.screens.haqeebat

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.haqeebat.DuaItem
import com.example.data.haqeebat.HaqeebatRepository
import com.example.data.haqeebat.ZiyaratItem
import com.example.ui.components.AlAnsariGoldCard
import com.example.ui.components.AlAnsariSubTopBar
import com.example.ui.components.CrimsonPillBadge
import com.example.ui.components.GoldOutlineChip
import com.example.ui.theme.CardSurfaceTop
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDarkBorder
import com.example.ui.theme.GoldSpecular
import com.example.ui.theme.MutedGray
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftCream
import com.example.ui.theme.SoftWhite

@Composable
fun DuasScreen(
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    onBackClick: () -> Unit,
) {
    val categories = remember {
        listOf("الكل", "المفضلة") + HaqeebatRepository.duasList.map { it.category }.distinct()
    }
    var selectedCategory by remember { mutableStateOf("الكل") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedDua by remember { mutableStateOf<DuaItem?>(null) }

    if (selectedDua != null) {
        PrayerReaderView(
            id = selectedDua!!.id,
            title = selectedDua!!.title,
            subtitle = "${selectedDua!!.category} • ${selectedDua!!.occasion}",
            body = selectedDua!!.body,
            isFavorite = favoriteIds.contains(selectedDua!!.id),
            hasAshuraCounter = false,
            onToggleFavorite = { onToggleFavorite(selectedDua!!.id) },
            onShowMessage = onShowMessage,
            onBackClick = { selectedDua = null },
        )
        return
    }

    val filtered = remember(selectedCategory, searchQuery, favoriteIds) {
        HaqeebatRepository.duasList.filter { item ->
            val catOk = when (selectedCategory) {
                "الكل" -> true
                "المفضلة" -> favoriteIds.contains(item.id)
                else -> item.category == selectedCategory
            }
            val q = searchQuery.trim()
            val queryOk = q.isEmpty() || item.title.contains(q) || item.body.contains(q)
            catOk && queryOk
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlAnsariSubTopBar(
            title = "الأدعية والمناجاة",
            badgeText = "مفاتيح الجنان والصحيفة السجادية",
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحث في الأدعية...", color = MutedGray) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = GoldBright) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldBright,
                    unfocusedBorderColor = GoldDarkBorder,
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite,
                    focusedContainerColor = CardSurfaceTop,
                    unfocusedContainerColor = CardSurfaceTop,
                ),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                categories.forEach { cat ->
                    GoldOutlineChip(
                        text = cat,
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(filtered, key = { it.id }) { dua ->
                    val isFav = favoriteIds.contains(dua.id)
                    AlAnsariGoldCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { selectedDua = dua },
                        testTag = "dua_card_${dua.id}",
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                CrimsonPillBadge(
                                    text = dua.category,
                                    horizontalPadding = 10.dp,
                                    verticalPadding = 2.dp,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = dua.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = GoldBright,
                                        fontWeight = FontWeight.ExtraBold,
                                    ),
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = dua.occasion,
                                    style = MaterialTheme.typography.bodySmall.copy(color = SoftCream),
                                )
                            }
                            IconButton(
                                onClick = { onToggleFavorite(dua.id) },
                                modifier = Modifier.size(48.dp),
                            ) {
                                Icon(
                                    imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "تفضيل الدعاء",
                                    tint = if (isFav) CrimsonRed else MutedGray,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ZiyaratScreen(
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    onBackClick: () -> Unit,
) {
    var onlyFavorites by remember { mutableStateOf(false) }
    var selectedZiyarat by remember { mutableStateOf<ZiyaratItem?>(null) }

    if (selectedZiyarat != null) {
        PrayerReaderView(
            id = selectedZiyarat!!.id,
            title = selectedZiyarat!!.title,
            subtitle = "${selectedZiyarat!!.shrineName} • ${selectedZiyarat!!.subtitle}",
            body = selectedZiyarat!!.body,
            isFavorite = favoriteIds.contains(selectedZiyarat!!.id),
            hasAshuraCounter = selectedZiyarat!!.hasCounterHelper,
            onToggleFavorite = { onToggleFavorite(selectedZiyarat!!.id) },
            onShowMessage = onShowMessage,
            onBackClick = { selectedZiyarat = null },
        )
        return
    }

    val list = remember(onlyFavorites, favoriteIds) {
        if (onlyFavorites) {
            HaqeebatRepository.ziyaratList.filter { favoriteIds.contains(it.id) }
        } else {
            HaqeebatRepository.ziyaratList
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlAnsariSubTopBar(
            title = "الزيارات المباركة",
            badgeText = "زيارات العتبات المقدسة",
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GoldOutlineChip(
                    text = "جميع الزيارات",
                    selected = !onlyFavorites,
                    onClick = { onlyFavorites = false },
                )
                GoldOutlineChip(
                    text = "المفضلة",
                    selected = onlyFavorites,
                    onClick = { onlyFavorites = true },
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(list, key = { it.id }) { ziyarat ->
                    val isFav = favoriteIds.contains(ziyarat.id)
                    AlAnsariGoldCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { selectedZiyarat = ziyarat },
                        testTag = "ziyarat_card_${ziyarat.id}",
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                CrimsonPillBadge(
                                    text = ziyarat.shrineName,
                                    horizontalPadding = 10.dp,
                                    verticalPadding = 2.dp,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = ziyarat.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = GoldBright,
                                        fontWeight = FontWeight.ExtraBold,
                                    ),
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = ziyarat.subtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(color = SoftCream),
                                )
                            }
                            IconButton(
                                onClick = { onToggleFavorite(ziyarat.id) },
                                modifier = Modifier.size(48.dp),
                            ) {
                                Icon(
                                    imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "تفضيل الزيارة",
                                    tint = if (isFav) CrimsonRed else MutedGray,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerReaderView(
    id: String,
    title: String,
    subtitle: String,
    body: String,
    isFavorite: Boolean,
    hasAshuraCounter: Boolean,
    onToggleFavorite: () -> Unit,
    onShowMessage: (String) -> Unit,
    onBackClick: () -> Unit,
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    var fontSizeSp by remember(id) { mutableFloatStateOf(19.5f) }
    var ashuraCounter by remember(id) { mutableIntStateOf(0) }
    var ashuraModeIsSalam by remember(id) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlAnsariSubTopBar(
            title = title,
            badgeText = subtitle,
            onBackClick = onBackClick,
            trailingAction = {
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "تفضيل",
                        tint = if (isFavorite) CrimsonRed else GoldBright,
                    )
                }
            },
        )

        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GoldOutlineChip(
                    text = "تكبير +",
                    selected = false,
                    onClick = { fontSizeSp = (fontSizeSp + 2f).coerceAtMost(30f) },
                )
                GoldOutlineChip(
                    text = "تصغير -",
                    selected = false,
                    onClick = { fontSizeSp = (fontSizeSp - 2f).coerceAtLeast(15f) },
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = {
                        clipboard.setText(AnnotatedString("$title\n\n$body"))
                        onShowMessage("تم نسخ النص بنجاح")
                    },
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "نسخ", tint = GoldBright)
                }
                IconButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "$title\n\n$body")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, title))
                    },
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(Icons.Filled.Share, contentDescription = "مشاركة", tint = GoldBright)
                }
            }

            if (hasAshuraCounter) {
                Spacer(modifier = Modifier.height(8.dp))
                AlAnsariGoldCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (ashuraModeIsSalam) "عدّاد السلام (100 مرة)" else "عدّاد اللعن (100 مرة)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = GoldSpecular,
                                    fontWeight = FontWeight.Bold,
                                ),
                            )
                            Text(
                                text = "$ashuraCounter / 100",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color = GoldBright,
                                    fontWeight = FontWeight.ExtraBold,
                                ),
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            GoldOutlineChip(
                                text = if (ashuraModeIsSalam) "للعن" else "للسلام",
                                selected = false,
                                onClick = {
                                    ashuraModeIsSalam = !ashuraModeIsSalam
                                    ashuraCounter = 0
                                },
                            )
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (ashuraCounter < 100) ashuraCounter++
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                modifier = Modifier.testTag("ashura_100_tap_button"),
                            ) {
                                Text("+1", color = PureWhite, fontWeight = FontWeight.ExtraBold)
                            }
                            IconButton(onClick = { ashuraCounter = 0 }, modifier = Modifier.size(44.dp)) {
                                Icon(Icons.Filled.Refresh, contentDescription = "تصفير", tint = GoldBright)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                AlAnsariGoldCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(18.dp),
                ) {
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = SoftWhite,
                            fontSize = fontSizeSp.sp,
                            lineHeight = (fontSizeSp * 1.78f).sp,
                        ),
                        textAlign = TextAlign.Start,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
