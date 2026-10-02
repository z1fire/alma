package com.z1fire.alma.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.z1fire.alma.data.Course
import com.z1fire.alma.ui.formatHours
import com.z1fire.alma.ui.formatHoursShort
import com.z1fire.alma.ui.formatMinutes
import com.z1fire.alma.ui.theme.deptColor

/** "212h of 10,000h", "3h 20m studied", or "not started". */
fun Course.timeSummary(): String = when {
    goalHours != null -> "${formatHoursShort(minutes)} of ${formatHours(goalHours)}"
    minutes > 0 -> "${formatMinutes(minutes)} studied"
    else -> "not started"
}

/** "100h goal · 6 items" — what a planned course will involve. */
fun Course.planSummary(): String =
    listOfNotNull(
        goalHours?.let { "${formatHours(it)} goal" },
        items.size.takeIf { it > 0 }?.let { "$it item${if (it == 1) "" else "s"}" },
    ).joinToString(" · ").ifBlank { "No plan yet" }

/**
 * A course in a list: color bar, title, subject and time, and progress toward the hour goal and/or checklist.
 * [planned] cards (catalogue) show the plan instead of progress, and leave the subject to the section header.
 */
@Composable
fun CourseCard(course: Course, onClick: () -> Unit, planned: Boolean = false, trailing: (@Composable () -> Unit)? = null) {
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
                Text(
                    if (planned) course.planSummary() else listOfNotNull(course.subject.ifBlank { null }, course.timeSummary()).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (planned) return@Column
                if (course.goalMinutes != null) {
                    ProgressLine(course.hourProgress, accent, "${(course.hourProgress * 100).toInt()}%")
                }
                if (course.items.isNotEmpty()) {
                    ProgressLine(course.itemProgress, accent, "${course.doneCount}/${course.items.size}")
                }
            }
            trailing?.let { Box(Modifier.padding(end = 10.dp)) { it() } }
        }
    }
}

@Composable
fun ProgressLine(progress: Float, color: Color, label: String, modifier: Modifier = Modifier) {
    Row(modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.18f),
            drawStopIndicator = {},
        )
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
