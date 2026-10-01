package com.z1fire.alma.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.z1fire.alma.data.Course
import com.z1fire.alma.data.Idea
import com.z1fire.alma.data.StudyItem
import com.z1fire.alma.data.starterIdeas
import com.z1fire.alma.ui.ColorDot
import com.z1fire.alma.ui.TimeField
import com.z1fire.alma.ui.FormDialog
import com.z1fire.alma.ui.formatDate
import com.z1fire.alma.ui.formatHoursLong
import com.z1fire.alma.ui.theme.DeptColors
import com.z1fire.alma.ui.theme.Gold
import java.time.LocalDate

/** Create a course (optionally from a starter idea) or edit one. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CourseDialog(initial: Course?, onDismiss: () -> Unit, onSave: (Course) -> Unit) {
    val isNew = initial == null
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var subject by remember { mutableStateOf(initial?.subject ?: "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }
    var color by remember { mutableIntStateOf(initial?.colorIndex ?: DeptColors.indices.random()) }
    var idea by remember { mutableStateOf<Idea?>(null) }

    FormDialog(
        title = if (isNew) "New course" else "Edit course",
        onDismiss = onDismiss,
        saveEnabled = title.isNotBlank(),
        saveLabel = if (isNew) "Create" else "Save",
        onSave = {
            val base = initial ?: idea?.toCourse() ?: Course(title = "")
            onSave(base.copy(title = title.trim(), subject = subject.trim(), notes = notes.trim(), colorIndex = color))
        },
    ) {
        if (isNew) {
            Text("Start from an idea, or type your own.", style = MaterialTheme.typography.bodySmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                starterIdeas.forEach { i ->
                    FilterChip(
                        selected = idea == i,
                        onClick = {
                            if (idea == i) {
                                idea = null
                            } else {
                                idea = i
                                title = i.title
                                subject = i.subject
                                color = i.colorIndex
                            }
                        },
                        label = { Text(i.title) },
                    )
                }
            }
            idea?.let {
                Text(
                    "Adds a starter list of ${it.items.size} things to study — edit freely.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        OutlinedTextField(
            title, { title = it },
            label = { Text("What are you learning?") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            subject, { subject = it },
            label = { Text("Subject (optional)") },
            placeholder = { Text("Languages, Biology, Music…") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            notes, { notes = it },
            label = { Text("Notes (optional)") },
            placeholder = { Text("Goals, why you're learning this…") },
            minLines = 2,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DeptColors.forEachIndexed { i, c ->
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { color = i }
                        .then(if (i == color) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier),
                    contentAlignment = Alignment.Center,
                ) { ColorDot(c, 26) }
            }
        }
    }
}

@Composable
fun ItemDialog(initial: StudyItem, onDismiss: () -> Unit, onSave: (StudyItem) -> Unit, onDelete: () -> Unit) {
    var text by remember { mutableStateOf(initial.text) }
    var link by remember { mutableStateOf(initial.link) }
    FormDialog(
        title = "Edit item",
        onDismiss = onDismiss,
        onSave = { onSave(initial.copy(text = text.trim(), link = link.trim())) },
        saveEnabled = text.isNotBlank(),
        onDelete = onDelete,
    ) {
        OutlinedTextField(text, { text = it }, label = { Text("Book, topic, chapter…") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            link, { link = it },
            label = { Text("Link (optional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Correct a course's total study time. */
