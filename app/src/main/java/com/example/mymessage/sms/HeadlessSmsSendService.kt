package com.example.mymessage.sms

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.Telephony
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HeadlessSmsSendService : Service() {

    @Inject lateinit var smsRepository: com.example.mymessage.data.SmsRepository

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val address = intent?.getStringExtra(Telephony.Sms.Intents.EXTRA_ADDRESS)
        val message = intent?.getStringExtra(Telephony.Sms.Intents.EXTRA_TEXT)
        if (!address.isNullOrBlank() && !message.isNullOrBlank()) {
            CoroutineScope(Dispatchers.IO).launch {
                smsRepository.sendMessage(address, message)
                stopSelf(startId)
            }
        } else {
            stopSelf(startId)
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
