package com.project.vaktim.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.project.vaktim.MainActivity
import com.project.vaktim.R
import com.project.vaktim.data.model.PrayerTime
import com.project.vaktim.util.PrayerTimeUtils
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "prayer_times_channel"
        const val NOTIFICATION_ID = 1001
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Ezan Vakti",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Namaz vakitlerini bildirim panelinde gosterir"
                setShowBadge(false)
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createCustomNotification(prayerTimes: List<PrayerTime>): Notification {
        val pendingIntent = createPendingIntent()
        val (nextPrayer, remainingTime) = PrayerTimeUtils.calculateNextPrayer(prayerTimes)

        val collapsedView = RemoteViews(context.packageName, R.layout.notification_collapsed)
        val collapsedText = if (nextPrayer != null && remainingTime.isNotBlank() && remainingTime != "--") {
            "Siradaki: ${nextPrayer.turkishName} ${nextPrayer.time} | $remainingTime kaldi"
        } else {
            prayerTimes.joinToString("  |  ") { "${it.turkishName} ${it.time}" }
        }
        collapsedView.setTextViewText(R.id.textTimes, collapsedText)

        val expandedView = RemoteViews(context.packageName, R.layout.notification_expanded)
        val nameIds = listOf(R.id.name1, R.id.name2, R.id.name3, R.id.name4, R.id.name5, R.id.name6)
        val timeIds = listOf(R.id.time1, R.id.time2, R.id.time3, R.id.time4, R.id.time5, R.id.time6)

        prayerTimes.forEachIndexed { index, prayer ->
            if (index < nameIds.size) {
                expandedView.setTextViewText(nameIds[index], prayer.turkishName)
                expandedView.setTextViewText(timeIds[index], prayer.time)
            }
        }

        applyNotificationTextColors(expandedView, prayerTimes, nameIds, timeIds)

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_hilal)
            .setCustomContentView(collapsedView)
            .setCustomBigContentView(expandedView)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    fun createSimpleNotification(text: String): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Ezan Vakti")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_hilal)
            .setContentIntent(createPendingIntent())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    fun notify(notification: Notification) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createPendingIntent(): PendingIntent {
        return PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun applyNotificationTextColors(
        expandedView: RemoteViews,
        prayerTimes: List<PrayerTime>,
        nameIds: List<Int>,
        timeIds: List<Int>
    ) {
        val highlightColor = Color.parseColor("#C17930")
        val activePrayerName = findActivePrayerName(prayerTimes)

        nameIds.forEachIndexed { index, nameId ->
            val prayer = prayerTimes.getOrNull(index)
            val isActive = prayer?.name == activePrayerName
            if (isActive) {
                expandedView.setTextColor(nameId, highlightColor)
                expandedView.setTextColor(timeIds[index], highlightColor)
            }
        }
    }

    private fun findActivePrayerName(
        prayerTimes: List<PrayerTime>,
        now: LocalTime = LocalTime.now()
    ): String? {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val mainPrayers = prayerTimes.filter { it.name != "Sunrise" }
        if (mainPrayers.isEmpty()) return null

        var currentPrayerName: String? = null
        mainPrayers.forEach { prayer ->
            runCatching { LocalTime.parse(prayer.time, formatter) }
                .onSuccess { prayerTime ->
                    if (!prayerTime.isAfter(now)) {
                        currentPrayerName = prayer.name
                    }
                }
        }

        return currentPrayerName ?: mainPrayers.lastOrNull()?.name
    }
}
