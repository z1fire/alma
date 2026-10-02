package com.z1fire.alma.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.z1fire.alma.data.Course
import com.z1fire.alma.ui.EmptyState
import com.z1fire.alma.ui.LocalRepository
import com.z1fire.alma.ui.SectionTitle
import com.z1fire.alma.ui.Stat
import com.z1fire.alma.ui.formatDate
import com.z1fire.alma.ui.formatHoursShort
import com.z1fire.alma.ui.rememberAppData
import com.z1fire.alma.ui.theme.Gold
import com.z1fire.alma.ui.theme.deptColor
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Shared scaffold for the three tabs: title, ⋮ menu, snackbar, and a "New course" button. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabScaffold(
    title: String,
    onNewCourse: (() -> Unit)?,
    content: @Composable (PaddingValues, (String) -> Unit) -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val say: (String) -> Unit = { msg -> scope.launch { snackbar.showSnackbar(msg) } }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { TopAppBar(title = { Text(title) }, actions = { AppMenu(onMessage = say) }) },
        floatingActionButton = {
            if (onNewCourse != null) {
                ExtendedFloatingActionButton(onClick = onNewCourse, icon = { Icon(Icons.Filled.Add, null) }, text = { Text("New course") })
            }
        },
    ) { padding -> content(padding, say) }
}

private val listPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp)

/** What you're studying now. */
@Composable
fun CurriculumScreen(onOpenCourse: (String) -> Unit, onOpenCatalogue: () -> Unit) {
    val repo = LocalRepository.current
    val data = rememberAppData()
    var creating by remember { mutableStateOf(false) }

    TabScaffold("Curriculum", onNewCourse = { creating = true }) { padding, _ ->
        if (data.curriculum.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.AutoStories,
                title = "Your curriculum is empty",
                message = "Start a course from your catalogue, or add one you're beginning right now.",
                modifier = Modifier.padding(padding),
                actionLabel = if (data.catalogue.isNotEmpty()) "Open the catalogue" else null,
                onAction = onOpenCatalogue,
            )
            return@TabScaffold
        }
        LazyColumn(Modifier.padding(padding), contentPadding = listPadding, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(data.curriculum, key = { it.id }) { c -> CourseCard(c, onClick = { onOpenCourse(c.id) }) }
        }
    }

    if (creating) {
        CourseDialog(null, onDismiss = { creating = false }, onSave = { c ->
            repo.saveCourse(c.copy(startedEpochDay = LocalDate.now().toEpochDay(), baselineMinutes = c.minutes))
            creating = false
            onOpenCourse(c.id)
        })
    }
}

/** Courses planned ahead, grouped by subject, each one tap away from starting. */
@Composable
fun CatalogueScreen(onOpenCourse: (String) -> Unit) {
    val repo = LocalRepository.current
    val data = rememberAppData()
    var creating by remember { mutableStateOf(false) }

    TabScaffold("Catalogue", onNewCourse = { creating = true }) { padding, say ->
        if (data.catalogue.isEmpty()) {
            EmptyState(
                icon = Icons.AutoMirrored.Filled.LibraryBooks,
                title = "Plan ahead",
                message = "Add courses you want to take later — with their checklists and hour goals — and start them when you're ready.",
                modifier = Modifier.padding(padding),
                actionLabel = "Add a course",
                onAction = { creating = true },
            )
            return@TabScaffold
        }
        LazyColumn(Modifier.padding(padding), contentPadding = listPadding, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            data.catalogue.groupBy { it.subject.ifBlank { "Other" } }.forEach { (subject, courses) ->
                item(key = "h-$subject") { SectionTitle(subject, Modifier.padding(top = 8.dp)) }
                items(courses, key = { it.id }) { c ->
                    CourseCard(c, onClick = { onOpenCourse(c.id) }, planned = true, trailing = {
                        FilledTonalButton(onClick = {
                            repo.start(c.id)
                            say("${c.title} added to your curriculum.")
                        }) { Text("Start") }
                    })
                }
            }
        }
    }

    if (creating) {
        CourseDialog(null, onDismiss = { creating = false }, onSave = { c ->
            repo.saveCourse(c)
            creating = false
            onOpenCourse(c.id)
        })
    }
}

/** Completed courses, newest first, with certificates. */
@Composable
fun FinishedScreen(onOpenCourse: (String) -> Unit) {
    val data = rememberAppData()
    var certificateFor by remember { mutableStateOf<Course?>(null) }

    TabScaffold("Finished", onNewCourse = null) { padding, _ ->
        val finished = data.finished
        if (finished.isEmpty()) {
            EmptyState(
                Icons.Filled.WorkspacePremium,
                "Nothing finished yet",
                "When you complete a course, mark it finished and it'll land here with a certificate.",
                Modifier.padding(padding),
            )
            return@TabScaffold
        }
        LazyColumn(Modifier.padding(padding), contentPadding = listPadding, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 18.dp)) {
                        Stat("${finished.size}", "FINISHED", Modifier.weight(1f))
                        Stat(formatHoursShort(finished.sumOf { it.minutes }), "STUDIED", Modifier.weight(1f))
                    }
                }
            }
            items(finished, key = { it.id }) { c ->
                Card(
                    onClick = { onOpenCourse(c.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    Row(Modifier.padding(start = 14.dp, end = 4.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, null, tint = deptColor(c.colorIndex))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                listOfNotNull(
                                    c.finishedEpochDay?.takeIf { it > 0 }?.let { formatDate(it) },
                                    c.timeSummary(),
                                    c.subject.ifBlank { null },
                                ).joinToString(" · "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (c.reflection.isNotBlank()) {
                                Text(
                                    "“${c.reflection}”",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontStyle = FontStyle.Italic,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        IconButton(onClick = { certificateFor = c }) { Icon(Icons.Filled.WorkspacePremium, "Certificate", tint = Gold) }
                    }
                }
            }
        }
    }
    certificateFor?.let { CertificateDialog(data.profile.name, it, onDismiss = { certificateFor = null }) }
}
