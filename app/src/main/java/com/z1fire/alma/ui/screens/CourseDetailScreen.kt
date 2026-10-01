package com.z1fire.alma.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.z1fire.alma.data.AppData
import com.z1fire.alma.data.Assignment
import com.z1fire.alma.data.Course
import com.z1fire.alma.data.CourseStatus
import com.z1fire.alma.data.Grades
import com.z1fire.alma.data.Module
import com.z1fire.alma.data.Resource
import com.z1fire.alma.data.ResourceStatus
import com.z1fire.alma.data.ResourceType
import com.z1fire.alma.data.StudySession
import com.z1fire.alma.data.Terms
import com.z1fire.alma.data.codeOf
import com.z1fire.alma.data.course
import com.z1fire.alma.data.department
import com.z1fire.alma.data.levelLabel
import com.z1fire.alma.data.upsert
import com.z1fire.alma.ui.ConfirmDialog
import com.z1fire.alma.ui.EmptyState
import com.z1fire.alma.ui.LocalRepository
import com.z1fire.alma.ui.Pill
import com.z1fire.alma.ui.SectionTitle
import com.z1fire.alma.ui.Stat
import com.z1fire.alma.ui.StatusChip
import com.z1fire.alma.ui.dayName
import com.z1fire.alma.ui.formatDate
import com.z1fire.alma.ui.formatMinutes
import com.z1fire.alma.ui.formatShortDate
import com.z1fire.alma.ui.formatTimeRange
import com.z1fire.alma.ui.rememberAppData
import com.z1fire.alma.ui.relativeDue
import com.z1fire.alma.ui.theme.deptColor
import kotlinx.coroutines.launch
import java.time.LocalDate

