package com.example.ui.components

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
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
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MutedGray
import com.example.ui.theme.NavBarSurface
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleDivider
import kotlinx.coroutines.delay

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

@OptIn(UnstableApi::class)
@Composable
fun AlAnsariMediaPlayer(
    streamUrl: String,
    title: String,
    isLiveStream: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentPosMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var reloadNonce by remember { mutableStateOf(0) }

    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    LaunchedEffect(streamUrl, reloadNonce) {
        if (streamUrl.isBlank()) {
            errorMessage = "لم يتم تعيين رابط بث صالح بعد."
            isBuffering = false
            return@LaunchedEffect
        }
        errorMessage = null
        isBuffering = true
        val mediaItemBuilder = MediaItem.Builder().setUri(streamUrl)
        if (streamUrl.contains(".m3u8", ignoreCase = true)) {
            mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_M3U8)
        }
        exoPlayer.setMediaItem(mediaItemBuilder.build())
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) {
                    errorMessage = null
                    durationMs = exoPlayer.duration.coerceAtLeast(0L)
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                errorMessage = "تعذر تشغيل البث (${error.errorCodeName}). تحقق من الاتصال بالإنترنت أو صحة الرابط."
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                Lifecycle.Event.ON_RESUME -> if (isPlaying) exoPlayer.play()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(exoPlayer, isLiveStream, isPlaying) {
        while (true) {
            if (!isLiveStream) {
                currentPosMs = exoPlayer.currentPosition.coerceAtLeast(0L)
                durationMs = exoPlayer.duration.coerceAtLeast(0L)
            }
            delay(500L)
        }
    }

    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying && errorMessage == null) {
            delay(4000L)
            showControls = false
        }
    }

    val toggleMute = {
        isMuted = !isMuted
        exoPlayer.volume = if (isMuted) 0f else 1f
    }

    val toggleFullscreen = { enter: Boolean ->
        isFullscreen = enter
        val activity = context as? Activity
        activity?.requestedOrientation = if (enter) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            val activity = context as? Activity
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    if (isFullscreen) {
        Dialog(
            onDismissRequest = { toggleFullscreen(false) },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
        ) {
            BackHandler { toggleFullscreen(false) }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
            ) {
                PlayerSurfaceWithOverlay(
                    exoPlayer = exoPlayer,
                    title = title,
                    isLiveStream = isLiveStream,
                    isPlaying = isPlaying,
                    isBuffering = isBuffering,
                    isMuted = isMuted,
                    isFullscreen = true,
                    showControls = showControls,
                    errorMessage = errorMessage,
                    currentPosMs = currentPosMs,
                    durationMs = durationMs,
                    onToggleControls = { showControls = !showControls },
                    onPlayPause = {
                        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                    },
                    onSeekBy = { deltaMs ->
                        val next = (exoPlayer.currentPosition + deltaMs).coerceIn(0L, exoPlayer.duration.coerceAtLeast(0L))
                        exoPlayer.seekTo(next)
                    },
                    onSeekTo = { target -> exoPlayer.seekTo(target) },
                    onToggleMute = toggleMute,
                    onReload = { reloadNonce++ },
                    onToggleFullscreen = { toggleFullscreen(false) },
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .border(1.2.dp, GoldPrimary, RoundedCornerShape(16.dp)),
    ) {
        if (!isFullscreen) {
            PlayerSurfaceWithOverlay(
                exoPlayer = exoPlayer,
                title = title,
                isLiveStream = isLiveStream,
                isPlaying = isPlaying,
                isBuffering = isBuffering,
                isMuted = isMuted,
                isFullscreen = false,
                showControls = showControls,
                errorMessage = errorMessage,
                currentPosMs = currentPosMs,
                durationMs = durationMs,
                onToggleControls = { showControls = !showControls },
                onPlayPause = {
                    if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                },
                onSeekBy = { deltaMs ->
                    val next = (exoPlayer.currentPosition + deltaMs).coerceIn(0L, exoPlayer.duration.coerceAtLeast(0L))
                    exoPlayer.seekTo(next)
                },
                onSeekTo = { target -> exoPlayer.seekTo(target) },
                onToggleMute = toggleMute,
                onReload = { reloadNonce++ },
                onToggleFullscreen = { toggleFullscreen(true) },
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun PlayerSurfaceWithOverlay(
    exoPlayer: ExoPlayer,
    title: String,
    isLiveStream: Boolean,
    isPlaying: Boolean,
    isBuffering: Boolean,
    isMuted: Boolean,
    isFullscreen: Boolean,
    showControls: Boolean,
    errorMessage: String?,
    currentPosMs: Long,
    durationMs: Long,
    onToggleControls: () -> Unit,
    onPlayPause: () -> Unit,
    onSeekBy: (Long) -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleMute: () -> Unit,
    onReload: () -> Unit,
    onToggleFullscreen: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggleControls,
            ),
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    keepScreenOn = true
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    player = exoPlayer
                }
            },
            update = { view ->
                if (view.player !== exoPlayer) {
                    view.player = exoPlayer
                }
            },
        )

        // Buffering spinner
        if (isBuffering && errorMessage == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = GoldBright, strokeWidth = 3.dp)
            }
        }

        // Error overlay with retry
        if (errorMessage != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(color = PureWhite),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    onClick = onReload,
                    shape = RoundedCornerShape(50),
                    color = CrimsonRed,
                    border = BorderStroke(1.dp, GoldBright),
                    modifier = Modifier.minimumInteractiveComponentSize(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "إعادة المحاولة",
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إعادة الاتصال", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        // Custom gold/black/red controls overlay
        AnimatedVisibility(
            visible = showControls && errorMessage == null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.72f),
                                Color.Black.copy(alpha = 0.18f),
                                Color.Black.copy(alpha = 0.82f),
                            ),
                        ),
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                // Top bar inside player
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    if (isLiveStream) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(CrimsonRed)
                                .padding(horizontal = 10.dp, vertical = 3.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(PureWhite),
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "مباشر",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold,
                                ),
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge.copy(color = GoldBright),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        textAlign = TextAlign.End,
                    )
                }

                // Center Play / Seek controls
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    if (!isLiveStream) {
                        IconButton(
                            onClick = { onSeekBy(-10_000L) },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.45f)),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Replay10,
                                contentDescription = "رجوع 10 ثوانٍ",
                                tint = PureWhite,
                            )
                        }
                    }

                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(CrimsonRed.copy(alpha = 0.92f))
                            .border(1.5.dp, GoldBright, CircleShape)
                            .testTag("player_play_pause_button"),
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                            tint = PureWhite,
                            modifier = Modifier.size(30.dp),
                        )
                    }

                    if (!isLiveStream) {
                        IconButton(
                            onClick = { onSeekBy(10_000L) },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.45f)),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Forward10,
                                contentDescription = "تقديم 10 ثوانٍ",
                                tint = PureWhite,
                            )
                        }
                    }
                }

                // Bottom bar inside player
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                ) {
                    if (!isLiveStream && durationMs > 0L) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = formatDurationMs(currentPosMs),
                                style = MaterialTheme.typography.labelSmall.copy(color = PureWhite),
                            )
                            Slider(
                                value = currentPosMs.toFloat().coerceIn(0f, durationMs.toFloat()),
                                onValueChange = { onSeekTo(it.toLong()) },
                                valueRange = 0f..durationMs.toFloat(),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = GoldBright,
                                    activeTrackColor = CrimsonRedBright,
                                    inactiveTrackColor = PureWhite.copy(alpha = 0.3f),
                                ),
                            )
                            Text(
                                text = formatDurationMs(durationMs),
                                style = MaterialTheme.typography.labelSmall.copy(color = MutedGray),
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onToggleMute,
                                modifier = Modifier.size(48.dp),
                            ) {
                                Icon(
                                    imageVector = if (isMuted) {
                                        Icons.AutoMirrored.Filled.VolumeOff
                                    } else {
                                        Icons.AutoMirrored.Filled.VolumeUp
                                    },
                                    contentDescription = if (isMuted) "تشغيل الصوت" else "كتم الصوت",
                                    tint = GoldBright,
                                )
                            }
                            IconButton(
                                onClick = onReload,
                                modifier = Modifier.size(48.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "تحديث البث",
                                    tint = PureWhite,
                                )
                            }
                        }

                        IconButton(
                            onClick = onToggleFullscreen,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("player_fullscreen_button"),
                        ) {
                            Icon(
                                imageVector = if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                                contentDescription = if (isFullscreen) "إنهاء ملء الشاشة" else "ملء الشاشة",
                                tint = GoldBright,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDurationMs(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val mins = totalSec / 60L
    val secs = totalSec % 60L
    return "%02d:%02d".format(mins, secs)
}
