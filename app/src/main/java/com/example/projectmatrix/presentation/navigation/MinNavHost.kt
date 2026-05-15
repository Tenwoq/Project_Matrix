package com.example.projectmatrix.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.projectmatrix.presentation.auth.AuthScreen
import com.example.projectmatrix.presentation.chat.ChatListScreen
import com.example.projectmatrix.presentation.chat.CompanionProfileScreen
import com.example.projectmatrix.presentation.chat.ConversationScreen
import com.example.projectmatrix.presentation.settings.ChangePasswordScreen
import com.example.projectmatrix.presentation.settings.ProfileScreen
import com.example.projectmatrix.presentation.settings.SettingsScreen

@Composable
fun MinNavHost(startDestination: String = Routes.Auth) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.Auth) {
            AuthScreen(
                onAuthorized = {
                    navController.navigate(Routes.Chats) {
                        popUpTo(Routes.Auth) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.Chats) {
            ChatListScreen(
                onChatSelected = { chatId -> navController.navigate(Routes.conversation(chatId)) },
                onSettings = {
                    navController.navigate(Routes.Settings) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(Routes.Settings) {
            SettingsScreen(
                onChats = {
                    navController.navigate(Routes.Chats) {
                        launchSingleTop = true
                        popUpTo(Routes.Chats)
                    }
                },
                onProfile = { navController.navigate(Routes.Profile) },
                onChangePassword = { navController.navigate(Routes.ChangePassword) },
                onLogout = {
                    navController.navigate(Routes.Auth) {
                        popUpTo(Routes.Chats) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.Profile) {
            ProfileScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.ChangePassword) {
            ChangePasswordScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.Conversation,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType }),
        ) {
            ConversationScreen(
                onBack = { navController.popBackStack() },
                onCompanionProfile = { chatId -> navController.navigate(Routes.companionProfile(chatId)) },
            )
        }
        composable(
            route = Routes.CompanionProfile,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType }),
        ) {
            CompanionProfileScreen(onBack = { navController.popBackStack() })
        }
    }
}

object Routes {
    const val Auth = "auth"
    const val Chats = "chats"
    const val Settings = "settings"
    const val Profile = "settings/profile"
    const val ChangePassword = "settings/change-password"
    const val Conversation = "conversation/{chatId}"
    const val CompanionProfile = "conversation/{chatId}/profile"
    fun conversation(chatId: String) = "conversation/$chatId"
    fun companionProfile(chatId: String) = "conversation/$chatId/profile"
}
