package com.example.projectmatrix.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.projectmatrix.domain.model.Message
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    onBack: () -> Unit,
    onCompanionProfile: (String) -> Unit,
    viewModel: ConversationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val text = conversationText(state.language)
    val listState = rememberLazyListState()
    val colors = chatColors()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.clickable(enabled = state.companionName.isNotBlank()) {
                            onCompanionProfile(state.chatId)
                        }
                    ) {
                        Text(
                            text = state.companionName.ifBlank { text.title },
                            style = MaterialTheme.typography.titleLarge,
                        )
                        if (state.companionLogin.isNotBlank()) {
                            Text(
                                text = state.companionLogin.asHandle(),
                                color = colors.secondaryText,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                },
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
                .background(colors.background)
                .padding(padding)
                .imePadding()
                .padding(horizontal = 12.dp)
                .padding(top = 10.dp, bottom = 6.dp),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.messages, key = { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        isOwn = message.senderId == state.currentUserId,
                        colors = colors,
                        text = text,
                        onDeleteMe = { viewModel.delete(message.id, deleteForAll = false) },
                        onDeleteAll = { viewModel.delete(message.id, deleteForAll = true) },
                    )
                }
                if (state.error != null) {
                    item {
                        Text(
                            text = state.error.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(CircleShape)
                        .background(colors.inputBackground)
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (state.draft.isBlank()) {
                        Text(text.message, color = colors.placeholder)
                    }
                    BasicTextField(
                        value = state.draft,
                        onValueChange = viewModel::setDraft,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.inputText),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                IconButton(
                    onClick = viewModel::send,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(colors.sendButton),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = text.send,
                        tint = colors.sendIcon,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    message: Message,
    isOwn: Boolean,
    colors: ConversationColors,
    text: ConversationText,
    onDeleteMe: () -> Unit,
    onDeleteAll: () -> Unit,
) {
    val bubbleColor = if (isOwn) colors.ownBubble else colors.companionBubble
    val horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start
    val isDeleted = message.deletedAt != null
    var menuExpanded by remember(message.id) { mutableStateOf(false) }
    val bubbleShape = if (isOwn) {
        MaterialTheme.shapes.medium
    } else {
        MaterialTheme.shapes.medium
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(bubbleShape)
                .background(bubbleColor)
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        if (isOwn && !isDeleted) menuExpanded = true
                    },
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text(text.deleteMe) },
                    onClick = {
                        menuExpanded = false
                        onDeleteMe()
                    },
                )
                DropdownMenuItem(
                    text = { Text(text.deleteAll) },
                    onClick = {
                        menuExpanded = false
                        onDeleteAll()
                    },
                )
            }
            Column {
                Text(
                    text = if (isDeleted) text.deletedMessage else message.body,
                    color = if (isDeleted) colors.secondaryText else colors.messageText,
                    style = if (isDeleted) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                )
                if (!isDeleted) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = formatMessageTime(message.createdAt),
                            color = colors.secondaryText,
                            style = MaterialTheme.typography.labelSmall,
                        )
                        if (isOwn) MessageStatusIcon(message, colors)
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageStatusIcon(message: Message, colors: ConversationColors) {
    val isRead = message.readAt != null
    val isDelivered = message.deliveredAt != null
    Icon(
        imageVector = if (isDelivered || isRead) Icons.Filled.DoneAll else Icons.Filled.Done,
        contentDescription = null,
        tint = if (isRead) colors.readStatus else colors.sentStatus,
        modifier = Modifier.size(16.dp),
    )
}

private data class ConversationColors(
    val background: Color,
    val ownBubble: Color,
    val companionBubble: Color,
    val messageText: Color,
    val secondaryText: Color,
    val inputBackground: Color,
    val inputText: Color,
    val placeholder: Color,
    val sendButton: Color,
    val sendIcon: Color,
    val sentStatus: Color,
    val readStatus: Color,
)

@Composable
private fun chatColors(): ConversationColors {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (isDark) {
        ConversationColors(
            background = Color(0xFF0E1117),
            ownBubble = Color(0xFF1F4F3A),
            companionBubble = Color(0xFF1D222B),
            messageText = Color.White,
            secondaryText = Color(0xFFB5BBC5),
            inputBackground = Color(0xFF1D222B),
            inputText = Color.White,
            placeholder = Color(0xFF8F98A6),
            sendButton = Color(0xFF2AABEE),
            sendIcon = Color.White,
            sentStatus = Color(0xFFB5BBC5),
            readStatus = Color(0xFF2AABEE),
        )
    } else {
        ConversationColors(
            background = Color(0xFFF4F0EA),
            ownBubble = Color(0xFFD8F5C7),
            companionBubble = Color.White,
            messageText = Color(0xFF161B22),
            secondaryText = Color(0xFF6F7580),
            inputBackground = Color.White,
            inputText = Color(0xFF161B22),
            placeholder = Color(0xFF8B9099),
            sendButton = Color(0xFF2AABEE),
            sendIcon = Color.White,
            sentStatus = Color(0xFF8A9099),
            readStatus = Color(0xFF2AABEE),
        )
    }
}

private fun formatMessageTime(value: String): String =
    runCatching {
        DateTimeFormatter.ofPattern("HH:mm")
            .withZone(ZoneId.systemDefault())
            .format(Instant.parse(value))
    }.getOrDefault("")

private data class ConversationText(
    val title: String,
    val back: String,
    val message: String,
    val send: String,
    val deleteMe: String,
    val deleteAll: String,
    val deletedMessage: String,
)

private fun conversationText(language: String): ConversationText =
    if (language == "en") {
        ConversationText(
            title = "Chat",
            back = "Back",
            message = "Message",
            send = "Send",
            deleteMe = "Delete for me",
            deleteAll = "Delete for everyone",
            deletedMessage = "Message deleted",
        )
    } else {
        ConversationText(
            title = "Чат",
            back = "Назад",
            message = "Сообщение",
            send = "Отправить",
            deleteMe = "Удалить у себя",
            deleteAll = "Удалить у обоих",
            deletedMessage = "Сообщение удалено",
        )
    }

private fun String.asHandle(): String =
    if (contains("@")) this else "@$this"
