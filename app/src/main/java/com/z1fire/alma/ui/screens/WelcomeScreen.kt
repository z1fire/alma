package com.z1fire.alma.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.z1fire.alma.data.Profile
import com.z1fire.alma.ui.LocalRepository
import com.z1fire.alma.ui.theme.Gold
import java.time.LocalDate

/** First launch: "matriculation" — name yourself and your university. */
@Composable
fun WelcomeScreen(onDone: () -> Unit) {
    val repo = LocalRepository.current
    val defaults = Profile()
    var name by rememberSaveable { mutableStateOf("") }
    var institution by rememberSaveable { mutableStateOf(defaults.institution) }
    var motto by rememberSaveable { mutableStateOf(defaults.motto) }
    var sample by rememberSaveable { mutableStateOf(true) }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.height(24.dp))
            Icon(Icons.Filled.School, null, Modifier.size(72.dp), tint = Gold)
            Text("Welcome, new student", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Text(
                "Alma treats your self-directed learning like a real education: design courses, build a syllabus and reading list, keep a class schedule, earn credits, and work toward degrees of your own making.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                name, { name = it },
                label = { Text("Your name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                institution, { institution = it },
                label = { Text("Name your university") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(motto, { motto = it }, label = { Text("Motto") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = sample, onCheckedChange = { sample = it })
                Column {
                    Text("Enroll me in a sample course", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "PHIL 110: Logic & the Art of Argument",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Button(
                onClick = {
                    repo.updateProfile {
                        it.copy(
                            onboarded = true,
                            studentName = name.trim(),
                            institution = institution.trim().ifBlank { defaults.institution },
                            motto = motto.trim(),
                            matriculatedEpochDay = LocalDate.now().toEpochDay(),
                        )
                    }
                    if (sample) repo.loadSample()
                    onDone()
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text("Matriculate") }
            Text(
                "You can change all of this later in the Registrar's Office.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}
