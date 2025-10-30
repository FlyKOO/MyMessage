package com.example.mymessage.data

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import com.example.mymessage.data.model.Contact
import com.example.mymessage.util.observe
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class ContactsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val resolver = context.contentResolver

    fun getContacts(): Flow<List<Contact>> {
        return resolver.observe(ContactsContract.Contacts.CONTENT_URI).map {
            queryContacts()
        }.distinctUntilChanged()
    }

    private suspend fun queryContacts(): List<Contact> = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.HAS_PHONE_NUMBER,
            ContactsContract.Contacts.PHOTO_URI
        )

        val contacts = mutableListOf<Contact>()
        resolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            null,
            null,
            "${ContactsContract.Contacts.DISPLAY_NAME} COLLATE NOCASE ASC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                val displayName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                val hasNumber = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0
                val photoUri = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI))

                if (!hasNumber) continue

                val numbers = resolveNumbers(id)
                contacts += Contact(
                    id = id,
                    displayName = displayName,
                    numbers = numbers,
                    photoUri = photoUri?.let(Uri::parse)
                )
            }
        }
        contacts
    }

    private fun resolveNumbers(contactId: Long): List<String> {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val numbers = mutableListOf<String>()
        resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId.toString()),
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                numbers += cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
            }
        }
        return numbers
    }
}
