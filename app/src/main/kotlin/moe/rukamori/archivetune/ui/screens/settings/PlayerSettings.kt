/*
 * YumaPlayer (2026) | Modified work by MuwMix
 * ArchiveTune (2026) | Original work by © Rukamori
 * GPL-3.0 License | Contributors: see git history
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package moe.rukamori.archivetune.ui.screens.settings

import android.content.Intent
import android.media.audiofx.AudioEffect
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
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
import moe.rukamori.archivetune.constants.MemoryCacheToggleKey
import moe.rukamori.archivetune.constants.PauseOnDeviceMuteKey
import moe.rukamori.archivetune.constants.SkipSilenceKey
import moe.rukamori.archivetune.constants.WakelockKey
import moe.rukamori.archivetune.ui.component.CrossfadeSliderPreference
import moe.rukamori.archivetune.ui.component.EnumListPreference
import moe.rukamori.archivetune.ui.component.PreferenceEntry
import moe.rukamori.archivetune.ui.component.PreferenceGroup
import moe.rukamori.archivetune.ui.component.SwitchPreference
import moe.rukamori.archivetune.ui.theme.TestThemeWrapper
import moe.rukamori.archivetune.ui.theme.ThemePreviews
import moe.rukamori.archivetune.ui.utils.backToMain
import moe.rukamori.archivetune.utils.rememberEnumPreference
import moe.rukamori.archivetune.utils.rememberPreference

@Composable
fun PlayerSettings(navController: NavController) {
    val context = LocalContext.current

    val (audioQuality, onAudioQualityChange) =
        rememberEnumPreference(
            AudioQualityKey,
            defaultValue = AudioQuality.AUTO,
        )
    val (skipSilence, onSkipSilenceChange) =
        rememberPreference(
            SkipSilenceKey,
            defaultValue = false,
        )
    val (audioNormalization, onAudioNormalizationChange) =
        rememberPreference(
            AudioNormalizationKey,
            defaultValue = true,
        )
    val (autoSkipNextOnError, onAutoSkipNextOnErrorChange) =
        rememberPreference(
            AutoSkipNextOnErrorKey,
            defaultValue = false,
        )
    val (pauseOnDeviceMute, onPauseOnDeviceMuteChange) =
        rememberPreference(
            PauseOnDeviceMuteKey,
            defaultValue = false,
        )
    val (autoStartOnBluetooth, onAutoStartOnBluetoothChange) =
        rememberPreference(
            AutoStartOnBluetoothKey,
            defaultValue = false,
        )
    val (wakelockEnabled, onWakelockChange) =
        rememberPreference(
            WakelockKey,
            defaultValue = false,
        )

    val (crossfadeEnabled, onCrossfadeEnabledChange) =
        rememberPreference(
            CrossfadeEnabledKey,
            defaultValue = false,
        )
    val (crossfadeDurationSeconds, onCrossfadeDurationSecondsChange) =
        rememberPreference(
            CrossfadeDurationKey,
            defaultValue = 5f,
        )
    val (crossfadeGapless, onCrossfadeGaplessChange) =
        rememberPreference(
            CrossfadeGaplessKey,
            defaultValue = true,
        )

    val (enableLossless, onEnableLosslessChange) = rememberPreference(EnableLosslessKey, false)
    val (memoryCacheToggle, onMemoryCacheToggleChange) = rememberPreference(MemoryCacheToggleKey, false)
    val (downloadLocationUri, onDownloadLocationUriChange) = rememberPreference(DownloadLocationUriKey, "")

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri ->
            uri?.let { onDownloadLocationUriChange(it.toString()) }
        }
    )

    YumaSettingsScaffold(
        title = { Text(stringResource(R.string.player_and_audio)) },
        onBackClick = navController::navigateUp,
        onBackLongClick = navController::backToMain,
    ) {

        PreferenceGroup(title = stringResource(R.string.player_and_audio)) {
            item {
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
                            AudioQuality.HIGHEST -> stringResource(R.string.audio_quality_high)
                        }
                    },
                )
            }

            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.skip_silence)) },
                    icon = { Icon(painterResource(R.drawable.skip_next), null) },
                    checked = skipSilence,
                    onCheckedChange = onSkipSilenceChange,
                )
            }

            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.audio_normalization)) },
                    icon = { Icon(painterResource(R.drawable.volume_up), null) },
                    checked = audioNormalization,
                    onCheckedChange = onAudioNormalizationChange,
                )
            }

            item {
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

        PreferenceGroup(title = stringResource(R.string.audio_crossfade_title)) {
            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.audio_crossfade_title)) },
                    icon = { Icon(painterResource(R.drawable.graphic_eq), null) },
                    checked = crossfadeEnabled,
                    onCheckedChange = onCrossfadeEnabledChange,
                )
            }

            item {
                CrossfadeSliderPreference(
                    valueSeconds = crossfadeDurationSeconds,
                    onValueChange = onCrossfadeDurationSecondsChange,
                    isEnabled = crossfadeEnabled,
                )
            }

            item {
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

        PreferenceGroup(title = stringResource(R.string.lossless_integration)) {
            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.enable_lossless)) },
                    icon = { Icon(painterResource(R.drawable.album), null) },
                    checked = enableLossless,
                    onCheckedChange = onEnableLosslessChange,
                )
            }
            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.memory_cache_toggle)) },
                    icon = { Icon(painterResource(R.drawable.cached), null) },
                    checked = memoryCacheToggle,
                    onCheckedChange = onMemoryCacheToggleChange,
                )
            }
            item {
                PreferenceEntry(
                    title = { Text(stringResource(R.string.select_flac_download_folder)) },
                    icon = { Icon(painterResource(R.drawable.snippet_folder), null) },
                    onClick = { folderPickerLauncher.launch(null) },
                )
            }
        }

        PreferenceGroup(title = stringResource(R.string.player)) {
            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.auto_start_on_bluetooth)) },
                    icon = { Icon(painterResource(R.drawable.bluetooth), null) },
                    checked = autoStartOnBluetooth,
                    onCheckedChange = onAutoStartOnBluetoothChange,
                )
            }

            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.pause_on_device_mute)) },
                    icon = { Icon(painterResource(R.drawable.volume_off), null) },
                    checked = pauseOnDeviceMute,
                    onCheckedChange = onPauseOnDeviceMuteChange,
                )
            }

            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.auto_skip_next_on_error)) },
                    icon = { Icon(painterResource(R.drawable.skip_next), null) },
                    checked = autoSkipNextOnError,
                    onCheckedChange = onAutoSkipNextOnErrorChange,
                )
            }

            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.wakelock)) },
                    description = stringResource(R.string.wakelock_desc),
                    icon = { Icon(painterResource(R.drawable.lock), null) },
                    checked = wakelockEnabled,
                    onCheckedChange = onWakelockChange,
                )
            }
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
