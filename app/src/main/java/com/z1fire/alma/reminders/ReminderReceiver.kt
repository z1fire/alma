package com.z1fire.alma.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.z1fire.alma.AlmaApp

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val repo = (context.applicationContext as AlmaApp).repository
        val data = repo.current
        when (intent.action) {
            ReminderScheduler.ACTION_END_SESSION -> data.activeTimer?.let { t ->
                val minutes = ((System.currentTimeMillis() - t.startedAtMillis) / 60_000).toInt().coerceAtLeast(1)
                repo.finishTimer(minutes, "")
            }
            ReminderScheduler.ACTION_DISCARD_SESSION -> repo.cancelTimer()
            ReminderScheduler.ACTION_REMINDER -> {
                if (data.profile.reminderEnabled) Notifier.postReminder(context, data)
                ReminderScheduler.reschedule(context, data)
            }
            // Alarms left over from v1: just re-plan.
            else -> ReminderScheduler.reschedule(context, data)
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val repo = (context.applicationContext as AlmaApp).repository
        ReminderScheduler.reschedule(context, repo.current)
        Notifier.syncSession(context, repo.current)
    }
}
