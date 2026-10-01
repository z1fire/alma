package com.z1fire.alma.reminders

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.z1fire.alma.MainActivity
import com.z1fire.alma.R
import com.z1fire.alma.data.AppData
import java.time.LocalDate

object Notifier {
    private const val ID_REMINDER = 1002

    fun canNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    /** Today's nudge, or null if something was already studied today (or there's nothing to study). */
    fun reminderText(data: AppData, today: LocalDate): String? {
        val studying = data.studying
        if (studying.isEmpty() || studying.any { it.lastStudiedEpochDay == today.toEpochDay() }) return null
        // Suggest the course that's gone longest without attention.
        val pick = studying.minBy { it.lastStudiedEpochDay ?: Long.MIN_VALUE }
        return "Nothing logged today yet. Even 20 minutes of ${pick.title} counts."
    }

    /** Posts today's reminder if relevant; with [force], posts a sample even when it isn't. */
    fun postReminder(context: Context, data: AppData, force: Boolean = false) {
        if (!canNotify(context)) return
        val text = reminderText(data, LocalDate.now())
            ?: if (force) "You'll get a nudge like this on days you haven't studied." else return
        val open = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val n = Notification.Builder(context, ReminderScheduler.CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time to study?")
            .setContentText(text)
            .setStyle(Notification.BigTextStyle().bigText(text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(ID_REMINDER, n)
    }
}
