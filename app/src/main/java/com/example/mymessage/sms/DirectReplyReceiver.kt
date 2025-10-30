package com.example.mymessage.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DirectReplyReceiver : BroadcastReceiver() {

    @Inject lateinit var smsRepository: com.example.mymessage.data.SmsRepository
    @Inject lateinit var notificationManager: SmsNotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_DIRECT_REPLY) return
        val result = notificationManager.handleDirectReply(intent) ?: return
        val (threadId, reply) = result
        val address = intent.getStringExtra(EXTRA_ADDRESS) ?: return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            smsRepository.sendMessage(address, reply)
            smsRepository.markThreadRead(threadId)
            notificationManager.cancel(threadId)
            pendingResult.finish()
        }
    }

    companion object {
        const val ACTION_DIRECT_REPLY = "com.example.mymessage.ACTION_DIRECT_REPLY"
        const val EXTRA_THREAD_ID = "extra_thread_id"
        const val EXTRA_ADDRESS = "extra_address"
    }
}
