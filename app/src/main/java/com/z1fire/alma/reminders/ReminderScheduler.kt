package com.z1fire.alma.reminders

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.z1fire.alma.data.AppData
import com.z1fire.alma.data.Course
import com.z1fire.alma.data.Meeting
import com.z1fire.alma.data.meetingsOn
import java.time.LocalDate
import java.time.ZoneId

/** Keeps exactly one alarm armed: the reminder for the next upcoming class meeting. */
object ReminderScheduler {
    const val CHANNEL_ID = "class_reminders"
    const val EXTRA_COURSE_ID = "courseId"
    const val EXTRA_MEETING_ID = "meetingId"
    const val EXTRA_START = "startMillis"

    data class Next(val course: Course, val meeting: Meeting, val startMillis: Long, val triggerMillis: Long)

    fun createChannel(context: Context) {
        val channel = NotificationChannel(CHANNEL_ID, "Class reminders", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "A heads-up before each scheduled study session"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun findNext(data: AppData, nowMillis: Long = System.currentTimeMillis()): Next? {
        val zone = ZoneId.systemDefault()
        val lead = data.profile.reminderLeadMinutes * 60_000L
        val today = LocalDate.now(zone)
        for (offset in 0..14) {
            val date = today.plusDays(offset.toLong())
            val hit = data.meetingsOn(date).firstNotNullOfOrNull { (course, meeting) ->
                val start = date.atStartOfDay(zone).plusMinutes(meeting.startMinute.toLong()).toInstant().toEpochMilli()
                val trigger = start - lead
                if (trigger > nowMillis + 15_000) Next(course, meeting, start, trigger) else null
            }
            if (hit != null) return hit
        }
        return null
    }

    fun canScheduleExact(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

    fun reschedule(context: Context, data: AppData) {
        val am = context.getSystemService(AlarmManager::class.java)
        val base = Intent(context, ReminderReceiver::class.java)
        am.cancel(PendingIntent.getBroadcast(context, 0, base, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        if (!data.profile.remindersEnabled) return
        val next = findNext(data) ?: return

        val intent = Intent(context, ReminderReceiver::class.java)
            .putExtra(EXTRA_COURSE_ID, next.course.id)
            .putExtra(EXTRA_MEETING_ID, next.meeting.id)
            .putExtra(EXTRA_START, next.startMillis)
        val pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        if (canScheduleExact(context)) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.triggerMillis, pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.triggerMillis, pi)
        }
    }
}
