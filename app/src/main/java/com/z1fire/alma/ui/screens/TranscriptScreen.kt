package com.z1fire.alma.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.z1fire.alma.data.AppData
import com.z1fire.alma.data.Course
import com.z1fire.alma.data.CourseStatus
import com.z1fire.alma.data.Department
import com.z1fire.alma.data.Program
import com.z1fire.alma.data.ProgramKind
import com.z1fire.alma.data.Terms
import com.z1fire.alma.data.codeOf
import com.z1fire.alma.data.countingCourses
import com.z1fire.alma.data.earnedCredits
import com.z1fire.alma.data.isComplete
import com.z1fire.alma.data.latinHonors
import com.z1fire.alma.data.recordStats
import com.z1fire.alma.data.requiredDone
import com.z1fire.alma.data.standingFor
import com.z1fire.alma.data.stats
import com.z1fire.alma.ui.Dropdown
import com.z1fire.alma.ui.EmptyState
import com.z1fire.alma.ui.FormDialog
import com.z1fire.alma.ui.LocalRepository
import com.z1fire.alma.ui.Pill
import com.z1fire.alma.ui.SectionTitle
import com.z1fire.alma.ui.Stat
import com.z1fire.alma.ui.formatDate
import com.z1fire.alma.ui.gpaText
import com.z1fire.alma.ui.rememberAppData
import com.z1fire.alma.ui.theme.Gold
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranscriptScreen(onOpenCourse: (String) -> Unit) {
    val repo = LocalRepository.current
    val data = rememberAppData()
    var editProgram by remember { mutableStateOf<Program?>(null) }
    var diploma by remember { mutableStateOf<Program?>(null) }

    val completed = data.courses.filter { it.status == CourseStatus.COMPLETED }
    val inProgress = data.courses.filter { it.status == CourseStatus.ENROLLED }
    val byTerm = completed.groupBy { it.term.ifBlank { "Independent study" } }
        .toList()
        .sortedBy { Terms.sortKey(it.first) }

    Scaffold(topBar = { TopAppBar(title = { Text("Transcript") }) }) { padding ->
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { TranscriptHeader(data) }

            item {
                SectionTitle("Degrees & programs", action = {
                    TextButton(onClick = { editProgram = Program(name = "") }) { Icon(Icons.Filled.Add, null); Text("Declare") }
                })
            }
            if (data.programs.isEmpty()) {
                item {
                    Text(
                        "Declare a major, minor, certificate or full degree, and watch your credits add up toward it.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            data.programs.forEach { p ->
                item(key = p.id) {
                    ProgramCard(
                        data, p,
                        onEdit = { editProgram = p },
                        onConfer = { repo.saveProgram(p.copy(conferredEpochDay = LocalDate.now().toEpochDay())); diploma = p },
                        onDiploma = { diploma = p },
                    )
                }
            }

            item { SectionTitle("Academic record", Modifier.padding(top = 8.dp)) }
            if (completed.isEmpty() && inProgress.isEmpty()) {
                item {
                    EmptyState(
                        Icons.Filled.School,
                        "A clean slate",
                        "Your transcript fills in as you complete courses. Every finished course earns credits and a grade.",
                    )
                }
            }
            byTerm.forEach { (term, courses) ->
                item(key = "t-$term") { TermCard(data, term, courses, onOpenCourse) }
            }
            if (inProgress.isNotEmpty()) {
                item(key = "ip") { TermCard(data, "In progress", inProgress, onOpenCourse, inProgress = true) }
            }
        }
    }

    editProgram?.let { p ->
        ProgramDialog(
            data = data,
            initial = p,
            isNew = data.programs.none { it.id == p.id },
            onDismiss = { editProgram = null },
            onSave = {
                repo.saveProgram(it)
                editProgram = null
            },
            onDelete = {
                repo.deleteProgram(p.id)
                editProgram = null
            },
        )
    }
    diploma?.let { p ->
        DiplomaDialog(data, data.programs.find { it.id == p.id } ?: p, onDismiss = { diploma = null })
    }
}

@Composable
private fun TranscriptHeader(data: AppData) {
    val stats = data.stats()
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "OFFICIAL ACADEMIC TRANSCRIPT",
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.height(6.dp))
            Text(data.profile.institution, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(data.profile.studentName.ifBlank { "Student" }, style = MaterialTheme.typography.titleMedium)
            data.profile.matriculatedEpochDay?.let {
                Text("Matriculated ${formatDate(it)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth()) {
                Stat(gpaText(stats.gpa), "CUMULATIVE GPA", Modifier.weight(1f))
                Stat("${stats.creditsEarned}", "CREDITS", Modifier.weight(1f))
                Stat(standingFor(stats.creditsEarned), "STANDING", Modifier.weight(1.3f))
            }
            latinHonors(stats.gpa)?.let { honors ->
                if (stats.gradedCredits >= 12) {
                    Spacer(Modifier.height(10.dp))
                    Text("On track to graduate $honors", style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }
    }
}

@Composable
private fun TermCard(data: AppData, term: String, courses: List<Course>, onOpenCourse: (String) -> Unit, inProgress: Boolean = false) {
    val s = recordStats(courses)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(term, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                if (!inProgress && s.gpa != null && s.gpa >= 3.5 && s.gradedCredits >= 6) {
                    Pill("Dean's List", Gold.copy(alpha = 0.25f), MaterialTheme.colorScheme.onSurface)
                }
            }
            Spacer(Modifier.height(6.dp))
            courses.sortedBy { data.codeOf(it) }.forEach { c ->
                TextButton(onClick = { onOpenCourse(c.id) }, contentPadding = PaddingValues(0.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            data.codeOf(c),
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.width(92.dp),
                        )
                        Text(c.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), maxLines = 1)
                        Text("${c.credits}", style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(24.dp), textAlign = TextAlign.End)
                        Text(
                            if (inProgress) "IP" else c.finalGrade ?: "—",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.End,
                        )
                    }
                }
            }
            if (!inProgress) {
                HorizontalDivider(Modifier.padding(vertical = 6.dp))
                Text(
                    "Term credits ${s.creditsEarned} · Term GPA ${gpaText(s.gpa)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ProgramCard(data: AppData, p: Program, onEdit: () -> Unit, onConfer: () -> Unit, onDiploma: () -> Unit) {
    val earned = p.earnedCredits(data)
    val complete = p.isComplete(data)
    Card(
        onClick = onEdit,
        colors = CardDefaults.cardColors(
            containerColor = if (p.conferredEpochDay != null) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(p.kind.label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, letterSpacing = 1.5.sp)
            Text(p.name, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { if (p.creditsRequired == 0) 1f else (earned.toFloat() / p.creditsRequired).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                drawStopIndicator = {},
            )
            Spacer(Modifier.height(4.dp))
            val req = if (p.courseIds.isEmpty()) "" else " · ${p.requiredDone(data)}/${p.courseIds.size} required courses"
            Text("$earned of ${p.creditsRequired} credits$req", style = MaterialTheme.typography.labelMedium)
            when {
                p.conferredEpochDay != null -> {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, null, tint = Gold)
                        Text(" Conferred ${formatDate(p.conferredEpochDay)}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = onDiploma) { Text("Diploma") }
                    }
                }
                complete -> {
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onConfer) { Text("All requirements met — confer!") }
                }
            }
        }
    }
}

@Composable
private fun ProgramDialog(
    data: AppData,
    initial: Program,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (Program) -> Unit,
    onDelete: () -> Unit,
) {
    var p by remember { mutableStateOf(initial) }
    var creditsText by remember { mutableStateOf(initial.creditsRequired.toString()) }
    FormDialog(
        title = if (isNew) "Declare a program" else "Edit program",
        onDismiss = onDismiss,
        onSave = { onSave(p.copy(name = p.name.trim(), creditsRequired = creditsText.toIntOrNull() ?: 0)) },
        saveEnabled = p.name.isNotBlank(),
        onDelete = if (isNew) null else onDelete,
    ) {
        Dropdown("Kind", ProgramKind.entries, p.kind, { it.label }, {
            p = p.copy(kind = it)
            creditsText = when (it) {
                ProgramKind.CERTIFICATE -> "12"
                ProgramKind.MINOR -> "18"
                ProgramKind.MAJOR -> "36"
                ProgramKind.ASSOCIATE -> "60"
                ProgramKind.BACHELOR -> "120"
                ProgramKind.MASTER -> "30"
                ProgramKind.DOCTORATE -> "60"
            }
        })
        OutlinedTextField(p.name, { p = p.copy(name = it) }, label = { Text("Field (e.g. Philosophy)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            creditsText,
            { creditsText = it.filter(Char::isDigit).take(3) },
            label = { Text("Credits required") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Dropdown(
            "Credits count from",
            listOf<Department?>(null) + data.departments,
            data.departments.find { it.id == p.departmentId },
            { it?.let { d -> "${d.name} courses" } ?: "Required courses only (or all, if none)" },
            { p = p.copy(departmentId = it?.id) },
        )
        Text("Required courses", style = MaterialTheme.typography.labelLarge)
        if (data.courses.isEmpty()) Text("No courses in the catalog yet.", style = MaterialTheme.typography.bodySmall)
        data.courses.sortedBy { data.codeOf(it) }.forEach { c ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = c.id in p.courseIds,
                    onCheckedChange = { p = p.copy(courseIds = if (it) p.courseIds + c.id else p.courseIds - c.id) },
                )
                Text("${data.codeOf(c)} — ${c.title}", style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (p.conferredEpochDay != null) {
            TextButton(onClick = { p = p.copy(conferredEpochDay = null) }) { Text("Revoke conferral") }
        }
    }
}

@Composable
private fun DiplomaDialog(data: AppData, p: Program, onDismiss: () -> Unit) {
    val parchment = Color(0xFFFBF5E6)
    val ink = Color(0xFF2A2418)
    val navy = Color(0xFF1F3A5F)
    val honors = latinHonors(recordStats(p.countingCourses(data)).gpa)
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            color = parchment,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
        ) {
            Box(Modifier.padding(8.dp).border(3.dp, Gold, RoundedCornerShape(4.dp)).padding(4.dp).border(1.dp, Gold, RoundedCornerShape(2.dp))) {
                Column(
                    Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(data.profile.institution, style = MaterialTheme.typography.headlineSmall, color = navy, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "upon the recommendation of the faculty\nhas conferred upon",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        color = ink,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        data.profile.studentName.ifBlank { "Student" },
                        style = MaterialTheme.typography.displaySmall,
                        fontStyle = FontStyle.Italic,
                        color = ink,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(p.kind.diplomaPhrase, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Serif, color = ink)
                    Spacer(Modifier.height(4.dp))
                    Text(p.name, style = MaterialTheme.typography.headlineMedium, color = navy, textAlign = TextAlign.Center)
                    honors?.let {
                        Text(it, style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, color = ink)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "with all the rights, honors and privileges thereunto appertaining.",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Serif,
                        color = ink,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(20.dp))
                    Box(
                        Modifier.size(72.dp).clip(CircleShape).border(3.dp, Gold, CircleShape).padding(6.dp).border(1.dp, Gold, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Filled.School, null, tint = Gold, modifier = Modifier.size(34.dp)) }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Given ${formatDate(p.conferredEpochDay ?: LocalDate.now().toEpochDay())}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Serif,
                        color = ink,
                    )
                    if (data.profile.motto.isNotBlank()) {
                        Text(data.profile.motto, style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic, color = ink.copy(alpha = 0.7f), textAlign = TextAlign.Center)
                    }
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = onDismiss) { Text("Close", color = navy) }
                }
            }
        }
    }
}

