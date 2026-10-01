package com.z1fire.alma.ui.screens

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import com.z1fire.alma.data.Department
import com.z1fire.alma.reminders.ReminderScheduler
import com.z1fire.alma.ui.ColorDot
import com.z1fire.alma.ui.ConfirmDialog
import com.z1fire.alma.ui.LocalRepository
import com.z1fire.alma.ui.SectionTitle
import com.z1fire.alma.ui.formatMinutes
import com.z1fire.alma.ui.rememberAppData
import com.z1fire.alma.ui.theme.deptColor
import com.z1fire.alma.widget.TodayWidgetReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val repo = LocalRepository.current
    val data = rememberAppData()
    val profile = data.profile
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var editDept by remember { mutableStateOf<Department?>(null) }
    var addingDept by remember { mutableStateOf(false) }
    var deletingDept by remember { mutableStateOf<Department?>(null) }
    var confirmImport by remember { mutableStateOf(false) }

    val notifPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        repo.updateProfile { it.copy(remindersEnabled = granted) }
        if (!granted) scope.launch { snackbar.showSnackbar("Notifications are blocked — enable them in system settings.") }
    }
    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            val ok = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { it.write(repo.exportJson().toByteArray()) }
                }.isSuccess
            }
            snackbar.showSnackbar(if (ok) "Records exported." else "Export failed.")
        }
    }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            val text = withContext(Dispatchers.IO) {
                runCatching { context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } }.getOrNull()
            }
            val ok = text != null && repo.importJson(text)
            snackbar.showSnackbar(if (ok) "Records restored." else "That file isn't an Alma backup.")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Registrar's Office") },
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
            SectionTitle("Student & institution")
            OutlinedTextField(profile.studentName, { v -> repo.updateProfile { it.copy(studentName = v) } }, label = { Text("Your name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(profile.institution, { v -> repo.updateProfile { it.copy(institution = v) } }, label = { Text("Institution name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(profile.motto, { v -> repo.updateProfile { it.copy(motto = v) } }, label = { Text("Motto") }, singleLine = true, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(4.dp))
            SectionTitle("Weekly study goal")
            Text(formatMinutes(profile.weeklyGoalMinutes) + " per week", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = profile.weeklyGoalMinutes / 60f,
                onValueChange = { h -> repo.updateProfile { it.copy(weeklyGoalMinutes = (h * 60).toInt()) } },
                valueRange = 1f..40f,
                steps = 38,
            )
            Text(
                "A full-time college load is roughly 2–3 hours of study per credit per week.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(4.dp))
            SectionTitle("Class reminders")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Notify me before class meetings", style = MaterialTheme.typography.bodyLarge)
                    Text("For enrolled courses with scheduled meetings", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = profile.remindersEnabled,
                    onCheckedChange = { on ->
                        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                        if (on && needsPermission) {
                            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            repo.updateProfile { it.copy(remindersEnabled = on) }
                        }
                    },
                )
            }
            if (profile.remindersEnabled) {
                Text("Heads-up time", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0, 5, 10, 15, 30, 60).forEach { m ->
                        FilterChip(
                            selected = profile.reminderLeadMinutes == m,
                            onClick = { repo.updateProfile { it.copy(reminderLeadMinutes = m) } },
                            label = { Text(if (m == 0) "At start" else "${formatMinutes(m)} before") },
                        )
                    }
                }
                if (!ReminderScheduler.canScheduleExact(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    TextButton(onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}")),
                        )
                    }) { Text("Allow on-time reminders (otherwise they may arrive a few minutes late)") }
                }
                ReminderScheduler.findNext(data)?.let { next ->
                    Text(
                        "Next reminder: ${next.course.title} — ${next.meeting.label}, " +
                            java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT)
                                .format(java.util.Date(next.triggerMillis)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            val widgets = context.getSystemService(AppWidgetManager::class.java)
            if (widgets.isRequestPinAppWidgetSupported) {
                Spacer(Modifier.height(4.dp))
                SectionTitle("Home screen widget")
                Text(
                    "Show today's classes on your home screen — tap a class to jump into its course.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(onClick = {
                    widgets.requestPinAppWidget(ComponentName(context, TodayWidgetReceiver::class.java), null, null)
                }) { Text("Add \"Today's classes\" widget") }
            }

            Spacer(Modifier.height(4.dp))
            SectionTitle("Departments", action = {
                TextButton(onClick = { addingDept = true }) { Icon(Icons.Filled.Add, null); Text("Found") }
            })
            if (data.departments.isEmpty()) {
                Text("No departments yet. Courses without one are listed under General Studies.", style = MaterialTheme.typography.bodySmall)
            }
            data.departments.sortedBy { it.name }.forEach { d ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ColorDot(deptColor(d.colorIndex), 14)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(d.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${d.code} · ${data.courses.count { it.departmentId == d.id }} courses",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { editDept = d }) { Icon(Icons.Filled.Edit, "Edit department") }
                    IconButton(onClick = { deletingDept = d }) { Icon(Icons.Filled.Delete, "Delete department") }
                }
            }

            Spacer(Modifier.height(4.dp))
            SectionTitle("Records")
            Text(
                "Everything stays on this phone. Export a backup file to keep your records safe or move them to a new device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { exporter.launch("alma-records-${LocalDate.now()}.json") }) { Text("Export backup") }
                OutlinedButton(onClick = { confirmImport = true }) { Text("Restore backup") }
            }
            OutlinedButton(onClick = {
                repo.loadSample()
                scope.launch { snackbar.showSnackbar("Added PHIL 110 & PHIL 210 and a Critical Thinking certificate.") }
            }) { Text("Add sample logic courses") }
            Spacer(Modifier.height(24.dp))
            Text(
                "Alma · your personal university",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (addingDept || editDept != null) {
        DepartmentDialog(
            initial = editDept,
            onDismiss = {
                addingDept = false
                editDept = null
            },
            onSave = {
                repo.saveDepartment(it)
                addingDept = false
                editDept = null
            },
        )
    }
    deletingDept?.let { d ->
        ConfirmDialog(
            title = "Close the ${d.name} department?",
            text = "Its courses stay in your catalog and move to General Studies.",
            confirmLabel = "Close department",
            onConfirm = { repo.deleteDepartment(d.id) },
            onDismiss = { deletingDept = null },
        )
    }
    if (confirmImport) {
        ConfirmDialog(
            title = "Restore from backup?",
            text = "This replaces all current courses, departments and records with the contents of the backup file.",
            confirmLabel = "Choose file",
            onConfirm = { importer.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
            onDismiss = { confirmImport = false },
        )
    }
}
