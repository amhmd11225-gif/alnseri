package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDarkBorder
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSpecular
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SocialFacebookBlue
import com.example.ui.theme.SocialTelegramCyan
import com.example.ui.theme.SocialTikTokDark
import com.example.ui.theme.SocialYouTubeRed
import com.example.ui.theme.SoftCream
import kotlin.math.cos
import kotlin.math.sin

private fun goldMetallicBrush(): Brush = Brush.linearGradient(
    colors = listOf(GoldSpecular, GoldBright, GoldPrimary, GoldDarkBorder),
)

@Composable
fun LiveBroadcastTvIcon(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
) {
    Canvas(modifier = modifier.size(width = size * 1.15f, height = size)) {
        val w = this.size.width
        val h = this.size.height
        val goldBrush = goldMetallicBrush()

        val tvLeft = w * 0.08f
        val tvRight = w * 0.72f
        val tvCenterX = (tvLeft + tvRight) / 2f
        val tvTop = h * 0.28f
        val tvBottom = h * 0.90f

        drawLine(
            brush = goldBrush,
            start = Offset(tvCenterX, tvTop),
            end = Offset(tvCenterX - w * 0.13f, h * 0.08f),
            strokeWidth = w * 0.045f,
            cap = StrokeCap.Round,
        )
        drawLine(
            brush = goldBrush,
            start = Offset(tvCenterX, tvTop),
            end = Offset(tvCenterX + w * 0.15f, h * 0.08f),
            strokeWidth = w * 0.045f,
            cap = StrokeCap.Round,
        )
        drawRoundRect(
            brush = goldBrush,
            topLeft = Offset(tvLeft, tvTop),
            size = Size(tvRight - tvLeft, tvBottom - tvTop),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
            style = Stroke(width = w * 0.06f, join = StrokeJoin.Round),
        )
        val triCenterY = (tvTop + tvBottom) / 2f
        val triSize = (tvBottom - tvTop) * 0.25f
        val playPath = Path().apply {
            moveTo(tvCenterX - triSize * 0.55f, triCenterY - triSize * 0.85f)
            lineTo(tvCenterX + triSize * 0.95f, triCenterY)
            lineTo(tvCenterX - triSize * 0.55f, triCenterY + triSize * 0.85f)
            close()
        }
        drawPath(path = playPath, brush = goldBrush)
        drawArc(
            brush = goldBrush,
            startAngle = -52f,
            sweepAngle = 104f,
            useCenter = false,
            topLeft = Offset(tvRight - w * 0.02f, tvTop + h * 0.08f),
            size = Size(w * 0.18f, (tvBottom - tvTop) - h * 0.16f),
            style = Stroke(width = w * 0.05f, cap = StrokeCap.Round),
        )
        drawArc(
            brush = goldBrush,
            startAngle = -58f,
            sweepAngle = 116f,
            useCenter = false,
            topLeft = Offset(tvRight + w * 0.05f, tvTop - h * 0.01f),
            size = Size(w * 0.24f, (tvBottom - tvTop) + h * 0.02f),
            style = Stroke(width = w * 0.05f, cap = StrokeCap.Round),
        )
    }
}

@Composable
fun VideosProgramsIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 44.dp, height = 34.dp)) {
        val w = size.width
        val h = size.height
        val creamGold = goldMetallicBrush()

        drawRoundRect(
            brush = creamGold,
            topLeft = Offset(w * 0.04f, h * 0.06f),
            size = Size(w * 0.92f, h * 0.88f),
            cornerRadius = CornerRadius(w * 0.18f, w * 0.18f),
        )
        val cx = w * 0.52f
        val cy = h * 0.50f
        val r = h * 0.24f
        val playPath = Path().apply {
            moveTo(cx - r * 0.72f, cy - r * 0.88f)
            lineTo(cx + r * 0.90f, cy)
            lineTo(cx - r * 0.72f, cy + r * 0.88f)
            close()
        }
        drawPath(path = playPath, color = ObsidianBlack)
    }
}

