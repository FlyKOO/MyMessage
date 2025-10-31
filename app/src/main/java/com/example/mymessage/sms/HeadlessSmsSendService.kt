package com.example.mymessage.sms

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.mymessage.di.AppDependencies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HeadlessSmsSendService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val address = intent?.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
        val message = intent?.getStringExtra(Intent.EXTRA_TEXT)
        if (!address.isNullOrBlank() && !message.isNullOrBlank()) {
            CoroutineScope(Dispatchers.IO).launch {
                AppDependencies.smsRepository(this@HeadlessSmsSendService).sendMessage(address, message)
                stopSelf(startId)
            }
        } else {
            stopSelf(startId)
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
