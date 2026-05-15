package com.example.projectmatrix.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.projectmatrix.domain.model.Chat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onChatSelected: (String) -> Unit,
    onSettings: () -> Unit,
    viewModel: ChatListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val text = chatListText(state.language)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MIN") },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null) },
                    label = { Text(text.chats) },
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onSettings,
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    label = { Text(text.settings) },
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
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                label = { Text(text.searchUser) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (state.users.isNotEmpty()) {
                    item { Text(text.users, style = MaterialTheme.typography.labelLarge) }
                    items(state.users) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.openDirectChat(user.id, onChatSelected) }
                                .padding(vertical = 12.dp),
                        ) {
                            Column {
                                Text(user.displayName, fontWeight = FontWeight.SemiBold)
                                Text(user.username.asHandle(), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        HorizontalDivider()
                    }
                }

                item { Text(text.chats, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp)) }
                items(state.chats, key = { it.id }) { chat ->
                    ChatRow(
                        chat = chat,
                        noMessages = text.noMessages,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChatSelected(chat.id) }
                            .padding(vertical = 14.dp),
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ChatRow(chat: Chat, noMessages: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(chat.companion.displayName, fontWeight = FontWeight.SemiBold)
            Text(
                text = chat.lastMessage?.body ?: noMessages,
                color = if (chat.unreadCount > 0) Color(0xFF1D1F24) else Color(0xFF6F7580),
                fontWeight = if (chat.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (chat.unreadCount > 0) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2AABEE)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

private data class ChatListText(
    val settings: String,
    val searchUser: String,
    val users: String,
    val chats: String,
    val noMessages: String,
)

private fun chatListText(language: String): ChatListText =
    if (language == "en") {
        ChatListText(
            settings = "Settings",
            searchUser = "Find user",
            users = "Users",
            chats = "Chats",
            noMessages = "No messages",
        )
    } else {
        ChatListText(
            settings = "Настройки",
            searchUser = "Найти пользователя",
            users = "Пользователи",
            chats = "Чаты",
            noMessages = "Нет сообщений",
        )
    }

private fun String.asHandle(): String =
    if (contains("@")) this else "@$this"