@Composable
fun FieldCoverageCameraIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 46.dp, height = 34.dp)) {
        val w = size.width
        val h = size.height
        val creamGold = goldMetallicBrush()

        drawRoundRect(
            brush = creamGold,
            topLeft = Offset(w * 0.22f, h * 0.06f),
            size = Size(w * 0.30f, h * 0.16f),
            cornerRadius = CornerRadius(4f, 4f),
            style = Stroke(width = w * 0.05f),
        )
        drawRoundRect(
            brush = creamGold,
            topLeft = Offset(w * 0.08f, h * 0.22f),
            size = Size(w * 0.58f, h * 0.68f),
            cornerRadius = CornerRadius(w * 0.10f, w * 0.10f),
        )
        val conePath = Path().apply {
            moveTo(w * 0.68f, h * 0.42f)
            lineTo(w * 0.92f, h * 0.28f)
            lineTo(w * 0.92f, h * 0.84f)
            lineTo(w * 0.68f, h * 0.70f)
            close()
        }
        drawPath(path = conePath, brush = creamGold)
        val lensCenter = Offset(w * 0.37f, h * 0.56f)
        drawCircle(color = ObsidianBlack, radius = h * 0.18f, center = lensCenter)
        drawCircle(brush = creamGold, radius = h * 0.08f, center = lensCenter)
    }
}

@Composable
fun EventsCalendarIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 42.dp, height = 36.dp)) {
        val w = size.width
        val h = size.height
        val creamGold = goldMetallicBrush()

        drawRoundRect(brush = creamGold, topLeft = Offset(w * 0.24f, h * 0.04f), size = Size(w * 0.10f, h * 0.18f), cornerRadius = CornerRadius(4f, 4f))
        drawRoundRect(brush = creamGold, topLeft = Offset(w * 0.66f, h * 0.04f), size = Size(w * 0.10f, h * 0.18f), cornerRadius = CornerRadius(4f, 4f))
        drawRoundRect(
            brush = creamGold,
            topLeft = Offset(w * 0.08f, h * 0.14f),
            size = Size(w * 0.84f, h * 0.80f),
            cornerRadius = CornerRadius(w * 0.10f, w * 0.10f),
            style = Stroke(width = w * 0.06f),
        )
        drawLine(brush = creamGold, start = Offset(w * 0.08f, h * 0.36f), end = Offset(w * 0.92f, h * 0.36f), strokeWidth = w * 0.055f)
        val startX = w * 0.20f
        val startY = h * 0.45f
        val cellW = w * 0.13f
        val cellH = h * 0.11f
        val gapX = w * 0.04f
        val gapY = h * 0.045f
        for (r in 0 until 3) {
            for (c in 0 until 4) {
                drawRoundRect(
                    brush = creamGold,
                    topLeft = Offset(startX + c * (cellW + gapX), startY + r * (cellH + gapY)),
                    size = Size(cellW, cellH),
                    cornerRadius = CornerRadius(2f, 2f),
                )
            }
        }
    }
}

@Composable
fun NewsNewspaperIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(width = 42.dp, height = 36.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val creamGold = goldMetallicBrush()

            drawRoundRect(
                brush = creamGold,
                topLeft = Offset(w * 0.06f, h * 0.08f),
                size = Size(w * 0.72f, h * 0.84f),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = w * 0.055f),
            )
            drawRoundRect(
                brush = creamGold,
                topLeft = Offset(w * 0.78f, h * 0.22f),
                size = Size(w * 0.15f, h * 0.70f),
                cornerRadius = CornerRadius(4f, 4f),
                style = Stroke(width = w * 0.05f),
            )
            drawRect(brush = creamGold, topLeft = Offset(w * 0.15f, h * 0.45f), size = Size(w * 0.24f, h * 0.34f))
            for (i in 0..2) {
                val y = h * (0.48f + i * 0.13f)
                drawLine(brush = creamGold, start = Offset(w * 0.44f, y), end = Offset(w * 0.69f, y), strokeWidth = h * 0.06f, cap = StrokeCap.Round)
            }
        }
        Text(
            text = "NEWS",
            color = SoftCream,
            fontSize = 6.5.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 7.dp, top = 5.dp),
        )
    }
}

