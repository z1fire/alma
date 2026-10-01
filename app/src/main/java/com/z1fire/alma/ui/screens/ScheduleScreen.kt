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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.z1fire.alma.data.AppData
import com.z1fire.alma.data.assignmentsDueOn
import com.z1fire.alma.data.codeOf
import com.z1fire.alma.data.department
import com.z1fire.alma.data.meetingsOn
import com.z1fire.alma.data.minutesBetween
import com.z1fire.alma.data.sessionsOn
import com.z1fire.alma.data.weekStart
import com.z1fire.alma.ui.ClickableRow
import com.z1fire.alma.ui.dayName
import com.z1fire.alma.ui.formatMinutes
import com.z1fire.alma.ui.formatMonthDay
import com.z1fire.alma.ui.formatTimeRange
import com.z1fire.alma.ui.rememberAppData
import com.z1fire.alma.ui.theme.deptColor
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(onOpenCourse: (String) -> Unit) {
    val data = rememberAppData()
    val today = LocalDate.now()
    var weekOffset by rememberSaveable { mutableIntStateOf(0) }
    val monday = weekStart(today).plusWeeks(weekOffset.toLong())
    val days = (0..6L).map { monday.plusDays(it) }
    val classMinutes = days.sumOf { d -> data.meetingsOn(d).sumOf { it.second.durationMinutes } }
    val dueCount = days.sumOf { d -> data.assignmentsDueOn(d).size }
    val studied = data.minutesBetween(days.first(), days.last())

    Scaffold(topBar = { TopAppBar(title = { Text("Schedule") }) }) { padding ->
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { weekOffset-- }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous week") }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${formatMonthDay(days.first())} – ${formatMonthDay(days.last())}",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            when (weekOffset) {
                                0 -> "This week"
                                1 -> "Next week"
                                -1 -> "Last week"
                                else -> if (weekOffset > 0) "In $weekOffset weeks" else "${-weekOffset} weeks ago"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { weekOffset++ }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next week") }
                }
                if (weekOffset != 0) {
                    TextButton(onClick = { weekOffset = 0 }, modifier = Modifier.fillMaxWidth()) { Text("Back to this week") }
                }
                Text(
                    "${formatMinutes(classMinutes)} of class time · $dueCount due · ${formatMinutes(studied)} studied",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                )
            }
            days.forEach { day -> item(key = day.toEpochDay()) { DayCard(data, day, today, onOpenCourse) } }
        }
    }
}

@Composable
private fun DayCard(data: AppData, day: LocalDate, today: LocalDate, onOpenCourse: (String) -> Unit) {
    val isToday = day == today
    val meetings = data.meetingsOn(day)
    val due = data.assignmentsDueOn(day)
    val sessions = data.sessionsOn(day)
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(dayName(day.dayOfWeek.value, short = false), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                Text(formatMonthDay(day), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (isToday) {
                    Spacer(Modifier.weight(1f))
                    Text("TODAY", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
            if (meetings.isEmpty() && due.isEmpty() && sessions.isEmpty()) {
                Text("Nothing scheduled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 4.dp))
            }
            meetings.forEach { (course, m) ->
                val accent = deptColor(data.department(course.departmentId)?.colorIndex)
                ClickableRow(onClick = { onOpenCourse(course.id) }) {
                    Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(4.dp).height(34.dp).clip(RoundedCornerShape(2.dp)).background(accent))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(formatTimeRange(m.startMinute, m.durationMinutes), style = MaterialTheme.typography.labelMedium)
                            Text(
                                "${data.codeOf(course)} · ${m.label}" + if (m.location.isNotBlank()) " · ${m.location}" else "",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
            due.forEach { (course, a) ->
                EntryLine(
                    icon = if (a.done) Icons.Filled.CheckCircle else Icons.AutoMirrored.Filled.Assignment,
                    tint = if (a.done) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                    text = "Due: ${a.title} (${data.codeOf(course)})",
                    struck = a.done,
                    onClick = { onOpenCourse(course.id) },
                )
            }
            sessions.forEach { (course, s) ->
                EntryLine(
                    icon = Icons.Filled.Timer,
                    tint = MaterialTheme.colorScheme.tertiary,
                    text = "Studied ${formatMinutes(s.minutes)} · ${data.codeOf(course)}",
                    struck = false,
                    onClick = { onOpenCourse(course.id) },
                )
            }
        }
    }
}

@Composable
private fun EntryLine(icon: ImageVector, tint: Color, text: String, struck: Boolean, onClick: () -> Unit) {
    ClickableRow(onClick = onClick) {
        Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(18.dp), tint = tint)
            Spacer(Modifier.width(10.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, textDecoration = if (struck) TextDecoration.LineThrough else null)
        }
    }
}
