package com.z1fire.alma.reminders

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.z1fire.alma.AlmaApp
import com.z1fire.alma.R
import com.z1fire.alma.data.codeOf
import com.z1fire.alma.data.course
import java.text.DateFormat
import java.util.Date

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val repo = (context.applicationContext as AlmaApp).repository
        val data = repo.current
        val p = data.profile

        when (intent.action) {
            ReminderScheduler.ACTION_END_SESSION -> {
                data.activeTimer?.let { t ->
                    val minutes = ((System.currentTimeMillis() - t.startedAtMillis) / 60_000).toInt().coerceAtLeast(1)
                    repo.finishTimer(minutes, "")
                }
                return
            }
            ReminderScheduler.ACTION_DISCARD_SESSION -> {
                repo.cancelTimer()
                return
            }
            ReminderScheduler.ACTION_BRIEFING -> if (p.briefingEnabled) Notifier.postBriefing(context, data)
            ReminderScheduler.ACTION_NUDGE -> if (p.nudgeEnabled) Notifier.postNudge(context, data)
            ReminderScheduler.ACTION_WEEKLY -> if (p.weeklyReportEnabled) Notifier.postWeekly(context, data)
            // ACTION_CLASS, or no action from alarms armed by older versions.
            else -> if (p.remindersEnabled) postClassReminder(context, intent)
        }
        ReminderScheduler.reschedule(context, repo.current)
    }

    private fun postClassReminder(context: Context, intent: Intent) {
        if (!Notifier.canNotify(context)) return
        val data = (context.applicationContext as AlmaApp).repository.current
        val course = intent.getStringExtra(ReminderScheduler.EXTRA_COURSE_ID)?.let { data.course(it) } ?: return
        val meeting = course.meetings.find { it.id == intent.getStringExtra(ReminderScheduler.EXTRA_MEETING_ID) } ?: return
        val start = intent.getLongExtra(ReminderScheduler.EXTRA_START, System.currentTimeMillis())
        val time = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(start))
        val where = meeting.location.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""
        val notification = Notification.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("${data.codeOf(course)} ${meeting.label} at $time")
            .setContentText("${course.title}$where")
            .setContentIntent(Notifier.openAppIntent(context, course.id))
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(course.id.hashCode(), notification)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val repo = (context.applicationContext as AlmaApp).repository
        ReminderScheduler.reschedule(context, repo.current)
        Notifier.syncSession(context, repo.current)
    }
}
