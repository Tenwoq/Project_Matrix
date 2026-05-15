package com.example.projectmatrix.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmatrix.data.local.AppSettingsRepository
import com.example.projectmatrix.domain.repository.MessengerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: String = "system",
    val language: String = "ru",
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    private val repository: MessengerRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            appSettingsRepository.settings.collect { settings ->
                _state.update {
                    it.copy(themeMode = settings.themeMode, language = settings.language)
                }
            }
        }
    }

    fun setThemeMode(mode: String) {
        appSettingsRepository.setThemeMode(mode)
    }

    fun setLanguage(language: String) {
        appSettingsRepository.setLanguage(language)
    }

    fun logout(onLogout: () -> Unit) {
        repository.logout()
        onLogout()
    }
}