private val tabTitles = listOf("Overview", "Syllabus", "Readings", "Assignments", "Study log")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onOpenCourse: (String) -> Unit,
) {
    val repo = LocalRepository.current
    val data = rememberAppData()
    val course = data.course(courseId)
    if (course == null) {
        // Deleted (or a stale reminder link): show a placeholder rather than auto-popping, which
        // could double-pop while the exit transition is still composing this screen.
        Scaffold(topBar = {
            TopAppBar(
                title = { Text("Course") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
            )
        }) { padding ->
            EmptyState(Icons.Filled.School, "Course not found", "It may have been deleted.", Modifier.padding(padding))
        }
        return
    }
    val accent = deptColor(data.department(course.departmentId)?.colorIndex)
    val today = LocalDate.now()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var menuOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var completing by remember { mutableStateOf(false) }
    var logging by remember { mutableStateOf(false) }
    var editModule by remember { mutableStateOf<Module?>(null) }
    var editResource by remember { mutableStateOf<Resource?>(null) }
    var editAssignment by remember { mutableStateOf<Assignment?>(null) }

    fun update(f: (Course) -> Course) = repo.updateCourse(course.id, f)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(data.codeOf(course)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = { onEdit(course.id) }) { Icon(Icons.Filled.Edit, "Edit course") }
                    Box {
                        IconButton(onClick = { menuOpen = true }) { Icon(Icons.Filled.MoreVert, "More") }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(text = { Text("Duplicate as new plan") }, onClick = {
                                menuOpen = false
                                repo.duplicateCourse(course.id)?.let(onOpenCourse)
                            })
                            if (course.status == CourseStatus.ENROLLED) {
                                DropdownMenuItem(text = { Text("Drop course") }, onClick = {
                                    menuOpen = false
                                    update { it.copy(status = CourseStatus.DROPPED) }
                                })
                            }
                            if (course.status == CourseStatus.COMPLETED) {
                                DropdownMenuItem(text = { Text("Reopen course") }, onClick = {
                                    menuOpen = false
                                    update { it.copy(status = CourseStatus.ENROLLED, finalGrade = null, completedEpochDay = null) }
                                })
                            }
                            if (course.status != CourseStatus.PLANNED) {
                                DropdownMenuItem(text = { Text("Move back to planned") }, onClick = {
                                    menuOpen = false
                                    update { it.copy(status = CourseStatus.PLANNED, finalGrade = null, completedEpochDay = null) }
                                })
                            }
                            DropdownMenuItem(
                                text = { Text("Delete course", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    menuOpen = false
                                    confirmDelete = true
                                },
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            val (label, action) = when (tab) {
                1 -> "Add unit" to { editModule = Module(title = "") }
                2 -> "Add reading" to { editResource = Resource(title = "") }
                3 -> "Add assignment" to { editAssignment = Assignment(title = "") }
                4 -> "Log session" to { logging = true }
                else -> null to {}
            }
            if (label != null) {
                ExtendedFloatingActionButton(onClick = action, icon = { Icon(Icons.Filled.Add, null) }, text = { Text(label) })
            }
        },
    ) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(bottom = 100.dp)) {
            item { CourseHeader(data, course, accent) }
            item {
                ActionRow(
                    data = data,
                    course = course,
                    onEnroll = {
                        update {
                            it.copy(
                                status = CourseStatus.ENROLLED,
                                term = it.term.ifBlank { Terms.forDate(today) },
                                startEpochDay = it.startEpochDay ?: today.toEpochDay(),
                            )
                        }
                        scope.launch { snackbar.showSnackbar("Enrolled in ${data.codeOf(course)}. Classes begin!") }
                    },
                    onStart = { repo.startTimer(course.id) },
                    onLog = { logging = true },
                    onComplete = { completing = true },
                )
            }
            item {
                ScrollableTabRow(selectedTabIndex = tab, edgePadding = 16.dp, containerColor = MaterialTheme.colorScheme.surface) {
                    tabTitles.forEachIndexed { i, t -> Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t) }) }
                }
            }
            when (tab) {
                0 -> overviewTab(data, course, today, onOpenCourse)
                1 -> syllabusTab(course, onToggle = { m ->
                    update { c -> c.copy(modules = c.modules.map { if (it.id == m.id) it.copy(done = !it.done) else it }) }
                }, onEdit = { editModule = it }, onMove = { m, delta ->
                    update { c ->
                        val list = c.modules.toMutableList()
                        val i = list.indexOfFirst { it.id == m.id }
                        val j = i + delta
                        if (i >= 0 && j in list.indices) list.add(j, list.removeAt(i))
                        c.copy(modules = list)
                    }
                })
                2 -> readingsTab(course, onEdit = { editResource = it }, onCycle = { r ->
                    val next = ResourceStatus.entries[(r.status.ordinal + 1) % ResourceStatus.entries.size]
                    update { c -> c.copy(resources = c.resources.map { if (it.id == r.id) it.copy(status = next) else it }) }
                })
                3 -> assignmentsTab(course, today, onEdit = { editAssignment = it }, onToggle = { a ->
                    update { c -> c.copy(assignments = c.assignments.map { if (it.id == a.id) it.copy(done = !it.done) else it }) }
                })
                4 -> studyLogTab(course, today, onDelete = { s ->
                    update { c -> c.copy(sessions = c.sessions.filter { it.id != s.id }) }
                })
            }
        }
    }

    editModule?.let { m ->
        val isNew = course.modules.none { it.id == m.id }
        ModuleDialog(m, isNew, onDismiss = { editModule = null }, onSave = { saved ->
            update { it.copy(modules = it.modules.upsert(saved) { x -> x.id }) }
            editModule = null
        }, onDelete = {
            update { it.copy(modules = it.modules.filter { x -> x.id != m.id }) }
            editModule = null
        })
    }
    editResource?.let { r ->
        val isNew = course.resources.none { it.id == r.id }
        ResourceDialog(r, isNew, onDismiss = { editResource = null }, onSave = { saved ->
            update { it.copy(resources = it.resources.upsert(saved) { x -> x.id }) }
            editResource = null
        }, onDelete = {
            update { it.copy(resources = it.resources.filter { x -> x.id != r.id }) }
            editResource = null
        })
    }
    editAssignment?.let { a ->
        val isNew = course.assignments.none { it.id == a.id }
        AssignmentDialog(a, isNew, onDismiss = { editAssignment = null }, onSave = { saved ->
            update { it.copy(assignments = it.assignments.upsert(saved) { x -> x.id }) }
            editAssignment = null
        }, onDelete = {
            update { it.copy(assignments = it.assignments.filter { x -> x.id != a.id }) }
            editAssignment = null
        })
    }
    if (logging) {
        LogSessionDialog(onDismiss = { logging = false }, onSave = { day, minutes, notes ->
            update { it.copy(sessions = it.sessions + StudySession(epochDay = day, minutes = minutes, notes = notes)) }
            logging = false
            scope.launch { snackbar.showSnackbar("Logged ${formatMinutes(minutes)} of study.") }
        })
    }
    if (completing) {
        CompleteCourseDialog(course, onDismiss = { completing = false }, onComplete = { grade, reflection, day ->
            update { it.copy(status = CourseStatus.COMPLETED, finalGrade = grade, reflection = reflection, completedEpochDay = day) }
            if (data.activeTimer?.courseId == course.id) repo.cancelTimer()
            completing = false
            val msg = if (Grades.earnsCredit(grade)) {
                "Congratulations! ${course.credits} credits earned with a $grade."
            } else {
                "Course closed out with a $grade."
            }
            scope.launch { snackbar.showSnackbar(msg) }
        })
    }
    if (confirmDelete) {
        ConfirmDialog(
            title = "Delete ${data.codeOf(course)}?",
            text = "This removes the course, its syllabus, reading list, assignments and study log. It can't be undone.",
            confirmLabel = "Delete",
            onConfirm = {
                onBack()
                repo.deleteCourse(course.id)
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun CourseHeader(data: AppData, course: Course, accent: Color) {
    val dept = data.department(course.departmentId)
    Column(
        Modifier
            .fillMaxWidth()
            .background(accent.copy(alpha = 0.10f))
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                listOfNotNull(dept?.name ?: "General Studies", levelLabel(course.number)).joinToString(" · ").uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = accent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            StatusChip(course.status)
        }
        Spacer(Modifier.height(4.dp))
        Text(course.title.ifBlank { "Untitled course" }, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text(
            listOf(
                "${course.credits} credit${if (course.credits == 1) "" else "s"}",
                course.term.ifBlank { "Unscheduled" },
            ).joinToString(" · "),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (course.status == CourseStatus.COMPLETED) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(course.finalGrade ?: "—", style = MaterialTheme.typography.displaySmall, color = accent)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Final grade", style = MaterialTheme.typography.labelLarge)
                    course.completedEpochDay?.let {
                        Text("Completed ${formatDate(it)}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        } else if (course.status == CourseStatus.ENROLLED) {
            Spacer(Modifier.height(12.dp))
            val total = course.modules.size + course.assignments.size
            val done = course.modules.count { it.done } + course.assignments.count { it.done }
            LinearProgressIndicator(
                progress = { course.progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = accent,
                trackColor = accent.copy(alpha = 0.2f),
                drawStopIndicator = {},
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (total == 0) "Add syllabus units and assignments to track progress" else "$done of $total units & assignments complete",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActionRow(
    data: AppData,
    course: Course,
    onEnroll: () -> Unit,
    onStart: () -> Unit,
    onLog: () -> Unit,
    onComplete: () -> Unit,
) {
    val repo = LocalRepository.current
    val missingPrereqs = course.prerequisiteIds.mapNotNull { data.course(it) }.filter { it.status != CourseStatus.COMPLETED }
    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            when (course.status) {
                CourseStatus.PLANNED -> Button(onClick = onEnroll) { Text("Enroll") }
                CourseStatus.DROPPED -> Button(onClick = onEnroll) { Text("Re-enroll") }
                CourseStatus.ENROLLED -> {
                    val timer = data.activeTimer
                    when {
                        timer == null -> Button(onClick = onStart) { Text("Start session") }
                        timer.courseId == course.id -> OutlinedButton(onClick = {}, enabled = false) { Text("Session running…") }
                        else -> {}
                    }
                    OutlinedButton(onClick = onLog) { Text("Log study") }
                    OutlinedButton(onClick = onComplete) { Text("Complete course") }
                }
                CourseStatus.COMPLETED -> OutlinedButton(onClick = onLog) { Text("Log review session") }
            }
        }
        if (course.status == CourseStatus.ENROLLED && data.activeTimer?.courseId == course.id) {
            Text(
                "Session in progress — end it from Campus.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(onClick = { repo.cancelTimer() }) { Text("Discard session") }
        }
        if (course.status == CourseStatus.PLANNED && missingPrereqs.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Lock, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(6.dp))
                Text(
                    "Prerequisite${if (missingPrereqs.size > 1) "s" else ""} not yet completed: " +
                        missingPrereqs.joinToString { data.codeOf(it) } + ". You can still enroll.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

// ---------------- Tabs ----------------

private fun LazyListScope.block(title: String, content: @Composable () -> Unit) {
    item {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            SectionTitle(title)
            Spacer(Modifier.height(6.dp))
            content()
        }
    }
}

private fun LazyListScope.hint(text: String) {
    item {
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
        )
    }
}

private fun LazyListScope.overviewTab(data: AppData, course: Course, today: LocalDate, onOpenCourse: (String) -> Unit) {
    block("Catalog description") {
        Text(
            course.description.ifBlank { "No description yet. Tap the pencil to write one — what is this course about, and why are you taking it?" },
            style = MaterialTheme.typography.bodyLarge,
            fontStyle = if (course.description.isBlank()) FontStyle.Italic else FontStyle.Normal,
        )
    }
    if (course.objectives.isNotEmpty()) {
        block("Learning objectives") {
            Text("By the end of this course you will be able to:", style = MaterialTheme.typography.bodyMedium)
            course.objectives.forEachIndexed { i, o ->
                Row(Modifier.padding(top = 4.dp)) {
                    Text("${i + 1}.", Modifier.width(24.dp), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(o, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
    val prereqs = course.prerequisiteIds.mapNotNull { data.course(it) }
    val unlocks = data.courses.filter { course.id in it.prerequisiteIds }
    if (prereqs.isNotEmpty() || unlocks.isNotEmpty()) {
        block("Course sequence") {
            prereqs.forEach { p ->
                SequenceRow("Requires", data.codeOf(p), p.title, p.status == CourseStatus.COMPLETED) { onOpenCourse(p.id) }
            }
            unlocks.forEach { u ->
                SequenceRow("Leads to", data.codeOf(u), u.title, u.status == CourseStatus.COMPLETED) { onOpenCourse(u.id) }
            }
        }
    }
    block("Class meetings") {
        if (course.meetings.isEmpty()) {
            Text("No regular meetings set. Add study blocks in the course editor so they show up on your schedule.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        course.meetings.sortedWith(compareBy({ it.dayOfWeek }, { it.startMinute })).forEach { m ->
            Text(
                "${dayName(m.dayOfWeek)}  ${formatTimeRange(m.startMinute, m.durationMinutes)} · ${m.label}" +
                    if (m.location.isNotBlank()) " · ${m.location}" else "",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 2.dp),
            )
        }
        val weekly = course.meetings.sumOf { it.durationMinutes }
        if (weekly > 0) {
            Text("${formatMinutes(weekly)} of class time per week", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    block("Calendar") {
        val start = course.startEpochDay
        val end = course.endEpochDay
        Text("Starts: ${start?.let { formatDate(it) } ?: "—"}", style = MaterialTheme.typography.bodyMedium)
        Text("Ends: ${end?.let { formatDate(it) } ?: "—"}", style = MaterialTheme.typography.bodyMedium)
        if (start != null && end != null && end >= start) {
            val weeks = (end - start + 1 + 6) / 7
            val left = end - today.toEpochDay()
            Text(
                "$weeks-week course" + when {
                    course.status != CourseStatus.ENROLLED -> ""
                    left >= 0 -> " · $left day${if (left == 1L) "" else "s"} left"
                    else -> " · past the end date"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    if (course.status == CourseStatus.COMPLETED && course.reflection.isNotBlank()) {
        block("Reflection") {
            Text(course.reflection, style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic)
        }
    }
}

@Composable
private fun SequenceRow(kind: String, code: String, title: String, done: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (done) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                null,
                tint = if (done) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("$kind · $code", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(title, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

private fun LazyListScope.syllabusTab(
    course: Course,
    onToggle: (Module) -> Unit,
    onEdit: (Module) -> Unit,
    onMove: (Module, Int) -> Unit,
) {
    if (course.modules.isEmpty()) {
        hint("Break the course into units — one per week, chapter, or big idea. Check them off as you finish.")
        return
    }
    item {
        Text(
            "${course.modules.count { it.done }} of ${course.modules.size} units complete",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp),
        )
    }
    itemsIndexed(course.modules, key = { _, m -> m.id }) { i, m ->
        var menu by remember { mutableStateOf(false) }
        Row(
            Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 4.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Checkbox(checked = m.done, onCheckedChange = { onToggle(m) })
            Column(Modifier.weight(1f).padding(top = 10.dp)) {
                Text("UNIT ${i + 1}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Text(
                    m.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (m.done) TextDecoration.LineThrough else null,
                )
                if (m.notes.isNotBlank()) {
                    Text(m.notes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreVert, "Unit options") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text("Edit") }, onClick = { menu = false; onEdit(m) })
                    if (i > 0) DropdownMenuItem(text = { Text("Move up") }, onClick = { menu = false; onMove(m, -1) })
                    if (i < course.modules.lastIndex) DropdownMenuItem(text = { Text("Move down") }, onClick = { menu = false; onMove(m, 1) })
                }
            }
        }
        HorizontalDivider(Modifier.padding(start = 56.dp, end = 16.dp, top = 6.dp))
    }
}

private fun ResourceType.icon(): ImageVector = when (this) {
    ResourceType.BOOK, ResourceType.TEXTBOOK -> Icons.AutoMirrored.Filled.MenuBook
    ResourceType.ARTICLE -> Icons.AutoMirrored.Filled.Article
    ResourceType.PAPER -> Icons.Filled.Science
    ResourceType.VIDEO, ResourceType.LECTURES -> Icons.Filled.PlayCircle
    ResourceType.PODCAST -> Icons.Filled.Headphones
    ResourceType.ONLINE_COURSE -> Icons.Filled.School
    ResourceType.WEBSITE, ResourceType.OTHER -> Icons.Filled.Language
}

private fun LazyListScope.readingsTab(course: Course, onEdit: (Resource) -> Unit, onCycle: (Resource) -> Unit) {
    if (course.resources.isEmpty()) {
        hint("List the books, articles, lectures and sites for this course. Mark them required or recommended, and track what you've finished.")
        return
    }
    item {
        Text(
            "${course.resources.count { it.status == ResourceStatus.DONE }} of ${course.resources.size} finished · tap a status to advance it",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp),
        )
    }
    listOf(true to "Required", false to "Recommended").forEach { (req, label) ->
        val list = course.resources.filter { it.required == req }
        if (list.isNotEmpty()) {
            item { SectionTitle(label, Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)) }
            list.forEach { r -> item(key = r.id) { ResourceRow(r, onEdit, onCycle) } }
        }
    }
}

@Composable
private fun ResourceRow(r: Resource, onEdit: (Resource) -> Unit, onCycle: (Resource) -> Unit) {
    val uri = LocalUriHandler.current
    Card(
        onClick = { onEdit(r) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(r.type.icon(), null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(r.title, style = MaterialTheme.typography.titleSmall)
                Text(
                    listOf(r.type.label, r.author).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (r.notes.isNotBlank()) {
                    Text(r.notes, style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
                }
                Spacer(Modifier.height(6.dp))
                val (bg, fg) = when (r.status) {
                    ResourceStatus.NOT_STARTED -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                    ResourceStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                    ResourceStatus.DONE -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
                }
                Pill(r.status.label, bg, fg, Modifier.clip(RoundedCornerShape(50)).clickable { onCycle(r) })
            }
            if (r.url.isNotBlank()) {
                IconButton(onClick = { runCatching { uri.openUri(r.url) } }) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, "Open link")
                }
            }
        }
    }
}

private fun LazyListScope.assignmentsTab(course: Course, today: LocalDate, onEdit: (Assignment) -> Unit, onToggle: (Assignment) -> Unit) {
    if (course.assignments.isEmpty()) {
        hint("Give yourself real coursework: reading checkpoints, problem sets, essays, projects, self-quizzes. Due dates appear on your schedule.")
        return
    }
    val sorted = course.assignments.sortedWith(compareBy({ it.done }, { it.dueEpochDay ?: Long.MAX_VALUE }))
    item {
        Text(
            "${course.assignments.count { it.done }} of ${course.assignments.size} submitted",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
        )
    }
    sorted.forEach { a ->
        item(key = a.id) {
            Card(
                onClick = { onEdit(a) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                Row(Modifier.padding(end = 12.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = a.done, onCheckedChange = { onToggle(a) })
                    Column(Modifier.weight(1f)) {
                        Text(
                            a.title,
                            style = MaterialTheme.typography.titleSmall,
                            textDecoration = if (a.done) TextDecoration.LineThrough else null,
                        )
                        val overdue = !a.done && a.dueEpochDay != null && a.dueEpochDay < today.toEpochDay()
                        Text(
                            listOfNotNull(
                                a.type.label,
                                a.dueEpochDay?.let { if (a.done) "Due ${formatShortDate(it)}" else relativeDue(it, today) },
                            ).joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (overdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (a.grade.isNotBlank()) {
                        Pill(a.grade, MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
            }
        }
    }
}

private fun LazyListScope.studyLogTab(course: Course, today: LocalDate, onDelete: (StudySession) -> Unit) {
    item {
        val weekStart = today.with(java.time.DayOfWeek.MONDAY).toEpochDay()
        val thisWeek = course.sessions.filter { it.epochDay >= weekStart }.sumOf { it.minutes }
        Row(Modifier.fillMaxWidth().padding(16.dp)) {
            Stat(formatMinutes(course.totalMinutes), "TOTAL", Modifier.weight(1f))
            Stat("${course.sessions.size}", "SESSIONS", Modifier.weight(1f))
            Stat(formatMinutes(thisWeek), "THIS WEEK", Modifier.weight(1f))
        }
    }
    if (course.sessions.isEmpty()) {
        hint("Every study session counts as attendance. Start a timed session or log one after the fact.")
        return
    }
    course.sessions.sortedByDescending { it.epochDay }.forEach { s ->
        item(key = s.id) {
            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("${formatShortDate(s.epochDay)} · ${formatMinutes(s.minutes)}", style = MaterialTheme.typography.titleSmall)
                    if (s.notes.isNotBlank()) {
                        Text(s.notes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = { onDelete(s) }) {
                    Icon(Icons.Filled.Delete, "Delete session", tint = MaterialTheme.colorScheme.outline)
                }
            }
            HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }
    }
}
