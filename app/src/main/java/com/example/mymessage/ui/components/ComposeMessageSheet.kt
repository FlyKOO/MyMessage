package com.example.mymessage.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.mymessage.data.model.Contact
import com.example.mymessage.R

@Composable
fun ComposeMessageSheet(
    draft: String,
    initialAddress: String?,
    contacts: List<Contact>,
    onDraftChanged: (String) -> Unit,
    onSend: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val addressState = remember(initialAddress) { mutableStateOf(TextFieldValue(initialAddress.orEmpty())) }
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)) {
            Text(text = stringResource(id = R.string.new_message_title), style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = addressState.value,
                onValueChange = { addressState.value = it },
                label = { Text(text = stringResource(id = R.string.recipient_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = draft,
                onValueChange = onDraftChanged,
                label = { Text(text = stringResource(id = R.string.message_body_label)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )
            Spacer(modifier = Modifier.height(16.dp))
            RowActions(
                onDismiss = onDismiss,
                onSend = {
                    val address = addressState.value.text.trim()
                    if (address.isNotEmpty() && draft.isNotBlank()) {
                        onSend(address, draft)
                    }
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = stringResource(id = R.string.recent_contacts), style = MaterialTheme.typography.titleMedium)
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(contacts) { contact ->
                    ContactSuggestion(
                        contact = contact,
                        onSelect = {
                            addressState.value = TextFieldValue(contact.numbers.firstOrNull().orEmpty())
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RowActions(
    onDismiss: () -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(onClick = onDismiss) { Text(text = stringResource(id = R.string.cancel)) }
        Spacer(modifier = Modifier.width(12.dp))
        Button(onClick = onSend) { Text(text = stringResource(id = R.string.send)) }
    }
}

@Composable
private fun ContactSuggestion(
    contact: Contact,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onSelect),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = contact.displayName, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = contact.numbers.joinToString(), style = MaterialTheme.typography.bodyMedium)
        }
        TextButton(onClick = onSelect) { Text(text = stringResource(id = R.string.use_contact)) }
    }
}
