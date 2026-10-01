package com.z1fire.alma.data

import kotlinx.serialization.Serializable

/*
 * Version 1 stored a full college model (departments, credits, terms, meetings, assignments…).
 * These classes read just enough of that format to carry the student's work into the simple model:
 * syllabus units, readings and assignments become checklist items; study sessions carry over as-is.
 * Enum-typed fields are read as strings so old files decode no matter what changed.
 */

@Serializable
internal data class LegacyAppData(
    val profile: LegacyProfile = LegacyProfile(),
    val departments: List<LegacyDepartment> = emptyList(),
    val courses: List<LegacyCourse> = emptyList(),
    val activeTimer: ActiveTimer? = null,
)

@Serializable
internal data class LegacyProfile(
    val studentName: String = "",
    val weeklyGoalMinutes: Int = 300,
    val nudgeEnabled: Boolean = false,
    val nudgeMinute: Int = 19 * 60,
    val sessionNotificationEnabled: Boolean = true,
    val notificationsPrompted: Boolean = false,
)

@Serializable
internal data class LegacyDepartment(val id: String, val name: String = "", val colorIndex: Int = 0)

@Serializable
internal data class LegacyCourse(
    val id: String,
    val departmentId: String? = null,
    val title: String = "",
    val description: String = "",
    val objectives: List<String> = emptyList(),
    val status: String = "PLANNED",
    val startEpochDay: Long? = null,
    val modules: List<LegacyNamedItem> = emptyList(),
    val resources: List<LegacyResource> = emptyList(),
    val assignments: List<LegacyNamedItem> = emptyList(),
    val sessions: List<StudySession> = emptyList(),
    val reflection: String = "",
    val completedEpochDay: Long? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
)

@Serializable
internal data class LegacyNamedItem(val title: String = "", val done: Boolean = false)

@Serializable
internal data class LegacyResource(
    val title: String = "",
    val author: String = "",
    val url: String = "",
    val status: String = "NOT_STARTED",
)

internal fun LegacyAppData.toCurrent(): AppData {
    val depts = departments.associateBy { it.id }
    return AppData(
        profile = Profile(
            name = profile.studentName,
            weeklyGoalMinutes = profile.weeklyGoalMinutes,
            reminderEnabled = profile.nudgeEnabled,
            reminderMinute = profile.nudgeMinute,
            sessionNotificationEnabled = profile.sessionNotificationEnabled,
            reminderPrompted = profile.notificationsPrompted,
        ),
        courses = courses.map { c ->
            val dept = c.departmentId?.let { depts[it] }
            val goals = if (c.objectives.isEmpty()) "" else "Goals:\n" + c.objectives.joinToString("\n") { "• $it" }
            Course(
                id = c.id,
                title = c.title.ifBlank { "Untitled course" },
                subject = dept?.name.orEmpty(),
                notes = listOf(c.description, goals).filter { it.isNotBlank() }.joinToString("\n\n"),
                colorIndex = dept?.colorIndex ?: 0,
                status = when (c.status) {
                    "ENROLLED" -> CourseStatus.ACTIVE
                    "COMPLETED" -> CourseStatus.FINISHED
                    else -> CourseStatus.SOMEDAY
                },
                items = c.modules.map { StudyItem(text = it.title, done = it.done) } +
                    c.resources.map { r ->
                        StudyItem(
                            text = r.title + if (r.author.isNotBlank()) " — ${r.author}" else "",
                            link = r.url,
                            done = r.status == "DONE",
                        )
                    } +
                    c.assignments.map { StudyItem(text = it.title, done = it.done) },
                sessions = c.sessions,
                startedEpochDay = c.startEpochDay,
                finishedEpochDay = c.completedEpochDay,
                reflection = c.reflection,
                createdAtMillis = c.createdAtMillis,
            )
        },
        activeTimer = activeTimer,
    )
}
