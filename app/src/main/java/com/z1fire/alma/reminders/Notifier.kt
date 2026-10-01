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
import com.z1fire.alma.data.CourseStatus
import com.z1fire.alma.data.assignmentsDueOn
import com.z1fire.alma.data.codeOf
import com.z1fire.alma.data.course
import com.z1fire.alma.data.meetingsOn
import com.z1fire.alma.data.minutesBetween
import com.z1fire.alma.data.streak
import com.z1fire.alma.data.weekMinutes
import com.z1fire.alma.data.weekStart
import com.z1fire.alma.ui.formatMinutes
import com.z1fire.alma.ui.formatTime
import java.time.LocalDate

/** A notification's words, built from the student's data; null means "nothing worth saying". */
data class Digest(val title: String, val text: String, val lines: List<String> = emptyList(), val courseId: String? = null)

/** Builds and posts every kind of Alma notification. */
object Notifier {
    private const val ID_BRIEFING = 1001
    private const val ID_NUDGE = 1002
    private const val ID_WEEKLY = 1003
    private const val ID_SESSION = 1004

    fun canNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    // ---------------- content ----------------

    fun briefing(data: AppData, today: LocalDate): Digest? {
        val classes = data.meetingsOn(today)
        val active = data.courses.filter { it.status == CourseStatus.ENROLLED }
        val dueToday = data.assignmentsDueOn(today).filter { !it.second.done && it.first.status == CourseStatus.ENROLLED }
        val overdue = active.flatMap { c -> c.assignments.filter { !it.done && (it.dueEpochDay ?: Long.MAX_VALUE) < today.toEpochDay() }.map { c to it } }
        val tomorrow = data.assignmentsDueOn(today.plusDays(1)).filter { !it.second.done && it.first.status == CourseStatus.ENROLLED }
        if (classes.isEmpty() && dueToday.isEmpty() && overdue.isEmpty() && tomorrow.isEmpty()) return null

        val summary = listOfNotNull(
            classes.size.takeIf { it > 0 }?.let { "$it class${if (it == 1) "" else "es"}" },
            dueToday.size.takeIf { it > 0 }?.let { "$it due today" },
            overdue.size.takeIf { it > 0 }?.let { "$it overdue" },
        ).ifEmpty { listOf("${tomorrow.size} due tomorrow") }.joinToString(" · ")

        val lines = classes.map { (c, m) -> "${formatTime(m.startMinute)}  ${data.codeOf(c)} ${m.label}" } +
            dueToday.map { (c, a) -> "Due today: ${a.title} (${data.codeOf(c)})" } +
            overdue.map { (c, a) -> "Overdue: ${a.title} (${data.codeOf(c)})" } +
            tomorrow.map { (c, a) -> "Tomorrow: ${a.title} (${data.codeOf(c)})" }
        val greeting = when (java.time.LocalTime.now().hour) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
        return Digest("$greeting — $summary", lines.first(), lines)
    }

    fun nudge(data: AppData, today: LocalDate): Digest? {
        val enrolled = data.courses.filter { it.status == CourseStatus.ENROLLED }
        if (enrolled.isEmpty() || data.minutesBetween(today, today) > 0) return null
        // Suggest the course that's gone longest without attention.
        val pick = enrolled.minByOrNull { c -> c.sessions.maxOfOrNull { it.epochDay } ?: Long.MIN_VALUE }!!
        val streak = data.streak(today)
        val title = if (streak > 0) "Keep your $streak-day streak alive" else "Class is in session"
        return Digest(title, "No study logged today. Even 20 minutes of ${data.codeOf(pick)} counts.", courseId = pick.id)
    }

    fun weekly(data: AppData, today: LocalDate): Digest? {
        val minutes = data.weekMinutes(today)
        val enrolled = data.courses.filter { it.status == CourseStatus.ENROLLED }
        if (enrolled.isEmpty() && minutes == 0) return null
        val goal = data.profile.weeklyGoalMinutes.coerceAtLeast(1)
        val start = weekStart(today)
        val sessions = data.courses.sumOf { c -> c.sessions.count { it.epochDay in start.toEpochDay()..today.toEpochDay() } }
        val nextWeek = (1..7L).sumOf { data.assignmentsDueOn(today.plusDays(it)).count { (c, a) -> !a.done && c.status == CourseStatus.ENROLLED } }
        val pct = minutes * 100 / goal
        val lines = listOf(
            "Studied ${formatMinutes(minutes)} of your ${formatMinutes(goal)} goal ($pct%)",
            "$sessions session${if (sessions == 1) "" else "s"} · ${data.streak(today)}-day streak",
            "${enrolled.size} course${if (enrolled.size == 1) "" else "s"} in progress",
            "$nextWeek assignment${if (nextWeek == 1) "" else "s"} due in the coming week",
        )
        val verdict = when {
            pct >= 100 -> "Goal met — Dean's List energy"
            pct >= 60 -> "Solid week"
            else -> "A fresh week starts tomorrow"
        }
        return Digest("Weekly report: $verdict", lines.first(), lines)
    }

    // ---------------- posting ----------------

    fun postBriefing(context: Context, data: AppData, today: LocalDate = LocalDate.now()): Boolean =
        briefing(data, today)?.let { post(context, ReminderScheduler.CHANNEL_DAILY, ID_BRIEFING, it) } != null

    fun postNudge(context: Context, data: AppData, today: LocalDate = LocalDate.now()): Boolean =
        nudge(data, today)?.let { post(context, ReminderScheduler.CHANNEL_DAILY, ID_NUDGE, it) } != null

    fun postWeekly(context: Context, data: AppData, today: LocalDate = LocalDate.now()): Boolean =
        weekly(data, today)?.let { post(context, ReminderScheduler.CHANNEL_WEEKLY, ID_WEEKLY, it) } != null

    /** Settings' "send a test": today's briefing if there is one, otherwise a simple hello. */
    fun postTest(context: Context, data: AppData) {
        if (!postBriefing(context, data)) {
            post(context, ReminderScheduler.CHANNEL_DAILY, ID_BRIEFING, Digest("Notifications are on", "Alma will keep you posted on classes and coursework."))
        }
    }

    fun openAppIntent(context: Context, courseId: String?): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        if (courseId != null) intent.putExtra(MainActivity.EXTRA_COURSE_ID, courseId)
        return PendingIntent.getActivity(
            context,
            courseId?.hashCode() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    fun post(context: Context, channel: String, id: Int, d: Digest) {
        if (!canNotify(context)) return
        val builder = Notification.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(d.title)
            .setContentText(d.text)
            .setContentIntent(openAppIntent(context, d.courseId))
            .setAutoCancel(true)
        if (d.lines.size > 1) {
            builder.setStyle(Notification.InboxStyle().also { s -> d.lines.take(7).forEach { s.addLine(it) } })
        } else {
            builder.setStyle(Notification.BigTextStyle().bigText(d.text))
        }
        context.getSystemService(NotificationManager::class.java).notify(id, builder.build())
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
            .setContentTitle("In session · ${data.codeOf(course)}")
            .setContentText(course.title)
            .setWhen(timer.startedAtMillis)
            .setShowWhen(true)
            .setUsesChronometer(true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(Notification.CATEGORY_STOPWATCH)
            .setContentIntent(openAppIntent(context, course.id))
            .addAction(action("End & log", ReminderScheduler.ACTION_END_SESSION, 10))
            .addAction(action("Discard", ReminderScheduler.ACTION_DISCARD_SESSION, 11))
            .build()
        nm.notify(ID_SESSION, notification)
    }
}
