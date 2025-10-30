package com.example.mymessage.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SmsBroadcastReceiver : BroadcastReceiver() {

    @Inject lateinit var smsNotificationManager: SmsNotificationManager
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isEmpty()) return
        val address = messages.first().displayOriginatingAddress
        val body = messages.joinToString(separator = "\n") { it.displayMessageBody }
        val threadId = Telephony.Threads.getOrCreateThreadId(context, address)

        smsNotificationManager.showIncomingMessage(threadId, address, body)
    }
}
