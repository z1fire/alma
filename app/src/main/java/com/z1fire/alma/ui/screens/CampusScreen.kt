package com.z1fire.alma.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.z1fire.alma.data.AppData
import com.z1fire.alma.data.CourseStatus
import com.z1fire.alma.data.Terms
import com.z1fire.alma.data.assignmentsDueOn
import com.z1fire.alma.data.codeOf
import com.z1fire.alma.data.course
import com.z1fire.alma.data.department
import com.z1fire.alma.data.dueSoon
import com.z1fire.alma.data.meetingsOn
import com.z1fire.alma.data.standingFor
import com.z1fire.alma.data.stats
import com.z1fire.alma.data.streak
import com.z1fire.alma.data.weekMinutes
import com.z1fire.alma.ui.CourseCard
import com.z1fire.alma.ui.EmptyState
import com.z1fire.alma.ui.LocalRepository
import com.z1fire.alma.ui.SectionTitle
import com.z1fire.alma.ui.Stat
import com.z1fire.alma.ui.dayName
import com.z1fire.alma.ui.formatMinutes
import com.z1fire.alma.ui.formatMonthDay
import com.z1fire.alma.ui.formatTimeRange
import com.z1fire.alma.ui.gpaText
import com.z1fire.alma.ui.initials
import com.z1fire.alma.ui.rememberAppData
import com.z1fire.alma.ui.relativeDue
import com.z1fire.alma.ui.theme.deptColor
import kotlinx.coroutines.delay
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusScreen(onOpenCourse: (String) -> Unit, onOpenSettings: () -> Unit, onNewCourse: () -> Unit) {
    val data = rememberAppData()
    val today = LocalDate.now()
    val enrolled = data.courses.filter { it.status == CourseStatus.ENROLLED }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(data.profile.institution, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (data.profile.motto.isNotBlank()) {
                            Text(
                                data.profile.motto,
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                },
                actions = { IconButton(onClick = onOpenSettings) { Icon(Icons.Filled.Settings, "Settings") } },
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { StudentIdCard(data, today) }
            data.activeTimer?.let { timer ->
                data.course(timer.courseId)?.let { c ->
                    item { TimerCard(data.codeOf(c), c.title, timer.startedAtMillis) }
                }
            }
            if (data.courses.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.School,
                        title = "Classes haven't started yet",
                        message = "Design your first course — pick a subject, list your books, set a schedule — and enroll.",
                        actionLabel = "Design a course",
                        onAction = onNewCourse,
                    )
                }
                return@LazyColumn
            }
            item { TodayCard(data, today, onOpenCourse) }
            item { WeekCard(data, today) }
            val due = data.dueSoon(today)
            if (due.isNotEmpty()) item { DueSoonCard(data, today, onOpenCourse) }
            item {
                SectionTitle(
                    "Current courses",
                    action = { TextButton(onClick = onNewCourse) { Icon(Icons.Filled.Add, null); Text("New") } },
                )
            }
            if (enrolled.isEmpty()) {
                item {
                    Text(
                        "You're not enrolled in anything right now. Open a planned course in the Catalog and enroll.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(enrolled, key = { it.id }) { c -> CourseCard(data, c, onClick = { onOpenCourse(c.id) }) }
        }
    }
}

@Composable
private fun StudentIdCard(data: AppData, today: LocalDate) {
    val stats = data.stats()
    val name = data.profile.studentName.ifBlank { "Student" }
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        initials(name),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(name, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${standingFor(stats.creditsEarned)} · ${Terms.forDate(today)}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) {
                Stat(gpaText(stats.gpa), "GPA", Modifier.weight(1f))
                Stat("${stats.creditsEarned}", "CREDITS", Modifier.weight(1f))
                Stat("${data.courses.count { it.status == CourseStatus.ENROLLED }}", "ENROLLED", Modifier.weight(1f))
                Stat("${stats.completedCount}", "COMPLETED", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TimerCard(code: String, title: String, startedAt: Long) {
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
                Text("In session · $code", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            }
            Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "%d:%02d:%02d".format(elapsed / 3600, (elapsed % 3600) / 60, elapsed % 60),
                style = MaterialTheme.typography.displaySmall,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { stopping = true }) { Text("End session") }
                TextButton(onClick = { repo.cancelTimer() }) { Text("Discard") }
            }
        }
    }
    if (stopping) {
        LogSessionDialog(
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
private fun TodayCard(data: AppData, today: LocalDate, onOpenCourse: (String) -> Unit) {
    val repo = LocalRepository.current
    val meetings = data.meetingsOn(today)
    val dueToday = data.assignmentsDueOn(today).filter { !it.second.done }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp)) {
            SectionTitle("Today · ${dayName(today.dayOfWeek.value, short = false)}, ${formatMonthDay(today)}")
            Spacer(Modifier.height(8.dp))
            if (meetings.isEmpty()) {
                Text(
                    "No classes on the timetable today. Free period — or a good day to get ahead.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            meetings.forEachIndexed { i, (course, meeting) ->
                if (i > 0) HorizontalDivider(Modifier.padding(vertical = 6.dp))
                val accent = deptColor(data.department(course.departmentId)?.colorIndex)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(4.dp).height(40.dp).clip(RoundedCornerShape(2.dp)).background(accent))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(formatTimeRange(meeting.startMinute, meeting.durationMinutes), style = MaterialTheme.typography.labelMedium)
                        TextButton(
                            onClick = { onOpenCourse(course.id) },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(24.dp),
                        ) {
                            Text(
                                "${data.codeOf(course)} · ${meeting.label}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        if (meeting.location.isNotBlank()) {
                            Text(meeting.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (data.activeTimer == null) {
                        FilledTonalButton(onClick = { repo.startTimer(course.id) }) {
                            Icon(Icons.Filled.PlayArrow, null, Modifier.size(18.dp))
                            Text("Start")
                        }
                    }
                }
            }
            if (dueToday.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "${dueToday.size} assignment${if (dueToday.size == 1) "" else "s"} due today",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

@Composable
private fun WeekCard(data: AppData, today: LocalDate) {
    val minutes = data.weekMinutes(today)
    val goal = data.profile.weeklyGoalMinutes.coerceAtLeast(1)
    val streak = data.streak(today)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp)) {
            SectionTitle("This week's study")
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(formatMinutes(minutes), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "  of ${formatMinutes(goal)} goal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp).weight(1f),
                )
                if (streak > 0) {
                    Icon(Icons.Filled.LocalFireDepartment, null, tint = MaterialTheme.colorScheme.secondary)
                    Text("$streak-day streak", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                }
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (minutes.toFloat() / goal).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                drawStopIndicator = {},
            )
        }
    }
}

@Composable
private fun DueSoonCard(data: AppData, today: LocalDate, onOpenCourse: (String) -> Unit) {
    val repo = LocalRepository.current
    val due = data.dueSoon(today).take(6)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(vertical = 12.dp)) {
            SectionTitle("Due soon", Modifier.padding(horizontal = 16.dp))
            due.forEach { (course, a) ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 12.dp)) {
                    Checkbox(
                        checked = a.done,
                        onCheckedChange = { checked ->
                            repo.updateCourse(course.id) { c ->
                                c.copy(assignments = c.assignments.map { if (it.id == a.id) it.copy(done = checked) else it })
                            }
                        },
                    )
                    Column(Modifier.weight(1f)) {
                        Text(a.title, style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        val overdue = (a.dueEpochDay ?: 0) < today.toEpochDay()
                        Text(
                            "${data.codeOf(course)} · ${a.type.label} · ${a.dueEpochDay?.let { relativeDue(it, today) } ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (overdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    OutlinedButton(onClick = { onOpenCourse(course.id) }, contentPadding = PaddingValues(horizontal = 10.dp)) {
                        Text("Open")
                    }
                }
            }
        }
    }
}
