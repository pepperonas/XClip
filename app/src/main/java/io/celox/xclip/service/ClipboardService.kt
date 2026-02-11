package io.celox.xclip.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.celox.xclip.MainActivity
import io.celox.xclip.R
import io.celox.xclip.data.ClipboardEntity
import io.celox.xclip.data.ClipboardRepository

class ClipboardService : Service() {

    private lateinit var clipboardManager: ClipboardManager
    private lateinit var repository: ClipboardRepository
    private lateinit var notificationManager: NotificationManager
    private var lastClipboardText = ""

    override fun onCreate() {
        super.onCreate()

        repository = ClipboardRepository(application)
        clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification(0))

        clipboardManager.addPrimaryClipChangedListener {
            val clipData = clipboardManager.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val text = clipData.getItemAt(0).text
                if (text != null && text.isNotEmpty()) {
                    val clipboardText = text.toString()
                    if (clipboardText != lastClipboardText) {
                        lastClipboardText = clipboardText
                        val entity = ClipboardEntity(
                            text = clipboardText,
                            timestamp = System.currentTimeMillis()
                        )
                        repository.insertSync(entity)
                        updateNotification()
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_description)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(count: Int): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_OPEN_DIALOG
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (count == 0) {
            getString(R.string.notification_ready)
        } else {
            resources.getQuantityString(R.plurals.notification_entries_saved, count, count)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification() {
        val count = repository.getCount()
        notificationManager.notify(NOTIFICATION_ID, createNotification(count))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "clipboard_service_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_OPEN_DIALOG = "io.celox.xclip.OPEN_DIALOG"
    }
}
