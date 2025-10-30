package com.example.mymessage.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mymessage.R
import com.example.mymessage.ui.theme.MyMessageTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyMessageScreen() {
    var messageDraft by remember { mutableStateOf("") }
    var recipientDraft by remember { mutableStateOf("") }
    val conversations = remember { SampleData.conversations }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ConversationList(conversations = conversations, modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.compose_message),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = recipientDraft,
                    onValueChange = { recipientDraft = it },
                    label = { Text(text = stringResource(id = R.string.recipient_label)) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = messageDraft,
                    onValueChange = { messageDraft = it },
                    label = { Text(text = stringResource(id = R.string.message_body_label)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        recipientDraft = ""
                        messageDraft = ""
                    }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }

                    FilledTonalIconButton(onClick = {
                        recipientDraft = ""
                        messageDraft = ""
                    }) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationList(
    conversations: List<ConversationUiModel>,
    modifier: Modifier = Modifier
) {
    if (conversations.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.empty_messages),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(conversations) { conversation ->
                ConversationCard(conversation = conversation)
            }
        }
    }
}

@Composable
private fun ConversationCard(conversation: ConversationUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = conversation.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = conversation.preview,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = stringResource(id = R.string.reply),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

private object SampleData {
    val conversations = listOf(
        ConversationUiModel(
            title = "王小明",
            preview = "嗨，今晚要不要一起去看电影？我们可以提前买票。",
            timestamp = "20:45"
        ),
        ConversationUiModel(
            title = "张老师",
            preview = "明天的家长会在七点开始，请提前十分钟到校准备。",
            timestamp = "18:22"
        ),
        ConversationUiModel(
            title = "物流通知",
            preview = "您有一个快递已到达菜鸟驿站，请在三日内前往领取，谢谢。",
            timestamp = "16:05"
        ),
        ConversationUiModel(
            title = "银行服务",
            preview = "本月信用卡账单已出，请于25日前完成还款，感谢使用。",
            timestamp = "周二"
        )
    )
}

data class ConversationUiModel(
    val title: String,
    val preview: String,
    val timestamp: String
)

@Preview(showBackground = true)
@Composable
private fun MyMessageScreenPreview() {
    MyMessageTheme {
        MyMessageScreen()
    }
}
