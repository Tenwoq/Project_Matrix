package com.example.projectmatrix.presentation.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.projectmatrix.data.local.Language

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanionProfileScreen(
    onBack: () -> Unit,
    viewModel: CompanionProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val text = companionProfileText(state.language)
    val user = state.user

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = text.back)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
        ) {
            if (user != null) {
                ProfileLine(text.name, user.displayName)
                ProfileLine(text.nickname, user.username.asHandle())
                ProfileLine(text.status, user.profileStatus.ifBlank { text.emptyStatus })
            } else if (state.error != null) {
                Text(state.error.orEmpty(), color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ProfileLine(label: String, value: String) {
    Text(label, style = MaterialTheme.typography.labelLarge)
    Spacer(Modifier.height(4.dp))
    Text(value, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(18.dp))
}

private data class CompanionProfileText(
    val title: String,
    val back: String,
    val name: String,
    val nickname: String,
    val status: String,
    val emptyStatus: String,
)

private fun companionProfileText(language: String): CompanionProfileText =
    if (language == Language.English) {
        CompanionProfileText("Profile", "Back", "Name", "Nickname", "Status", "No status")
    } else {
        CompanionProfileText("Профиль", "Назад", "Имя", "Никнейм", "Статус", "Статус не указан")
    }

private fun String.asHandle(): String =
    if (contains("@")) this else "@$this"
