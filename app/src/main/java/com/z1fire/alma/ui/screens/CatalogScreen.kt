package com.z1fire.alma.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.z1fire.alma.data.CourseStatus
import com.z1fire.alma.data.codeOf
import com.z1fire.alma.ui.ColorDot
import com.z1fire.alma.ui.CourseCard
import com.z1fire.alma.ui.EmptyState
import com.z1fire.alma.ui.LocalRepository
import com.z1fire.alma.ui.rememberAppData
import com.z1fire.alma.ui.theme.deptColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(onOpenCourse: (String) -> Unit, onNewCourse: () -> Unit) {
    val repo = LocalRepository.current
    val data = rememberAppData()
    var filter by rememberSaveable { mutableStateOf<CourseStatus?>(null) }
    var query by rememberSaveable { mutableStateOf("") }

    val visible = data.courses.filter { c ->
        (filter == null || c.status == filter) &&
            (query.isBlank() || listOf(c.title, data.codeOf(c), c.description).any { it.contains(query.trim(), ignoreCase = true) })
    }
    val groups = (data.departments.sortedBy { it.name }.map { it.id to it } + (null to null))
        .map { (id, dept) -> dept to visible.filter { it.departmentId == id || (id == null && data.departments.none { d -> d.id == it.departmentId }) } }
        .filter { it.second.isNotEmpty() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Course Catalog") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewCourse,
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("New course") },
            )
        },
    ) { padding ->
        if (data.courses.isEmpty()) {
            EmptyState(
                icon = Icons.AutoMirrored.Filled.LibraryBooks,
                title = "The catalog is empty",
                message = "Every university starts with a first course. Design one from scratch, or load a sample logic course to see how it works.",
                modifier = Modifier.padding(padding),
                actionLabel = "Load sample course",
                onAction = { repo.loadSample() },
            )
            return@Scaffold
        }
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                OutlinedTextField(
                    query,
                    { query = it },
                    placeholder = { Text("Search courses") },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = filter == null, onClick = { filter = null }, label = { Text("All ${data.courses.size}") })
                    CourseStatus.entries.forEach { s ->
                        val n = data.courses.count { it.status == s }
                        if (n > 0) FilterChip(selected = filter == s, onClick = { filter = if (filter == s) null else s }, label = { Text("${s.label} $n") })
                    }
                }
            }
            if (groups.isEmpty()) {
                item {
                    Text(
                        "No courses match.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
            }
            groups.forEach { (dept, courses) ->
                item(key = "h-${dept?.id ?: "gen"}") {
                    Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        ColorDot(deptColor(dept?.colorIndex), 12)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            dept?.let { "${it.name}  ·  ${it.code}" } ?: "General Studies  ·  GEN",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                items(courses.sortedWith(compareBy({ it.number.padStart(6, '0') }, { it.title })), key = { it.id }) { c ->
                    CourseCard(data, c, onClick = { onOpenCourse(c.id) })
                }
            }
        }
    }
}
