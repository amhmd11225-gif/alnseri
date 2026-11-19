package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.ui.theme.CardSurfaceBottom
import com.example.ui.theme.CardSurfaceTop
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CrimsonRedBright
import com.example.ui.theme.CrimsonRedDark
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDarkBorder
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.GoldMuted
import com.example.ui.theme.NavBarSurface
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite

@Composable
fun AlAnsariGoldCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    testTag: String = "",
    cornerRadius: Dp = 16.dp,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val borderBrush = Brush.verticalGradient(
        colors = listOf(GoldBright.copy(alpha = 0.85f), GoldMuted.copy(alpha = 0.75f), GoldDarkBorder),
    )
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(CardSurfaceTop, CardSurfaceBottom),
    )

    val clickModifier = if (onClick != null) {
        Modifier
            .minimumInteractiveComponentSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = GoldBright.copy(alpha = 0.25f)),
                onClick = onClick,
            )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
            .drawBehind {
                drawRoundRect(
                    color = GoldGlow,
                    cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
                    style = Stroke(width = 3.dp.toPx()),
                )
            }
            .clip(shape)
            .background(backgroundBrush)
            .border(width = 1.1.dp, brush = borderBrush, shape = shape)
            .then(clickModifier)
            .padding(contentPadding),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content,
        )
    }
}

@Composable
fun CrimsonPillBadge(
    text: String,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 14.dp,
    verticalPadding: Dp = 3.dp,
) {
    val pillGradient = Brush.horizontalGradient(
        colors = listOf(CrimsonRedDark, CrimsonRedBright, CrimsonRedDark),
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(pillGradient)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                color = PureWhite,
                fontSize = 12.5.sp,
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
            .minimumInteractiveComponentSize(),
        onClick = onClick,
        shape = shape,
        color = if (selected) CrimsonRed else CardSurfaceTop,
        border = BorderStroke(
            width = 1.dp,
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
            .background(ObsidianBlack)
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
        HorizontalDivider(color = GoldDarkBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
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
            .background(NavBarSurface)
            .navigationBarsPadding(),
    ) {
        HorizontalDivider(
            color = GoldDarkBorder.copy(alpha = 0.65f),
            thickness = 0.8.dp,
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
            .clip(RoundedCornerShape(12.dp))
            .minimumInteractiveComponentSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = GoldBright.copy(alpha = 0.2f)),
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
                .width(if (selected) 20.dp else 0.dp)
                .height(2.dp)
                .clip(RoundedCornerShape(50))
                .background(if (selected) GoldBright else Color.Transparent),
        )
    }
}
