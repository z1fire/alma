package com.z1fire.alma.ui.screens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.z1fire.alma.data.Course
import com.z1fire.alma.reminders.Notifier
import com.z1fire.alma.ui.ConfirmDialog
import com.z1fire.alma.ui.EmptyState
import com.z1fire.alma.ui.LocalRepository
import com.z1fire.alma.ui.SectionTitle
import com.z1fire.alma.ui.formatDate
import com.z1fire.alma.ui.formatMinutes
import com.z1fire.alma.ui.rememberAppData
import com.z1fire.alma.ui.rememberNotificationGate
import com.z1fire.alma.ui.theme.deptColor
import com.z1fire.alma.widget.TodayWidgetReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** The whole app on one screen: what you're studying, and what you've finished. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onOpenCourse: (String) -> Unit) {
    val repo = LocalRepository.current
    val data = rememberAppData()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var creating by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var editingName by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf(false) }
    var confirmRestore by remember { mutableStateOf(false) }

    fun say(message: String) = scope.launch { snackbar.showSnackbar(message) }
    val withNotifications = rememberNotificationGate(onDenied = { say("Notifications are blocked — allow them for Alma in system settings.") })

    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            val ok = withContext(Dispatchers.IO) {
                runCatching { context.contentResolver.openOutputStream(uri)?.use { it.write(repo.exportJson().toByteArray()) } }.isSuccess
            }
            snackbar.showSnackbar(if (ok) "Backup saved." else "Couldn't save the backup.")
        }
    }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            val text = withContext(Dispatchers.IO) {
                runCatching { context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } }.getOrNull()
            }
            snackbar.showSnackbar(if (text != null && repo.importJson(text)) "Backup restored." else "That file isn't an Alma backup.")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Studying") },
                actions = {
                    Box {
                        IconButton(onClick = { menuOpen = true }) { Icon(Icons.Filled.MoreVert, "Menu") }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(text = { Text("Daily reminder") }, onClick = { menuOpen = false; editingReminder = true })
                            DropdownMenuItem(text = { Text("Name on certificates") }, onClick = { menuOpen = false; editingName = true })
                            val widgets = context.getSystemService(AppWidgetManager::class.java)
                            if (widgets.isRequestPinAppWidgetSupported) {
                                DropdownMenuItem(text = { Text("Add home-screen widget") }, onClick = {
                                    menuOpen = false
                                    widgets.requestPinAppWidget(ComponentName(context, TodayWidgetReceiver::class.java), null, null)
                                })
                            }
                            DropdownMenuItem(text = { Text("Save backup") }, onClick = {
                                menuOpen = false
                                exporter.launch("alma-backup-${LocalDate.now()}.json")
                            })
                            DropdownMenuItem(text = { Text("Restore backup") }, onClick = { menuOpen = false; confirmRestore = true })
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { creating = true }, icon = { Icon(Icons.Filled.Add, null) }, text = { Text("New course") })
        },
    ) { padding ->
        if (data.courses.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.AutoStories,
                title = "What do you want to learn?",
                message = "Add a course, list what to study, and keep track of your time.",
                modifier = Modifier.padding(padding),
                actionLabel = "Add your first course",
                onAction = { creating = true },
            )
            return@Scaffold
        }
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (data.studying.isEmpty()) {
                item {
                    Text(
                        "Nothing in progress — add a new course.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(data.studying, key = { it.id }) { c -> CourseCard(c, onClick = { onOpenCourse(c.id) }) }

            if (data.done.isNotEmpty()) {
                item { SectionTitle("Done · ${data.done.size}", Modifier.padding(top = 16.dp)) }
                items(data.done, key = { it.id }) { c -> DoneRow(c, onClick = { onOpenCourse(c.id) }) }
            }
        }
    }

    if (creating) {
        CourseDialog(null, onDismiss = { creating = false }, onSave = { c ->
            repo.saveCourse(c)
            creating = false
            onOpenCourse(c.id)
        })
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

@Composable
private fun CourseCard(course: Course, onClick: () -> Unit) {
    val accent = deptColor(course.colorIndex)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.width(6.dp).fillMaxHeight().background(accent))
            Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp).weight(1f)) {
                Text(course.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val meta = listOfNotNull(
                    course.subject.ifBlank { null },
                    if (course.minutes > 0) "${formatMinutes(course.minutes)} studied" else "not started",
                ).joinToString(" · ")
                Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (course.items.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { course.progress },
                            modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = accent,
                            trackColor = accent.copy(alpha = 0.18f),
                            drawStopIndicator = {},
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("${course.doneCount}/${course.items.size}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun DoneRow(course: Course, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CheckCircle, null, tint = deptColor(course.colorIndex))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(course.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    listOfNotNull(
                        course.finishedEpochDay?.takeIf { it > 0 }?.let { "Finished ${formatDate(it)}" },
                        course.minutes.takeIf { it > 0 }?.let { formatMinutes(it) },
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
