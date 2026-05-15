package com.example.projectmatrix.data.local

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class AppSettings(
    val themeMode: String = ThemeMode.System,
    val language: String = Language.Russian,
)

object ThemeMode {
    const val System = "system"
    const val Light = "light"
    const val Dark = "dark"
}

object Language {
    const val Russian = "ru"
    const val English = "en"
}

@Singleton
class AppSettingsRepository @Inject constructor(
    private val preferences: SharedPreferences,
) {
    private val _settings = MutableStateFlow(readSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun setThemeMode(mode: String) {
        preferences.edit().putString(KEY_THEME_MODE, mode).apply()
        _settings.value = readSettings()
    }

    fun setLanguage(language: String) {
        preferences.edit().putString(KEY_LANGUAGE, language).apply()
        _settings.value = readSettings()
    }

    private fun readSettings(): AppSettings =
        AppSettings(
            themeMode = preferences.getString(KEY_THEME_MODE, ThemeMode.System) ?: ThemeMode.System,
            language = preferences.getString(KEY_LANGUAGE, Language.Russian) ?: Language.Russian,
        )

    private companion object {
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_LANGUAGE = "language"
    }
}
