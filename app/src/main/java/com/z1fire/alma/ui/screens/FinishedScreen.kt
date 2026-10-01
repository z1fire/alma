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
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.z1fire.alma.data.Course
import com.z1fire.alma.data.finished
import com.z1fire.alma.ui.ColorDot
import com.z1fire.alma.ui.EmptyState
import com.z1fire.alma.ui.Stat
import com.z1fire.alma.ui.formatDate
import com.z1fire.alma.ui.formatMinutes
import com.z1fire.alma.ui.rememberAppData
import com.z1fire.alma.ui.theme.Gold
import com.z1fire.alma.ui.theme.deptColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinishedScreen(onOpenCourse: (String) -> Unit) {
    val data = rememberAppData()
    val finished = data.finished
    var certificateFor by remember { mutableStateOf<Course?>(null) }

    Scaffold(topBar = { TopAppBar(title = { Text("Finished") }) }) { padding ->
        if (finished.isEmpty()) {
            EmptyState(
                Icons.Filled.WorkspacePremium,
                "Nothing finished yet",
                "When you wrap up a course, mark it finished. It'll land here with a certificate and your total study time.",
                Modifier.padding(padding),
            )
            return@Scaffold
        }
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 18.dp)) {
                        Stat("${finished.size}", "FINISHED", Modifier.weight(1f))
                        Stat(formatMinutes(finished.sumOf { it.totalMinutes }), "STUDIED", Modifier.weight(1f))
                        Stat("${finished.sumOf { it.doneCount }}", "ITEMS DONE", Modifier.weight(1f))
                    }
                }
            }
            items(finished, key = { it.id }) { c ->
                Card(
                    onClick = { onOpenCourse(c.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    Row(Modifier.padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        ColorDot(deptColor(c.colorIndex), 12)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                listOfNotNull(
                                    c.finishedEpochDay?.let { formatDate(it) },
                                    c.totalMinutes.takeIf { it > 0 }?.let { formatMinutes(it) },
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
                        IconButton(onClick = { certificateFor = c }) {
                            Icon(Icons.Filled.WorkspacePremium, "Certificate", tint = Gold)
                        }
                    }
                }
            }
        }
    }
    certificateFor?.let { CertificateDialog(data.profile.name, it, onDismiss = { certificateFor = null }) }
}