@Composable
fun ServicesCommunityIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 48.dp, height = 36.dp)) {
        val w = size.width
        val h = size.height
        val creamGold = goldMetallicBrush()

        drawCircle(brush = creamGold, radius = w * 0.09f, center = Offset(w * 0.20f, h * 0.34f))
        drawArc(brush = creamGold, startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = Offset(w * 0.05f, h * 0.52f), size = Size(w * 0.30f, h * 0.68f))
        drawCircle(brush = creamGold, radius = w * 0.09f, center = Offset(w * 0.80f, h * 0.34f))
        drawArc(brush = creamGold, startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = Offset(w * 0.65f, h * 0.52f), size = Size(w * 0.30f, h * 0.68f))
        drawCircle(color = ObsidianBlack, radius = w * 0.145f, center = Offset(w * 0.50f, h * 0.27f))
        drawCircle(brush = creamGold, radius = w * 0.125f, center = Offset(w * 0.50f, h * 0.27f))
        drawArc(color = ObsidianBlack, startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = Offset(w * 0.28f, h * 0.45f), size = Size(w * 0.44f, h * 0.90f))
        drawArc(brush = creamGold, startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = Offset(w * 0.31f, h * 0.48f), size = Size(w * 0.38f, h * 0.82f))
    }
}

@Composable
fun SocialBadgesRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(SocialFacebookBlue),
            contentAlignment = Alignment.Center,
        ) {
            Text("f", color = PureWhite, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(SocialYouTubeRed),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(10.dp)) {
                val p = Path().apply {
                    moveTo(size.width * 0.22f, size.height * 0.15f)
                    lineTo(size.width * 0.88f, size.height * 0.50f)
                    lineTo(size.width * 0.22f, size.height * 0.85f)
                    close()
                }
                drawPath(p, color = PureWhite)
            }
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(SocialTikTokDark)
                .border(0.8.dp, Color(0xFF3A3A3A), RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("♪", color = PureWhite, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(SocialTelegramCyan),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(13.dp)) {
                val p = Path().apply {
                    moveTo(size.width * 0.90f, size.height * 0.14f)
                    lineTo(size.width * 0.10f, size.height * 0.48f)
                    lineTo(size.width * 0.42f, size.height * 0.58f)
                    lineTo(size.width * 0.75f, size.height * 0.30f)
                    lineTo(size.width * 0.50f, size.height * 0.64f)
                    lineTo(size.width * 0.72f, size.height * 0.86f)
                    close()
                }
                drawPath(p, color = PureWhite)
            }
        }
    }
}

// --- Custom 3D Islamic Icons for Haqeebat Al-Mu'min Tiles ---

