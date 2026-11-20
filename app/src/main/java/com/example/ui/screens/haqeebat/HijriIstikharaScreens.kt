package com.example.ui.screens.haqeebat

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.haqeebat.HaqeebatRepository
import com.example.data.haqeebat.IstikharaOutcome
import com.example.data.haqeebat.PrayerTimesCalculator
import com.example.ui.components.AlAnsariGoldCard
import com.example.ui.components.AlAnsariSubTopBar
import com.example.ui.components.CrimsonPillBadge
import com.example.ui.components.GoldOutlineChip
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CrimsonRedBright
import com.example.ui.theme.CrimsonRedDark
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSpecular
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftCream
import com.example.ui.theme.SoftWhite
import kotlin.random.Random

@Composable
fun HijriCalendarScreen(
    hijriOffsetDays: Int,
    onAdjustOffset: (Int) -> Unit,
    onBackClick: () -> Unit,
) {
    val hijriInfo = remember(hijriOffsetDays) {
        PrayerTimesCalculator.getCurrentHijriDate(hijriOffsetDays)
    }
    var selectedMonth by remember { mutableIntStateOf(0) } // 0 = All months

    val filteredOccasions = remember(selectedMonth) {
        if (selectedMonth == 0) {
            HaqeebatRepository.islamicOccasions
        } else {
            HaqeebatRepository.islamicOccasions.filter { it.monthNumber == selectedMonth }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlAnsariSubTopBar(
            title = "التقويم الهجري والمناسبات",
            badgeText = "${hijriInfo.monthNameAr} ${hijriInfo.year} هـ",
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Today Hijri Date 3D Banner
            AlAnsariGoldCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = hijriInfo.weekdayAr,
                        style = MaterialTheme.typography.titleMedium.copy(color = SoftCream),
                    )
                    Text(
                        text = "${hijriInfo.day} ${hijriInfo.monthNameAr}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = GoldBright,
                            fontWeight = FontWeight.ExtraBold,
                        ),
                    )
                    Text(
                        text = "${hijriInfo.year} هـ  •  الموافق ${hijriInfo.gregorianFormatted} م",
                        style = MaterialTheme.typography.bodyMedium.copy(color = PureWhite),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        GoldOutlineChip(
                            text = "-1 يوم",
                            selected = false,
                            onClick = { onAdjustOffset(-1) },
                        )
                        CrimsonPillBadge(
                            text = "رؤية الهلال (${if (hijriOffsetDays >= 0) "+$hijriOffsetDays" else "$hijriOffsetDays"})",
                            horizontalPadding = 12.dp,
                            verticalPadding = 3.dp,
                        )
                        GoldOutlineChip(
                            text = "+1 يوم",
                            selected = false,
                            onClick = { onAdjustOffset(1) },
                        )
                    }
                }
            }

            // Month Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GoldOutlineChip(
                    text = "كل الأشهر",
                    selected = selectedMonth == 0,
                    onClick = { selectedMonth = 0 },
                )
                PrayerTimesCalculator.hijriMonthsArabic.forEachIndexed { idx, name ->
                    val monthNum = idx + 1
                    GoldOutlineChip(
                        text = name,
                        selected = selectedMonth == monthNum,
                        onClick = { selectedMonth = monthNum },
                    )
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(filteredOccasions, key = { it.id }) { occ ->
                    val isCurrentMonth = occ.monthNumber == hijriInfo.month
                    val monthName = PrayerTimesCalculator.hijriMonthsArabic[(occ.monthNumber - 1).coerceIn(0, 11)]
                    AlAnsariGoldCard(
                        modifier = Modifier.fillMaxWidth(),
                        accentBorderColor = if (isCurrentMonth) EmeraldAccent else GoldBright,
                        contentPadding = PaddingValues(14.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    CrimsonPillBadge(
                                        text = "${occ.day} $monthName",
                                        horizontalPadding = 10.dp,
                                        verticalPadding = 2.dp,
                                    )
                                    CrimsonPillBadge(
                                        text = occ.category,
                                        horizontalPadding = 8.dp,
                                        verticalPadding = 2.dp,
                                        gradientColors = if (occ.category == "عزاء") {
                                            listOf(CrimsonRedDark, CrimsonRedBright, CrimsonRedDark)
                                        } else {
                                            listOf(EmeraldAccent, EmeraldAccent, EmeraldAccent)
                                        },
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = occ.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = GoldBright,
                                        fontWeight = FontWeight.ExtraBold,
                                    ),
                                )
                                Text(
                                    text = occ.note,
                                    style = MaterialTheme.typography.bodySmall.copy(color = SoftCream),
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
fun IstikharaFiqhScreen(
    onBackClick: () -> Unit,
) {
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Istikhara, 1 = Fiqh Q&A
    var currentResult by remember { mutableStateOf<IstikharaOutcome?>(null) }
    var expandedFiqhIds by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlAnsariSubTopBar(
            title = "الاستخارة والمسائل الشرعية",
            badgeText = "الإرشاد الديني والفقهي",
            onBackClick = onBackClick,
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
            ) {
                GoldOutlineChip(
                    text = "الاستخارة بالقرآن الكريم",
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    modifier = Modifier.weight(1f),
                    testTag = "tab_istikhara",
                )
                GoldOutlineChip(
                    text = "المسائل الشرعية",
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    modifier = Modifier.weight(1f),
                    testTag = "tab_fiqh",
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (activeTab == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AlAnsariGoldCard(modifier = Modifier.fillMaxWidth()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            CrimsonPillBadge(text = "آداب الاستخارة المأثورة")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "1. توضأ واستقبل القبلة وصلِّ على محمد وآل محمد (3 مرات).\n2. استحضر نيتك بوضوح وقل: «أستخير الله برحمته خيرة في عافية».\n3. اضغط على زر الاستخارة للتوكل على الله.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = SoftWhite,
                                    lineHeight = 23.sp,
                                ),
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    currentResult = HaqeebatRepository.istikharaPool.random(Random(System.nanoTime()))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("istikhara_draw_button"),
                            ) {
                                Text(
                                    text = if (currentResult == null) "توكل على الله وخذ الاستخارة" else "استخارة جديدة بنية أخرى",
                                    color = PureWhite,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                )
                            }
                        }
                    }

                    currentResult?.let { res ->
                        val verdictColors = when (res.verdictColorKey) {
                            "good" -> listOf(EmeraldAccent, EmeraldAccent, EmeraldAccent)
                            "medium" -> listOf(GoldPrimary, GoldBright, GoldPrimary)
                            else -> listOf(CrimsonRedDark, CrimsonRedBright, CrimsonRedDark)
                        }
                        AlAnsariGoldCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("istikhara_result_card"),
                            accentBorderColor = verdictColors.first(),
                            contentPadding = PaddingValues(18.dp),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = "${res.surahName} • الآية ${res.ayahNumber}",
                                    style = MaterialTheme.typography.labelLarge.copy(color = GoldSpecular),
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = res.ayahText,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = GoldBright,
                                        fontWeight = FontWeight.ExtraBold,
                                        lineHeight = 32.sp,
                                    ),
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                CrimsonPillBadge(
                                    text = res.verdictTitle,
                                    horizontalPadding = 16.dp,
                                    verticalPadding = 5.dp,
                                    gradientColors = verdictColors,
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = res.advice,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = SoftWhite,
                                        lineHeight = 24.sp,
                                    ),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    items(HaqeebatRepository.fiqhQuestions, key = { it.id }) { fq ->
                        val expanded = expandedFiqhIds.contains(fq.id)
                        AlAnsariGoldCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                expandedFiqhIds = if (expanded) {
                                    expandedFiqhIds - fq.id
                                } else {
                                    expandedFiqhIds + fq.id
                                }
                            },
                            testTag = "fiqh_card_${fq.id}",
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    CrimsonPillBadge(
                                        text = fq.topic,
                                        horizontalPadding = 10.dp,
                                        verticalPadding = 2.dp,
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = fq.question,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = GoldBright,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                    )
                                }
                                Icon(
                                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = "عرض الجواب",
                                    tint = GoldBright,
                                )
                            }
                            AnimatedVisibility(visible = expanded) {
                                Column {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = fq.answer,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = SoftWhite,
                                            lineHeight = 24.sp,
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
}
