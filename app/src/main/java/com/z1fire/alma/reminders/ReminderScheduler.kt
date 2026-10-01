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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

/**
 * Keeps one alarm armed per notification kind: the next class reminder, the next morning briefing,
 * the next study nudge and the next weekly report. Each fire posts (if still relevant) and re-arms.
 */
object ReminderScheduler {
    const val CHANNEL_ID = "class_reminders"
    const val CHANNEL_DAILY = "daily_digest"
    const val CHANNEL_WEEKLY = "weekly_report"
    const val CHANNEL_SESSION = "study_session"

    const val ACTION_CLASS = "com.z1fire.alma.action.CLASS"
    const val ACTION_BRIEFING = "com.z1fire.alma.action.BRIEFING"
    const val ACTION_NUDGE = "com.z1fire.alma.action.NUDGE"
    const val ACTION_WEEKLY = "com.z1fire.alma.action.WEEKLY"
    const val ACTION_END_SESSION = "com.z1fire.alma.action.END_SESSION"
    const val ACTION_DISCARD_SESSION = "com.z1fire.alma.action.DISCARD_SESSION"

    const val EXTRA_COURSE_ID = "courseId"
    const val EXTRA_MEETING_ID = "meetingId"
    const val EXTRA_START = "startMillis"

    /** Weekly report goes out Sunday at 6 PM. */
    private val WEEKLY_DAY = DayOfWeek.SUNDAY
    private const val WEEKLY_MINUTE = 18 * 60

    data class Next(val course: Course, val meeting: Meeting, val startMillis: Long, val triggerMillis: Long)

    fun createChannels(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannels(
            listOf(
                NotificationChannel(CHANNEL_ID, "Class reminders", NotificationManager.IMPORTANCE_HIGH)
                    .apply { description = "A heads-up before each scheduled class meeting" },
                NotificationChannel(CHANNEL_DAILY, "Daily briefing & study nudges", NotificationManager.IMPORTANCE_DEFAULT)
                    .apply { description = "Morning summary of classes and due work; evening reminder if you haven't studied" },
                NotificationChannel(CHANNEL_WEEKLY, "Weekly report", NotificationManager.IMPORTANCE_LOW)
                    .apply { description = "Sunday summary of your study week" },
                NotificationChannel(CHANNEL_SESSION, "Study session timer", NotificationManager.IMPORTANCE_LOW)
                    .apply { description = "Shows the running timer during a study session" },
            ),
        )
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

    /** Next time-of-day occurrence strictly in the future. */
    fun nextDaily(minuteOfDay: Int, now: ZonedDateTime = ZonedDateTime.now()): ZonedDateTime {
        val today = now.toLocalDate().atStartOfDay(now.zone).plusMinutes(minuteOfDay.toLong())
        return if (today.isAfter(now.plusSeconds(15))) today else today.plusDays(1)
    }

    fun nextWeekly(now: ZonedDateTime = ZonedDateTime.now()): ZonedDateTime {
        val candidate = now.toLocalDate().with(TemporalAdjusters.nextOrSame(WEEKLY_DAY))
            .atStartOfDay(now.zone).plusMinutes(WEEKLY_MINUTE.toLong())
        return if (candidate.isAfter(now.plusSeconds(15))) candidate else candidate.plusWeeks(1)
    }

    fun reschedule(context: Context, data: AppData) {
        val p = data.profile

        // Class reminder. Older versions armed this alarm without an action; cancel that too.
        cancel(context, null, 0)
        val next = if (p.remindersEnabled) findNext(data) else null
        if (next == null) {
            cancel(context, ACTION_CLASS, 0)
        } else {
            val intent = intent(context, ACTION_CLASS)
                .putExtra(EXTRA_COURSE_ID, next.course.id)
                .putExtra(EXTRA_MEETING_ID, next.meeting.id)
                .putExtra(EXTRA_START, next.startMillis)
            arm(context, next.triggerMillis, pending(context, intent, 0), exact = true)
        }

        schedule(context, ACTION_BRIEFING, 1, p.briefingEnabled) { nextDaily(p.briefingMinute) }
        schedule(context, ACTION_NUDGE, 2, p.nudgeEnabled) { nextDaily(p.nudgeMinute) }
        schedule(context, ACTION_WEEKLY, 3, p.weeklyReportEnabled) { nextWeekly() }
    }

    private fun schedule(context: Context, action: String, code: Int, enabled: Boolean, at: () -> ZonedDateTime) {
        if (!enabled) {
            cancel(context, action, code)
            return
        }
        val pi = pending(context, intent(context, action), code)
        arm(context, at().toInstant().toEpochMilli(), pi, exact = false)
    }

    private fun intent(context: Context, action: String?) =
        Intent(context, ReminderReceiver::class.java).also { if (action != null) it.action = action }

    private fun pending(context: Context, intent: Intent, code: Int): PendingIntent =
        PendingIntent.getBroadcast(context, code, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    private fun cancel(context: Context, action: String?, code: Int) {
        context.getSystemService(AlarmManager::class.java).cancel(pending(context, intent(context, action), code))
    }

    /**
     * Arms an alarm. Plain inexact alarms can drift by up to an hour, which would make a class
     * heads-up useless, so without exact-alarm access we use a bounded window instead: centered on
     * the time for class reminders (±5 min), starting at the time for everything else.
     */
    private fun arm(context: Context, atMillis: Long, pi: PendingIntent, exact: Boolean) {
        val am = context.getSystemService(AlarmManager::class.java)
        when {
            exact && canScheduleExact(context) -> am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pi)
            exact -> am.setWindow(AlarmManager.RTC_WAKEUP, atMillis - WINDOW_MS / 2, WINDOW_MS, pi)
            else -> am.setWindow(AlarmManager.RTC_WAKEUP, atMillis, WINDOW_MS, pi)
        }
    }

    /** The shortest window Android 12+ honors. */
    private const val WINDOW_MS = 10 * 60_000L
}
