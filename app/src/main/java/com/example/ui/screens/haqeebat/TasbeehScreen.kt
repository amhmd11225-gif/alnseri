package com.example.ui.screens.haqeebat

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AlAnsariGoldCard
import com.example.ui.components.AlAnsariSubTopBar
import com.example.ui.components.CrimsonPillBadge
import com.example.ui.components.GoldOutlineChip
import com.example.ui.theme.CardSurfaceBottom
import com.example.ui.theme.CardSurfaceTop
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CrimsonRedBright
import com.example.ui.theme.CrimsonRedDark
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDarkBorder
import com.example.ui.theme.GoldMuted
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSpecular
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftCream
import kotlin.math.cos
import kotlin.math.sin

private data class TasbeehPreset(
    val id: String,
    val label: String,
    val target: Int,
    val isZahraCycle: Boolean = false,
)

@Composable
fun TasbeehScreen(
    totalLifetimeCount: Int,
    onIncrementTotal: (Int) -> Unit,
    onResetTotal: () -> Unit,
    onBackClick: () -> Unit,
) {
    val presets = remember {
        listOf(
            TasbeehPreset("zahra", "تسبيح الزهراء (ع)", 100, isZahraCycle = true),
            TasbeehPreset("subhan", "سبحان الله", 33),
            TasbeehPreset("hamd", "الحمد لله", 33),
            TasbeehPreset("akbar", "الله أكبر", 34),
            TasbeehPreset("tahlil", "لا إله إلا الله", 100),
            TasbeehPreset("istighfar", "أستغفر الله ربي وأتوب إليه", 70),
            TasbeehPreset("salawat", "اللهم صل على محمد وآل محمد", 100),
        )
    }

    var selectedPreset by remember { mutableStateOf(presets.first()) }
    var roundCount by remember { mutableIntStateOf(0) }
    var hapticEnabled by remember { mutableStateOf(true) }
    val haptic = LocalHapticFeedback.current

    val activeZikrDisplay = remember(selectedPreset, roundCount) {
        if (!selectedPreset.isZahraCycle) {
            selectedPreset.label to "${roundCount % selectedPreset.target} / ${selectedPreset.target}"
        } else {
            val step = roundCount % 100
            when {
                step < 34 -> "الله أكبر" to "${step + 0} / 34  (المرحلة ١ من ٣)"
                step < 67 -> "الحمد لله" to "${step - 34} / 33  (المرحلة ٢ من ٣)"
                else -> "سبحان الله" to "${step - 67} / 33  (المرحلة ٣ من ٣)"
            }
        }
    }

    val beadCount = if (selectedPreset.isZahraCycle) {
        val step = roundCount % 100
        if (step < 34) 34 else 33
    } else {
        selectedPreset.target.coerceAtMost(34)
    }

    val activeBeadIndex = if (selectedPreset.isZahraCycle) {
        val step = roundCount % 100
        when {
            step < 34 -> step
            step < 67 -> step - 34
            else -> step - 67
        }
    } else {
        roundCount % beadCount
    }

    val tapInteraction = remember { MutableInteractionSource() }
    val isTapPressed by tapInteraction.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isTapPressed) 0.94f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = 0.65f),
        label = "tasbeeh_sphere_scale",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlAnsariSubTopBar(
            title = "المسبحة الإلكترونية",
            badgeText = "تسبيح الزهراء (ع) والأذكار",
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Presets row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                presets.forEach { preset ->
                    GoldOutlineChip(
                        text = preset.label,
                        selected = selectedPreset.id == preset.id,
                        onClick = {
                            selectedPreset = preset
                            roundCount = 0
                        },
                    )
                }
            }

            // Current Zikr Header Card
            AlAnsariGoldCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(14.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = activeZikrDisplay.first,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = GoldBright,
                            fontWeight = FontWeight.ExtraBold,
                        ),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CrimsonPillBadge(
                        text = activeZikrDisplay.second,
                        horizontalPadding = 16.dp,
                        verticalPadding = 4.dp,
                    )
                }
            }

            // 3D Circular Tasbeeh Beads + Tactile 3D Tap Sphere
            Box(
                modifier = Modifier.size(276.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val ringRadius = size.minDimension / 2f - 18.dp.toPx()

                    for (i in 0 until beadCount) {
                        val angle = (2.0 * Math.PI * i / beadCount) - Math.PI / 2.0
                        val bx = cx + (ringRadius * cos(angle)).toFloat()
                        val by = cy + (ringRadius * sin(angle)).toFloat()
                        val passed = i < activeBeadIndex
                        val isCurrent = i == activeBeadIndex
                        val beadRadius = if (isCurrent) 8.5.dp.toPx() else 6.2.dp.toPx()

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = when {
                                    isCurrent -> listOf(PureWhite, CrimsonRedBright, CrimsonRedDark)
                                    passed -> listOf(GoldSpecular, GoldBright, GoldPrimary)
                                    else -> listOf(Color(0xFF4F422B), GoldDarkBorder, CardSurfaceBottom)
                                },
                                center = Offset(bx - 2f, by - 2f),
                                radius = beadRadius * 1.4f,
                            ),
                            radius = beadRadius,
                            center = Offset(bx, by),
                        )
                    }
                }

                // Central 3D Tactile Sphere Button
                Box(
                    modifier = Modifier
                        .size(168.dp)
                        .graphicsLayer {
                            scaleX = buttonScale
                            scaleY = buttonScale
                        }
                        .shadow(16.dp, CircleShape, ambientColor = GoldPrimary, spotColor = Color.Black)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(CardSurfaceTop, CrimsonRedDark, ObsidianBlack),
                            ),
                        )
                        .border(
                            width = 2.5.dp,
                            brush = Brush.linearGradient(listOf(GoldSpecular, GoldBright, GoldMuted)),
                            shape = CircleShape,
                        )
                        .clickable(
                            interactionSource = tapInteraction,
                            indication = ripple(color = GoldSpecular.copy(alpha = 0.45f)),
                            onClick = {
                                if (hapticEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                roundCount++
                                onIncrementTotal(1)
                            },
                        )
                        .testTag("tasbeeh_tap_button"),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$roundCount",
                            style = MaterialTheme.typography.displayMedium.copy(
                                color = GoldSpecular,
                                fontWeight = FontWeight.ExtraBold,
                            ),
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "اضغط للتسبيح",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = SoftCream,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }
                }
            }

            // Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                GoldOutlineChip(
                    text = if (hapticEnabled) "الاهتزاز: مفعّل" else "الاهتزاز: صامت",
                    selected = hapticEnabled,
                    onClick = { hapticEnabled = !hapticEnabled },
                )
                GoldOutlineChip(
                    text = "تصفير الجولة",
                    selected = false,
                    onClick = { roundCount = 0 },
                )
            }

            // Lifetime Total Card
            AlAnsariGoldCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = "مجموع التسبيحات المحفوظ",
                            style = MaterialTheme.typography.labelLarge.copy(color = SoftCream),
                        )
                        Text(
                            text = "$totalLifetimeCount تسبيحة",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = GoldBright,
                                fontWeight = FontWeight.ExtraBold,
                            ),
                            modifier = Modifier.testTag("tasbeeh_total_counter"),
                        )
                    }
                    GoldOutlineChip(
                        text = "تصفير الكل",
                        selected = false,
                        onClick = onResetTotal,
                    )
                }
            }
        }
    }
}