@Composable
fun QuranBook3DIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 44.dp, height = 36.dp)) {
        val w = size.width
        val h = size.height
        val gold = goldMetallicBrush()
        // Open Quran Book Wings
        val leftPage = Path().apply {
            moveTo(w * 0.50f, h * 0.24f)
            cubicTo(w * 0.34f, h * 0.10f, w * 0.15f, h * 0.12f, w * 0.08f, h * 0.20f)
            lineTo(w * 0.08f, h * 0.78f)
            cubicTo(w * 0.18f, h * 0.70f, w * 0.34f, h * 0.72f, w * 0.50f, h * 0.84f)
            close()
        }
        val rightPage = Path().apply {
            moveTo(w * 0.50f, h * 0.24f)
            cubicTo(w * 0.66f, h * 0.10f, w * 0.85f, h * 0.12f, w * 0.92f, h * 0.20f)
            lineTo(w * 0.92f, h * 0.78f)
            cubicTo(w * 0.82f, h * 0.70f, w * 0.66f, h * 0.72f, w * 0.50f, h * 0.84f)
            close()
        }
        drawPath(leftPage, brush = gold)
        drawPath(rightPage, brush = gold)
        // Center spine & bookmark ribbon
        drawLine(color = ObsidianBlack, start = Offset(w * 0.50f, h * 0.22f), end = Offset(w * 0.50f, h * 0.84f), strokeWidth = 2.dp.toPx())
        // Rahl / wooden book stand X below
        drawLine(brush = gold, start = Offset(w * 0.28f, h * 0.94f), end = Offset(w * 0.56f, h * 0.78f), strokeWidth = 2.6.dp.toPx(), cap = StrokeCap.Round)
        drawLine(brush = gold, start = Offset(w * 0.72f, h * 0.94f), end = Offset(w * 0.44f, h * 0.78f), strokeWidth = 2.6.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
fun DuaHands3DIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 42.dp, height = 36.dp)) {
        val w = size.width
        val h = size.height
        val gold = goldMetallicBrush()
        // Raised palms in Dua + radiating star light above
        val leftPalm = Path().apply {
            moveTo(w * 0.48f, h * 0.88f)
            lineTo(w * 0.16f, h * 0.62f)
            cubicTo(w * 0.12f, h * 0.46f, w * 0.20f, h * 0.36f, w * 0.32f, h * 0.42f)
            lineTo(w * 0.47f, h * 0.68f)
            close()
        }
        val rightPalm = Path().apply {
            moveTo(w * 0.52f, h * 0.88f)
            lineTo(w * 0.84f, h * 0.62f)
            cubicTo(w * 0.88f, h * 0.46f, w * 0.80f, h * 0.36f, w * 0.68f, h * 0.42f)
            lineTo(w * 0.53f, h * 0.68f)
            close()
        }
        drawPath(leftPalm, brush = gold)
        drawPath(rightPalm, brush = gold)
        // Crescent & star glow above palms
        drawCircle(brush = gold, radius = 3.2.dp.toPx(), center = Offset(w * 0.50f, h * 0.22f))
        drawLine(brush = gold, start = Offset(w * 0.50f, h * 0.06f), end = Offset(w * 0.50f, h * 0.14f), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(brush = gold, start = Offset(w * 0.36f, h * 0.16f), end = Offset(w * 0.42f, h * 0.20f), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(brush = gold, start = Offset(w * 0.64f, h * 0.16f), end = Offset(w * 0.58f, h * 0.20f), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
fun ZiyaratDome3DIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 44.dp, height = 36.dp)) {
        val w = size.width
        val h = size.height
        val gold = goldMetallicBrush()

        // Finial / Alam
        drawLine(brush = gold, start = Offset(w * 0.50f, h * 0.05f), end = Offset(w * 0.50f, h * 0.20f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        // Onion Dome
        val dome = Path().apply {
            moveTo(w * 0.50f, h * 0.16f)
            cubicTo(w * 0.72f, h * 0.24f, w * 0.72f, h * 0.46f, w * 0.68f, h * 0.56f)
            lineTo(w * 0.32f, h * 0.56f)
            cubicTo(w * 0.28f, h * 0.46f, w * 0.28f, h * 0.24f, w * 0.50f, h * 0.16f)
            close()
        }
        drawPath(dome, brush = gold)
        // Left & Right Minarets
        drawRoundRect(brush = gold, topLeft = Offset(w * 0.12f, h * 0.24f), size = Size(w * 0.10f, h * 0.66f), cornerRadius = CornerRadius(3f, 3f))
        drawRoundRect(brush = gold, topLeft = Offset(w * 0.78f, h * 0.24f), size = Size(w * 0.10f, h * 0.66f), cornerRadius = CornerRadius(3f, 3f))
        // Shrine Sanctuary Base + Arch Door
        drawRoundRect(brush = gold, topLeft = Offset(w * 0.24f, h * 0.56f), size = Size(w * 0.52f, h * 0.34f), cornerRadius = CornerRadius(4f, 4f))
        drawRoundRect(color = ObsidianBlack, topLeft = Offset(w * 0.43f, h * 0.66f), size = Size(w * 0.14f, h * 0.24f), cornerRadius = CornerRadius(12f, 12f))
    }
}

@Composable
fun PrayerTimesCrescent3DIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 42.dp, height = 36.dp)) {
        val w = size.width
        val h = size.height
        val gold = goldMetallicBrush()
        // Outer golden clock/horizon ring + crescent + hands
        drawCircle(brush = gold, radius = h * 0.42f, center = Offset(w * 0.50f, h * 0.50f), style = Stroke(width = 2.5.dp.toPx()))
        drawCircle(color = CrimsonRed, radius = h * 0.10f, center = Offset(w * 0.50f, h * 0.50f))
        drawLine(brush = gold, start = Offset(w * 0.50f, h * 0.50f), end = Offset(w * 0.50f, h * 0.24f), strokeWidth = 2.2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(brush = gold, start = Offset(w * 0.50f, h * 0.50f), end = Offset(w * 0.68f, h * 0.58f), strokeWidth = 2.2.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
fun QiblaCompass3DIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 42.dp, height = 36.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val gold = goldMetallicBrush()
        drawCircle(brush = gold, radius = h * 0.44f, center = Offset(cx, cy), style = Stroke(width = 2.2.dp.toPx()))
        // North red/gold needle
        val northNeedle = Path().apply {
            moveTo(cx, cy - h * 0.34f)
            lineTo(cx + w * 0.11f, cy)
            lineTo(cx - w * 0.11f, cy)
            close()
        }
        val southNeedle = Path().apply {
            moveTo(cx, cy + h * 0.34f)
            lineTo(cx + w * 0.11f, cy)
            lineTo(cx - w * 0.11f, cy)
            close()
        }
        drawPath(northNeedle, color = CrimsonRed)
        drawPath(southNeedle, brush = gold)
        drawCircle(color = GoldSpecular, radius = 2.4.dp.toPx(), center = Offset(cx, cy))
    }
}

@Composable
fun TasbeehBeads3DIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 42.dp, height = 36.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = h * 0.34f
        val gold = goldMetallicBrush()
        val count = 10
        for (i in 0 until count) {
            val angle = (2.0 * Math.PI * i / count) - Math.PI / 2.0
            val bx = cx + (r * cos(angle)).toFloat()
            val by = cy + (r * sin(angle)).toFloat()
            if (i == 0) {
                drawCircle(color = CrimsonRed, radius = 3.6.dp.toPx(), center = Offset(bx, by))
            } else {
                drawCircle(brush = gold, radius = 2.8.dp.toPx(), center = Offset(bx, by))
            }
        }
        // Tassel below
        drawLine(brush = gold, start = Offset(cx, cy + r + 3.dp.toPx()), end = Offset(cx, h * 0.96f), strokeWidth = 2.2.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
fun HijriCrescentCalendar3DIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 42.dp, height = 36.dp)) {
        val w = size.width
        val h = size.height
        val gold = goldMetallicBrush()
        drawCircle(brush = gold, radius = h * 0.38f, center = Offset(w * 0.44f, h * 0.50f))
        drawCircle(color = ObsidianBlack, radius = h * 0.31f, center = Offset(w * 0.55f, h * 0.44f))
        drawCircle(color = CrimsonRed, radius = 3.2.dp.toPx(), center = Offset(w * 0.72f, h * 0.36f))
    }
}

