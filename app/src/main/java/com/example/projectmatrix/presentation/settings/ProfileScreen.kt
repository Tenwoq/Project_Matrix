package com.example.projectmatrix.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.projectmatrix.data.local.Language

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val text = profileText(state.language)

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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            if (state.isEditing) {
                OutlinedTextField(
                    value = state.displayName,
                    onValueChange = viewModel::setDisplayName,
                    label = { Text(text.name) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.username,
                    onValueChange = viewModel::setUsername,
                    label = { Text(text.nickname) },
                    prefix = { Text("@") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                )
                OutlinedTextField(
                    value = state.profileStatus,
                    onValueChange = viewModel::setProfileStatus,
                    label = { Text(text.status) },
                    minLines = 3,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                )
            } else {
                ProfileValue(label = text.name, value = state.displayName)
                ProfileValue(label = text.nickname, value = state.username.asNickname())
                ProfileValue(label = text.status, value = state.profileStatus.ifBlank { text.emptyStatus })
            }
            if (state.info != null) {
                Text(
                    state.info.orEmpty(),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
            if (state.error != null) {
                Text(
                    state.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
            Button(
                onClick = viewModel::save,
                enabled = !state.isSaving && !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (state.isSaving || state.isLoading) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (state.isEditing) text.save else text.edit)
                }
            }
        }
    }
}

@Composable
private fun ProfileValue(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

private data class ProfileText(
    val title: String,
    val back: String,
    val name: String,
    val nickname: String,
    val status: String,
    val emptyStatus: String,
    val edit: String,
    val save: String,
)

private fun profileText(language: String): ProfileText =
    if (language == Language.English) {
        ProfileText(
            title = "Profile",
            back = "Back",
            name = "Name",
            nickname = "Nickname",
            status = "Profile status",
            emptyStatus = "No status",
            edit = "Edit",
            save = "Save",
        )
    } else {
        ProfileText(
            title = "Профиль",
            back = "Назад",
            name = "Имя",
            nickname = "Никнейм",
            status = "Статус профиля",
            emptyStatus = "Статус не указан",
            edit = "Изменить",
            save = "Сохранить",
        )
    }

private fun String.asNickname(): String =
    if (contains("@")) this else "@$this"
