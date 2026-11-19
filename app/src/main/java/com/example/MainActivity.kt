package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.auth.GoogleAuthHelper
import com.example.data.local.ChannelConfigStore
import com.example.data.remote.FirestoreChannelRepository
import com.example.data.repository.ChannelRepository
import com.example.ui.components.AlAnsariBottomBar
import com.example.ui.navigation.AlAnsariRoutes
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.BookingCoverageScreen
import com.example.ui.screens.EventsScreen
import com.example.ui.screens.FieldCoverageScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveStreamScreen
import com.example.ui.screens.MoreServicesScreen
import com.example.ui.screens.NewsDetailScreen
import com.example.ui.screens.NewsScreen
import com.example.ui.screens.SocialLinksScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.screens.VideosScreen
import com.example.ui.theme.AlAnsariTheme
import com.example.ui.theme.CardSurfaceTop
import com.example.ui.theme.GoldBright
import com.example.ui.theme.ObsidianBlack
import com.example.ui.viewmodel.AlAnsariViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlAnsariTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AlAnsariApp()
                }
            }
        }
    }
}

@Composable
fun AlAnsariApp() {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val viewModel: AlAnsariViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val configStore = ChannelConfigStore(appContext)
                val repo = ChannelRepository(configStore)
                val firestoreDb = FirebaseFirestore.getInstance(
                    appContext.getString(R.string.firestore_database_id),
                )
                val firestoreRepo = FirestoreChannelRepository(firestoreDb)
                AlAnsariViewModel(repo, firestoreRepo)
            }
        },
    )

    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AlAnsariRoutes.HOME

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    LaunchedEffect(Unit) {
        GoogleAuthHelper.attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            scope = coroutineScope,
            onAuthSuccess = {},
            onUnauthenticated = {},
        )
    }

    LaunchedEffect(uiState.statusMessage) {
        val msg = uiState.statusMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    val navigateTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val activeBottomTab = when {
        currentRoute == AlAnsariRoutes.HOME -> AlAnsariRoutes.HOME
        currentRoute == AlAnsariRoutes.VIDEOS || currentRoute.startsWith("video_player") -> AlAnsariRoutes.VIDEOS
        currentRoute == AlAnsariRoutes.LIVE -> AlAnsariRoutes.LIVE
        else -> AlAnsariRoutes.MORE
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        containerColor = ObsidianBlack,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = CardSurfaceTop,
                    contentColor = GoldBright,
                )
            }
        },
        bottomBar = {
            AlAnsariBottomBar(
                currentRoute = activeBottomTab,
                onNavigate = navigateTab,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            NavHost(
                navController = navController,
                startDestination = AlAnsariRoutes.HOME,
            ) {
                composable(AlAnsariRoutes.HOME) {
                    HomeScreen(
                        onNavigate = { route ->
                            when (route) {
                                AlAnsariRoutes.VIDEOS, AlAnsariRoutes.LIVE, AlAnsariRoutes.MORE -> navigateTab(route)
                                else -> navController.navigate(route)
                            }
                        },
                        channelLogoUrl = uiState.channelLogoUrl,
                        channelTitle = uiState.channelTitle,
                        channelHandle = uiState.settings.youtubeHandle,
                    )
                }

                composable(AlAnsariRoutes.LIVE) {
                    LiveStreamScreen(
                        uiState = uiState,
                        onSelectSource = viewModel::selectLiveSource,
                        onBackClick = { navigateTab(AlAnsariRoutes.HOME) },
                    )
                }

                composable(AlAnsariRoutes.BOOKING_REQUEST) {
                    BookingCoverageScreen(
                        uiState = uiState,
                        onSubmitBooking = viewModel::submitCloudCoverageBooking,
                        onShowMessage = viewModel::showStatusMessage,
                        onBackClick = { navController.popBackStack() },
                    )
                }

                composable(AlAnsariRoutes.VIDEOS) {
                    VideosScreen(
                        uiState = uiState,
                        onVideoClick = { videoId ->
                            navController.navigate(AlAnsariRoutes.videoPlayer(videoId))
                        },
                        onToggleBookmark = viewModel::toggleVideoBookmark,
                        onRefreshYouTube = { viewModel.refreshFromYouTube(showToast = true) },
                        onBackClick = { navigateTab(AlAnsariRoutes.HOME) },
                    )
                }

                composable(
                    route = AlAnsariRoutes.VIDEO_PLAYER,
                    arguments = listOf(navArgument("videoId") { type = NavType.StringType }),
                ) { entry ->
                    val videoId = entry.arguments?.getString("videoId").orEmpty()
                    VideoPlayerScreen(
                        videoId = videoId,
                        uiState = uiState,
                        onSelectVideo = { nextId ->
                            navController.navigate(AlAnsariRoutes.videoPlayer(nextId)) {
                                popUpTo(AlAnsariRoutes.VIDEOS)
                            }
                        },
                        onToggleBookmark = viewModel::toggleVideoBookmark,
                        onBackClick = { navController.popBackStack() },
                    )
                }

                composable(AlAnsariRoutes.FIELD_COVERAGE) {
                    FieldCoverageScreen(
                        uiState = uiState,
                        onBackClick = { navController.popBackStack() },
                    )
                }

                composable(AlAnsariRoutes.EVENTS) {
                    EventsScreen(
                        uiState = uiState,
                        onBackClick = { navController.popBackStack() },
                    )
                }

                composable(AlAnsariRoutes.NEWS) {
                    NewsScreen(
                        uiState = uiState,
                        onNewsClick = { newsId ->
                            navController.navigate(AlAnsariRoutes.newsDetail(newsId))
                        },
                        onToggleBookmark = viewModel::toggleNewsBookmark,
                        onBackClick = { navController.popBackStack() },
                    )
                }

                composable(
                    route = AlAnsariRoutes.NEWS_DETAIL,
                    arguments = listOf(navArgument("newsId") { type = NavType.StringType }),
                ) { entry ->
                    val newsId = entry.arguments?.getString("newsId").orEmpty()
                    NewsDetailScreen(
                        newsId = newsId,
                        uiState = uiState,
                        onToggleBookmark = viewModel::toggleNewsBookmark,
                        onBackClick = { navController.popBackStack() },
                    )
                }

                composable(AlAnsariRoutes.SOCIAL) {
                    SocialLinksScreen(
                        uiState = uiState,
                        onShowMessage = { msg ->
                            coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                        },
                        onBackClick = { navController.popBackStack() },
                    )
                }

                composable(AlAnsariRoutes.MORE) {
                    MoreServicesScreen(
                        uiState = uiState,
                        onNavigate = { route ->
                            when (route) {
                                AlAnsariRoutes.LIVE, AlAnsariRoutes.VIDEOS -> navigateTab(route)
                                else -> navController.navigate(route)
                            }
                        },
                        onShowMessage = { msg ->
                            coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                        },
                        onBackClick = { navigateTab(AlAnsariRoutes.HOME) },
                    )
                }

                composable(AlAnsariRoutes.ADMIN_PANEL) {
                    AdminPanelScreen(
                        uiState = uiState,
                        onPublishCloudConfig = viewModel::publishCloudChannelConfig,
                        onUpdateBookingStatus = viewModel::updateBookingStatus,
                        onRefreshYouTube = { viewModel.refreshFromYouTube(showToast = true) },
                        onSyncCms = viewModel::syncFromCmsDashboard,
                        onShowMessage = viewModel::showStatusMessage,
                        onBackClick = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}
