@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package moe.rukamori.archivetune.ui.screens.settings

import android.content.Intent
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import moe.rukamori.archivetune.ui.screens.settings.SettingsSectionLabel
import moe.rukamori.archivetune.ui.screens.settings.SettingsScreenBackground
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import moe.rukamori.archivetune.LocalPlayerAwareWindowInsets
import moe.rukamori.archivetune.R
import moe.rukamori.archivetune.constants.AudioNormalizationKey
import moe.rukamori.archivetune.constants.AudioQuality
import moe.rukamori.archivetune.constants.AudioQualityKey
import moe.rukamori.archivetune.constants.AutoSkipNextOnErrorKey
import moe.rukamori.archivetune.constants.AutoStartOnBluetoothKey
import moe.rukamori.archivetune.constants.CrossfadeDurationKey
import moe.rukamori.archivetune.constants.CrossfadeEnabledKey
import moe.rukamori.archivetune.constants.CrossfadeGaplessKey
import moe.rukamori.archivetune.constants.DownloadLocationUriKey
import moe.rukamori.archivetune.constants.EnableLosslessKey
import moe.rukamori.archivetune.constants.FlacQuality
import moe.rukamori.archivetune.constants.FlacQualityKey
import moe.rukamori.archivetune.constants.MemoryCacheToggleKey
import moe.rukamori.archivetune.constants.PauseOnDeviceMuteKey
import moe.rukamori.archivetune.constants.PlaybackSource
import moe.rukamori.archivetune.constants.PlaybackSourceKey
import moe.rukamori.archivetune.constants.SkipSilenceKey
import moe.rukamori.archivetune.constants.WakelockKey
import moe.rukamori.archivetune.ui.component.CrossfadeSliderPreference
import moe.rukamori.archivetune.ui.component.EditTextPreference
import moe.rukamori.archivetune.ui.component.EnumListPreference
import moe.rukamori.archivetune.ui.component.LocalPreferenceGroupPosition
import moe.rukamori.archivetune.ui.component.LocalPreferenceInGroup
import moe.rukamori.archivetune.ui.component.LocalPreferenceItemIndex
import moe.rukamori.archivetune.ui.component.PreferenceEntry
import moe.rukamori.archivetune.ui.component.PreferenceGroupPosition
import moe.rukamori.archivetune.ui.component.SwitchPreference
import moe.rukamori.archivetune.ui.component.IconButton
import moe.rukamori.archivetune.ui.settings.SettingsDimensions
import moe.rukamori.archivetune.ui.theme.LocalYumaColors
import moe.rukamori.archivetune.ui.theme.TestThemeWrapper
import moe.rukamori.archivetune.ui.theme.ThemePreviews
import moe.rukamori.archivetune.ui.theme.yumaGlassCard
import moe.rukamori.archivetune.ui.utils.appBarScrollBehavior
import moe.rukamori.archivetune.ui.utils.backToMain
import moe.rukamori.archivetune.utils.rememberEnumPreference
import moe.rukamori.archivetune.utils.rememberPreference

