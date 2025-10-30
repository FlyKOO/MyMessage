package com.example.mymessage.sms

import android.net.Uri
import android.os.Bundle
import android.provider.Telephony
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MmsDownloadService : Telephony.MmsService() {
    override fun onDownloadMessage(messageUri: Uri?, p1: Int, p2: Bundle?): Int {
        return Telephony.MmsService.RESULT_SUCCESS
    }

    override fun onSendMessage(messageUri: Uri?, p1: Bundle?): Int {
        return Telephony.MmsService.RESULT_SUCCESS
    }
}