@Composable
fun IstikharaQuranStar3DIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 42.dp, height = 36.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val gold = goldMetallicBrush()
        // 8-pointed Islamic geometric star (Rub el Hizb)
        val boxSize = h * 0.62f
        drawRect(
            brush = gold,
            topLeft = Offset(cx - boxSize / 2f, cy - boxSize / 2f),
            size = Size(boxSize, boxSize),
            style = Stroke(width = 2.dp.toPx()),
        )
        val diamond = Path().apply {
            val d = boxSize * 0.70f
            moveTo(cx, cy - d)
            lineTo(cx + d, cy)
            lineTo(cx, cy + d)
            lineTo(cx - d, cy)
            close()
        }
        drawPath(diamond, brush = gold, style = Stroke(width = 2.dp.toPx()))
        drawCircle(color = CrimsonRed, radius = 3.5.dp.toPx(), center = Offset(cx, cy))
    }
}

@Composable
fun BottomNavBroadcastIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)

        drawCircle(color = color, radius = w * 0.11f, center = center)
        drawArc(color = color, startAngle = 135f, sweepAngle = 90f, useCenter = false, topLeft = Offset(w * 0.24f, h * 0.24f), size = Size(w * 0.52f, h * 0.52f), style = Stroke(width = w * 0.08f, cap = StrokeCap.Round))
        drawArc(color = color, startAngle = -45f, sweepAngle = 90f, useCenter = false, topLeft = Offset(w * 0.24f, h * 0.24f), size = Size(w * 0.52f, h * 0.52f), style = Stroke(width = w * 0.08f, cap = StrokeCap.Round))
        drawArc(color = color, startAngle = 132f, sweepAngle = 96f, useCenter = false, topLeft = Offset(w * 0.08f, h * 0.08f), size = Size(w * 0.84f, h * 0.84f), style = Stroke(width = w * 0.08f, cap = StrokeCap.Round))
        drawArc(color = color, startAngle = -48f, sweepAngle = 96f, useCenter = false, topLeft = Offset(w * 0.08f, h * 0.08f), size = Size(w * 0.84f, h * 0.84f), style = Stroke(width = w * 0.08f, cap = StrokeCap.Round))
    }
}
