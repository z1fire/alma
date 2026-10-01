package com.z1fire.alma.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.z1fire.alma.data.StudyItem
import com.z1fire.alma.ui.ConfirmDialog
import com.z1fire.alma.ui.EmptyState
import com.z1fire.alma.ui.LocalRepository
import com.z1fire.alma.ui.SectionTitle
import com.z1fire.alma.ui.Stat
import com.z1fire.alma.ui.formatDate
import com.z1fire.alma.ui.formatMinutes
import com.z1fire.alma.ui.rememberAppData
import com.z1fire.alma.ui.theme.deptColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScreen(courseId: String, onBack: () -> Unit) {
    val repo = LocalRepository.current
    val data = rememberAppData()
    val course = data.course(courseId)
    if (course == null) {
        // Deleted (or a stale widget link): show a placeholder rather than auto-popping, which
        // could double-pop while the exit transition is still composing this screen.
        Scaffold(topBar = {
            TopAppBar(title = {}, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } })
        }) { padding ->
            EmptyState(Icons.Filled.AutoStories, "Course not found", "It may have been deleted.", Modifier.padding(padding))
        }
        return
    }
    val accent = deptColor(course.colorIndex)
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var menuOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }
    var editingTime by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var finishing by remember { mutableStateOf(false) }
    var certificate by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<StudyItem?>(null) }
    var newItem by rememberSaveable { mutableStateOf("") }

    fun addItem() {
        if (newItem.isBlank()) return
        val text = newItem.trim()
        repo.updateCourse(course.id) { it.copy(items = it.items + StudyItem(text = text)) }
        newItem = ""
    }

    fun addTime(minutes: Int) {
        val before = course
        repo.addMinutes(course.id, minutes)
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            val result = snackbar.showSnackbar("Added ${formatMinutes(minutes)}", actionLabel = "Undo", duration = SnackbarDuration.Long)
            if (result == SnackbarResult.ActionPerformed) {
                repo.updateCourse(before.id) { it.copy(minutes = it.minutes - minutes, lastStudiedEpochDay = before.lastStudiedEpochDay) }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = { editing = true }) { Icon(Icons.Filled.Edit, "Edit course") }
                    Box {
                        IconButton(onClick = { menuOpen = true }) { Icon(Icons.Filled.MoreVert, "More") }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            if (course.finished) {
                                DropdownMenuItem(text = { Text("Move back to studying") }, onClick = { menuOpen = false; repo.reopen(course.id) })
                            }
                            DropdownMenuItem(
                                text = { Text("Delete course", color = MaterialTheme.colorScheme.error) },
                                onClick = { menuOpen = false; confirmDelete = true },
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(bottom = 32.dp)) {
            // ---- header ----
            item {
                Column(Modifier.fillMaxWidth().background(accent.copy(alpha = 0.10f)).padding(16.dp)) {
                    Text(
                        listOfNotNull(course.subject.ifBlank { null }, if (course.finished) "Finished" else null)
                            .joinToString(" · ").uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = accent,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(course.title, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Stat(formatMinutes(course.minutes), "STUDIED  ✎", Modifier.weight(1f).clickable { editingTime = true })
                        Stat(
                            if (course.items.isEmpty()) "—" else "${course.doneCount}/${course.items.size}",
                            "DONE",
                            Modifier.weight(1f),
                        )
                    }
                    if (course.items.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { course.progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = accent,
                            trackColor = accent.copy(alpha = 0.2f),
                            drawStopIndicator = {},
                        )
                    }
                }
            }

            // ---- add time / finish ----
            item {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Add time", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                        listOf(15, 30, 60).forEach { m ->
                            FilledTonalButton(onClick = { addTime(m) }) { Text("+${formatMinutes(m)}") }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    if (course.finished) {
                        Button(onClick = { certificate = true }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.WorkspacePremium, null)
                            Text("  View certificate")
                        }
                        course.finishedEpochDay?.takeIf { it > 0 }?.let {
                            Text(
                                "Finished ${formatDate(it)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                        if (course.reflection.isNotBlank()) {
                            Text("“${course.reflection}”", style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic, modifier = Modifier.padding(top = 4.dp))
                        }
                    } else {
                        OutlinedButton(onClick = { finishing = true }, modifier = Modifier.fillMaxWidth()) { Text("Mark finished") }
                    }
                }
            }

            // ---- what to study ----
            item { SectionTitle("What to study", Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)) }
            if (course.items.isEmpty()) {
                item {
                    Text(
                        "List the books, chapters, topics or videos you plan to work through, then check them off.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    )
                }
            }
            items(course.items, key = { it.id }) { item ->
                ChecklistRow(item, accent, onToggle = { repo.toggleItem(course.id, item.id) }, onEdit = { editItem = item })
            }
            item {
                OutlinedTextField(
                    newItem,
                    { newItem = it },
                    placeholder = { Text("Add a book, topic, chapter…") },
                    singleLine = true,
                    trailingIcon = { IconButton(onClick = ::addItem, enabled = newItem.isNotBlank()) { Icon(Icons.Filled.Add, "Add") } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, capitalization = KeyboardCapitalization.Sentences),
                    keyboardActions = KeyboardActions(onDone = { addItem() }),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            // ---- notes ----
            if (course.notes.isNotBlank()) {
                item {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp).clickable { editing = true }) {
                        SectionTitle("Notes")
                        Spacer(Modifier.height(6.dp))
                        Text(course.notes, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }

    if (editing) {
        CourseDialog(course, onDismiss = { editing = false }, onSave = {
            repo.saveCourse(it)
            editing = false
        })
    }
    if (editingTime) {
        TotalTimeDialog(course.minutes, onDismiss = { editingTime = false }, onSave = { m ->
            repo.updateCourse(course.id) { it.copy(minutes = m) }
            editingTime = false
        })
    }
    editItem?.let { item ->
        ItemDialog(item, onDismiss = { editItem = null }, onSave = { saved ->
            repo.updateCourse(course.id) { c -> c.copy(items = c.items.map { if (it.id == saved.id) saved else it }) }
            editItem = null
        }, onDelete = {
            repo.updateCourse(course.id) { c -> c.copy(items = c.items.filter { it.id != item.id }) }
            editItem = null
        })
    }
    if (finishing) {
        FinishDialog(course, onDismiss = { finishing = false }, onFinish = { reflection ->
            repo.finish(course.id, reflection)
            finishing = false
            certificate = true
        })
    }
    if (certificate) {
        CertificateDialog(data.profile.name, course, onDismiss = { certificate = false })
    }
    if (confirmDelete) {
        ConfirmDialog(
            title = "Delete ${course.title}?",
            text = "This removes the course and its checklist. It can't be undone.",
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
private fun ChecklistRow(item: StudyItem, accent: Color, onToggle: () -> Unit, onEdit: () -> Unit) {
    val uri = LocalUriHandler.current
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onEdit).padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = item.done, onCheckedChange = { onToggle() })
        Text(
            item.text,
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = if (item.done) TextDecoration.LineThrough else null,
            color = if (item.done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f).padding(vertical = 10.dp),
        )
        if (item.link.isNotBlank()) {
            IconButton(onClick = { runCatching { uri.openUri(item.link) } }) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, "Open link", tint = accent)
            }
        }
    }
}
