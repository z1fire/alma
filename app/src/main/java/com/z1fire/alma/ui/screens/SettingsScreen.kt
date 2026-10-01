package com.z1fire.alma.ui.screens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.z1fire.alma.data.Profile
import com.z1fire.alma.reminders.Notifier
import com.z1fire.alma.ui.ConfirmDialog
import com.z1fire.alma.ui.LocalRepository
import com.z1fire.alma.ui.SectionTitle
import com.z1fire.alma.ui.TimeField
import com.z1fire.alma.ui.formatMinutes
import com.z1fire.alma.ui.rememberAppData
import com.z1fire.alma.ui.rememberNotificationGate
import com.z1fire.alma.widget.TodayWidgetReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val repo = LocalRepository.current
    val data = rememberAppData()
    val profile = data.profile
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var confirmImport by remember { mutableStateOf(false) }

    val withNotifications = rememberNotificationGate(onDenied = {
        scope.launch { snackbar.showSnackbar("Notifications are blocked — allow them for Alma in system settings.") }
    })
    fun toggle(on: Boolean, set: (Profile, Boolean) -> Profile) {
        if (on) withNotifications { repo.updateProfile { set(it, true) } } else repo.updateProfile { set(it, false) }
    }

    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            val ok = withContext(Dispatchers.IO) {
                runCatching { context.contentResolver.openOutputStream(uri)?.use { it.write(repo.exportJson().toByteArray()) } }.isSuccess
            }
            snackbar.showSnackbar(if (ok) "Backup saved." else "Export failed.")
        }
    }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            val text = withContext(Dispatchers.IO) {
                runCatching { context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } }.getOrNull()
            }
            val ok = text != null && repo.importJson(text)
            snackbar.showSnackbar(if (ok) "Backup restored." else "That file isn't an Alma backup.")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionTitle("You")
            OutlinedTextField(
                profile.name,
                { v -> repo.updateProfile { it.copy(name = v) } },
                label = { Text("Your name (for certificates)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(4.dp))
            SectionTitle("Weekly goal")
            Text(formatMinutes(profile.weeklyGoalMinutes) + " a week", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = profile.weeklyGoalMinutes / 60f,
                onValueChange = { h -> repo.updateProfile { it.copy(weeklyGoalMinutes = (h * 2).roundToInt() * 30) } },
                valueRange = 1f..40f,
            )

            Spacer(Modifier.height(4.dp))
            SectionTitle("Notifications")
            Toggle(
                "Daily study reminder",
                "Only on days you haven't studied yet",
                profile.reminderEnabled,
            ) { on -> toggle(on) { p, v -> p.copy(reminderEnabled = v) } }
            if (profile.reminderEnabled) {
                TimeField("Reminder time", profile.reminderMinute, { m -> repo.updateProfile { it.copy(reminderMinute = m) } })
            }
            Toggle(
                "Timer notification",
                "Show the running timer with Stop & log while you study",
                profile.sessionNotificationEnabled,
            ) { on -> toggle(on) { p, v -> p.copy(sessionNotificationEnabled = v) } }
            OutlinedButton(onClick = { withNotifications { Notifier.postReminder(context, data, force = true) } }) {
                Text("Send a test reminder")
            }

            val widgets = context.getSystemService(AppWidgetManager::class.java)
            if (widgets.isRequestPinAppWidgetSupported) {
                Spacer(Modifier.height(4.dp))
                SectionTitle("Home screen widget")
                Text(
                    "This week's study time and what you're working on.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(onClick = {
                    widgets.requestPinAppWidget(ComponentName(context, TodayWidgetReceiver::class.java), null, null)
                }) { Text("Add widget") }
            }

            Spacer(Modifier.height(4.dp))
            SectionTitle("Backup")
            Text(
                "Everything stays on this phone. Save a backup file to keep it safe or move to a new device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { exporter.launch("alma-backup-${LocalDate.now()}.json") }) { Text("Save backup") }
                OutlinedButton(onClick = { confirmImport = true }) { Text("Restore") }
            }
        }
    }

    if (confirmImport) {
        ConfirmDialog(
            title = "Restore from backup?",
            text = "This replaces everything in the app with the contents of the backup file.",
            confirmLabel = "Choose file",
            onConfirm = { importer.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
            onDismiss = { confirmImport = false },
        )
    }
}

@Composable
private fun Toggle(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
