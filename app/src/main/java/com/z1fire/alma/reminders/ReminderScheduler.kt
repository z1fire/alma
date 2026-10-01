package com.z1fire.alma.reminders

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.z1fire.alma.data.AppData
import java.time.ZonedDateTime

/** Keeps one alarm armed: the next daily study reminder. */
object ReminderScheduler {
    const val CHANNEL = "study_reminder"
    const val ACTION_REMINDER = "com.z1fire.alma.action.REMINDER"

    /** Channels from older versions, removed on upgrade. */
    private val OLD_CHANNELS = listOf("class_reminders", "daily_digest", "weekly_report", "study_session")

    /** The shortest window Android 12+ honors; plain inexact alarms can drift by an hour. */
    private const val WINDOW_MS = 10 * 60_000L

    fun createChannel(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        OLD_CHANNELS.forEach { nm.deleteNotificationChannel(it) }
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "Daily study reminder", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "A nudge on days you haven't studied yet" },
        )
    }

    fun reschedule(context: Context, data: AppData) {
        val am = context.getSystemService(AlarmManager::class.java)
        val pi = PendingIntent.getBroadcast(
            context, 0,
            Intent(context, ReminderReceiver::class.java).setAction(ACTION_REMINDER),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        if (!data.profile.reminderEnabled) {
            am.cancel(pi)
            return
        }
        val now = ZonedDateTime.now()
        var at = now.toLocalDate().atStartOfDay(now.zone).plusMinutes(data.profile.reminderMinute.toLong())
        if (!at.isAfter(now.plusSeconds(15))) at = at.plusDays(1)
        am.setWindow(AlarmManager.RTC_WAKEUP, at.toInstant().toEpochMilli(), WINDOW_MS, pi)
    }
}
