package com.example.mymessage.sms

import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MessagingService : Telephony.SmsReceiverService() {

    @Inject lateinit var notificationManager: SmsNotificationManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && (intent.action == Telephony.Sms.Intents.SMS_DELIVER_ACTION || intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            handleMessages(messages)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun handleMessages(messages: Array<SmsMessage>) {
        if (messages.isEmpty()) return
        val address = messages.first().displayOriginatingAddress
        val body = messages.joinToString("\n") { it.displayMessageBody }
        val threadId = Telephony.Threads.getOrCreateThreadId(this, address)
        notificationManager.showIncomingMessage(threadId, address, body)
    }
}
