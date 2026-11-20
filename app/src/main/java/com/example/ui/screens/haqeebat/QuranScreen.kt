package com.example.ui.screens.haqeebat

import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.haqeebat.HaqeebatRepository
import com.example.data.haqeebat.QuranSurahMeta
import com.example.ui.components.AlAnsariGoldCard
import com.example.ui.components.AlAnsariSubTopBar
import com.example.ui.components.CrimsonPillBadge
import com.example.ui.components.GoldOutlineChip
import com.example.ui.theme.CardSurfaceTop
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.ElevatedDark
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDarkBorder
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSpecular
import com.example.ui.theme.MutedGray
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftCream
import com.example.ui.theme.SoftWhite

@Composable
fun QuranScreen(
    bookmarkedSurahNumber: Int,
    onBookmarkSurah: (Int) -> Unit,
    onShowMessage: (String) -> Unit,
    onBackClick: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSurah by remember { mutableStateOf<QuranSurahMeta?>(null) }

    val filteredSurahs = remember(searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) {
            HaqeebatRepository.allSurahsMeta
        } else {
            HaqeebatRepository.allSurahsMeta.filter {
                it.nameAr.contains(q) || it.number.toString() == q || it.revelationType.contains(q)
            }
        }
    }

    val bookmarkedMeta = remember(bookmarkedSurahNumber) {
        HaqeebatRepository.allSurahsMeta.firstOrNull { it.number == bookmarkedSurahNumber }
            ?: HaqeebatRepository.allSurahsMeta.first()
    }

    if (selectedSurah != null) {
        SurahReaderView(
            surah = selectedSurah!!,
            isBookmarked = selectedSurah!!.number == bookmarkedSurahNumber,
            onBookmarkClick = { onBookmarkSurah(selectedSurah!!.number) },
            onShowMessage = onShowMessage,
            onBackClick = { selectedSurah = null },
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlAnsariSubTopBar(
            title = "القرآن الكريم",
            badgeText = "114 سورة • المصحف الشريف",
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            // Khatma Bookmark 3D Card
            AlAnsariGoldCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { selectedSurah = bookmarkedMeta },
                testTag = "quran_bookmark_jump_card",
                contentPadding = PaddingValues(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "علامة الختمة القرآنية",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = GoldSpecular,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "سورة ${bookmarkedMeta.nameAr} (${bookmarkedMeta.number})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.ExtraBold,
                            ),
                        )
                    }
                    CrimsonPillBadge(
                        text = "متابعة القراءة",
                        horizontalPadding = 14.dp,
                        verticalPadding = 6.dp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحث عن اسم السورة أو رقمها...", color = MutedGray) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = GoldBright) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quran_search_input"),
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

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(filteredSurahs, key = { it.number }) { surah ->
                    val isBookmarked = surah.number == bookmarkedSurahNumber
                    AlAnsariGoldCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { selectedSurah = surah },
                        testTag = "quran_surah_${surah.number}",
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
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
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(ElevatedDark)
                                        .border(1.2.dp, GoldBright, CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "${surah.number}",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = GoldBright,
                                            fontWeight = FontWeight.ExtraBold,
                                        ),
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "سورة ${surah.nameAr}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = PureWhite,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                    )
                                    Text(
                                        text = "${surah.revelationType} • ${surah.versesCount} آية",
                                        style = MaterialTheme.typography.bodySmall.copy(color = SoftCream),
                                    )
                                }
                            }
                            IconButton(
                                onClick = { onBookmarkSurah(surah.number) },
                                modifier = Modifier.size(48.dp),
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = "تثبيت علامة الختمة",
                                    tint = if (isBookmarked) GoldBright else MutedGray,
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
private fun SurahReaderView(
    surah: QuranSurahMeta,
    isBookmarked: Boolean,
    onBookmarkClick: () -> Unit,
    onShowMessage: (String) -> Unit,
    onBackClick: () -> Unit,
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var fontSizeSp by remember { mutableFloatStateOf(21f) }
    var isLoading by remember { mutableStateOf(true) }
    var verses by remember { mutableStateOf<List<String>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var reloadNonce by remember { mutableIntStateOf(0) }

    LaunchedEffect(surah.number, reloadNonce) {
        isLoading = true
        errorMessage = null
        val res = HaqeebatRepository.loadSurahVerses(surah.number)
        isLoading = false
        res.onSuccess { verses = it }
            .onFailure {
                errorMessage = "يتطلب تحميل نص سورة ${surah.nameAr} اتصالاً بالإنترنت في المرة الأولى"
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlAnsariSubTopBar(
            title = "سورة ${surah.nameAr}",
            badgeText = "${surah.revelationType} • ${surah.versesCount} آية",
            onBackClick = onBackClick,
            trailingAction = {
                IconButton(onClick = onBookmarkClick, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "حفظ علامة القراءة",
                        tint = GoldBright,
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
                    text = "تكبير الخط +",
                    selected = false,
                    onClick = { fontSizeSp = (fontSizeSp + 2f).coerceAtMost(32f) },
                )
                GoldOutlineChip(
                    text = "تصغير -",
                    selected = false,
                    onClick = { fontSizeSp = (fontSizeSp - 2f).coerceAtLeast(16f) },
                )
                Spacer(modifier = Modifier.weight(1f))
                if (verses.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(verses.joinToString("\n")))
                            onShowMessage("تم نسخ آيات سورة ${surah.nameAr}")
                        },
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "نسخ", tint = GoldBright)
                    }
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "سورة ${surah.nameAr}\n\n${verses.joinToString(" ")}",
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "مشاركة السورة"))
                        },
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "مشاركة", tint = GoldBright)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = GoldBright)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("جاري تحميل الآيات المباركة...", color = SoftCream)
                        }
                    }
                }

                errorMessage != null -> {
                    AlAnsariGoldCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodyLarge.copy(color = PureWhite),
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { reloadNonce++ },
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                            ) {
                                Text("إعادة المحاولة", color = PureWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        if (surah.number != 1 && surah.number != 9) {
                            Text(
                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = GoldBright,
                                    fontWeight = FontWeight.ExtraBold,
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                textAlign = TextAlign.Center,
                            )
                        }
                        AlAnsariGoldCard(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(18.dp),
                        ) {
                            Text(
                                text = verses.joinToString("  "),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = SoftWhite,
                                    fontSize = fontSizeSp.sp,
                                    lineHeight = (fontSizeSp * 1.75f).sp,
                                ),
                                textAlign = TextAlign.Justify,
                            )
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                    }
                }
            }
        }
    }
}
