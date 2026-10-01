package com.z1fire.alma.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.z1fire.alma.data.AppData
import com.z1fire.alma.data.Meeting
import com.z1fire.alma.data.bulletin.Bulletin
import com.z1fire.alma.data.bulletin.EnrollPlan
import com.z1fire.alma.data.bulletin.TemplateCourse
import com.z1fire.alma.data.bulletin.TemplateDept
import com.z1fire.alma.data.bulletin.hasCourse
import com.z1fire.alma.data.codeOf
import com.z1fire.alma.data.levelLabel
import com.z1fire.alma.data.newId
import com.z1fire.alma.ui.ColorDot
import com.z1fire.alma.ui.DateField
import com.z1fire.alma.ui.FormDialog
import com.z1fire.alma.ui.LocalRepository
import com.z1fire.alma.ui.SectionTitle
import com.z1fire.alma.ui.TimeField
import com.z1fire.alma.ui.dayName
import com.z1fire.alma.ui.formatMinutes
import com.z1fire.alma.ui.rememberAppData
import com.z1fire.alma.ui.theme.deptColor
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Browse ready-made course designs and copy them into your own catalog. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulletinScreen(onBack: () -> Unit, onOpenCourse: (String) -> Unit) {
    val repo = LocalRepository.current
    val data = rememberAppData()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var query by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(listOf<String>()) }
    var preview by remember { mutableStateOf<Pair<TemplateDept, TemplateCourse>?>(null) }

    fun announce(message: String, openId: String?) {
        scope.launch {
            val result = snackbar.showSnackbar(message, actionLabel = openId?.let { "Open" }, duration = SnackbarDuration.Short)
            if (result == SnackbarResult.ActionPerformed && openId != null) onOpenCourse(openId)
        }
    }

    fun quickAdd(dept: TemplateDept, course: TemplateCourse) {
        val ids = repo.addFromBulletin(dept, listOf(course))
        announce("${course.code} added to your catalog as planned.", ids.firstOrNull())
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Course Bulletin")
                        Text(
                            "${Bulletin.courseCount} courses · ${Bulletin.departments.size} departments",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    "Ready-made course designs with syllabi, reading lists and assignments. Add one to your catalog and make it your own — everything stays editable.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                OutlinedTextField(
                    query,
                    { query = it },
                    placeholder = { Text("Search: guitar, kanji, CRISPR, orbits…") },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (query.isNotBlank()) {
                val q = query.trim()
                val hits = Bulletin.departments.flatMap { d -> d.courses.map { d to it } }.filter { (d, c) ->
                    listOf(c.code, c.title, c.description, d.name).any { it.contains(q, ignoreCase = true) } ||
                        c.units.any { it.first.contains(q, ignoreCase = true) || it.second.contains(q, ignoreCase = true) }
                }
                if (hits.isEmpty()) {
                    item { Text("No courses match \"$q\".", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                items(hits, key = { it.second.code }) { (d, c) ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                        TemplateRow(data, d, c, onPreview = { preview = d to c }, onQuickAdd = { quickAdd(d, c) }, onOpenCourse = onOpenCourse)
                    }
                }
            } else {
                items(Bulletin.departments, key = { it.code }) { d ->
                    DeptCard(
                        data = data,
                        dept = d,
                        open = d.code in expanded,
                        onToggle = { expanded = if (d.code in expanded) expanded - d.code else expanded + d.code },
                        onPreview = { c -> preview = d to c },
                        onQuickAdd = { c -> quickAdd(d, c) },
                        onAddAll = {
                            val ids = repo.addFromBulletin(d, d.courses, withPrograms = true)
                            announce(
                                if (ids.isEmpty()) "${d.name} is already in your catalog — programs declared."
                                else "Added ${ids.size} ${d.name} course${if (ids.size == 1) "" else "s"} and declared its programs.",
                                null,
                            )
                        },
                        onOpenCourse = onOpenCourse,
                    )
                }
            }
        }
    }

    preview?.let { (d, c) ->
        TemplatePreviewDialog(
            data = data,
            dept = d,
            course = c,
            onDismiss = { preview = null },
            onOpenCourse = {
                preview = null
                onOpenCourse(it)
            },
            onAdd = { plan ->
                val ids = repo.addFromBulletin(d, listOf(c), enroll = plan)
                preview = null
                announce(
                    if (plan != null) "Enrolled in ${c.code}. Classes begin ${if (plan.start == LocalDate.now()) "today" else "soon"}!"
                    else "${c.code} added to your catalog as planned.",
                    ids.firstOrNull(),
                )
            },
        )
    }
}

@Composable
private fun DeptCard(
    data: AppData,
    dept: TemplateDept,
    open: Boolean,
    onToggle: () -> Unit,
    onPreview: (TemplateCourse) -> Unit,
    onQuickAdd: (TemplateCourse) -> Unit,
    onAddAll: () -> Unit,
    onOpenCourse: (String) -> Unit,
) {
    val have = dept.courses.count { data.hasCourse(it.code) }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column {
            Row(
                Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ColorDot(deptColor(dept.colorIndex), 14)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("${dept.name}  ·  ${dept.code}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(dept.blurb, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = if (open) 4 else 2, overflow = TextOverflow.Ellipsis)
                    Text(
                        "${dept.courses.size} courses" + if (have > 0) " · $have in your catalog" else "",
                        style = MaterialTheme.typography.labelMedium,
                        color = deptColor(dept.colorIndex),
                    )
                }
                Icon(if (open) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, if (open) "Collapse" else "Expand")
            }
            if (open) {
                HorizontalDivider()
                dept.courses.forEach { c ->
                    TemplateRow(data, dept, c, onPreview = { onPreview(c) }, onQuickAdd = { onQuickAdd(c) }, onOpenCourse = onOpenCourse)
                }
                if (dept.programs.isNotEmpty()) {
                    Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                        Text("Programs", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                        dept.programs.forEach { p ->
                            val credits = p.numbers.sumOf { n -> dept.courses.find { it.number == n }?.credits ?: 0 }
                            Text(
                                "${p.kind.label} in ${p.name} — $credits credits (${p.numbers.joinToString { "${dept.code} $it" }})",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
                FilledTonalButton(
                    onClick = onAddAll,
                    enabled = have < dept.courses.size || dept.programs.any { tp -> data.programs.none { it.name == tp.name && it.kind == tp.kind } },
                    modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                ) {
                    Text(if (have == 0) "Add the whole department" else "Add the rest of ${dept.code}")
                }
            }
        }
    }
}

@Composable
private fun TemplateRow(
    data: AppData,
    dept: TemplateDept,
    course: TemplateCourse,
    onPreview: () -> Unit,
    onQuickAdd: () -> Unit,
    onOpenCourse: (String) -> Unit,
) {
    val existing = data.courses.find { data.codeOf(it).equals(course.code, ignoreCase = true) }
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onPreview).padding(start = 14.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(course.code, style = MaterialTheme.typography.labelLarge, color = deptColor(dept.colorIndex), fontWeight = FontWeight.Bold)
            Text(course.title, style = MaterialTheme.typography.titleSmall)
            val meta = listOfNotNull(
                "${course.credits} cr",
                "${course.weeks} wk",
                levelLabel(course.number),
                course.prereqs.takeIf { it.isNotEmpty() }?.let { "Requires ${it.joinToString()}" },
            ).joinToString(" · ")
            Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (existing != null) {
            IconButton(onClick = { onOpenCourse(existing.id) }) {
                Icon(Icons.Filled.CheckCircle, "In your catalog — open", tint = MaterialTheme.colorScheme.tertiary)
            }
        } else {
            IconButton(onClick = onQuickAdd) {
                Icon(Icons.Filled.AddCircleOutline, "Add to catalog", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TemplatePreviewDialog(
    data: AppData,
    dept: TemplateDept,
    course: TemplateCourse,
    onDismiss: () -> Unit,
    onOpenCourse: (String) -> Unit,
    onAdd: (EnrollPlan?) -> Unit,
) {
    val existing = data.courses.find { data.codeOf(it).equals(course.code, ignoreCase = true) }
    var enrollNow by remember { mutableStateOf(false) }
    var start by remember { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    var days by remember { mutableStateOf(setOf<Int>()) }
    var time by remember { mutableIntStateOf(19 * 60) }
    var duration by remember { mutableIntStateOf(60) }

    FormDialog(
        title = "${course.code}: ${course.title}",
        onDismiss = onDismiss,
        saveLabel = when {
            existing != null -> "Open course"
            enrollNow -> "Enroll"
            else -> "Add to catalog"
        },
        onSave = {
            when {
                existing != null -> onOpenCourse(existing.id)
                enrollNow -> onAdd(
                    EnrollPlan(
                        start = LocalDate.ofEpochDay(start),
                        meetings = days.sorted().map { Meeting(id = newId(), dayOfWeek = it, startMinute = time, durationMinutes = duration, label = "Class") },
                    ),
                )
                else -> onAdd(null)
            }
        },
    ) {
        Text(
            "${dept.name} · ${course.credits} credits · ${course.weeks} weeks",
            style = MaterialTheme.typography.labelLarge,
            color = deptColor(dept.colorIndex),
        )
        Text(course.description, style = MaterialTheme.typography.bodyMedium)
        if (course.prereqs.isNotEmpty()) {
            Text(
                "Prerequisites: " + course.prereqs.joinToString { code ->
                    code + if (data.hasCourse(code)) " ✓" else ""
                },
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
            )
        }
        SectionTitle("You will be able to")
        course.objectives.forEach { Text("•  $it", style = MaterialTheme.typography.bodySmall) }
        SectionTitle("Syllabus · ${course.units.size} units")
        Text(
            course.units.mapIndexed { i, u -> "${i + 1}. ${u.first}" }.joinToString("\n"),
            style = MaterialTheme.typography.bodySmall,
        )
        SectionTitle("Reading list")
        course.resources.forEach { r ->
            Text(
                "${if (r.required) "●" else "○"}  ${r.title}" + if (r.author.isNotBlank()) " — ${r.author}" else "",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (course.tasks.isNotEmpty()) {
            SectionTitle("Assignments")
            course.tasks.forEach { Text("Week ${it.week}: ${it.title}", style = MaterialTheme.typography.bodySmall) }
        }

        if (existing == null) {
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            Row(Modifier.fillMaxWidth().selectable(selected = !enrollNow, onClick = { enrollNow = false }), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = !enrollNow, onClick = { enrollNow = false })
                Text("Add to my catalog as planned")
            }
            Row(Modifier.fillMaxWidth().selectable(selected = enrollNow, onClick = { enrollNow = true }), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = enrollNow, onClick = { enrollNow = true })
                Text("Enroll now and set a schedule")
            }
            if (enrollNow) {
                DateField("Start date", start, { it?.let { d -> start = d } })
                Text("Class days", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..7).forEach { d ->
                        FilterChip(selected = d in days, onClick = { days = if (d in days) days - d else days + d }, label = { Text(dayName(d)) })
                    }
                }
                if (days.isNotEmpty()) {
                    TimeField("Class time", time, { time = it })
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(30, 45, 60, 90, 120).forEach { m ->
                            FilterChip(selected = duration == m, onClick = { duration = m }, label = { Text(formatMinutes(m)) })
                        }
                    }
                }
                Text(
                    "Assignment due dates are set from the start date. You can change anything later.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CheckCircle, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.tertiary)
                Spacer(Modifier.width(6.dp))
                Text("Already in your catalog (${existing.status.label.lowercase()}).", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
