package moe.rukamori.archivetune.data.repository

import kotlinx.coroutines.flow.Flow
import moe.rukamori.archivetune.constants.AppFontPreference

data class UserSettings(
    val isDarkTheme: Boolean = true,
    val themeColorHex: String = "ED5564",
    val fontPreference: AppFontPreference = AppFontPreference.DEFAULT,
)

interface SettingsRepository {
    val userSettings: Flow<UserSettings>
    suspend fun updateThemeColor(colorHex: String): Result<Unit>
    suspend fun updateFontPreference(preference: AppFontPreference): Result<Unit>
    fun isBlurBackgroundEnabled(): Boolean
    fun setBlurBackgroundEnabled(enabled: Boolean)
    fun isAutoDownloadLyricsEnabled(): Boolean
    fun setAutoDownloadLyricsEnabled(enabled: Boolean)
    fun isFirstLaunch(): Boolean
    fun setFirstLaunch(isFirst: Boolean)
    fun isSearchHistoryPaused(): Boolean
    fun isImmersiveEnabled(): Boolean
    fun setImmersiveEnabled(enabled: Boolean)
}