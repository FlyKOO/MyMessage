package com.example.mymessage.ui.contacts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mymessage.R
import com.example.mymessage.data.model.Contact
import com.example.mymessage.ui.permissions.RequiredPermissionsState

@Composable
fun ContactsScreen(
    contacts: List<Contact>,
    onContactSelected: (Contact) -> Unit,
    requiredPermissionsState: RequiredPermissionsState,
    onRequestPermissions: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (!requiredPermissionsState.allGranted) {
            PermissionRequest(onRequestPermissions)
        }
        if (contacts.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(contacts) { contact ->
                    ContactCard(contact = contact, onClick = { onContactSelected(contact) })
                }
            }
        }
    }
}

@Composable
private fun PermissionRequest(onRequestPermissions: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onRequestPermissions() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(id = R.string.permission_rationale), style = MaterialTheme.typography.bodyLarge)
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
        Text(text = stringResource(id = R.string.empty_contacts), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ContactCard(
    contact: Contact,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = contact.displayName, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = contact.numbers.joinToString(),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
