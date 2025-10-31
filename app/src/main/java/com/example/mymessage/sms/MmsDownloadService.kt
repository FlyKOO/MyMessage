package com.example.mymessage.sms

import android.app.Service
import android.content.Intent
import android.os.IBinder

class MmsDownloadService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        stopSelf(startId)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
