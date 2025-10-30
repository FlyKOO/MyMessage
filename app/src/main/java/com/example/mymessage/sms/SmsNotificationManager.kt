package com.example.mymessage.sms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.example.mymessage.MainActivity
import com.example.mymessage.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val CHANNEL_ID = "sms_channel"
private const val KEY_TEXT_REPLY = "key_text_reply"

@Singleton
class SmsNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val notificationManager
        get() = NotificationManagerCompat.from(context)

    fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = context.getString(R.string.app_name)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showIncomingMessage(threadId: Long, address: String, body: String) {
        ensureChannel()
        val replyLabel = context.getString(R.string.reply)
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel(replyLabel)
            .build()

        val replyIntent = Intent(context, DirectReplyReceiver::class.java).apply {
            action = DirectReplyReceiver.ACTION_DIRECT_REPLY
            putExtra(DirectReplyReceiver.EXTRA_THREAD_ID, threadId)
            putExtra(DirectReplyReceiver.EXTRA_ADDRESS, address)
        }

        val replyFlags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_MUTABLE else 0
        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            threadId.toInt(),
            replyIntent,
            replyFlags
        )

        val action = NotificationCompat.Action.Builder(
            R.drawable.ic_launcher_foreground,
            replyLabel,
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()

        val contentFlags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val contentIntent = PendingIntent.getActivity(
            context,
            threadId.toInt(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            contentFlags
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(address)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .addAction(action)
            .build()

        notificationManager.notify(threadId.toInt(), notification)
    }

    fun handleDirectReply(intent: Intent): Pair<Long, String>? {
        val reply = RemoteInput.getResultsFromIntent(intent)?.getCharSequence(KEY_TEXT_REPLY)?.toString()
        val threadId = intent.getLongExtra(DirectReplyReceiver.EXTRA_THREAD_ID, -1L)
        val address = intent.getStringExtra(DirectReplyReceiver.EXTRA_ADDRESS)
        return if (!reply.isNullOrBlank() && threadId > 0 && !address.isNullOrBlank()) {
            Pair(threadId, reply)
        } else {
            null
        }
    }

    fun cancel(threadId: Long) {
        notificationManager.cancel(threadId.toInt())
    }
}
