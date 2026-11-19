package com.example.ui.components

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.example.data.model.StreamProtocol
import com.example.data.model.detectStreamProtocol
import com.example.data.model.protocolBadgeLabel
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CrimsonRedBright
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MutedGray
import com.example.ui.theme.PureWhite
import kotlinx.coroutines.delay
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout

@Composable
fun AlAnsariMediaPlayer(
    streamUrl: String,
    title: String,
    isLiveStream: Boolean,
    modifier: Modifier = Modifier,
) {
    val protocol = remember(streamUrl) { detectStreamProtocol(streamUrl) }
    val useLibVlcEngine = protocol == StreamProtocol.SRT ||
        protocol == StreamProtocol.RTMP ||
        protocol == StreamProtocol.RTSP

    if (useLibVlcEngine) {
        LibVlcBroadcastPlayer(
            streamUrl = streamUrl,
            title = title,
            protocol = protocol,
            isLiveStream = isLiveStream,
            modifier = modifier,
        )
    } else {
        ExoMediaPlayerView(
            streamUrl = streamUrl,
            title = title,
            protocol = protocol,
            isLiveStream = isLiveStream,
            modifier = modifier,
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun ExoMediaPlayerView(
    streamUrl: String,
    title: String,
    protocol: StreamProtocol,
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
    var reloadNonce by remember { mutableIntStateOf(0) }

    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    LaunchedEffect(streamUrl, reloadNonce) {
        if (streamUrl.isBlank()) {
            errorMessage = "لا يوجد رابط بث فعّال حالياً."
            isBuffering = false
            return@LaunchedEffect
        }
        errorMessage = null
        isBuffering = true
        val mediaItemBuilder = MediaItem.Builder().setUri(streamUrl)
        if (protocol == StreamProtocol.HLS) {
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
                errorMessage = "تعذر تشغيل البث (${error.errorCodeName}). تحقق من الاتصال بالإنترنت."
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
                ExoSurfaceWithOverlay(
                    exoPlayer = exoPlayer,
                    title = title,
                    protocolLabel = protocolBadgeLabel(protocol),
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
                    onPlayPause = { if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play() },
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
            ExoSurfaceWithOverlay(
                exoPlayer = exoPlayer,
                title = title,
                protocolLabel = protocolBadgeLabel(protocol),
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
                onPlayPause = { if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play() },
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

@Composable
private fun LibVlcBroadcastPlayer(
    streamUrl: String,
    title: String,
    protocol: StreamProtocol,
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
    var reloadNonce by remember { mutableIntStateOf(0) }

    val vlcHolder = remember(context) {
        runCatching {
            val options = arrayListOf(
                "--no-drop-late-frames",
                "--no-skip-frames",
                "--network-caching=450",
                "--live-caching=450",
                "--rtsp-tcp",
            )
            val libVlc = LibVLC(context.applicationContext, options)
            val mediaPlayer = MediaPlayer(libVlc).apply {
                videoScale = MediaPlayer.ScaleType.SURFACE_BEST_FIT
            }
            libVlc to mediaPlayer
        }.getOrNull()
    }

    val libVlc = vlcHolder?.first
    val mediaPlayer = vlcHolder?.second

    LaunchedEffect(mediaPlayer, streamUrl, reloadNonce) {
        if (mediaPlayer == null || libVlc == null) {
            isBuffering = false
            errorMessage = "مشغل RTMP/SRT غير متاح على هذه البيئة."
            return@LaunchedEffect
        }
        if (streamUrl.isBlank()) {
            isBuffering = false
            errorMessage = "لا يوجد رابط بث فعّال حالياً."
            return@LaunchedEffect
        }
        errorMessage = null
        isBuffering = true
        runCatching {
            mediaPlayer.stop()
            val media = Media(libVlc, Uri.parse(streamUrl)).apply {
                setHWDecoderEnabled(true, false)
                addOption(":network-caching=450")
                addOption(":live-caching=450")
                addOption(":clock-jitter=0")
                addOption(":clock-synchro=0")
            }
            mediaPlayer.media = media
            media.release()
            mediaPlayer.volume = if (isMuted) 0 else 100
            mediaPlayer.play()
        }.onFailure { err ->
            isBuffering = false
            errorMessage = "تعذر الاتصال ببث ${protocol.name}: ${err.localizedMessage ?: "تحقق من الرابط"}"
        }
    }

    DisposableEffect(mediaPlayer) {
        if (mediaPlayer != null) {
            mediaPlayer.setEventListener { event ->
                when (event.type) {
                    MediaPlayer.Event.Opening -> {
                        isBuffering = true
                        errorMessage = null
                    }
                    MediaPlayer.Event.Buffering -> {
                        isBuffering = event.buffering < 100f
                    }
                    MediaPlayer.Event.Playing -> {
                        isBuffering = false
                        isPlaying = true
                        errorMessage = null
                    }
                    MediaPlayer.Event.Paused -> {
                        isPlaying = false
                        isBuffering = false
                    }
                    MediaPlayer.Event.Stopped -> {
                        isPlaying = false
                    }
                    MediaPlayer.Event.EndReached -> {
                        isPlaying = false
                        isBuffering = false
                    }
                    MediaPlayer.Event.EncounteredError -> {
                        isBuffering = false
                        isPlaying = false
                        errorMessage = "تعذر الاتصال ببث ${protocol.name}. تأكد من تشغيل المرسل وتوفر الإنترنت."
                    }
                }
            }
        }
        onDispose {
            runCatching {
                mediaPlayer?.setEventListener(null)
                mediaPlayer?.stop()
                mediaPlayer?.detachViews()
                mediaPlayer?.release()
                libVlc?.release()
            }
        }
    }

    DisposableEffect(lifecycleOwner, mediaPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> runCatching { mediaPlayer?.pause() }
                Lifecycle.Event.ON_RESUME -> if (isPlaying) runCatching { mediaPlayer?.play() }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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
        runCatching { mediaPlayer?.volume = if (isMuted) 0 else 100 }
        Unit
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
                VlcSurfaceWithOverlay(
                    mediaPlayer = mediaPlayer,
                    title = title,
                    protocolLabel = protocolBadgeLabel(protocol),
                    isLiveStream = isLiveStream,
                    isPlaying = isPlaying,
                    isBuffering = isBuffering,
                    isMuted = isMuted,
                    isFullscreen = true,
                    showControls = showControls,
                    errorMessage = errorMessage,
                    onToggleControls = { showControls = !showControls },
                    onPlayPause = {
                        runCatching {
                            if (mediaPlayer?.isPlaying == true) mediaPlayer.pause() else mediaPlayer?.play()
                        }
                    },
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
            VlcSurfaceWithOverlay(
                mediaPlayer = mediaPlayer,
                title = title,
                protocolLabel = protocolBadgeLabel(protocol),
                isLiveStream = isLiveStream,
                isPlaying = isPlaying,
                isBuffering = isBuffering,
                isMuted = isMuted,
                isFullscreen = false,
                showControls = showControls,
                errorMessage = errorMessage,
                onToggleControls = { showControls = !showControls },
                onPlayPause = {
                    runCatching {
                        if (mediaPlayer?.isPlaying == true) mediaPlayer.pause() else mediaPlayer?.play()
                    }
                },
                onToggleMute = toggleMute,
                onReload = { reloadNonce++ },
                onToggleFullscreen = { toggleFullscreen(true) },
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun ExoSurfaceWithOverlay(
    exoPlayer: ExoPlayer,
    title: String,
    protocolLabel: String,
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

        PlayerControlsOverlay(
            title = title,
            protocolLabel = protocolLabel,
            isLiveStream = isLiveStream,
            isPlaying = isPlaying,
            isBuffering = isBuffering,
            isMuted = isMuted,
            isFullscreen = isFullscreen,
            showControls = showControls,
            errorMessage = errorMessage,
            currentPosMs = currentPosMs,
            durationMs = durationMs,
            onPlayPause = onPlayPause,
            onSeekBy = onSeekBy,
            onSeekTo = onSeekTo,
            onToggleMute = onToggleMute,
            onReload = onReload,
            onToggleFullscreen = onToggleFullscreen,
        )
    }
}

@Composable
private fun VlcSurfaceWithOverlay(
    mediaPlayer: MediaPlayer?,
    title: String,
    protocolLabel: String,
    isLiveStream: Boolean,
    isPlaying: Boolean,
    isBuffering: Boolean,
    isMuted: Boolean,
    isFullscreen: Boolean,
    showControls: Boolean,
    errorMessage: String?,
    onToggleControls: () -> Unit,
    onPlayPause: () -> Unit,
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
        if (mediaPlayer != null) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    VLCVideoLayout(ctx).apply {
                        keepScreenOn = true
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        runCatching {
                            mediaPlayer.attachViews(this, null, false, false)
                        }
                    }
                },
                onRelease = {
                    runCatching { mediaPlayer.detachViews() }
                },
            )
        }

        PlayerControlsOverlay(
            title = title,
            protocolLabel = protocolLabel,
            isLiveStream = isLiveStream,
            isPlaying = isPlaying,
            isBuffering = isBuffering,
            isMuted = isMuted,
            isFullscreen = isFullscreen,
            showControls = showControls,
            errorMessage = errorMessage,
            currentPosMs = 0L,
            durationMs = 0L,
            onPlayPause = onPlayPause,
            onSeekBy = {},
            onSeekTo = {},
            onToggleMute = onToggleMute,
            onReload = onReload,
            onToggleFullscreen = onToggleFullscreen,
        )
    }
}

@Composable
private fun PlayerControlsOverlay(
    title: String,
    protocolLabel: String,
    isLiveStream: Boolean,
    isPlaying: Boolean,
    isBuffering: Boolean,
    isMuted: Boolean,
    isFullscreen: Boolean,
    showControls: Boolean,
    errorMessage: String?,
    currentPosMs: Long,
    durationMs: Long,
    onPlayPause: () -> Unit,
    onSeekBy: (Long) -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleMute: () -> Unit,
    onReload: () -> Unit,
    onToggleFullscreen: () -> Unit,
) {
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
                            text = protocolLabel,
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

            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                if (!isLiveStream && durationMs > 0L) {
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

                if (!isLiveStream && durationMs > 0L) {
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

private fun formatDurationMs(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val mins = totalSec / 60L
    val secs = totalSec % 60L
    return "%02d:%02d".format(mins, secs)
}
