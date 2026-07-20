package com.hnahofra.app.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.hnahofra.app.R

/** Notifications locales pour prévenir l'admin des signalements en attente. */
object Notifications {
    private const val CHANNEL_ID = "moderation"
    private const val NOTIF_ID = 1001

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Modération",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val mgr = context.getSystemService(NotificationManager::class.java)
            mgr?.createNotificationChannel(channel)
        }
    }

    fun showPendingReports(context: Context, count: Int) {
        if (count <= 0) return
        // Android 13+ : la notification n'apparaît que si la permission est accordée.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        ensureChannel(context)
        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_flag)
            .setContentTitle(context.getString(R.string.notif_pending_title))
            .setContentText(context.getString(R.string.notif_pending_text, count))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID, notif)
        } catch (e: SecurityException) {
        }
    }
}
