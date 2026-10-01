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
    const val CHANNEL_REMINDER = "study_reminder"
    const val CHANNEL_SESSION = "study_session"

    const val ACTION_REMINDER = "com.z1fire.alma.action.REMINDER"
    const val ACTION_END_SESSION = "com.z1fire.alma.action.END_SESSION"
    const val ACTION_DISCARD_SESSION = "com.z1fire.alma.action.DISCARD_SESSION"

    /** Channels and alarm actions used by v1, removed on upgrade. */
    private val OLD_CHANNELS = listOf("class_reminders", "daily_digest", "weekly_report")
    private val OLD_ACTIONS = listOf(
        null to 0,
        "com.z1fire.alma.action.CLASS" to 0,
        "com.z1fire.alma.action.BRIEFING" to 1,
        "com.z1fire.alma.action.NUDGE" to 2,
        "com.z1fire.alma.action.WEEKLY" to 3,
    )

    /** The shortest window Android 12+ honors; plain inexact alarms can drift by an hour. */
    private const val WINDOW_MS = 10 * 60_000L

    fun createChannels(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        OLD_CHANNELS.forEach { nm.deleteNotificationChannel(it) }
        nm.createNotificationChannels(
            listOf(
                NotificationChannel(CHANNEL_REMINDER, "Daily study reminder", NotificationManager.IMPORTANCE_DEFAULT)
                    .apply { description = "A nudge on days you haven't studied yet" },
                NotificationChannel(CHANNEL_SESSION, "Study timer", NotificationManager.IMPORTANCE_LOW)
                    .apply { description = "Shows the running timer during a study session" },
            ),
        )
    }

    /** Next occurrence of a time of day, strictly in the future. */
    fun nextAt(minuteOfDay: Int, now: ZonedDateTime = ZonedDateTime.now()): ZonedDateTime {
        val today = now.toLocalDate().atStartOfDay(now.zone).plusMinutes(minuteOfDay.toLong())
        return if (today.isAfter(now.plusSeconds(15))) today else today.plusDays(1)
    }

    fun reschedule(context: Context, data: AppData) {
        val am = context.getSystemService(AlarmManager::class.java)
        OLD_ACTIONS.forEach { (action, code) -> am.cancel(pending(context, action, code)) }
        val pi = pending(context, ACTION_REMINDER, 0)
        if (!data.profile.reminderEnabled) {
            am.cancel(pi)
            return
        }
        val at = nextAt(data.profile.reminderMinute).toInstant().toEpochMilli()
        am.setWindow(AlarmManager.RTC_WAKEUP, at, WINDOW_MS, pi)
    }

    private fun pending(context: Context, action: String?, code: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).also { if (action != null) it.action = action }
        return PendingIntent.getBroadcast(context, code, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}
