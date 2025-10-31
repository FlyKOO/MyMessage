package com.example.mymessage.ui.inbox

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mymessage.R
import com.example.mymessage.data.model.ConversationSummary
import com.example.mymessage.ui.permissions.RequiredPermissionsState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")
private val zoneId = ZoneId.systemDefault()
private fun Instant.formatForDisplay(): String = timeFormatter.format(this.atZone(zoneId))

@Composable
fun InboxScreen(
    conversations: List<ConversationSummary>,
    onConversationClick: (ConversationSummary) -> Unit,
    onComposeClick: () -> Unit,
    requiredPermissionsState: RequiredPermissionsState,
    onRequestPermissions: () -> Unit,
    onRequestDefaultSms: () -> Unit,
    isDefaultSmsApp: Boolean
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (!requiredPermissionsState.allGranted) {
                PermissionRequestPanel(
                    onRequestPermissions = onRequestPermissions,
                    onRequestDefaultSms = onRequestDefaultSms
                )
            }
            if (!isDefaultSmsApp) {
                DefaultSmsPrompt(onRequestDefaultSms = onRequestDefaultSms)
            }
            if (conversations.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(conversations) { conversation ->
                        ConversationCard(conversation = conversation, onClick = { onConversationClick(conversation) })
                    }
                }
            }
        }
        FloatingActionButton(
            modifier = Modifier
                .padding(24.dp)
                .align(Alignment.BottomEnd),
            onClick = onComposeClick
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(id = R.string.compose_message)
            )
        }
    }
}

@Composable
private fun ColumnScope.PermissionRequestPanel(
    onRequestPermissions: () -> Unit,
    onRequestDefaultSms: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onRequestPermissions() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(id = R.string.permission_rationale), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = stringResource(id = R.string.default_sms_description), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ColumnScope.DefaultSmsPrompt(onRequestDefaultSms: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onRequestDefaultSms() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(id = R.string.default_sms_title), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = stringResource(id = R.string.default_sms_description), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(id = R.string.empty_messages), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ConversationCard(
    conversation: ConversationSummary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = conversation.title.ifEmpty { stringResource(id = R.string.thread_title_unknown) },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
                Text(text = conversation.timestamp.formatForDisplay(), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = conversation.snippet,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
