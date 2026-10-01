package com.z1fire.alma.reminders

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.z1fire.alma.AlmaApp
import com.z1fire.alma.MainActivity
import com.z1fire.alma.R
import com.z1fire.alma.data.codeOf
import com.z1fire.alma.data.course
import java.text.DateFormat
import java.util.Date

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val repo = (context.applicationContext as AlmaApp).repository
        val data = repo.current
        val courseId = intent.getStringExtra(ReminderScheduler.EXTRA_COURSE_ID)
        val course = courseId?.let { data.course(it) }
        val meeting = course?.meetings?.find { it.id == intent.getStringExtra(ReminderScheduler.EXTRA_MEETING_ID) }

        if (course != null && meeting != null && data.profile.remindersEnabled && canNotify(context)) {
            val start = intent.getLongExtra(ReminderScheduler.EXTRA_START, System.currentTimeMillis())
            val time = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(start))
            val open = PendingIntent.getActivity(
                context,
                course.id.hashCode(),
                Intent(context, MainActivity::class.java)
                    .putExtra(MainActivity.EXTRA_COURSE_ID, course.id)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val where = meeting.location.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""
            val notification = Notification.Builder(context, ReminderScheduler.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("${data.codeOf(course)} ${meeting.label} at $time")
                .setContentText("${course.title}$where")
                .setContentIntent(open)
                .setAutoCancel(true)
                .build()
            context.getSystemService(NotificationManager::class.java).notify(course.id.hashCode(), notification)
        }

        ReminderScheduler.reschedule(context, data)
    }

    private fun canNotify(context: Context) =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val repo = (context.applicationContext as AlmaApp).repository
        ReminderScheduler.reschedule(context, repo.current)
    }
}
