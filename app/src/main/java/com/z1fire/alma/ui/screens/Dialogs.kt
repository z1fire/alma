package com.z1fire.alma.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.z1fire.alma.data.Assignment
import com.z1fire.alma.data.AssignmentType
import com.z1fire.alma.data.Course
import com.z1fire.alma.data.Department
import com.z1fire.alma.data.Grades
import com.z1fire.alma.data.Meeting
import com.z1fire.alma.data.Module
import com.z1fire.alma.data.Resource
import com.z1fire.alma.data.ResourceStatus
import com.z1fire.alma.data.ResourceType
import com.z1fire.alma.ui.ColorDot
import com.z1fire.alma.ui.DateField
import com.z1fire.alma.ui.Dropdown
import com.z1fire.alma.ui.FormDialog
import com.z1fire.alma.ui.TimeField
import com.z1fire.alma.ui.dayName
import com.z1fire.alma.ui.formatMinutes
import com.z1fire.alma.ui.theme.DeptColors
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogSessionDialog(
    title: String = "Log study session",
    initialMinutes: Int = 45,
    showDate: Boolean = true,
    onDismiss: () -> Unit,
    onSave: (epochDay: Long, minutes: Int, notes: String) -> Unit,
) {
    var day by remember { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    var minutesText by remember { mutableStateOf(initialMinutes.toString()) }
    var notes by remember { mutableStateOf("") }
    val minutes = minutesText.toIntOrNull() ?: 0
    FormDialog(title, onDismiss, onSave = { onSave(day, minutes, notes.trim()) }, saveEnabled = minutes > 0) {
        if (showDate) DateField("Date", day, { it?.let { d -> day = d } })
        OutlinedTextField(
            value = minutesText,
            onValueChange = { minutesText = it.filter(Char::isDigit).take(4) },
            label = { Text("Minutes") },
            supportingText = { if (minutes > 0) Text(formatMinutes(minutes)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(15, 30, 45, 60, 90, 120).forEach { m ->
                FilterChip(selected = minutes == m, onClick = { minutesText = m.toString() }, label = { Text(formatMinutes(m)) })
            }
        }
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("What did you cover?") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun ModuleDialog(initial: Module, isNew: Boolean, onDismiss: () -> Unit, onSave: (Module) -> Unit, onDelete: () -> Unit) {
    var title by remember { mutableStateOf(initial.title) }
    var notes by remember { mutableStateOf(initial.notes) }
    FormDialog(
        title = if (isNew) "Add unit" else "Edit unit",
        onDismiss = onDismiss,
        onSave = { onSave(initial.copy(title = title.trim(), notes = notes.trim())) },
        saveEnabled = title.isNotBlank(),
        onDelete = if (isNew) null else onDelete,
    ) {
        OutlinedTextField(title, { title = it }, label = { Text("Unit title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(notes, { notes = it }, label = { Text("Topics & notes") }, minLines = 3, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun ResourceDialog(initial: Resource, isNew: Boolean, onDismiss: () -> Unit, onSave: (Resource) -> Unit, onDelete: () -> Unit) {
    var r by remember { mutableStateOf(initial) }
    FormDialog(
        title = if (isNew) "Add to reading list" else "Edit resource",
        onDismiss = onDismiss,
        onSave = { onSave(r.copy(title = r.title.trim(), author = r.author.trim(), url = r.url.trim(), notes = r.notes.trim())) },
        saveEnabled = r.title.isNotBlank(),
        onDelete = if (isNew) null else onDelete,
    ) {
        Dropdown("Type", ResourceType.entries, r.type, { it.label }, { r = r.copy(type = it) })
        OutlinedTextField(r.title, { r = r.copy(title = it) }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(r.author, { r = r.copy(author = it) }, label = { Text("Author / creator") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            r.url, { r = r.copy(url = it) }, label = { Text("Link (optional)") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri), modifier = Modifier.fillMaxWidth(),
        )
        Dropdown("Status", ResourceStatus.entries, r.status, { it.label }, { r = r.copy(status = it) })
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Required reading", Modifier.weight(1f))
            Switch(checked = r.required, onCheckedChange = { r = r.copy(required = it) })
        }
        OutlinedTextField(r.notes, { r = r.copy(notes = it) }, label = { Text("Notes (chapters, edition…)") }, minLines = 2, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun AssignmentDialog(initial: Assignment, isNew: Boolean, onDismiss: () -> Unit, onSave: (Assignment) -> Unit, onDelete: () -> Unit) {
    var a by remember { mutableStateOf(initial) }
    FormDialog(
        title = if (isNew) "New assignment" else "Edit assignment",
        onDismiss = onDismiss,
        onSave = { onSave(a.copy(title = a.title.trim(), grade = a.grade.trim(), notes = a.notes.trim())) },
        saveEnabled = a.title.isNotBlank(),
        onDelete = if (isNew) null else onDelete,
    ) {
        OutlinedTextField(a.title, { a = a.copy(title = it) }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        Dropdown("Type", AssignmentType.entries, a.type, { it.label }, { a = a.copy(type = it) })
        DateField("Due date", a.dueEpochDay, { a = a.copy(dueEpochDay = it) })
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = a.done, onCheckedChange = { a = a.copy(done = it) })
            Text("Completed")
        }
        OutlinedTextField(a.grade, { a = a.copy(grade = it) }, label = { Text("Self-assessed grade or score") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(a.notes, { a = a.copy(notes = it) }, label = { Text("Instructions / notes") }, minLines = 2, modifier = Modifier.fillMaxWidth())
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MeetingDialog(initial: Meeting?, onDismiss: () -> Unit, onSave: (List<Meeting>) -> Unit, onDelete: () -> Unit) {
    val isNew = initial == null
    var days by remember { mutableStateOf(setOf(initial?.dayOfWeek ?: LocalDate.now().dayOfWeek.value)) }
    var start by remember { mutableIntStateOf(initial?.startMinute ?: (19 * 60)) }
    var duration by remember { mutableIntStateOf(initial?.durationMinutes ?: 60) }
    var label by remember { mutableStateOf(initial?.label ?: "Lecture") }
    var location by remember { mutableStateOf(initial?.location ?: "") }
    FormDialog(
        title = if (isNew) "Add class meeting" else "Edit class meeting",
        onDismiss = onDismiss,
        onSave = {
            val base = Meeting(dayOfWeek = 1, startMinute = start, durationMinutes = duration, label = label.trim().ifBlank { "Class" }, location = location.trim())
            onSave(
                if (initial != null) listOf(base.copy(id = initial.id, dayOfWeek = days.first()))
                else days.sorted().map { base.copy(id = com.z1fire.alma.data.newId(), dayOfWeek = it) },
            )
        },
        saveEnabled = days.isNotEmpty(),
        onDelete = if (isNew) null else onDelete,
    ) {
        Text(if (isNew) "Meets on (pick one or more)" else "Meets on", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (1..7).forEach { d ->
                FilterChip(
                    selected = d in days,
                    onClick = { days = if (isNew) (if (d in days) days - d else days + d) else setOf(d) },
                    label = { Text(dayName(d)) },
                )
            }
        }
        TimeField("Starts at", start, { start = it })
        Text("Length", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(30, 45, 60, 90, 120, 180).forEach { m ->
                FilterChip(selected = duration == m, onClick = { duration = m }, label = { Text(formatMinutes(m)) })
            }
        }
        OutlinedTextField(label, { label = it }, label = { Text("Kind (Lecture, Seminar, Lab…)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(location, { location = it }, label = { Text("Where (desk, library, café…)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DepartmentDialog(initial: Department?, onDismiss: () -> Unit, onSave: (Department) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var code by remember { mutableStateOf(initial?.code ?: "") }
    var codeEdited by remember { mutableStateOf(initial != null) }
    var color by remember { mutableIntStateOf(initial?.colorIndex ?: (0..9).random()) }
    FormDialog(
        title = if (initial == null) "Found a department" else "Edit department",
        onDismiss = onDismiss,
        onSave = {
            val c = code.trim().uppercase().ifBlank { name.filter(Char::isLetter).take(4).uppercase() }
            onSave((initial ?: Department(name = "", code = "")).copy(name = name.trim(), code = c, colorIndex = color))
        },
        saveEnabled = name.isNotBlank(),
    ) {
        OutlinedTextField(
            name,
            {
                name = it
                if (!codeEdited) code = it.filter(Char::isLetter).take(4).uppercase()
            },
            label = { Text("Name (e.g. Philosophy)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            code,
            {
                code = it.uppercase().take(6)
                codeEdited = true
            },
            label = { Text("Course code prefix (e.g. PHIL)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Text("Color", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DeptColors.forEachIndexed { i, c ->
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { color = i }
                        .then(
                            if (i == color) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier,
                        ),
                    contentAlignment = Alignment.Center,
                ) { ColorDot(c, 30) }
            }
        }
    }
}

@Composable
fun CompleteCourseDialog(course: Course, onDismiss: () -> Unit, onComplete: (grade: String, reflection: String, epochDay: Long) -> Unit) {
    var grade by remember { mutableStateOf(course.finalGrade ?: "A") }
    var reflection by remember { mutableStateOf(course.reflection) }
    var day by remember { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    FormDialog(
        title = "Complete course",
        onDismiss = onDismiss,
        onSave = { onComplete(grade, reflection.trim(), day) },
        saveLabel = "Graduate this course",
    ) {
        Text(
            "Grade yourself honestly against your learning objectives. Letter grades count toward your GPA; Pass and Audit do not.",
            style = MaterialTheme.typography.bodyMedium,
        )
        if (course.objectives.isNotEmpty()) {
            course.objectives.forEach {
                Text("•  $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Dropdown("Final grade", Grades.all, grade, { Grades.label(it) }, { grade = it })
        DateField("Completed on", day, { it?.let { d -> day = d } })
        OutlinedTextField(
            reflection,
            { reflection = it },
            label = { Text("Reflection: what did you learn?") },
            minLines = 4,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
