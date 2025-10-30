package com.example.mymessage.ui.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.mymessage.data.model.ConversationMessage
import com.example.mymessage.R
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.time.Instant

private val timeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")
private val zoneId = ZoneId.systemDefault()
private fun Instant.formatConversationTime(): String = timeFormatter.format(this.atZone(zoneId))

@Composable
fun ConversationScreen(
    threadId: Long?,
    messages: List<ConversationMessage>,
    onBack: () -> Unit,
    onSend: (String, String) -> Unit,
    draft: String,
    onDraftChanged: (String) -> Unit
) {
    val address = messages.lastOrNull()?.address.orEmpty()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (address.isNotEmpty()) address else stringResource(id = R.string.thread_title_unknown)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                reverseLayout = false
            ) {
                items(messages) { message ->
                    MessageBubble(message = message)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Composer(
                value = draft,
                onValueChange = onDraftChanged,
                onSend = {
                    val destination = if (address.isNotBlank()) address else messages.lastOrNull()?.address.orEmpty()
                    if (destination.isNotBlank()) {
                        onSend(destination, draft)
                    }
                }
            )
        }
    }
}

@Composable
private fun MessageBubble(message: ConversationMessage) {
    val alignment = if (message.isIncoming) Alignment.CenterStart else Alignment.CenterEnd
    val bubbleColor = if (message.isIncoming) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary
    val textColor = if (message.isIncoming) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .background(color = bubbleColor, shape = MaterialTheme.shapes.medium)
                .padding(12.dp)
        ) {
            Text(text = message.body, color = textColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.timestamp.formatConversationTime(),
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.7f),
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun Composer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            maxLines = 4
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onSend, enabled = value.isNotBlank()) {
            Icon(imageVector = Icons.Default.Send, contentDescription = stringResource(id = R.string.send))
        }
    }
}
