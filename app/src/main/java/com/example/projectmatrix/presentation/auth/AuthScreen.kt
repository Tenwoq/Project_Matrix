package com.example.projectmatrix.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthorized: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isAuthorized) {
        if (state.isAuthorized) onAuthorized()
    }

    AuthContent(
        state = state,
        onUsernameChange = viewModel::setUsername,
        onPasswordChange = viewModel::setPassword,
        onPasswordConfirmationChange = viewModel::setPasswordConfirmation,
        onResetCodeChange = viewModel::setResetCode,
        onResetNewPasswordChange = viewModel::setResetNewPassword,
        onResetPasswordConfirmationChange = viewModel::setResetPasswordConfirmation,
        onDisplayNameChange = viewModel::setDisplayName,
        onLanguageChange = viewModel::setLanguage,
        onSubmit = viewModel::submit,
        onTogglePasswordReset = viewModel::togglePasswordReset,
        onToggleMode = viewModel::toggleMode,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthContent(
    state: AuthUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmationChange: (String) -> Unit,
    onResetCodeChange: (String) -> Unit,
    onResetNewPasswordChange: (String) -> Unit,
    onResetPasswordConfirmationChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onTogglePasswordReset: () -> Unit,
    onToggleMode: () -> Unit,
) {
    val text = authText(state.language)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("MIN", style = MaterialTheme.typography.displayMedium)
        Text(text.subtitle, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(
            value = state.username,
            onValueChange = onUsernameChange,
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        if (state.isPasswordResetMode) {
            if (state.isWaitingForResetCode) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.resetCode,
                    onValueChange = onResetCodeChange,
                    label = { Text(text.resetCode) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Text(text.resetInstructions, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.resetNewPassword,
                    onValueChange = onResetNewPasswordChange,
                    label = { Text(text.newPassword) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.resetPasswordConfirmation,
                    onValueChange = onResetPasswordConfirmationChange,
                    label = { Text(text.repeatPassword) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = { Text(text.password) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (state.isRegisterMode && !state.isPasswordResetMode) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.passwordConfirmation,
                onValueChange = onPasswordConfirmationChange,
                label = { Text(text.repeatPassword) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.displayName,
                onValueChange = onDisplayNameChange,
                label = { Text(text.name) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            SingleChoiceSegmentedButtonRow {
                SegmentedButton(
                    selected = state.language == "ru",
                    onClick = { onLanguageChange("ru") },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                ) {
                    Text("RU")
                }
                SegmentedButton(
                    selected = state.language == "en",
                    onClick = { onLanguageChange("en") },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                ) {
                    Text("EN")
                }
            }
            if (state.isWaitingForCode) {
                Spacer(Modifier.height(12.dp))
                Text(text.openEmail, style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (state.info != null) {
            Spacer(Modifier.height(12.dp))
            Text(state.info.orEmpty(), color = MaterialTheme.colorScheme.primary)
        }
        if (state.error != null) {
            Spacer(Modifier.height(12.dp))
            Text(state.error.orEmpty(), color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onSubmit,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    when {
                        state.isPasswordResetMode && state.isWaitingForResetCode -> text.saveResetPassword
                        state.isPasswordResetMode -> text.sendResetEmail
                        state.isRegisterMode && state.isWaitingForCode -> text.confirm
                        state.isRegisterMode -> text.sendCode
                        else -> text.login
                    }
                )
            }
        }
        if (state.isPasswordResetMode) {
            TextButton(onClick = onTogglePasswordReset, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(text.backToLogin)
            }
        } else {
            if (!state.isRegisterMode) {
                TextButton(onClick = onTogglePasswordReset, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(text.forgotPassword)
                }
            }
            TextButton(onClick = onToggleMode, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(if (state.isRegisterMode) text.hasAccount else text.createAccount)
            }
        }
    }
}

private data class AuthText(
    val subtitle: String,
    val password: String,
    val repeatPassword: String,
    val newPassword: String,
    val name: String,
    val openEmail: String,
    val resetCode: String,
    val resetInstructions: String,
    val sendCode: String,
    val sendResetEmail: String,
    val saveResetPassword: String,
    val confirm: String,
    val register: String,
    val login: String,
    val forgotPassword: String,
    val backToLogin: String,
    val hasAccount: String,
    val createAccount: String,
)

private fun authText(language: String): AuthText =
    if (language == "en") {
        AuthText(
            subtitle = "Personal messaging with Kotlin and Matrix",
            password = "Password",
            repeatPassword = "Repeat password",
            newPassword = "New password",
            name = "Name",
            openEmail = "Open the verification link from the email, then return here.",
            resetCode = "Firebase reset code or link",
            resetInstructions = "Paste the full reset link or the oobCode value from the email.",
            sendCode = "Send verification email",
            sendResetEmail = "Send reset email",
            saveResetPassword = "Save new password",
            confirm = "I verified email",
            register = "Create account",
            login = "Sign in",
            forgotPassword = "Forgot password?",
            backToLogin = "Back to sign in",
            hasAccount = "Already have an account",
            createAccount = "Create account",
        )
    } else {
        AuthText(
            subtitle = "Личные сообщения на Kotlin и Matrix",
            password = "Пароль",
            repeatPassword = "Повторите пароль",
            newPassword = "Новый пароль",
            name = "Имя",
            openEmail = "Открой ссылку подтверждения из письма, затем вернись сюда.",
            resetCode = "Код или ссылка Firebase",
            resetInstructions = "Вставьте всю ссылку сброса из письма или значение oobCode.",
            sendCode = "Отправить письмо",
            sendResetEmail = "Отправить письмо",
            saveResetPassword = "Сохранить новый пароль",
            confirm = "Я подтвердил email",
            register = "Зарегистрироваться",
            login = "Войти",
            forgotPassword = "Забыли пароль?",
            backToLogin = "Вернуться ко входу",
            hasAccount = "Уже есть аккаунт",
            createAccount = "Создать аккаунт",
        )
    }
