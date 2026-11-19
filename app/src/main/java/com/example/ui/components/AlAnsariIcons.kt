package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SocialFacebookBlue
import com.example.ui.theme.SocialTelegramCyan
import com.example.ui.theme.SocialTikTokDark
import com.example.ui.theme.SocialYouTubeRed
import com.example.ui.theme.SoftCream

/**
 * TV icon with antenna, inner play triangle, and twin curved broadcast waves
 * matching the top featured Live Broadcast card in the reference design.
 */
@Composable
fun LiveBroadcastTvIcon(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
) {
    Canvas(modifier = modifier.size(width = size * 1.15f, height = size)) {
        val w = this.size.width
        val h = this.size.height
        val goldBrush = Brush.linearGradient(listOf(SoftCream, GoldBright, GoldPrimary))

        // Antenna V-lines on top of TV
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

        // TV frame
        drawRoundRect(
            brush = goldBrush,
            topLeft = Offset(tvLeft, tvTop),
            size = Size(tvRight - tvLeft, tvBottom - tvTop),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
            style = Stroke(width = w * 0.06f, join = StrokeJoin.Round),
        )

        // Play triangle inside TV
        val triCenterY = (tvTop + tvBottom) / 2f
        val triSize = (tvBottom - tvTop) * 0.25f
        val playPath = Path().apply {
            moveTo(tvCenterX - triSize * 0.55f, triCenterY - triSize * 0.85f)
            lineTo(tvCenterX + triSize * 0.95f, triCenterY)
            lineTo(tvCenterX - triSize * 0.55f, triCenterY + triSize * 0.85f)
            close()
        }
        drawPath(path = playPath, brush = goldBrush)

        // Broadcast waves on the right side of the TV frame
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

/**
 * Rounded rectangle video badge with inner dark play triangle.
 */
@Composable
fun VideosProgramsIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 56.dp, height = 42.dp)) {
        val w = size.width
        val h = size.height
        val creamGold = Brush.linearGradient(listOf(SoftCream, GoldBright))

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

/**
 * Field coverage camera/camcorder icon matching the reference design.
 */
@Composable
fun FieldCoverageCameraIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 58.dp, height = 44.dp)) {
        val w = size.width
        val h = size.height
        val creamGold = Brush.linearGradient(listOf(SoftCream, GoldBright))

        // Top viewfinder handle
        drawRoundRect(
            brush = creamGold,
            topLeft = Offset(w * 0.22f, h * 0.06f),
            size = Size(w * 0.30f, h * 0.16f),
            cornerRadius = CornerRadius(4f, 4f),
            style = Stroke(width = w * 0.05f),
        )
        // Main camera body
        drawRoundRect(
            brush = creamGold,
            topLeft = Offset(w * 0.08f, h * 0.22f),
            size = Size(w * 0.58f, h * 0.68f),
            cornerRadius = CornerRadius(w * 0.10f, w * 0.10f),
        )
        // Camera lens cone on right
        val conePath = Path().apply {
            moveTo(w * 0.68f, h * 0.42f)
            lineTo(w * 0.92f, h * 0.28f)
            lineTo(w * 0.92f, h * 0.84f)
            lineTo(w * 0.68f, h * 0.70f)
            close()
        }
        drawPath(path = conePath, brush = creamGold)

        // Lens circle
        val lensCenter = Offset(w * 0.37f, h * 0.56f)
        drawCircle(
            color = ObsidianBlack,
            radius = h * 0.18f,
            center = lensCenter,
        )
        drawCircle(
            brush = creamGold,
            radius = h * 0.08f,
            center = lensCenter,
        )
    }
}

/**
 * Calendar icon with top binding tabs and date dots matching Events card.
 */