@Composable
fun TotalTimeDialog(minutes: Int, onDismiss: () -> Unit, onSave: (Int) -> Unit) {
    var hours by remember { mutableStateOf((minutes / 60).toString()) }
    var mins by remember { mutableStateOf((minutes % 60).toString()) }
    val total = (hours.toIntOrNull() ?: 0) * 60 + (mins.toIntOrNull() ?: 0)
    FormDialog("Total time studied", onDismiss, onSave = { onSave(total) }) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                hours, { hours = it.filter(Char::isDigit).take(4) },
                label = { Text("Hours") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                mins, { mins = it.filter(Char::isDigit).take(2) },
                label = { Text("Minutes") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun NameDialog(name: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf(name) }
    FormDialog("Name on certificates", onDismiss, onSave = { onSave(text.trim()) }) {
        OutlinedTextField(
            text, { text = it },
            label = { Text("Your name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Turn the daily reminder on/off and pick its time. Permission is handled by the caller. */
@Composable
fun ReminderDialog(
    enabled: Boolean,
    minute: Int,
    onDismiss: () -> Unit,
    onSave: (enabled: Boolean, minute: Int) -> Unit,
    onTest: () -> Unit,
) {
    var on by remember { mutableStateOf(enabled) }
    var at by remember { mutableIntStateOf(minute) }
    FormDialog("Daily reminder", onDismiss, onSave = { onSave(on, at) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Remind me on days I haven't studied",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Switch(checked = on, onCheckedChange = { on = it })
        }
        if (on) {
            TimeField("Time", at, { at = it })
            TextButton(onClick = onTest) { Text("Send a test notification") }
        }
    }
}

@Composable
fun FinishDialog(course: Course, onDismiss: () -> Unit, onFinish: (reflection: String) -> Unit) {
    var reflection by remember { mutableStateOf(course.reflection) }
    FormDialog(
        title = "Finish ${course.title}?",
        onDismiss = onDismiss,
        onSave = { onFinish(reflection.trim()) },
        saveLabel = "Mark finished",
    ) {
        val stats = listOfNotNull(
            course.minutes.takeIf { it > 0 }?.let { "${formatHoursLong(it)} of study" },
            course.items.takeIf { it.isNotEmpty() }?.let { "${course.doneCount} of ${it.size} items done" },
        )
        if (stats.isNotEmpty()) Text(stats.joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            reflection,
            { reflection = it },
            label = { Text("What did you learn? (optional)") },
            minLines = 3,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** A printable-looking certificate of completion for a finished course. */
@Composable
fun CertificateDialog(name: String, course: Course, onDismiss: () -> Unit) {
    val parchment = Color(0xFFFBF5E6)
    val ink = Color(0xFF2A2418)
    val navy = Color(0xFF1F3A5F)
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(color = parchment, shape = RoundedCornerShape(6.dp), modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Box(
                Modifier
                    .padding(8.dp)
                    .border(3.dp, Gold, RoundedCornerShape(4.dp))
                    .padding(4.dp)
                    .border(1.dp, Gold, RoundedCornerShape(2.dp)),
            ) {
                Column(
                    Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("CERTIFICATE OF COMPLETION", style = MaterialTheme.typography.labelLarge, color = navy)
                    Spacer(Modifier.height(16.dp))
                    Text("This certifies that", style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, color = ink)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        name.ifBlank { "a dedicated learner" },
                        style = MaterialTheme.typography.displaySmall,
                        fontStyle = FontStyle.Italic,
                        color = ink,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("has completed a self-directed course in", style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Serif, color = ink)
                    Spacer(Modifier.height(6.dp))
                    Text(course.title, style = MaterialTheme.typography.headlineMedium, color = navy, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(12.dp))
                    val stats = listOfNotNull(
                        course.minutes.takeIf { it > 0 }?.let { formatHoursLong(it) + " of study" },
                        course.doneCount.takeIf { it > 0 }?.let { "$it item${if (it == 1) "" else "s"} completed" },
                    )
                    if (stats.isNotEmpty()) {
                        Text(stats.joinToString("  ·  "), style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Serif, color = ink)
                    }
                    Spacer(Modifier.height(20.dp))
                    Box(
                        Modifier.size(64.dp).clip(CircleShape).border(3.dp, Gold, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Filled.School, null, tint = Gold, modifier = Modifier.size(32.dp)) }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        formatDate(course.finishedEpochDay?.takeIf { it > 0 } ?: LocalDate.now().toEpochDay()),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Serif,
                        color = ink,
                    )
                    if (course.reflection.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text("“${course.reflection}”", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic, color = ink.copy(alpha = 0.75f), textAlign = TextAlign.Center)
                    }
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = onDismiss) { Text("Close", color = navy) }
                }
            }
        }
    }
}
