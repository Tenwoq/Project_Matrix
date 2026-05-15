package com.example.projectmatrix.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.projectmatrix.data.local.Language
import com.example.projectmatrix.data.local.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onChats: () -> Unit,
    onProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val text = settingsText(state.language)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text.title) },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = onChats,
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null) },
                    label = { Text(text.chats) },
                )
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    label = { Text(text.title) },
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            SettingsRow(title = text.profile, subtitle = text.profileSubtitle, onClick = onProfile)
            SettingsRow(title = text.changePassword, subtitle = text.changePasswordSubtitle, onClick = onChangePassword)

            Text(
                text.theme,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
            )
            SingleChoiceSegmentedButtonRow {
                listOf(ThemeMode.System, ThemeMode.Light, ThemeMode.Dark).forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = state.themeMode == mode,
                        onClick = { viewModel.setThemeMode(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 3),
                    ) {
                        Text(text.themeLabel(mode))
                    }
                }
            }

            Text(
                text.language,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
            )
            SingleChoiceSegmentedButtonRow {
                SegmentedButton(
                    selected = state.language == Language.Russian,
                    onClick = { viewModel.setLanguage(Language.Russian) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                ) {
                    Text("RU")
                }
                SegmentedButton(
                    selected = state.language == Language.English,
                    onClick = { viewModel.setLanguage(Language.English) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                ) {
                    Text("EN")
                }
            }

            TextButton(
                onClick = { viewModel.logout(onLogout) },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 32.dp),
            ) {
                Text(text.logout)
            }
        }
    }
}

@Composable
private fun SettingsRow(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            Text(">")
        }
    }
    HorizontalDivider()
}

private data class SettingsText(
    val title: String,
    val back: String,
    val chats: String,
    val profile: String,
    val profileSubtitle: String,
    val changePassword: String,
    val changePasswordSubtitle: String,
    val theme: String,
    val language: String,
    val logout: String,
    val themeLabel: (String) -> String,
)

private fun settingsText(language: String): SettingsText =
    if (language == Language.English) {
        SettingsText(
            title = "Settings",
            back = "Back",
            chats = "Chats",
            profile = "Profile",
            profileSubtitle = "Name and profile status",
            changePassword = "Change password",
            changePasswordSubtitle = "Current password and new password",
            theme = "Theme",
            language = "Language",
            logout = "Log out",
            themeLabel = { mode ->
                when (mode) {
                    ThemeMode.Light -> "Light"
                    ThemeMode.Dark -> "Dark"
                    else -> "System"
                }
            },
        )
    } else {
        SettingsText(
            title = "Настройки",
            back = "Назад",
            chats = "Чаты",
            profile = "Профиль",
            profileSubtitle = "Имя и статус профиля",
            changePassword = "Поменять пароль",
            changePasswordSubtitle = "Текущий пароль и новый пароль",
            theme = "Тема",
            language = "Язык",
            logout = "Выйти",
            themeLabel = { mode ->
                when (mode) {
                    ThemeMode.Light -> "Светлая"
                    ThemeMode.Dark -> "Темная"
                    else -> "Системная"
                }
            },
        )
    }