@Composable
fun EventsCalendarIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 52.dp, height = 46.dp)) {
        val w = size.width
        val h = size.height
        val creamGold = Brush.linearGradient(listOf(SoftCream, GoldBright))

        // Top rings
        drawRoundRect(
            brush = creamGold,
            topLeft = Offset(w * 0.24f, h * 0.04f),
            size = Size(w * 0.10f, h * 0.18f),
            cornerRadius = CornerRadius(4f, 4f),
        )
        drawRoundRect(
            brush = creamGold,
            topLeft = Offset(w * 0.66f, h * 0.04f),
            size = Size(w * 0.10f, h * 0.18f),
            cornerRadius = CornerRadius(4f, 4f),
        )

        // Outer calendar frame
        drawRoundRect(
            brush = creamGold,
            topLeft = Offset(w * 0.08f, h * 0.14f),
            size = Size(w * 0.84f, h * 0.80f),
            cornerRadius = CornerRadius(w * 0.10f, w * 0.10f),
            style = Stroke(width = w * 0.06f),
        )

        // Top separator bar
        drawLine(
            brush = creamGold,
            start = Offset(w * 0.08f, h * 0.36f),
            end = Offset(w * 0.92f, h * 0.36f),
            strokeWidth = w * 0.055f,
        )

        // Grid of 4x3 squares
        val cols = 4
        val rows = 3
        val startX = w * 0.20f
        val startY = h * 0.45f
        val cellW = w * 0.13f
        val cellH = h * 0.11f
        val gapX = w * 0.04f
        val gapY = h * 0.045f
        for (r in 0 until rows) {
            for (c in 0 until cols) {
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

/**
 * Folded newspaper icon with "NEWS" header matching the News card.
 */
@Composable
fun NewsNewspaperIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 52.dp, height = 46.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val creamGold = Brush.linearGradient(listOf(SoftCream, GoldBright))

            // Main newspaper sheet
            drawRoundRect(
                brush = creamGold,
                topLeft = Offset(w * 0.06f, h * 0.08f),
                size = Size(w * 0.72f, h * 0.84f),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = w * 0.055f),
            )
            // Side fold flap
            drawRoundRect(
                brush = creamGold,
                topLeft = Offset(w * 0.78f, h * 0.22f),
                size = Size(w * 0.15f, h * 0.70f),
                cornerRadius = CornerRadius(4f, 4f),
                style = Stroke(width = w * 0.05f),
            )
            // Image block bottom left
            drawRect(
                brush = creamGold,
                topLeft = Offset(w * 0.15f, h * 0.45f),
                size = Size(w * 0.24f, h * 0.34f),
            )
            // Text lines on right of image block
            for (i in 0..2) {
                val y = h * (0.48f + i * 0.13f)
                drawLine(
                    brush = creamGold,
                    start = Offset(w * 0.44f, y),
                    end = Offset(w * 0.69f, y),
                    strokeWidth = h * 0.06f,
                    cap = StrokeCap.Round,
                )
            }
        }
        Text(
            text = "NEWS",
            color = SoftCream,
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 9.dp, top = 7.dp),
        )
    }
}

/**
 * Three-person community/services silhouette matching All Services card.
 */
@Composable
fun ServicesCommunityIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 62.dp, height = 44.dp)) {
        val w = size.width
        val h = size.height
        val creamGold = Brush.linearGradient(listOf(SoftCream, GoldBright))

        // Left person (smaller)
        drawCircle(brush = creamGold, radius = w * 0.09f, center = Offset(w * 0.20f, h * 0.34f))
        drawArc(
            brush = creamGold,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.05f, h * 0.52f),
            size = Size(w * 0.30f, h * 0.68f),
        )

        // Right person (smaller)
        drawCircle(brush = creamGold, radius = w * 0.09f, center = Offset(w * 0.80f, h * 0.34f))
        drawArc(
            brush = creamGold,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.65f, h * 0.52f),
            size = Size(w * 0.30f, h * 0.68f),
        )

        // Center person (prominent)
        drawCircle(color = ObsidianBlack, radius = w * 0.145f, center = Offset(w * 0.50f, h * 0.27f))
        drawCircle(brush = creamGold, radius = w * 0.125f, center = Offset(w * 0.50f, h * 0.27f))
        drawArc(
            color = ObsidianBlack,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.28f, h * 0.45f),
            size = Size(w * 0.44f, h * 0.90f),
        )
        drawArc(
            brush = creamGold,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.31f, h * 0.48f),
            size = Size(w * 0.38f, h * 0.82f),
        )
    }
}

/**
 * Row of 4 rounded-square social platform badges (Telegram, TikTok, YouTube, Facebook)
 * matching the Official Social Media card in the reference design.
 */
@Composable
fun SocialBadgesRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Facebook
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SocialFacebookBlue),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "f",
                color = PureWhite,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
            )
        }
        // YouTube
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SocialYouTubeRed),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(12.dp)) {
                val p = Path().apply {
                    moveTo(size.width * 0.22f, size.height * 0.15f)
                    lineTo(size.width * 0.88f, size.height * 0.50f)
                    lineTo(size.width * 0.22f, size.height * 0.85f)
                    close()
                }
                drawPath(p, color = PureWhite)
            }
        }
        // TikTok
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SocialTikTokDark)
                .border(0.8.dp, Color(0xFF3A3A3A), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "♪",
                color = PureWhite,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
            )
        }
        // Telegram
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SocialTelegramCyan),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(15.dp)) {
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

/**
 * Concentric broadcast waves icon ((•)) matching the bottom navigation Live tab.
 */
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

        // Inner arcs
        drawArc(
            color = color,
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(w * 0.24f, h * 0.24f),
            size = Size(w * 0.52f, h * 0.52f),
            style = Stroke(width = w * 0.08f, cap = StrokeCap.Round),
        )
        drawArc(
            color = color,
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(w * 0.24f, h * 0.24f),
            size = Size(w * 0.52f, h * 0.52f),
            style = Stroke(width = w * 0.08f, cap = StrokeCap.Round),
        )

        // Outer arcs
        drawArc(
            color = color,
            startAngle = 132f,
            sweepAngle = 96f,
            useCenter = false,
            topLeft = Offset(w * 0.08f, h * 0.08f),
            size = Size(w * 0.84f, h * 0.84f),
            style = Stroke(width = w * 0.08f, cap = StrokeCap.Round),
        )
        drawArc(
            color = color,
            startAngle = -48f,
            sweepAngle = 96f,
            useCenter = false,
            topLeft = Offset(w * 0.08f, h * 0.08f),
            size = Size(w * 0.84f, h * 0.84f),
            style = Stroke(width = w * 0.08f, cap = StrokeCap.Round),
        )
    }
}
