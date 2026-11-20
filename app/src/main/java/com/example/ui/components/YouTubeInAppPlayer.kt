package com.example.ui.components

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
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
    val appReferer = remember(context) { "https://${context.packageName}/" }

    var isLoading by remember(videoId, fallbackWebUrl) { mutableStateOf(true) }
    var embedBlockedOrLoginNeeded by remember(videoId) { mutableStateOf(false) }
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
                runCatching {
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(this, true)
                }
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
                fun onEmbedIssue() {
                    mainHandler.post {
                        isLoading = false
                        embedBlockedOrLoginNeeded = true
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
                    val urlString = uri.toString()
                    val scheme = uri.scheme?.lowercase().orEmpty()
                    val host = uri.host?.lowercase().orEmpty()

                    // Never navigate the embedded player to YouTube's Google Sign-In page
                    if (host.contains("accounts.google.") ||
                        urlString.contains("ServiceLogin", ignoreCase = true) ||
                        urlString.contains("signin", ignoreCase = true)
                    ) {
                        mainHandler.post {
                            embedBlockedOrLoginNeeded = true
                            isLoading = false
                        }
                        return true
                    }

                    if (scheme != "http" && scheme != "https") {
                        return true
                    }

                    val isEmbedMediaHost = host.contains("youtube-nocookie.com") ||
                        host.contains("youtube.com") ||
                        host.contains("googlevideo.com") ||
                        host.contains("ytimg.com")
                    return !isEmbedMediaHost
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    isLoading = false
                    // Detect YouTube's bot/sign-in interstitial inside the embed DOM without showing a login screen
                    view?.evaluateJavascript(
                        """
                        (function() {
                          try {
                            var bodyText = (document.body && document.body.innerText) ? document.body.innerText : '';
                            if (bodyText.indexOf('Sign in to confirm') !== -1 ||
                                bodyText.indexOf('تسجيل الدخول لتأكيد') !== -1 ||
                                bodyText.indexOf('Error 153') !== -1 ||
                                bodyText.indexOf('Video unavailable') !== -1) {
                              if (window.AndroidYtBridge) AndroidYtBridge.onEmbedIssue();
                            }
                          } catch(e) {}
                        })();
                        """.trimIndent(),
                        null,
                    )
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
                Lifecycle.Event.ON_PAUSE -> runCatching { webViewHolder?.onPause() }
                Lifecycle.Event.ON_RESUME -> runCatching { webViewHolder?.onResume() }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(webViewHolder, videoId, fallbackWebUrl, reloadNonce) {
        val wv = webViewHolder ?: return@LaunchedEffect
        val cleanId = videoId?.trim()?.replace(Regex("[^a-zA-Z0-9_-]"), "").orEmpty()
        if (cleanId.isBlank()) {
            isLoading = false
            embedBlockedOrLoginNeeded = true
            return@LaunchedEffect
        }
        embedBlockedOrLoginNeeded = false
        isLoading = true
        val embedUrl = "https://www.youtube-nocookie.com/embed/$cleanId?autoplay=1&playsinline=1&rel=0&modestbranding=1&fs=1&hl=ar"
        wv.loadUrl(embedUrl, mapOf("Referer" to appReferer))
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
            if (webViewHolder != null && fullscreenView == null && !embedBlockedOrLoginNeeded) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = {
                        (webViewHolder.parent as? ViewGroup)?.removeView(webViewHolder)
                        webViewHolder
                    },
                )
            }

            if (embedBlockedOrLoginNeeded) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.92f))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "تعذر تشغيل هذا المقطع بالتضمين المباشر على الشبكة الحالية بدون حساب.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = PureWhite),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                embedBlockedOrLoginNeeded = false
                                reloadNonce++
                            },
                            shape = RoundedCornerShape(50),
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, tint = GoldBright, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إعادة المحاولة", color = GoldBright)
                        }
                        Button(
                            onClick = {
                                val target = if (!videoId.isNullOrBlank()) {
                                    "https://www.youtube.com/watch?v=$videoId"
                                } else {
                                    fallbackWebUrl
                                }
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(target)))
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = PureWhite),
                            shape = RoundedCornerShape(50),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تطبيق يوتيوب", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            if (isLoading && !embedBlockedOrLoginNeeded) {
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
                            text = "جاري تحميل المشغّل...",
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
                    text = "مشغّل مباشر",
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
                    embedBlockedOrLoginNeeded = false
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
