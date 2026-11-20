package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.navigation.AlAnsariRoutes
import com.example.ui.theme.BevelShadow
import com.example.ui.theme.CardSurfaceBottom
import com.example.ui.theme.CardSurfaceMid
import com.example.ui.theme.CardSurfaceTop
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CrimsonRedBright
import com.example.ui.theme.CrimsonRedDark
import com.example.ui.theme.ElevatedDark
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDarkBorder
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.GoldMuted
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSpecular
import com.example.ui.theme.NavBarSurface
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite

@Composable
fun AlAnsariGoldCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    testTag: String = "",
    cornerRadius: Dp = 18.dp,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    accentBorderColor: Color = GoldBright,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (onClick != null && isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.72f),
        label = "card_press_scale",
    )

    // 3D beveled metallic rim: bright specular highlight top-left -> rich gold -> deep bronze bottom-right
    val bevelBorderBrush = Brush.linearGradient(
        colors = listOf(
            GoldSpecular.copy(alpha = 0.92f),
            accentBorderColor.copy(alpha = 0.88f),
            GoldMuted.copy(alpha = 0.72f),
            GoldDarkBorder.copy(alpha = 0.95f),
        ),
    )

    // Multi-stop vertical lighting simulating a raised domed/beveled obsidian tablet
    val surfaceBrush = Brush.verticalGradient(
        colors = listOf(
            ElevatedDark,
            CardSurfaceTop,
            CardSurfaceMid,
            CardSurfaceBottom,
        ),
    )

    val clickModifier = if (onClick != null) {
        Modifier
            .minimumInteractiveComponentSize()
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = GoldBright.copy(alpha = 0.28f)),
                onClick = onClick,
            )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
            // Bottom 3D plinth thickness & ambient gold halo
            .padding(bottom = 3.dp)
            .shadow(
                elevation = if (onClick != null && isPressed) 3.dp else 10.dp,
                shape = shape,
                ambientColor = GoldPrimary.copy(alpha = 0.45f),
                spotColor = Color.Black,
            )
            .drawBehind {
                val rPx = cornerRadius.toPx()
                // 3D extruded bottom lip
                drawRoundRect(
                    color = GoldDarkBorder.copy(alpha = 0.55f),
                    topLeft = Offset(0f, 3.dp.toPx()),
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(rPx, rPx),
                )
                drawRoundRect(
                    color = GoldGlow,
                    cornerRadius = CornerRadius(rPx, rPx),
                    style = Stroke(width = 3.5.dp.toPx()),
                )
            }
            .clip(shape)
            .background(surfaceBrush)
            .drawBehind {
                // Top inner metallic sheen reflection
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PureWhite.copy(alpha = 0.08f),
                            Color.Transparent,
                        ),
                        startY = 0f,
                        endY = size.height * 0.36f,
                    ),
                )
                // Inner bottom shadow groove
                drawLine(
                    color = BevelShadow.copy(alpha = 0.9f),
                    start = Offset(12.dp.toPx(), size.height - 1.dp.toPx()),
                    end = Offset(size.width - 12.dp.toPx(), size.height - 1.dp.toPx()),
                    strokeWidth = 2.dp.toPx(),
                )
            }
            .border(width = 1.25.dp, brush = bevelBorderBrush, shape = shape)
            .then(clickModifier)
            .padding(contentPadding),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content,
        )
    }
}

/**
 * 3D Square Tile Card with recessed glowing icon pedestal for Home Grid sections.
 */
@Composable
fun AlAnsari3DTileCard(
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 122.dp,
    badgeColorTop: Color = CrimsonRedBright,
    badgeColorBottom: Color = CrimsonRedDark,
    icon: @Composable () -> Unit,
) {
    AlAnsariGoldCard(
        modifier = modifier.height(height),
        onClick = onClick,
        testTag = testTag,
        cornerRadius = 18.dp,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 9.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // 3D Recessed Jewel Medallion Pedestal for Icon
            Box(
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                ObsidianBlack,
                                Color(0xFF17130C),
                            ),
                        ),
                    )
                    .border(
                        width = 0.9.dp,
                        brush = Brush.verticalGradient(
                            listOf(GoldDarkBorder, GoldBright.copy(alpha = 0.55f)),
                        ),
                        shape = RoundedCornerShape(14.dp),
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                icon()
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PureWhite,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )

            CrimsonPillBadge(
                text = subtitle,
                horizontalPadding = 12.dp,
                verticalPadding = 2.dp,
                gradientColors = listOf(badgeColorBottom, badgeColorTop, badgeColorBottom),
            )
        }
    }
}

@Composable
fun HomeSectionHeader(
    title: String,
    badge: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.verticalGradient(listOf(GoldSpecular, GoldPrimary)),
                    ),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = GoldBright,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.5.sp,
                ),
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(ElevatedDark)
                .border(0.8.dp, GoldDarkBorder, RoundedCornerShape(50))
                .padding(horizontal = 10.dp, vertical = 3.dp),
        ) {
            Text(
                text = badge,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = GoldSpecular,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
    }
}

