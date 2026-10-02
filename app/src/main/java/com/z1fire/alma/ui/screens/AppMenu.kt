package com.z1fire.alma.ui.screens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.z1fire.alma.reminders.Notifier
import com.z1fire.alma.ui.ConfirmDialog
import com.z1fire.alma.ui.LocalRepository
import com.z1fire.alma.ui.rememberAppData
import com.z1fire.alma.ui.rememberNotificationGate
import com.z1fire.alma.widget.TodayWidgetReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** The ⋮ menu shared by every tab: reminder, certificate name, widget, backup. */
@Composable
fun AppMenu(onMessage: (String) -> Unit) {
    val repo = LocalRepository.current
    val data = rememberAppData()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var open by remember { mutableStateOf(false) }
    var editingName by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf(false) }
    var confirmRestore by remember { mutableStateOf(false) }
    val withNotifications = rememberNotificationGate(onDenied = { onMessage("Notifications are blocked — allow them for Alma in system settings.") })

    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            val ok = withContext(Dispatchers.IO) {
                runCatching { context.contentResolver.openOutputStream(uri)?.use { it.write(repo.exportJson().toByteArray()) } }.isSuccess
            }
            onMessage(if (ok) "Backup saved." else "Couldn't save the backup.")
        }
    }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            val text = withContext(Dispatchers.IO) {
                runCatching { context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } }.getOrNull()
            }
            onMessage(if (text != null && repo.importJson(text)) "Backup restored." else "That file isn't an Alma backup.")
        }
    }

    Box {
        IconButton(onClick = { open = true }) { Icon(Icons.Filled.MoreVert, "Menu") }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(text = { Text("Daily reminder") }, onClick = { open = false; editingReminder = true })
            DropdownMenuItem(text = { Text("Name on certificates") }, onClick = { open = false; editingName = true })
            val widgets = context.getSystemService(AppWidgetManager::class.java)
            if (widgets.isRequestPinAppWidgetSupported) {
                DropdownMenuItem(text = { Text("Add home-screen widget") }, onClick = {
                    open = false
                    widgets.requestPinAppWidget(ComponentName(context, TodayWidgetReceiver::class.java), null, null)
                })
            }
            DropdownMenuItem(text = { Text("Save backup") }, onClick = {
                open = false
                exporter.launch("alma-backup-${LocalDate.now()}.json")
            })
            DropdownMenuItem(text = { Text("Restore backup") }, onClick = { open = false; confirmRestore = true })
        }
    }

    if (editingName) {
        NameDialog(data.profile.name, onDismiss = { editingName = false }, onSave = { n ->
            repo.updateProfile { it.copy(name = n) }
            editingName = false
        })
    }
    if (editingReminder) {
        ReminderDialog(
            enabled = data.profile.reminderEnabled,
            minute = data.profile.reminderMinute,
            onDismiss = { editingReminder = false },
            onSave = { on, minute ->
                editingReminder = false
                val save = { repo.updateProfile { it.copy(reminderEnabled = on, reminderMinute = minute) } }
                if (on) withNotifications(save) else save()
            },
            onTest = { withNotifications { Notifier.postReminder(context, repo.current, force = true) } },
        )
    }
    if (confirmRestore) {
        ConfirmDialog(
            title = "Restore from backup?",
            text = "This replaces everything in the app with the contents of the backup file.",
            confirmLabel = "Choose file",
            onConfirm = { importer.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
            onDismiss = { confirmRestore = false },
        )
    }
}
