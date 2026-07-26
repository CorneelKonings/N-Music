package moe.rukamori.archivetune.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import moe.rukamori.archivetune.constants.AppFontPreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {
    private val prefs = context.getSharedPreferences("_settings", Context.MODE_PRIVATE)
    private val sharedPrefs = context.getSharedPreferences("yuma_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    override val userSettings: Flow<UserSettings> = _settingsFlow.asStateFlow()

    private fun loadSettings(): UserSettings {
        val colorHex = prefs.getString("theme_color_hex", "ED5564") ?: "ED5564"
        val fontPrefStr = prefs.getString("font_preference", AppFontPreference.DEFAULT.name)
        val fontPref = try {
            AppFontPreference.valueOf(fontPrefStr ?: AppFontPreference.DEFAULT.name)
        } catch (_: Exception) {
            AppFontPreference.DEFAULT
        }
        return UserSettings(
            isDarkTheme = prefs.getBoolean("dark_theme", true),
            themeColorHex = colorHex.removePrefix("#"),
            fontPreference = fontPref
        )
    }

    override suspend fun updateThemeColor(colorHex: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanHex = colorHex.removePrefix("#")
            prefs.edit().putString("theme_color_hex", cleanHex).apply()
            _settingsFlow.value = _settingsFlow.value.copy(themeColorHex = cleanHex)
        }
    }

    override suspend fun updateFontPreference(preference: AppFontPreference): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            prefs.edit().putString("font_preference", preference.name).apply()
            _settingsFlow.value = _settingsFlow.value.copy(fontPreference = preference)
        }
    }

    override fun isBlurBackgroundEnabled(): Boolean =
        prefs.getBoolean("blur_bg_enabled", false)

    override fun setBlurBackgroundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("blur_bg_enabled", enabled).apply()
    }

    override fun isAutoDownloadLyricsEnabled(): Boolean =
        prefs.getBoolean("auto_download_lyrics", true)

    override fun setAutoDownloadLyricsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("auto_download_lyrics", enabled).apply()
    }

    override fun isFirstLaunch(): Boolean =
        sharedPrefs.getBoolean("is_first_launch", true)

    override fun setFirstLaunch(isFirst: Boolean) {
        sharedPrefs.edit().putBoolean("is_first_launch", isFirst).apply()
    }

    override fun isSearchHistoryPaused(): Boolean =
        prefs.getBoolean("pause_search_history", false)

    override fun isImmersiveEnabled(): Boolean =
        prefs.getBoolean("immersive_enabled", false)

    override fun setImmersiveEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("immersive_enabled", enabled).apply()
    }
}