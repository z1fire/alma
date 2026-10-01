package com.z1fire.alma.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.z1fire.alma.AlmaApp

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val data = (context.applicationContext as AlmaApp).repository.current
        // Older versions posted other actions here (timer buttons, digests); those just re-plan.
        if (intent.action == ReminderScheduler.ACTION_REMINDER && data.profile.reminderEnabled) {
            Notifier.postReminder(context, data)
        }
        ReminderScheduler.reschedule(context, data)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ReminderScheduler.reschedule(context, (context.applicationContext as AlmaApp).repository.current)
    }
}
