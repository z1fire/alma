package com.z1fire.alma.ui.screens

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
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.z1fire.alma.data.AppData
import com.z1fire.alma.data.Course
import com.z1fire.alma.data.CourseStatus
import com.z1fire.alma.data.active
import com.z1fire.alma.data.course
import com.z1fire.alma.data.streak
import com.z1fire.alma.data.upNext
import com.z1fire.alma.data.weekMinutes
import com.z1fire.alma.ui.EmptyState
import com.z1fire.alma.ui.LocalRepository
import com.z1fire.alma.ui.SectionTitle
import com.z1fire.alma.ui.formatMinutes
import com.z1fire.alma.ui.rememberAppData
import com.z1fire.alma.ui.rememberNotificationGate
import com.z1fire.alma.ui.theme.deptColor
import kotlinx.coroutines.delay
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onOpenCourse: (String) -> Unit, onOpenSettings: () -> Unit) {
    val repo = LocalRepository.current
    val data = rememberAppData()
    val today = LocalDate.now()
    var creating by remember { mutableStateOf(false) }
    val withNotifications = rememberNotificationGate(onDenied = { repo.updateProfile { it.copy(reminderPrompted = true) } })
    val offerReminder = !data.profile.reminderPrompted && !data.profile.reminderEnabled && data.active.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Studying") },
                actions = { IconButton(onClick = onOpenSettings) { Icon(Icons.Filled.Settings, "Settings") } },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { creating = true }, icon = { Icon(Icons.Filled.Add, null) }, text = { Text("New course") })
        },
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { WeekCard(data, today) }
            data.activeTimer?.let { t ->
                data.course(t.courseId)?.let { c -> item { TimerCard(c.title, t.startedAtMillis) } }
            }
            if (offerReminder) {
                item {
                    ReminderOfferCard(
                        onAccept = { withNotifications { repo.updateProfile { it.copy(reminderEnabled = true, reminderPrompted = true) } } },
                        onDecline = { repo.updateProfile { it.copy(reminderPrompted = true) } },
                    )
                }
            }

            if (data.courses.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.AutoStories,
                        title = "What do you want to learn?",
                        message = "Add a course, list what to study, and track your time. Pick a starter idea or make your own.",
                        actionLabel = "Add your first course",
                        onAction = { creating = true },
                    )
                }
                return@LazyColumn
            }

            item { SectionTitle("Studying now", Modifier.padding(top = 4.dp)) }
            if (data.active.isEmpty()) {
                item {
                    Text(
                        "Nothing in progress. Start something from Up next, or add a new course.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(data.active, key = { it.id }) { c ->
                ActiveCourseCard(
                    course = c,
                    today = today,
                    canStart = data.activeTimer == null,
                    onClick = { onOpenCourse(c.id) },
                    onStart = { repo.startTimer(c.id) },
                )
            }

            if (data.upNext.isNotEmpty()) {
                item { SectionTitle("Up next", Modifier.padding(top = 8.dp)) }
                items(data.upNext, key = { it.id }) { c ->
                    Card(
                        onClick = { onOpenCourse(c.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    ) {
                        Row(Modifier.padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.width(10.dp).height(10.dp).clip(RoundedCornerShape(5.dp)).background(deptColor(c.colorIndex)))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(c.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (c.subject.isNotBlank()) {
                                    Text(c.subject, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            TextButton(onClick = { repo.setStatus(c.id, CourseStatus.ACTIVE) }) { Text("Start") }
                        }
                    }
                }
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
}

@Composable
private fun WeekCard(data: AppData, today: LocalDate) {
    val minutes = data.weekMinutes(today)
    val goal = data.profile.weeklyGoalMinutes.coerceAtLeast(1)
    val streak = data.streak(today)
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text("THIS WEEK", style = MaterialTheme.typography.labelLarge)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(formatMinutes(minutes), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "  of ${formatMinutes(goal)}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 6.dp).weight(1f),
                )
                if (streak > 0) {
                    Icon(Icons.Filled.LocalFireDepartment, null, tint = MaterialTheme.colorScheme.tertiaryContainer)
                    Text(
                        " $streak day${if (streak == 1) "" else "s"}",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (minutes.toFloat() / goal).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                drawStopIndicator = {},
            )
        }
    }
}

@Composable
private fun ActiveCourseCard(course: Course, today: LocalDate, canStart: Boolean, onClick: () -> Unit, onStart: () -> Unit) {
    val accent = deptColor(course.colorIndex)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(Modifier.height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(6.dp).fillMaxHeight().background(accent))
            Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp).weight(1f)) {
                Text(course.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val week = course.weekMinutes(today)
                val meta = listOfNotNull(
                    course.subject.ifBlank { null },
                    if (week > 0) "${formatMinutes(week)} this week" else "not studied this week",
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
            FilledTonalIconButton(onClick = onStart, enabled = canStart, modifier = Modifier.padding(end = 10.dp)) {
                Icon(Icons.Filled.PlayArrow, "Start studying ${course.title}")
            }
        }
    }
}

@Composable
private fun TimerCard(title: String, startedAt: Long) {
    val repo = LocalRepository.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var stopping by remember { mutableStateOf(false) }
    LaunchedEffect(startedAt) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    val elapsed = ((now - startedAt) / 1000).coerceAtLeast(0)
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Timer, null)
                Spacer(Modifier.width(8.dp))
                Text("Studying · $title", style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(
                "%d:%02d:%02d".format(elapsed / 3600, (elapsed % 3600) / 60, elapsed % 60),
                style = MaterialTheme.typography.displaySmall,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { stopping = true }) { Text("Stop & log") }
                TextButton(onClick = { repo.cancelTimer() }) { Text("Discard") }
            }
        }
    }
    if (stopping) {
        LogTimeDialog(
            title = "Log this session",
            initialMinutes = (elapsed / 60).toInt().coerceAtLeast(1),
            showDate = false,
            onDismiss = { stopping = false },
            onSave = { _, minutes, notes ->
                repo.finishTimer(minutes, notes)
                stopping = false
            },
        )
    }
}

@Composable
private fun ReminderOfferCard(onAccept: () -> Unit, onDecline: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.NotificationsActive, null)
                Spacer(Modifier.width(8.dp))
                Text("Want a daily nudge?", style = MaterialTheme.typography.titleMedium)
            }
            Text(
                "One evening reminder, only on days you haven't studied yet. Change the time in Settings.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAccept) { Text("Turn on") }
                TextButton(onClick = onDecline) { Text("No thanks") }
            }
        }
    }
}
