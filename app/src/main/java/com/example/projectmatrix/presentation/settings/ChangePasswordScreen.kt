package com.example.projectmatrix.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.projectmatrix.data.local.Language

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit,
    viewModel: ChangePasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val text = changePasswordText(state.language)

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
            OutlinedTextField(
                value = state.currentPassword,
                onValueChange = viewModel::setCurrentPassword,
                label = { Text(text.currentPassword) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.newPassword,
                onValueChange = viewModel::setNewPassword,
                label = { Text(text.newPassword) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            )
            OutlinedTextField(
                value = state.repeatedPassword,
                onValueChange = viewModel::setRepeatedPassword,
                label = { Text(text.repeatPassword) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            )
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
                onClick = viewModel::submit,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(text.save)
                }
            }
        }
    }
}

private data class ChangePasswordText(
    val title: String,
    val back: String,
    val currentPassword: String,
    val newPassword: String,
    val repeatPassword: String,
    val save: String,
)

private fun changePasswordText(language: String): ChangePasswordText =
    if (language == Language.English) {
        ChangePasswordText(
            title = "Change password",
            back = "Back",
            currentPassword = "Current password",
            newPassword = "New password",
            repeatPassword = "Repeat new password",
            save = "Save password",
        )
    } else {
        ChangePasswordText(
            title = "Поменять пароль",
            back = "Назад",
            currentPassword = "Текущий пароль",
            newPassword = "Новый пароль",
            repeatPassword = "Повторите новый пароль",
            save = "Сохранить пароль",
        )
    }
