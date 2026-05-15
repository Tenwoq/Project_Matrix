package com.example.projectmatrix

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.projectmatrix.presentation.auth.AuthContent
import com.example.projectmatrix.presentation.auth.AuthUiState
import com.example.projectmatrix.ui.theme.ProjectMatrixTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AuthContentUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loginContentShowsRussianTitleAndSubtitle() {
        setAuthContent()

        composeRule.onNodeWithText("MIN").assertIsDisplayed()
        composeRule.onNodeWithText("Личные сообщения на Kotlin и Matrix").assertIsDisplayed()
    }

    @Test
    fun loginContentShowsEmailAndPasswordFields() {
        setAuthContent()

        composeRule.onNodeWithText("Email").assertIsDisplayed()
        composeRule.onNodeWithText("Пароль").assertIsDisplayed()
    }

    @Test
    fun loginButtonClickCallsSubmit() {
        var submitted = false
        setAuthContent(onSubmit = { submitted = true })

        composeRule.onNodeWithText("Войти").performClick()

        assertTrue(submitted)
    }

    @Test
    fun createAccountClickCallsToggleMode() {
        var toggled = false
        setAuthContent(onToggleMode = { toggled = true })

        composeRule.onNodeWithText("Создать аккаунт").performClick()

        assertTrue(toggled)
    }

    @Test
    fun forgotPasswordClickCallsResetToggle() {
        var toggled = false
        setAuthContent(onTogglePasswordReset = { toggled = true })

        composeRule.onNodeWithText("Забыли пароль?").performClick()

        assertTrue(toggled)
    }

    @Test
    fun emailInputCallsUsernameCallback() {
        var email = ""
        setAuthContent(onUsernameChange = { email = it })

        composeRule.onNodeWithText("Email").performTextInput("user@example.com")

        assertEquals("user@example.com", email)
    }

    @Test
    fun passwordInputCallsPasswordCallback() {
        var password = ""
        setAuthContent(onPasswordChange = { password = it })

        composeRule.onNodeWithText("Пароль").performTextInput("11122233")

        assertEquals("11122233", password)
    }

    @Test
    fun registerContentShowsRegistrationFields() {
        setAuthContent(AuthUiState(isRegisterMode = true))

        composeRule.onNodeWithText("Повторите пароль").assertIsDisplayed()
        composeRule.onNodeWithText("Имя").assertIsDisplayed()
        composeRule.onNodeWithText("Отправить письмо").assertIsDisplayed()
    }

    @Test
    fun registerLanguageButtonCallsLanguageCallback() {
        var language = ""
        setAuthContent(AuthUiState(isRegisterMode = true), onLanguageChange = { language = it })

        composeRule.onNodeWithText("EN").performClick()

        assertEquals("en", language)
    }

    @Test
    fun registerWaitingForCodeShowsInstructionAndConfirmAction() {
        setAuthContent(AuthUiState(isRegisterMode = true, isWaitingForCode = true))

        composeRule.onNodeWithText("Открой ссылку подтверждения из письма, затем вернись сюда.").assertIsDisplayed()
        composeRule.onNodeWithText("Я подтвердил email").assertIsDisplayed()
    }

    @Test
    fun englishRegisterContentUsesEnglishLabels() {
        setAuthContent(AuthUiState(language = "en", isRegisterMode = true))

        composeRule.onNodeWithText("Personal messaging with Kotlin and Matrix").assertIsDisplayed()
        composeRule.onNodeWithText("Repeat password").assertIsDisplayed()
        composeRule.onNodeWithText("Send verification email").assertIsDisplayed()
    }

    @Test
    fun passwordResetModeShowsResetEmailAction() {
        setAuthContent(AuthUiState(isPasswordResetMode = true))

        composeRule.onNodeWithText("Отправить письмо").assertIsDisplayed()
        composeRule.onNodeWithText("Вернуться ко входу").assertIsDisplayed()
    }

    @Test
    fun passwordResetWaitingModeShowsCodeAndNewPasswordFields() {
        setAuthContent(AuthUiState(isPasswordResetMode = true, isWaitingForResetCode = true))

        composeRule.onNodeWithText("Код или ссылка Firebase").assertIsDisplayed()
        composeRule.onNodeWithText("Новый пароль").assertIsDisplayed()
        composeRule.onNodeWithText("Сохранить новый пароль").assertIsDisplayed()
    }

    @Test
    fun infoAndErrorMessagesAreRendered() {
        setAuthContent(AuthUiState(info = "Письмо отправлено", error = "Ошибка входа"))

        composeRule.onNodeWithText("Письмо отправлено").assertIsDisplayed()
        composeRule.onNodeWithText("Ошибка входа").assertIsDisplayed()
    }

    private fun setAuthContent(
        state: AuthUiState = AuthUiState(),
        onUsernameChange: (String) -> Unit = {},
        onPasswordChange: (String) -> Unit = {},
        onPasswordConfirmationChange: (String) -> Unit = {},
        onResetCodeChange: (String) -> Unit = {},
        onResetNewPasswordChange: (String) -> Unit = {},
        onResetPasswordConfirmationChange: (String) -> Unit = {},
        onDisplayNameChange: (String) -> Unit = {},
        onLanguageChange: (String) -> Unit = {},
        onSubmit: () -> Unit = {},
        onTogglePasswordReset: () -> Unit = {},
        onToggleMode: () -> Unit = {},
    ) {
        composeRule.setContent {
            ProjectMatrixTheme {
                AuthContent(
                    state = state,
                    onUsernameChange = onUsernameChange,
                    onPasswordChange = onPasswordChange,
                    onPasswordConfirmationChange = onPasswordConfirmationChange,
                    onResetCodeChange = onResetCodeChange,
                    onResetNewPasswordChange = onResetNewPasswordChange,
                    onResetPasswordConfirmationChange = onResetPasswordConfirmationChange,
                    onDisplayNameChange = onDisplayNameChange,
                    onLanguageChange = onLanguageChange,
                    onSubmit = onSubmit,
                    onTogglePasswordReset = onTogglePasswordReset,
                    onToggleMode = onToggleMode,
                )
            }
        }
    }
}
