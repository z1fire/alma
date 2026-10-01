package com.z1fire.alma.reminders

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Build
import com.z1fire.alma.MainActivity
import com.z1fire.alma.R
import com.z1fire.alma.data.AppData
import com.z1fire.alma.data.active
import com.z1fire.alma.data.course
import com.z1fire.alma.data.minutesOn
import com.z1fire.alma.data.streak
import java.time.LocalDate

object Notifier {
    private const val ID_REMINDER = 1002
    private const val ID_SESSION = 1004

    fun canNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    /** Title and text for today's reminder, or null if there's nothing to nudge about. */
    fun reminderText(data: AppData, today: LocalDate): Pair<String, String>? {
        val active = data.active
        if (active.isEmpty() || data.minutesOn(today) > 0) return null
        // Suggest the course that's gone longest without attention.
        val pick = active.minBy { c -> c.sessions.maxOfOrNull { it.epochDay } ?: Long.MIN_VALUE }
        val streak = data.streak(today)
        val title = if (streak > 0) "Keep your $streak-day streak going" else "Time to study?"
        return title to "Nothing logged today yet. Even 20 minutes of ${pick.title} counts."
    }

    /** Posts today's reminder if relevant; with [force], posts a sample even when it isn't. */
    fun postReminder(context: Context, data: AppData, force: Boolean = false) {
        val (title, text) = reminderText(data, LocalDate.now())
            ?: if (force) "Reminders are on" to "You'll get a nudge on days you haven't studied." else return
        post(context, ReminderScheduler.CHANNEL_REMINDER, ID_REMINDER, title, text, null)
    }

    fun openApp(context: Context, courseId: String?): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        if (courseId != null) intent.putExtra(MainActivity.EXTRA_COURSE_ID, courseId)
        return PendingIntent.getActivity(
            context,
            courseId?.hashCode() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun post(context: Context, channel: String, id: Int, title: String, text: String, courseId: String?) {
        if (!canNotify(context)) return
        val n = Notification.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(Notification.BigTextStyle().bigText(text))
            .setContentIntent(openApp(context, courseId))
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(id, n)
    }

    /** Shows (or clears) the ongoing timer notification to match the running study session. */
    fun syncSession(context: Context, data: AppData) {
        val nm = context.getSystemService(NotificationManager::class.java)
        val timer = data.activeTimer
        val course = timer?.let { data.course(it.courseId) }
        if (timer == null || course == null || !data.profile.sessionNotificationEnabled || !canNotify(context)) {
            nm.cancel(ID_SESSION)
            return
        }
        fun action(label: String, action: String, code: Int) = Notification.Action.Builder(
            Icon.createWithResource(context, R.drawable.ic_launcher_foreground),
            label,
            PendingIntent.getBroadcast(
                context, code,
                Intent(context, ReminderReceiver::class.java).setAction(action),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ),
        ).build()

        val notification = Notification.Builder(context, ReminderScheduler.CHANNEL_SESSION)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Studying · ${course.title}")
            .setWhen(timer.startedAtMillis)
            .setShowWhen(true)
            .setUsesChronometer(true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(Notification.CATEGORY_STOPWATCH)
            .setContentIntent(openApp(context, course.id))
            .addAction(action("Stop & log", ReminderScheduler.ACTION_END_SESSION, 10))
            .addAction(action("Discard", ReminderScheduler.ACTION_DISCARD_SESSION, 11))
            .build()
        nm.notify(ID_SESSION, notification)
    }
}