@Composable
fun PlayerSettings(navController: NavController) {
    val context = LocalContext.current

    val (playbackSource, onPlaybackSourceChange) = rememberEnumPreference(
        PlaybackSourceKey,
        defaultValue = PlaybackSource.YT_MUSIC,
    )
    val (flacQuality, onFlacQualityChange) = rememberEnumPreference(
        FlacQualityKey,
        defaultValue = FlacQuality.HI_RES,
    )
    val (audioQuality, onAudioQualityChange) = rememberEnumPreference(
        AudioQualityKey,
        defaultValue = AudioQuality.AUTO,
    )
    val (skipSilence, onSkipSilenceChange) = rememberPreference(
        SkipSilenceKey,
        defaultValue = false,
    )
    val (audioNormalization, onAudioNormalizationChange) = rememberPreference(
        AudioNormalizationKey,
        defaultValue = true,
    )
    val (autoSkipNextOnError, onAutoSkipNextOnErrorChange) = rememberPreference(
        AutoSkipNextOnErrorKey,
        defaultValue = false,
    )
    val (pauseOnDeviceMute, onPauseOnDeviceMuteChange) = rememberPreference(
        PauseOnDeviceMuteKey,
        defaultValue = false,
    )
    val (autoStartOnBluetooth, onAutoStartOnBluetoothChange) = rememberPreference(
        AutoStartOnBluetoothKey,
        defaultValue = false,
    )
    val (wakelockEnabled, onWakelockChange) = rememberPreference(
        WakelockKey,
        defaultValue = false,
    )

    val (crossfadeEnabled, onCrossfadeEnabledChange) = rememberPreference(
        CrossfadeEnabledKey,
        defaultValue = false,
    )
    val (crossfadeDurationSeconds, onCrossfadeDurationSecondsChange) = rememberPreference(
        CrossfadeDurationKey,
        defaultValue = 5f,
    )
    val (crossfadeGapless, onCrossfadeGaplessChange) = rememberPreference(
        CrossfadeGaplessKey,
        defaultValue = true,
    )

    val (_, onEnableLosslessChange) = rememberPreference(EnableLosslessKey, false)
    val (memoryCacheToggle, onMemoryCacheToggleChange) = rememberPreference(MemoryCacheToggleKey, false)
    val (downloadLocationUri, onDownloadLocationUriChange) = rememberPreference(DownloadLocationUriKey, "")

    val flacFolderPath = remember(downloadLocationUri) {
        if (downloadLocationUri.isBlank()) {
            null
        } else {
            runCatching {
                val uri = Uri.parse(downloadLocationUri)
                val docFile = DocumentFile.fromTreeUri(context, uri)
                val name = docFile?.name?.takeIf { it.isNotBlank() }
                val rawPath = uri.lastPathSegment?.substringAfterLast(":")?.takeIf { it.isNotBlank() }
                name ?: rawPath ?: downloadLocationUri
            }.getOrNull() ?: downloadLocationUri
        }
    }

    val (squidCaptchaCookie, onSquidCaptchaCookieChange) = rememberPreference(moe.rukamori.archivetune.constants.SquidCaptchaCookieKey, "")
    val (arcodStashKey, onArcodStashKeyChange) = rememberPreference(moe.rukamori.archivetune.constants.ArcodStashKeyKey, "")
    val (arcodBearerToken, onArcodBearerTokenChange) = rememberPreference(moe.rukamori.archivetune.constants.ArcodBearerTokenKey, "")
    val (qobuzAppId, onQobuzAppIdChange) = rememberPreference(moe.rukamori.archivetune.constants.QobuzAppIdKey, "")
    val (qobuzAppSecret, onQobuzAppSecretChange) = rememberPreference(moe.rukamori.archivetune.constants.QobuzAppSecretKey, "")
    val (qobuzUserAuthToken, onQobuzUserAuthTokenChange) = rememberPreference(moe.rukamori.archivetune.constants.QobuzUserAuthTokenKey, "")

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri ->
            uri?.let { 
                onDownloadLocationUriChange(it.toString())
                try {
                    context.contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    Toast.makeText(context, context.getString(R.string.folder_persist_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    val scrollBehavior = appBarScrollBehavior()
    val listState = rememberLazyListState()

    val scrimAlpha by remember {
        derivedStateOf {
            val offset = listState.firstVisibleItemScrollOffset
            val index = listState.firstVisibleItemIndex
            if (index > 0) 0.85f else (offset / 100f).coerceIn(0f, 0.85f)
        }
    }

    SettingsScreenBackground {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Box {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surface.copy(alpha = scrimAlpha),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    LargeFlexibleTopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.player_and_audio),
                                fontWeight = FontWeight.Bold,
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = navController::navigateUp,
                                onLongClick = navController::backToMain,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.arrow_back),
                                    contentDescription = stringResource(R.string.back_button_desc),
                                )
                            }
                        },
                        colors = TopAppBarDefaults.largeTopAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = Color.Transparent,
                        ),
                        scrollBehavior = scrollBehavior,
                    )
                }
            },
        ) { innerPadding ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(
                        LocalPlayerAwareWindowInsets.current.only(
                            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
                        ),
                    ),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = SettingsDimensions.ScreenBottomPadding,
                ),
            ) {
                item {
                    SettingsSectionLabel(
                        text = stringResource(R.string.lossless_integration),
                        modifier = Modifier
                            .padding(bottom = SettingsDimensions.SectionHeaderBottomPadding)
                            .padding(horizontal = SettingsDimensions.ScreenHorizontalPadding),
                    )
                }

                val losslessItems = buildList<@Composable () -> Unit> {
                    add {
                        EnumListPreference(
                            title = { Text(stringResource(R.string.playback_source)) },
                            icon = { Icon(painterResource(R.drawable.album), null) },
                            selectedValue = playbackSource,
                            onValueSelected = { 
                                onPlaybackSourceChange(it)
                                onEnableLosslessChange(it == PlaybackSource.FLAC)
                            },
                            valueText = { source ->
                                when (source) {
                                    PlaybackSource.YT_MUSIC -> stringResource(R.string.source_yt_music)
                                    PlaybackSource.FLAC -> stringResource(R.string.source_flac)
                                }
                            },
                        )
                    }
                    if (playbackSource == PlaybackSource.FLAC) {
                        add {
                            EnumListPreference(
                                title = { Text(stringResource(R.string.audio_quality)) },
                                icon = { Icon(painterResource(R.drawable.graphic_eq), null) },
                                selectedValue = flacQuality,
                                onValueSelected = onFlacQualityChange,
                                valueText = { quality ->
                                    when (quality) {
                                        FlacQuality.CD -> stringResource(R.string.flac_quality_cd)
                                        FlacQuality.HI_RES -> stringResource(R.string.flac_quality_hi_res)
                                        FlacQuality.MAX -> stringResource(R.string.flac_quality_max)
                                    }
                                },
                            )
                        }
                        add {
                            SwitchPreference(
                                title = { Text(stringResource(R.string.memory_cache_toggle)) },
                                icon = { Icon(painterResource(R.drawable.cached), null) },
                                checked = memoryCacheToggle,
                                onCheckedChange = onMemoryCacheToggleChange,
                            )
                        }
                        add {
                            PreferenceEntry(
                                title = { Text(stringResource(R.string.select_flac_download_folder)) },
                                description = flacFolderPath,
                                icon = { Icon(painterResource(R.drawable.snippet_folder), null) },
                                onClick = { folderPickerLauncher.launch(null) },
                            )
                        }
                        add {
                            EditTextPreference(
                                title = { Text(stringResource(R.string.squid_captcha_cookie)) },
                                icon = { Icon(painterResource(R.drawable.lock), null) },
                                value = squidCaptchaCookie,
                                onValueChange = onSquidCaptchaCookieChange,
                            )
                        }
                        add {
                            EditTextPreference(
                                title = { Text(stringResource(R.string.arcod_stash_key)) },
                                icon = { Icon(painterResource(R.drawable.lock), null) },
                                value = arcodStashKey,
                                onValueChange = onArcodStashKeyChange,
                            )
                        }
                        add {
                            EditTextPreference(
                                title = { Text(stringResource(R.string.arcod_bearer_token)) },
                                icon = { Icon(painterResource(R.drawable.lock), null) },
                                value = arcodBearerToken,
                                onValueChange = onArcodBearerTokenChange,
                            )
                        }
                        add {
                            EditTextPreference(
                                title = { Text(stringResource(R.string.qobuz_app_id)) },
                                icon = { Icon(painterResource(R.drawable.lock), null) },
                                value = qobuzAppId,
                                onValueChange = onQobuzAppIdChange,
                            )
                        }
                        add {
                            EditTextPreference(
                                title = { Text(stringResource(R.string.qobuz_app_secret)) },
                                icon = { Icon(painterResource(R.drawable.lock), null) },
                                value = qobuzAppSecret,
                                onValueChange = onQobuzAppSecretChange,
                            )
                        }
                        add {
                            EditTextPreference(
                                title = { Text(stringResource(R.string.qobuz_user_auth_token)) },
                                icon = { Icon(painterResource(R.drawable.lock), null) },
                                value = qobuzUserAuthToken,
                                onValueChange = onQobuzUserAuthTokenChange,
                            )
                        }
                    } else {
                        add {
                            EnumListPreference(
                                title = { Text(stringResource(R.string.audio_quality)) },
                                icon = { Icon(painterResource(R.drawable.graphic_eq), null) },
                                selectedValue = audioQuality,
                                onValueSelected = onAudioQualityChange,
                                valueText = { quality ->
                                    when (quality) {
                                        AudioQuality.AUTO -> stringResource(R.string.audio_quality_auto)
                                        AudioQuality.HIGH -> stringResource(R.string.audio_quality_high)
                                        AudioQuality.LOW -> stringResource(R.string.audio_quality_low)
                                        AudioQuality.HIGHEST -> stringResource(R.string.audio_quality_highest)
                                    }
                                },
                            )
                        }
                    }
                }

                items(losslessItems.size) { index ->
                    PlayerSettingsSegmentedItem(
                        index = index,
                        count = losslessItems.size,
                        content = losslessItems[index]
                    )
                }

                item {
                    SettingsSectionLabel(
                        text = stringResource(R.string.player_and_audio),
                        modifier = Modifier
                            .padding(top = SettingsDimensions.SectionSpacing)
                            .padding(bottom = SettingsDimensions.SectionHeaderBottomPadding)
                            .padding(horizontal = SettingsDimensions.ScreenHorizontalPadding),
                    )
                }

                val audioItems = buildList<@Composable () -> Unit> {
                    add {
                        SwitchPreference(
                            title = { Text(stringResource(R.string.skip_silence)) },
                            icon = { Icon(painterResource(R.drawable.skip_next), null) },
                            checked = skipSilence,
                            onCheckedChange = onSkipSilenceChange,
                        )
                    }
                    add {
                        SwitchPreference(
                            title = { Text(stringResource(R.string.audio_normalization)) },
                            icon = { Icon(painterResource(R.drawable.volume_up), null) },
                            checked = audioNormalization,
                            onCheckedChange = onAudioNormalizationChange,
                        )
                    }
                    add {
                        PreferenceEntry(
                            title = { Text(stringResource(R.string.equalizer)) },
                            icon = { Icon(painterResource(R.drawable.equalizer), null) },
                            onClick = {
                                val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                                if (intent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(intent)
                                } else {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.equalizer),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            },
                        )
                    }
                }

                items(audioItems.size) { index ->
                    PlayerSettingsSegmentedItem(
                        index = index,
                        count = audioItems.size,
                        content = audioItems[index]
                    )
                }

                item {
                    SettingsSectionLabel(
                        text = stringResource(R.string.audio_crossfade_title),
                        modifier = Modifier
                            .padding(top = SettingsDimensions.SectionSpacing)
                            .padding(bottom = SettingsDimensions.SectionHeaderBottomPadding)
                            .padding(horizontal = SettingsDimensions.ScreenHorizontalPadding),
                    )
                }

                val crossfadeItems = buildList<@Composable () -> Unit> {
                    add {
                        SwitchPreference(
                            title = { Text(stringResource(R.string.audio_crossfade_title)) },
                            icon = { Icon(painterResource(R.drawable.graphic_eq), null) },
                            checked = crossfadeEnabled,
                            onCheckedChange = onCrossfadeEnabledChange,
                        )
                    }
                    add {
                        CrossfadeSliderPreference(
                            valueSeconds = crossfadeDurationSeconds,
                            onValueChange = onCrossfadeDurationSecondsChange,
                            isEnabled = crossfadeEnabled,
                        )
                    }
                    add {
                        SwitchPreference(
                            title = { Text(stringResource(R.string.crossfade_gapless_title)) },
                            description = stringResource(R.string.crossfade_gapless_description),
                            icon = { Icon(painterResource(R.drawable.graphic_eq), null) },
                            checked = crossfadeGapless,
                            onCheckedChange = onCrossfadeGaplessChange,
                            isEnabled = crossfadeEnabled,
                        )
                    }
                }

                items(crossfadeItems.size) { index ->
                    PlayerSettingsSegmentedItem(
                        index = index,
                        count = crossfadeItems.size,
                        content = crossfadeItems[index]
                    )
                }

                item {
                    SettingsSectionLabel(
                        text = stringResource(R.string.player),
                        modifier = Modifier
                            .padding(top = SettingsDimensions.SectionSpacing)
                            .padding(bottom = SettingsDimensions.SectionHeaderBottomPadding)
                            .padding(horizontal = SettingsDimensions.ScreenHorizontalPadding),
                    )
                }

                val playerItems = buildList<@Composable () -> Unit> {
                    add {
                        SwitchPreference(
                            title = { Text(stringResource(R.string.auto_start_on_bluetooth)) },
                            icon = { Icon(painterResource(R.drawable.bluetooth), null) },
                            checked = autoStartOnBluetooth,
                            onCheckedChange = onAutoStartOnBluetoothChange,
                        )
                    }
                    add {
                        SwitchPreference(
                            title = { Text(stringResource(R.string.pause_on_device_mute)) },
                            icon = { Icon(painterResource(R.drawable.volume_off), null) },
                            checked = pauseOnDeviceMute,
                            onCheckedChange = onPauseOnDeviceMuteChange,
                        )
                    }
                    add {
                        SwitchPreference(
                            title = { Text(stringResource(R.string.auto_skip_next_on_error)) },
                            icon = { Icon(painterResource(R.drawable.skip_next), null) },
                            checked = autoSkipNextOnError,
                            onCheckedChange = onAutoSkipNextOnErrorChange,
                        )
                    }
                    add {
                        SwitchPreference(
                            title = { Text(stringResource(R.string.wakelock)) },
                            description = stringResource(R.string.wakelock_desc),
                            icon = { Icon(painterResource(R.drawable.lock), null) },
                            checked = wakelockEnabled,
                            onCheckedChange = onWakelockChange,
                        )
                    }
                }

                items(playerItems.size) { index ->
                    PlayerSettingsSegmentedItem(
                        index = index,
                        count = playerItems.size,
                        content = playerItems[index]
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerSettingsSegmentedItem(
    index: Int,
    count: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = LocalYumaColors.current
    val shape = segmentedSettingsItemShape(index, count)
    val position = when {
        count == 1 -> PreferenceGroupPosition.Single
        index == 0 -> PreferenceGroupPosition.First
        index == count - 1 -> PreferenceGroupPosition.Last
        else -> PreferenceGroupPosition.Middle
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SettingsDimensions.SegmentedGroupHorizontalPadding)
            .padding(bottom = if (index < count - 1) SettingsDimensions.SegmentedItemGap else 0.dp)
            .yumaGlassCard(
                shape = shape,
                backgroundColor = colors.glassBackground,
                borderColor = colors.glassBorder,
            )
    ) {
        CompositionLocalProvider(
            LocalPreferenceInGroup provides true,
            LocalPreferenceGroupPosition provides position,
            LocalPreferenceItemIndex provides index,
        ) {
            content()
        }
    }
}

private fun segmentedSettingsItemShape(
    index: Int,
    count: Int,
): Shape {
    val large = SettingsDimensions.SegmentedCornerLarge
    val small = SettingsDimensions.SegmentedCornerSmall
    return when {
        count <= 1 -> {
            RoundedCornerShape(large)
        }
        index == 0 -> {
            RoundedCornerShape(
                topStart = large,
                topEnd = large,
                bottomEnd = small,
                bottomStart = small,
            )
        }
        index == count - 1 -> {
            RoundedCornerShape(
                topStart = small,
                topEnd = small,
                bottomEnd = large,
                bottomStart = large,
            )
        }
        else -> {
            RoundedCornerShape(small)
        }
    }
}

@ThemePreviews
@Composable
private fun PlayerSettingsPreview() {
    TestThemeWrapper {
        PlayerSettings(navController = rememberNavController())
    }
}
