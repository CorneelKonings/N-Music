package moe.rukamori.archivetune.ui.player.player_0.sett

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.work.WorkInfo
import androidx.work.WorkManager
import moe.rukamori.archivetune.R
import moe.rukamori.archivetune.playback.ExoDownloadService
import androidx.compose.material3.CircularWavyProgressIndicator
import moe.rukamori.archivetune.ui.player.player_0.buttons.PlayerAction
import moe.rukamori.archivetune.ui.state.PlayerUiState
import moe.rukamori.archivetune.ui.state.UpdateState
import moe.rukamori.archivetune.LocalDownloadUtil
import moe.rukamori.archivetune.ui.utils.ShowMediaInfo
import moe.rukamori.archivetune.download.FlacDownloader

// Состояния суб-навигации
enum class PlayerMenuScreen { SETTINGS, CUSTOMIZATION, SLEEP_TIMER, ABOUT, DETAILS, DOWNLOAD }

@Composable
fun FullPlayerOptionsMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    state: PlayerUiState,
    updateState: UpdateState,
    onAction: (PlayerAction) -> Unit,
    onBackgroundStyleChanged: (Boolean) -> Unit,
    onImmersiveChanged: (Boolean) -> Unit,
    onOpenEqualizer: () -> Unit = {},
    onOpenPlaybackSpeed: () -> Unit = {},
    onOpenAddToPlaylist: () -> Unit = {},
    modifier: Modifier = Modifier,
    initialScreen: PlayerMenuScreen = PlayerMenuScreen.SETTINGS
) {

    val GoogleSans = FontFamily(
        Font(R.font.google_sans_regular, FontWeight.Normal),
        Font(R.font.google_sans_bold, FontWeight.Bold)
    )

    // Стейт текущего экрана внутри меню
    var currentScreen by remember { mutableStateOf(PlayerMenuScreen.SETTINGS) }

    // Сбрасываем или выставляем экран при открытии меню
    LaunchedEffect(expanded) {
        if (expanded) {
            currentScreen = initialScreen
        }
    }

    // Обработка системной кнопки "Назад"
    if (expanded) {
        BackHandler {
            when {
                currentScreen == PlayerMenuScreen.DETAILS ||
                currentScreen == PlayerMenuScreen.CUSTOMIZATION ||
                currentScreen == PlayerMenuScreen.SLEEP_TIMER ||
                currentScreen == PlayerMenuScreen.DOWNLOAD ||
                currentScreen == PlayerMenuScreen.ABOUT -> currentScreen = PlayerMenuScreen.SETTINGS
                else -> onDismissRequest()
            }
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(300, easing = LinearEasing),
        label = "MenuAlpha"
    )

    val translateY by animateFloatAsState(
        targetValue = if (expanded) 0f else 300f,
        animationSpec = spring(dampingRatio = 1f, stiffness = Spring.StiffnessLow),
        label = "MenuSlide"
    )

    if (alpha > 0f) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = alpha * 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismissRequest
                ),
            contentAlignment = Alignment.Center
        ) {
            val cardGradient = Brush.verticalGradient(
                colors = listOf(Color(state.darkMutedColor), Color(0xFF161616))
            )

            Box(
                modifier = Modifier
                    .width(340.dp)
                    .graphicsLayer {
                        this.translationY = translateY
                        this.alpha = alpha
                    }
                    .clip(RoundedCornerShape(28.dp))
                    .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(28.dp))
                    .background(cardGradient)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {})
                    .padding(20.dp)
            ) {
                Column {
                    // ==========================================
                    // АНИМИРОВАННЫЙ КОНТЕНТ (Суб-навигация)
                    // ==========================================
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            if (targetState != PlayerMenuScreen.SETTINGS) {
                                (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
                            } else {
                                (slideInHorizontally { -it } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
                            }.using(SizeTransform(clip = false))
                        },
                        label = "MenuScreenTransition"
                    ) { screen ->
                        when (screen) {
                            PlayerMenuScreen.SETTINGS -> {
                                SettingsMenuContent(
                                    state = state,
                                    updateState = updateState,
                                    onNavigateToAbout = { currentScreen = PlayerMenuScreen.ABOUT },
                                    onNavigateToCustomization = { currentScreen = PlayerMenuScreen.CUSTOMIZATION },
                                    onNavigateToSleepTimer = { currentScreen = PlayerMenuScreen.SLEEP_TIMER },
                                    onNavigateToDetails = { currentScreen = PlayerMenuScreen.DETAILS },
                                    onNavigateToDownload = { currentScreen = PlayerMenuScreen.DOWNLOAD },
                                    onOpenEqualizer = onOpenEqualizer,
                                    onOpenPlaybackSpeed = onOpenPlaybackSpeed,
                                    onOpenAddToPlaylist = onOpenAddToPlaylist,
                                    onAction = onAction
                                )
                            }

                            PlayerMenuScreen.CUSTOMIZATION -> {
                                CustomizationMenuContent(
                                    state = state,
                                    onBackgroundStyleChanged = onBackgroundStyleChanged,
                                    onImmersiveChanged = onImmersiveChanged,
                                    onAction = onAction
                                )
                            }
                            PlayerMenuScreen.SLEEP_TIMER -> {
                                SleepTimerMenuContent(
                                    state = state,
                                    onAction = onAction,
                                    onBackClick = { currentScreen = PlayerMenuScreen.SETTINGS }
                                )
                            }
                            PlayerMenuScreen.ABOUT -> {
                                AboutMenuSection(
                                    state = state,
                                    updateState = updateState,
                                    onAction = onAction,
                                    onDismissRequest = onDismissRequest
                                )
                            }
                            PlayerMenuScreen.DOWNLOAD -> {
                                DownloadMenuContent(
                                    state = state,
                                    onDismissRequest = onDismissRequest
                                )
                            }
                            PlayerMenuScreen.DETAILS -> {
                                // Показываем информацию о треке (ShowMediaInfo содержит собственный LazyColumn)
                                val songId = state.trackUrl
                                if (songId.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(460.dp)
                                    ) {
                                        ShowMediaInfo(videoId = songId)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ==========================================
                    // АДАПТИВНЫЙ ПОДВАЛ (Footer)
                    // ==========================================
                    Text(
                        text = if (currentScreen == PlayerMenuScreen.SETTINGS) "Close" else "Back",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = GoogleSans,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                when {
                                    currentScreen != PlayerMenuScreen.SETTINGS -> currentScreen = PlayerMenuScreen.SETTINGS
                                    else -> onDismissRequest()
                                }
                            }
                            .padding(6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadMenuContent(
    state: PlayerUiState,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val download by LocalDownloadUtil.current
        .getDownload(state.trackUrl)
        .collectAsState(initial = null)
    
    val (playbackSource) = moe.rukamori.archivetune.utils.rememberEnumPreference(
        moe.rukamori.archivetune.constants.PlaybackSourceKey, 
        defaultValue = moe.rukamori.archivetune.constants.PlaybackSource.YT_MUSIC
    )
    val (externalDownloaderEnabled) = moe.rukamori.archivetune.utils.rememberPreference(
        moe.rukamori.archivetune.constants.ExternalDownloaderEnabledKey, 
        defaultValue = false
    )
    val (externalDownloaderPackage) = moe.rukamori.archivetune.utils.rememberPreference(
        moe.rukamori.archivetune.constants.ExternalDownloaderPackageKey, 
        defaultValue = ""
    )

    val GoogleSans = FontFamily(
        Font(R.font.google_sans_regular, FontWeight.Normal),
        Font(R.font.google_sans_bold, FontWeight.Bold)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Download",
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = GoogleSans,
            modifier = Modifier.padding(start = 4.dp, bottom = 16.dp)
        )

        when (download?.state) {
            Download.STATE_COMPLETED -> {
                ListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(R.string.remove_download),
                            color = MaterialTheme.colorScheme.error,
                        )
                    },
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.offline),
                            tint = MaterialTheme.colorScheme.error,
                            contentDescription = null,
                        )
                    },
                    modifier = Modifier.clickable {
                        DownloadService.sendRemoveDownload(
                            context,
                            ExoDownloadService::class.java,
                            state.trackUrl,
                            false,
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            Download.STATE_QUEUED, Download.STATE_DOWNLOADING -> {
                ListItem(
                    headlineContent = { Text(text = stringResource(R.string.downloading), color = Color.White) },
                    leadingContent = {
                        CircularWavyProgressIndicator(
                            modifier = Modifier.size(24.dp),
                        )
                    },
                    modifier = Modifier.clickable {
                        DownloadService.sendRemoveDownload(
                            context,
                            ExoDownloadService::class.java,
                            state.trackUrl,
                            false,
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            else -> {
                ListItem(
                    headlineContent = { Text(text = stringResource(R.string.action_download), color = Color.White) },
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.download),
                            contentDescription = null,
                            tint = Color.White
                        )
                    },
                    modifier = Modifier.clickable {
                        val downloadRequest = DownloadRequest
                            .Builder(state.trackUrl, state.trackUrl.toUri())
                            .setCustomCacheKey(state.trackUrl)
                            .setData(state.title.toByteArray())
                            .build()
                        DownloadService.sendAddDownload(
                            context,
                            ExoDownloadService::class.java,
                            downloadRequest,
                            false,
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
        }

        if (playbackSource == moe.rukamori.archivetune.constants.PlaybackSource.FLAC) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 56.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            val flacWorkInfos by WorkManager.getInstance(context)
                .getWorkInfosForUniqueWorkFlow("flac_download_${state.trackUrl}")
                .collectAsState(emptyList())
            val flacWorkState = flacWorkInfos.firstOrNull()?.state

            when (flacWorkState) {
                WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED -> {
                    ListItem(
                        headlineContent = { Text(text = stringResource(R.string.downloading), color = Color.White) },
                        leadingContent = {
                            CircularWavyProgressIndicator(
                                modifier = Modifier.size(24.dp),
                            )
                        },
                        modifier = Modifier.clickable {
                            WorkManager.getInstance(context).cancelUniqueWork("flac_download_${state.trackUrl}")
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
                WorkInfo.State.SUCCEEDED -> {
                    ListItem(
                        headlineContent = { Text(text = stringResource(R.string.remove_download), color = Color.White) },
                        leadingContent = {
                            Icon(
                                painter = painterResource(R.drawable.offline),
                                contentDescription = null,
                                tint = Color.White
                            )
                        },
                        modifier = Modifier.clickable {
                            FlacDownloader.deleteFlac(
                                context,
                                state.trackUrl,
                                state.title,
                                state.artist,
                                "",
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
                else -> {
                    ListItem(
                        headlineContent = { Text(text = stringResource(R.string.download_flac), color = Color.White) },
                        leadingContent = {
                            Icon(
                                painter = painterResource(R.drawable.download),
                                contentDescription = null,
                                tint = Color.White
                            )
                        },
                        modifier = Modifier.clickable {
                            FlacDownloader.downloadFlac(
                                context,
                                state.trackUrl,
                                state.title,
                                state.artist,
                                "",
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
            }
        }

        if (externalDownloaderEnabled) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 56.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            ListItem(
                headlineContent = { Text(text = stringResource(R.string.open_with_downloader), color = Color.White) },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.download),
                        contentDescription = null,
                        tint = Color.White
                    )
                },
                modifier = Modifier.clickable {
                    onDismissRequest()
                    val url = "https://music.youtube.com/watch?v=${state.trackUrl}"
                    if (externalDownloaderPackage.isBlank()) {
                        android.widget.Toast.makeText(
                            context,
                            context.getString(R.string.external_downloader_not_configured),
                            android.widget.Toast.LENGTH_LONG,
                        ).show()
                        return@clickable
                    }
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        setPackage(externalDownloaderPackage)
                        data = android.net.Uri.parse(url)
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: android.content.ActivityNotFoundException) {
                        android.widget.Toast.makeText(
                            context,
                            context.getString(R.string.external_downloader_not_installed),
                            android.widget.Toast.LENGTH_SHORT,
                        ).show()
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }
    }
}
