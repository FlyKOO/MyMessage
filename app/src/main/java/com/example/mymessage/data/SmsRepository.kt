package com.example.mymessage.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import android.telephony.SmsManager
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.example.mymessage.data.model.ConversationMessage
import com.example.mymessage.data.model.ConversationSummary
import com.example.mymessage.util.observe
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class SmsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val resolver = context.contentResolver

    fun getConversationSummaries(): Flow<List<ConversationSummary>> {
        val uri = Telephony.Threads.CONTENT_URI
        return resolver.observe(uri).map {
            queryConversations()
        }.distinctUntilChanged()
    }

    fun getMessages(threadId: Long): Flow<List<ConversationMessage>> {
        val uri = Telephony.Sms.CONTENT_URI
        return resolver.observe(uri).map {
            queryMessages(threadId)
        }.distinctUntilChanged()
    }

    suspend fun sendMessage(address: String, body: String) = withContext(Dispatchers.IO) {
        val manager = SmsManager.getDefault()
        val parts = manager.divideMessage(body)
        manager.sendMultipartTextMessage(address, null, parts, null, null)

        val values = ContentValues().apply {
            put(Telephony.Sms.ADDRESS, address)
            put(Telephony.Sms.BODY, body)
            put(Telephony.Sms.DATE, System.currentTimeMillis())
            put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT)
        }
        resolver.insert(Telephony.Sms.Sent.CONTENT_URI, values)
    }

    suspend fun markThreadRead(threadId: Long) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(Telephony.Sms.READ, 1)
        }
        resolver.update(
            Telephony.Sms.CONTENT_URI,
            values,
            "${Telephony.Sms.THREAD_ID} = ?",
            arrayOf(threadId.toString())
        )
    }

    private suspend fun queryConversations(): List<ConversationSummary> = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            Telephony.Threads._ID,
            Telephony.Threads.DATE,
            Telephony.Threads.SNIPPET,
            Telephony.Threads.READ
        )

        val conversations = mutableListOf<ConversationSummary>()
        resolver.query(
            Telephony.Threads.CONTENT_URI,
            projection,
            null,
            null,
            "${Telephony.Threads.DATE} DESC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val threadId = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Threads._ID))
                val snippet = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(Telephony.Threads.SNIPPET)).orEmpty()
                val date = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Threads.DATE))
                val read = cursor.getIntOrNull(cursor.getColumnIndexOrThrow(Telephony.Threads.READ)) ?: 1
                val addresses = resolveAddressesForThread(threadId)
                val title = addresses.firstOrNull() ?: ""

                conversations += ConversationSummary(
                    threadId = threadId,
                    title = title.ifEmpty { snippet.ifEmpty { "" } },
                    snippet = snippet,
                    timestamp = Instant.ofEpochMilli(date),
                    unreadCount = if (read == 0) 1 else 0,
                    participants = addresses
                )
            }
        }
        conversations
    }

    private suspend fun queryMessages(threadId: Long): List<ConversationMessage> = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE,
            Telephony.Sms.READ
        )

        val messages = mutableListOf<ConversationMessage>()
        resolver.query(
            Telephony.Sms.CONTENT_URI,
            projection,
            "${Telephony.Sms.THREAD_ID} = ?",
            arrayOf(threadId.toString()),
            "${Telephony.Sms.DATE} ASC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                messages += cursor.toConversationMessage()
            }
        }
        messages
    }

    private fun Cursor.toConversationMessage(): ConversationMessage {
        val id = getLong(getColumnIndexOrThrow(Telephony.Sms._ID))
        val threadId = getLong(getColumnIndexOrThrow(Telephony.Sms.THREAD_ID))
        val address = getStringOrNull(getColumnIndexOrThrow(Telephony.Sms.ADDRESS)).orEmpty()
        val body = getStringOrNull(getColumnIndexOrThrow(Telephony.Sms.BODY)).orEmpty()
        val date = getLong(getColumnIndexOrThrow(Telephony.Sms.DATE))
        val type = getInt(getColumnIndexOrThrow(Telephony.Sms.TYPE))
        val read = getIntOrNull(getColumnIndexOrThrow(Telephony.Sms.READ)) ?: 1

        return ConversationMessage(
            id = id,
            threadId = threadId,
            address = address,
            body = body,
            timestamp = Instant.ofEpochMilli(date),
            isIncoming = type == Telephony.Sms.MESSAGE_TYPE_INBOX,
            read = read == 1
        )
    }

    private suspend fun resolveAddressesForThread(threadId: Long): List<String> = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            Telephony.Sms.ADDRESS
        )
        val addresses = mutableSetOf<String>()
        resolver.query(
            Telephony.Sms.CONTENT_URI,
            projection,
            "${Telephony.Sms.THREAD_ID} = ?",
            arrayOf(threadId.toString()),
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val address = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)).orEmpty()
                if (address.isNotEmpty()) {
                    addresses.add(resolveDisplayName(address) ?: address)
                }
            }
        }
        addresses.toList()
    }

    private fun resolveDisplayName(address: String): String? {
        val projection = arrayOf(
            ContactsContract.PhoneLookup.DISPLAY_NAME
        )
        resolver.query(
            Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address)),
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
            }
        }
        return null
    }
}