@Composable
fun CrimsonPillBadge(
    text: String,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 14.dp,
    verticalPadding: Dp = 3.dp,
    gradientColors: List<Color> = listOf(CrimsonRedDark, CrimsonRedBright, CrimsonRedDark),
) {
    val pillGradient = Brush.verticalGradient(
        colors = listOf(
            gradientColors.getOrElse(1) { CrimsonRedBright },
            gradientColors.getOrElse(0) { CrimsonRedDark },
        ),
    )
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .background(pillGradient)
            .border(
                width = 0.7.dp,
                color = PureWhite.copy(alpha = 0.25f),
                shape = RoundedCornerShape(50),
            )
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                color = PureWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun GoldOutlineChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "",
) {
    val shape = RoundedCornerShape(50)
    Surface(
        modifier = modifier
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
            .shadow(if (selected) 6.dp else 2.dp, shape)
            .minimumInteractiveComponentSize(),
        onClick = onClick,
        shape = shape,
        color = if (selected) CrimsonRed else CardSurfaceTop,
        border = BorderStroke(
            width = 1.1.dp,
            color = if (selected) GoldBright else GoldDarkBorder,
        ),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = if (selected) PureWhite else GoldBright,
                    fontSize = 13.sp,
                ),
                maxLines = 1,
            )
        }
    }
}

@Composable
fun AlAnsariSubTopBar(
    title: String,
    badgeText: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingAction: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(ElevatedDark, ObsidianBlack),
                ),
            )
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("top_bar_back_button"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = GoldBright,
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(color = GoldBright),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    CrimsonPillBadge(
                        text = badgeText,
                        horizontalPadding = 10.dp,
                        verticalPadding = 2.dp,
                    )
                }
            }
            if (trailingAction != null) {
                trailingAction()
            }
        }
        HorizontalDivider(color = GoldPrimary.copy(alpha = 0.45f), thickness = 1.dp)
    }
}

@Composable
fun AlAnsariBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CardSurfaceTop, NavBarSurface),
                ),
            )
            .navigationBarsPadding(),
    ) {
        HorizontalDivider(
            color = GoldPrimary.copy(alpha = 0.55f),
            thickness = 1.dp,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomNavTabItem(
                label = stringResource(R.string.nav_home),
                selected = currentRoute == AlAnsariRoutes.HOME,
                testTag = "bottom_nav_home",
                onClick = { onNavigate(AlAnsariRoutes.HOME) },
                iconContent = { color, selected ->
                    Icon(
                        imageVector = if (selected) Icons.Filled.Home else Icons.Outlined.Home,
                        contentDescription = stringResource(R.string.nav_home),
                        tint = color,
                        modifier = Modifier.size(23.dp),
                    )
                },
            )
            BottomNavTabItem(
                label = stringResource(R.string.nav_videos),
                selected = currentRoute == AlAnsariRoutes.VIDEOS,
                testTag = "bottom_nav_videos",
                onClick = { onNavigate(AlAnsariRoutes.VIDEOS) },
                iconContent = { color, _ ->
                    Icon(
                        imageVector = Icons.Outlined.PlayArrow,
                        contentDescription = stringResource(R.string.nav_videos),
                        tint = color,
                        modifier = Modifier.size(25.dp),
                    )
                },
            )
            BottomNavTabItem(
                label = stringResource(R.string.nav_live),
                selected = currentRoute == AlAnsariRoutes.LIVE,
                testTag = "bottom_nav_live",
                onClick = { onNavigate(AlAnsariRoutes.LIVE) },
                iconContent = { color, _ ->
                    BottomNavBroadcastIcon(color = color)
                },
            )
            BottomNavTabItem(
                label = stringResource(R.string.nav_more),
                selected = currentRoute == AlAnsariRoutes.MORE,
                testTag = "bottom_nav_more",
                onClick = { onNavigate(AlAnsariRoutes.MORE) },
                iconContent = { color, _ ->
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = stringResource(R.string.nav_more),
                        tint = color,
                        modifier = Modifier.size(23.dp),
                    )
                },
            )
        }
    }
}

@Composable
private fun BottomNavTabItem(
    label: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    iconContent: @Composable (Color, Boolean) -> Unit,
) {
    val contentColor = if (selected) GoldBright else PureWhite
    Column(
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (selected) {
                    Modifier
                        .background(GoldPrimary.copy(alpha = 0.14f))
                        .border(0.8.dp, GoldPrimary.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                } else {
                    Modifier
                },
            )
            .minimumInteractiveComponentSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = GoldBright.copy(alpha = 0.22f)),
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        iconContent(contentColor, selected)
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = contentColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.5.sp,
            ),
            maxLines = 1,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .width(if (selected) 22.dp else 0.dp)
                .height(2.5.dp)
                .clip(RoundedCornerShape(50))
                .background(if (selected) GoldBright else Color.Transparent),
        )
    }
}
