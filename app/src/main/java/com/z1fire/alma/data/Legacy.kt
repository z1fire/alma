package com.z1fire.alma.data

import kotlinx.serialization.Serializable

/*
 * Readers for older save formats, so upgrading never loses work.
 *   v1: the full "college" model (departments, units, readings, assignments, dated sessions).
 *   v2: courses with a checklist, a status, and dated study sessions.
 * Both become v3: checklist items carry over, and dated sessions collapse into a total and a last-studied day.
 * Enum-typed fields are read as strings so old files decode no matter what changed since.
 */

@Serializable
internal data class OldSession(val epochDay: Long = 0, val minutes: Int = 0)

private fun List<OldSession>.total() = sumOf { it.minutes }
private fun List<OldSession>.lastDay() = filter { it.minutes > 0 }.maxOfOrNull { it.epochDay }

// ---------------- v2 ----------------

@Serializable
internal data class V2AppData(
    val profile: V2Profile = V2Profile(),
    val courses: List<V2Course> = emptyList(),
)

@Serializable
internal data class V2Profile(
    val name: String = "",
    val reminderEnabled: Boolean = false,
    val reminderMinute: Int = 19 * 60,
)

@Serializable
internal data class V2Course(
    val id: String,
    val title: String = "",
    val subject: String = "",
    val notes: String = "",
    val colorIndex: Int = 0,
    val status: String = "ACTIVE",
    val items: List<StudyItem> = emptyList(),
    val sessions: List<OldSession> = emptyList(),
    val finishedEpochDay: Long? = null,
    val reflection: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
)

internal fun V2AppData.toCurrent() = AppData(
    profile = Profile(profile.name, profile.reminderEnabled, profile.reminderMinute),
    courses = courses.map { c ->
        Course(
            id = c.id,
            title = c.title.ifBlank { "Untitled course" },
            subject = c.subject,
            notes = c.notes,
            colorIndex = c.colorIndex,
            items = c.items,
            minutes = c.sessions.total(),
            lastStudiedEpochDay = c.sessions.lastDay(),
            finishedEpochDay = if (c.status == "FINISHED") c.finishedEpochDay ?: c.sessions.lastDay() ?: 0 else null,
            reflection = c.reflection,
            createdAtMillis = c.createdAtMillis,
        )
    },
)

// ---------------- v1 ----------------

@Serializable
internal data class V1AppData(
    val profile: V1Profile = V1Profile(),
    val departments: List<V1Department> = emptyList(),
    val courses: List<V1Course> = emptyList(),
)

@Serializable
internal data class V1Profile(
    val studentName: String = "",
    val nudgeEnabled: Boolean = false,
    val nudgeMinute: Int = 19 * 60,
)

@Serializable
internal data class V1Department(val id: String, val name: String = "", val colorIndex: Int = 0)

@Serializable
internal data class V1Course(
    val id: String,
    val departmentId: String? = null,
    val title: String = "",
    val description: String = "",
    val objectives: List<String> = emptyList(),
    val status: String = "PLANNED",
    val modules: List<V1Named> = emptyList(),
    val resources: List<V1Resource> = emptyList(),
    val assignments: List<V1Named> = emptyList(),
    val sessions: List<OldSession> = emptyList(),
    val reflection: String = "",
    val completedEpochDay: Long? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
)

@Serializable
internal data class V1Named(val title: String = "", val done: Boolean = false)

@Serializable
internal data class V1Resource(
    val title: String = "",
    val author: String = "",
    val url: String = "",
    val status: String = "NOT_STARTED",
)

internal fun V1AppData.toCurrent(): AppData {
    val depts = departments.associateBy { it.id }
    return AppData(
        profile = Profile(profile.studentName, profile.nudgeEnabled, profile.nudgeMinute),
        courses = courses.map { c ->
            val dept = c.departmentId?.let { depts[it] }
            val goals = if (c.objectives.isEmpty()) "" else "Goals:\n" + c.objectives.joinToString("\n") { "• $it" }
            Course(
                id = c.id,
                title = c.title.ifBlank { "Untitled course" },
                subject = dept?.name.orEmpty(),
                notes = listOf(c.description, goals).filter { it.isNotBlank() }.joinToString("\n\n"),
                colorIndex = dept?.colorIndex ?: 0,
                items = c.modules.map { StudyItem(text = it.title, done = it.done) } +
                    c.resources.map { r ->
                        StudyItem(
                            text = r.title + if (r.author.isNotBlank()) " — ${r.author}" else "",
                            link = r.url,
                            done = r.status == "DONE",
                        )
                    } +
                    c.assignments.map { StudyItem(text = it.title, done = it.done) },
                minutes = c.sessions.total(),
                lastStudiedEpochDay = c.sessions.lastDay(),
                finishedEpochDay = if (c.status == "COMPLETED") c.completedEpochDay ?: 0 else null,
                reflection = c.reflection,
                createdAtMillis = c.createdAtMillis,
            )
        },
    )
}
