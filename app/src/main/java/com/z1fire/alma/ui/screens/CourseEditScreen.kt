package com.z1fire.alma.ui.screens

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.z1fire.alma.data.Course
import com.z1fire.alma.data.CourseStatus
import com.z1fire.alma.data.Department
import com.z1fire.alma.data.Grades
import com.z1fire.alma.data.Meeting
import com.z1fire.alma.data.Terms
import com.z1fire.alma.data.codeOf
import com.z1fire.alma.data.course
import com.z1fire.alma.data.levelLabel
import com.z1fire.alma.data.upsert
import com.z1fire.alma.ui.DateField
import com.z1fire.alma.ui.Dropdown
import com.z1fire.alma.ui.FormDialog
import com.z1fire.alma.ui.LocalRepository
import com.z1fire.alma.ui.SectionTitle
import com.z1fire.alma.ui.dayName
import com.z1fire.alma.ui.formatTimeRange
import com.z1fire.alma.ui.rememberAppData
import kotlinx.serialization.json.Json
import java.time.LocalDate

private val courseSaver = Saver<androidx.compose.runtime.MutableState<Course>, String>(
    save = { Json.encodeToString(Course.serializer(), it.value) },
    restore = { mutableStateOf(Json.decodeFromString(Course.serializer(), it)) },
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CourseEditScreen(courseId: String?, onBack: () -> Unit, onSaved: (String) -> Unit) {
    val repo = LocalRepository.current
    val data = rememberAppData()
    val today = LocalDate.now()
    var draft by rememberSaveable(courseId, saver = courseSaver) {
        mutableStateOf(courseId?.let { data.course(it) } ?: Course(departmentId = data.departments.firstOrNull()?.id))
    }
    var newObjective by rememberSaveable { mutableStateOf("") }
    var addingDept by remember { mutableStateOf(false) }
    var pickingPrereqs by remember { mutableStateOf(false) }
    var editMeeting by remember { mutableStateOf<Meeting?>(null) }
    var addingMeeting by remember { mutableStateOf(false) }

    fun save() {
        var c = draft.copy(title = draft.title.trim(), number = draft.number.trim(), description = draft.description.trim())
        if (newObjective.isNotBlank()) c = c.copy(objectives = c.objectives + newObjective.trim())
        if (c.status == CourseStatus.COMPLETED) {
            c = c.copy(finalGrade = c.finalGrade ?: "P", completedEpochDay = c.completedEpochDay ?: today.toEpochDay())
        }
        repo.saveCourse(c)
        onSaved(c.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (courseId == null) "Design a course" else "Edit course") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.Close, "Cancel") } },
                actions = { TextButton(onClick = ::save, enabled = draft.title.isNotBlank()) { Text("Save") } },
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
            SectionTitle("Catalog entry")
            val deptOptions: List<Department?> = listOf<Department?>(null) + data.departments.sortedBy { it.name }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Dropdown(
                    "Department",
                    deptOptions,
                    data.departments.find { it.id == draft.departmentId },
                    { it?.let { d -> "${d.name} (${d.code})" } ?: "General Studies (GEN)" },
                    { draft = draft.copy(departmentId = it?.id) },
                    Modifier.weight(1f),
                )
                TextButton(onClick = { addingDept = true }) { Text("New") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    draft.number,
                    { draft = draft.copy(number = it.take(8)) },
                    label = { Text("Number") },
                    placeholder = { Text("101") },
                    supportingText = { levelLabel(draft.number)?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.width(120.dp),
                )
                OutlinedTextField(
                    draft.title,
                    { draft = draft.copy(title = it) },
                    label = { Text("Course title") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                "Tip: 100-level = introductory, 200 = intermediate, 300 = advanced, 400 = senior seminar, 500+ = graduate.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Credits", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                FilledTonalIconButton(onClick = { draft = draft.copy(credits = (draft.credits - 1).coerceAtLeast(0)) }) {
                    Icon(Icons.Filled.Remove, "Fewer credits")
                }
                Text("${draft.credits}", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(horizontal = 16.dp))
                FilledTonalIconButton(onClick = { draft = draft.copy(credits = (draft.credits + 1).coerceAtMost(12)) }) {
                    Icon(Icons.Filled.Add, "More credits")
                }
            }
            OutlinedTextField(
                draft.description,
                { draft = draft.copy(description = it) },
                label = { Text("Catalog description") },
                placeholder = { Text("What is this course about, and why are you taking it?") },
                minLines = 3,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(4.dp))
            SectionTitle("Learning objectives")
            draft.objectives.forEachIndexed { i, o ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${i + 1}.", Modifier.width(24.dp))
                    Text(o, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    IconButton(onClick = { draft = draft.copy(objectives = draft.objectives.filterIndexed { j, _ -> j != i }) }) {
                        Icon(Icons.Filled.Close, "Remove objective")
                    }
                }
            }
            fun addObjective() {
                if (newObjective.isNotBlank()) {
                    draft = draft.copy(objectives = draft.objectives + newObjective.trim())
                    newObjective = ""
                }
            }
            OutlinedTextField(
                newObjective,
                { newObjective = it },
                label = { Text("Students will be able to…") },
                trailingIcon = { IconButton(onClick = ::addObjective) { Icon(Icons.Filled.Add, "Add objective") } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, capitalization = KeyboardCapitalization.Sentences),
                keyboardActions = KeyboardActions(onDone = { addObjective() }),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(4.dp))
            SectionTitle("Enrollment")
            Dropdown("Status", CourseStatus.entries, draft.status, { it.label }, { draft = draft.copy(status = it) })
            if (draft.status == CourseStatus.COMPLETED) {
                Dropdown("Final grade", Grades.all, draft.finalGrade ?: "P", { Grades.label(it) }, { draft = draft.copy(finalGrade = it) })
            }
            Dropdown(
                "Term",
                listOf("") + Terms.options(today, draft.term),
                draft.term,
                { it.ifBlank { "Unscheduled / self-paced" } },
                { draft = draft.copy(term = it) },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DateField("Start date", draft.startEpochDay, { draft = draft.copy(startEpochDay = it) }, Modifier.weight(1f))
                DateField("End date", draft.endEpochDay, { draft = draft.copy(endEpochDay = it) }, Modifier.weight(1f))
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(4, 8, 12, 15).forEach { weeks ->
                    TextButton(onClick = {
                        val start = draft.startEpochDay ?: today.toEpochDay()
                        draft = draft.copy(startEpochDay = start, endEpochDay = start + weeks * 7 - 1)
                    }) { Text("$weeks weeks") }
                }
            }

            Spacer(Modifier.height(4.dp))
            SectionTitle("Class meetings", action = {
                TextButton(onClick = { addingMeeting = true }) { Icon(Icons.Filled.Add, null); Text("Add") }
            })
            if (draft.meetings.isEmpty()) {
                Text(
                    "Reserve regular study blocks — they appear on your schedule and can send reminders.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            draft.meetings.sortedWith(compareBy({ it.dayOfWeek }, { it.startMinute })).forEach { m ->
                Card(
                    onClick = { editMeeting = m },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${dayName(m.dayOfWeek, short = false)} · ${formatTimeRange(m.startMinute, m.durationMinutes)}", style = MaterialTheme.typography.titleSmall)
                        Text(
                            listOf(m.label, m.location).filter { it.isNotBlank() }.joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            SectionTitle("Prerequisites", action = {
                TextButton(onClick = { pickingPrereqs = true }, enabled = data.courses.any { it.id != draft.id }) {
                    Text("Choose")
                }
            })
            if (draft.prerequisiteIds.isEmpty()) {
                Text("None — open to all students.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                draft.prerequisiteIds.mapNotNull { data.course(it) }.forEach { p ->
                    InputChip(
                        selected = false,
                        onClick = { draft = draft.copy(prerequisiteIds = draft.prerequisiteIds - p.id) },
                        label = { Text(data.codeOf(p)) },
                        trailingIcon = { Icon(Icons.Filled.Close, "Remove") },
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Button(onClick = ::save, enabled = draft.title.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                Text(if (courseId == null) "Add to catalog" else "Save changes")
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (addingDept) {
        DepartmentDialog(null, onDismiss = { addingDept = false }, onSave = { d ->
            repo.saveDepartment(d)
            draft = draft.copy(departmentId = d.id)
            addingDept = false
        })
    }
    if (addingMeeting || editMeeting != null) {
        val editing = editMeeting
        MeetingDialog(
            initial = editing,
            onDismiss = {
                addingMeeting = false
                editMeeting = null
            },
            onSave = { list ->
                var meetings = draft.meetings
                list.forEach { m -> meetings = meetings.upsert(m) { it.id } }
                draft = draft.copy(meetings = meetings)
                addingMeeting = false
                editMeeting = null
            },
            onDelete = {
                draft = draft.copy(meetings = draft.meetings.filter { it.id != editing?.id })
                editMeeting = null
            },
        )
    }
    if (pickingPrereqs) {
        var chosen by remember { mutableStateOf(draft.prerequisiteIds.toSet()) }
        FormDialog(
            title = "Prerequisites",
            onDismiss = { pickingPrereqs = false },
            onSave = {
                draft = draft.copy(prerequisiteIds = chosen.toList())
                pickingPrereqs = false
            },
        ) {
            data.courses.filter { it.id != draft.id }.sortedBy { data.codeOf(it) }.forEach { c ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = c.id in chosen, onCheckedChange = { chosen = if (it) chosen + c.id else chosen - c.id })
                    Column {
                        Text(data.codeOf(c), style = MaterialTheme.typography.labelLarge)
                        Text(c.title, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
