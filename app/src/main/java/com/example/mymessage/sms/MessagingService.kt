package com.example.mymessage.sms

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.Telephony
import android.telephony.SmsMessage
import com.example.mymessage.di.AppDependencies

class MessagingService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && (intent.action == Telephony.Sms.Intents.SMS_DELIVER_ACTION || intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            handleMessages(messages)
        }
        stopSelf(startId)
        return START_NOT_STICKY
    }

    private fun handleMessages(messages: Array<SmsMessage>) {
        if (messages.isEmpty()) return
        val address = messages.first().displayOriginatingAddress
        val body = messages.joinToString("\n") { it.displayMessageBody }
        val threadId = Telephony.Threads.getOrCreateThreadId(this, address)
        AppDependencies.smsNotificationManager(this).showIncomingMessage(threadId, address, body)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
