package com.z1fire.alma

import android.app.Application
import androidx.glance.appwidget.updateAll
import com.z1fire.alma.data.Repository
import com.z1fire.alma.data.course
import com.z1fire.alma.reminders.Notifier
import com.z1fire.alma.reminders.ReminderScheduler
import com.z1fire.alma.widget.TodayWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File

class AlmaApp : Application() {
    lateinit var repository: Repository
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        repository = Repository(File(filesDir, "curriculum.json"))
        ReminderScheduler.createChannels(this)

        // Re-arm the daily reminder when its settings change.
        appScope.launch {
            repository.state
                .map { Pair(it.profile.reminderEnabled, it.profile.reminderMinute) }
                .distinctUntilChanged()
                .collect { ReminderScheduler.reschedule(this@AlmaApp, repository.current) }
        }

        // Show or clear the ongoing timer notification as study sessions start and stop.
        appScope.launch {
            repository.state
                .map { d -> Triple(d.activeTimer, d.profile.sessionNotificationEnabled, d.activeTimer?.let { d.course(it.courseId)?.title }) }
                .distinctUntilChanged()
                .collect { Notifier.syncSession(this@AlmaApp, repository.current) }
        }

        // Keep the home-screen widget in step with courses and logged time.
        appScope.launch {
            repository.state
                .map { Pair(it.courses, it.profile.weeklyGoalMinutes) }
                .distinctUntilChanged()
                .collect { TodayWidget().updateAll(this@AlmaApp) }
        }
    }
}
