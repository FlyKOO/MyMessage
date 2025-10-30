package com.example.mymessage.util

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun ContentResolver.observe(uri: Uri): Flow<Unit> = callbackFlow {
    val handler = Handler(Looper.getMainLooper())
    val observer = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            trySend(Unit)
        }

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            trySend(Unit)
        }
    }

    registerContentObserver(uri, true, observer)
    trySend(Unit)

    awaitClose {
        unregisterContentObserver(observer)
    }
}
