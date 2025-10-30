package com.example.mymessage.data.model

import android.net.Uri
import java.time.Instant

data class ConversationSummary(
    val threadId: Long,
    val title: String,
    val snippet: String,
    val timestamp: Instant,
    val unreadCount: Int,
    val participants: List<String>
)

data class ConversationMessage(
    val id: Long,
    val threadId: Long,
    val address: String,
    val body: String,
    val timestamp: Instant,
    val isIncoming: Boolean,
    val read: Boolean
)

data class Contact(
    val id: Long,
    val displayName: String,
    val numbers: List<String>,
    val photoUri: Uri?
)
