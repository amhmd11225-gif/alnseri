package com.example.ui.components

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Color as AndroidColor
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AlAnsariYouTubePlayer(
    videoId: String?,
    fallbackWebUrl: String,
    title: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    var isLoading by remember(videoId, fallbackWebUrl) { mutableStateOf(true) }
    var useMobileWebFallback by remember(videoId) { mutableStateOf(videoId.isNullOrBlank()) }
    var reloadNonce by remember { mutableIntStateOf(0) }
    var fullscreenView by remember { mutableStateOf<View?>(null) }
    var fullscreenCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    val webViewHolder = remember(context) {
        runCatching {
            WebView(context).apply {
                setBackgroundColor(AndroidColor.BLACK)
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    allowContentAccess = true
                    javaScriptCanOpenWindowsAutomatically = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    userAgentString =
                        "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"
                }
            }
        }.getOrNull()
    }

    fun exitFullscreen() {
        runCatching { fullscreenCallback?.onCustomViewHidden() }
        fullscreenCallback = null
        fullscreenView = null
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    DisposableEffect(webViewHolder) {
        if (webViewHolder != null) {
            val bridge = object {
                @JavascriptInterface
                fun onPlayerReady() {
                    mainHandler.post { isLoading = false }
                }

                @JavascriptInterface
                fun onPlayerError(code: Int) {
                    mainHandler.post {
                        if (!useMobileWebFallback && !videoId.isNullOrBlank()) {
                            useMobileWebFallback = true
                        } else {
                            isLoading = false
                        }
                    }
                }
            }
            webViewHolder.addJavascriptInterface(bridge, "AndroidYtBridge")
            webViewHolder.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?,
                ): Boolean {
                    val uri = request?.url ?: return false
                    val scheme = uri.scheme?.lowercase().orEmpty()
                    val host = uri.host?.lowercase().orEmpty()
                    if (scheme != "http" && scheme != "https") {
                        return true
                    }
                    val isAllowedYouTubeHost = host.contains("youtube.com") ||
                        host.contains("youtu.be") ||
                        host.contains("youtube-nocookie.com") ||
                        host.contains("googlevideo.com") ||
                        host.contains("ytimg.com") ||
                        host.contains("google.com")
                    return !isAllowedYouTubeHost
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    isLoading = false
                }
            }
            webViewHolder.webChromeClient = object : WebChromeClient() {
                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    if (view == null) return
                    fullscreenCallback?.onCustomViewHidden()
                    fullscreenView = view
                    fullscreenCallback = callback
                    val activity = context as? Activity
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                }

                override fun onHideCustomView() {
                    exitFullscreen()
                }
            }
        }
        onDispose {
            exitFullscreen()
            runCatching {
                webViewHolder?.apply {
                    loadUrl("about:blank")
                    stopLoading()
                    removeJavascriptInterface("AndroidYtBridge")
                    clearHistory()
                    removeAllViews()
                    destroy()
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner, webViewHolder) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> runCatching {
                    webViewHolder?.evaluateJavascript("if (window.ytPlayer && ytPlayer.pauseVideo) { ytPlayer.pauseVideo(); }", null)
                    webViewHolder?.onPause()
                }
                Lifecycle.Event.ON_RESUME -> runCatching {
                    webViewHolder?.onResume()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(webViewHolder, videoId, fallbackWebUrl, useMobileWebFallback, reloadNonce) {
        val wv = webViewHolder ?: return@LaunchedEffect
        isLoading = true
        val cleanId = videoId?.trim().orEmpty()
        if (cleanId.isNotBlank() && !useMobileWebFallback) {
            val html = buildYouTubeIframeHtml(cleanId)
            wv.loadDataWithBaseURL(
                "https://www.youtube.com/",
                html,
                "text/html",
                "UTF-8",
                "https://www.youtube.com/",
            )
        } else {
            val targetUrl = if (cleanId.isNotBlank()) {
                "https://m.youtube.com/watch?v=$cleanId"
            } else {
                fallbackWebUrl.ifBlank { "https://m.youtube.com/@alansarie1" }
            }
            wv.loadUrl(targetUrl, mapOf("Referer" to "https://www.youtube.com/"))
        }
    }

    if (fullscreenView != null) {
        Dialog(
            onDismissRequest = { exitFullscreen() },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
        ) {
            BackHandler { exitFullscreen() }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        FrameLayout(ctx).apply {
                            val target = fullscreenView
                            if (target != null) {
                                (target.parent as? ViewGroup)?.removeView(target)
                                addView(
                                    target,
                                    FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                    ),
                                )
                            }
                        }
                    },
                )
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("youtube_inapp_player"),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black)
                .border(1.2.dp, GoldPrimary, RoundedCornerShape(16.dp)),
        ) {
            if (webViewHolder != null && fullscreenView == null) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = {
                        (webViewHolder.parent as? ViewGroup)?.removeView(webViewHolder)
                        webViewHolder
                    },
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.65f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = GoldBright, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "جاري تشغيل الفيديو داخل التطبيق...",
                            style = MaterialTheme.typography.labelMedium.copy(color = GoldBright),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ObsidianBlack)
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                CrimsonPillBadge(
                    text = if (useMobileWebFallback) "مشغل يوتيوب المباشر" else "تشغيل داخلي",
                    horizontalPadding = 10.dp,
                    verticalPadding = 2.dp,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.Medium,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(
                onClick = {
                    useMobileWebFallback = videoId.isNullOrBlank()
                    reloadNonce++
                },
                modifier = Modifier
                    .size(40.dp)
                    .testTag("youtube_reload_button"),
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "إعادة تحميل الفيديو",
                    tint = GoldBright,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

private fun buildYouTubeIframeHtml(rawVideoId: String): String {
    val safeId = rawVideoId.replace(Regex("[^a-zA-Z0-9_-]"), "")
    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
          <meta name="referrer" content="strict-origin-when-cross-origin" />
          <style>
            html, body {
              margin: 0;
              padding: 0;
              width: 100%;
              height: 100%;
              background-color: #000000;
              overflow: hidden;
            }
            #player {
              position: fixed;
              inset: 0;
              width: 100%;
              height: 100%;
              border: 0;
            }
          </style>
        </head>
        <body>
          <div id="player"></div>
          <script>
            var tag = document.createElement('script');
            tag.src = 'https://www.youtube.com/iframe_api';
            tag.onerror = function() {
              if (window.AndroidYtBridge) { AndroidYtBridge.onPlayerError(-1); }
            };
            var firstScriptTag = document.getElementsByTagName('script')[0];
            firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

            var ytPlayer;
            function onYouTubeIframeAPIReady() {
              ytPlayer = new YT.Player('player', {
                width: '100%',
                height: '100%',
                videoId: '$safeId',
                playerVars: {
                  autoplay: 1,
                  playsinline: 1,
                  controls: 1,
                  fs: 1,
                  rel: 0,
                  modestbranding: 1,
                  enablejsapi: 1,
                  origin: 'https://www.youtube.com'
                },
                events: {
                  onReady: function(event) {
                    if (window.AndroidYtBridge) { AndroidYtBridge.onPlayerReady(); }
                    try { event.target.playVideo(); } catch (e) {}
                  },
                  onError: function(event) {
                    if (window.AndroidYtBridge) { AndroidYtBridge.onPlayerError(event.data || 150); }
                  }
                }
              });
            }
          </script>
        </body>
        </html>
    """.trimIndent()
}
