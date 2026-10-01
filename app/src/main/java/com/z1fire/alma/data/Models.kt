package com.z1fire.alma.data

import kotlinx.serialization.Serializable
import java.util.UUID

fun newId(): String = UUID.randomUUID().toString()

/** Everything the app knows, persisted as a single JSON document. */
@Serializable
data class AppData(
    val version: Int = 1,
    val profile: Profile = Profile(),
    val departments: List<Department> = emptyList(),
    val courses: List<Course> = emptyList(),
    val programs: List<Program> = emptyList(),
    val activeTimer: ActiveTimer? = null,
)

@Serializable
data class Profile(
    val onboarded: Boolean = false,
    val studentName: String = "",
    val institution: String = "The University of Lifelong Learning",
    val motto: String = "Semper discens — always learning",
    val matriculatedEpochDay: Long? = null,
    val weeklyGoalMinutes: Int = 300,
    val remindersEnabled: Boolean = false,
    val reminderLeadMinutes: Int = 10,
)

@Serializable
data class Department(
    val id: String = newId(),
    val name: String,
    val code: String,
    val colorIndex: Int = 0,
)

@Serializable
enum class CourseStatus(val label: String) {
    PLANNED("Planned"),
    ENROLLED("Enrolled"),
    COMPLETED("Completed"),
    DROPPED("Dropped"),
}

@Serializable
data class Course(
    val id: String = newId(),
    val departmentId: String? = null,
    val number: String = "",
    val title: String = "",
    val credits: Int = 3,
    val description: String = "",
    val objectives: List<String> = emptyList(),
    val prerequisiteIds: List<String> = emptyList(),
    val status: CourseStatus = CourseStatus.PLANNED,
    val term: String = "",
    val startEpochDay: Long? = null,
    val endEpochDay: Long? = null,
    val meetings: List<Meeting> = emptyList(),
    val modules: List<Module> = emptyList(),
    val resources: List<Resource> = emptyList(),
    val assignments: List<Assignment> = emptyList(),
    val sessions: List<StudySession> = emptyList(),
    val finalGrade: String? = null,
    val reflection: String = "",
    val completedEpochDay: Long? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
) {
    /** Share of syllabus units and assignments checked off. */
    val progress: Float
        get() {
            val total = modules.size + assignments.size
            if (total == 0) return if (status == CourseStatus.COMPLETED) 1f else 0f
            val done = modules.count { it.done } + assignments.count { it.done }
            return done.toFloat() / total
        }

    val totalMinutes: Int get() = sessions.sumOf { it.minutes }
}

/** A recurring weekly "class meeting" — a block of time reserved for this course. */
@Serializable
data class Meeting(
    val id: String = newId(),
    /** ISO day of week, Monday = 1 … Sunday = 7. */
    val dayOfWeek: Int,
    val startMinute: Int,
    val durationMinutes: Int = 60,
    val label: String = "Class",
    val location: String = "",
)

/** A syllabus unit (week, chapter, topic block). */
@Serializable
data class Module(
    val id: String = newId(),
    val title: String,
    val notes: String = "",
    val done: Boolean = false,
)

@Serializable
enum class ResourceType(val label: String) {
    BOOK("Book"),
    TEXTBOOK("Textbook"),
    ARTICLE("Article"),
    PAPER("Paper"),
    VIDEO("Video"),
    LECTURES("Lecture series"),
    PODCAST("Podcast"),
    ONLINE_COURSE("Online course"),
    WEBSITE("Website"),
    OTHER("Other"),
}

@Serializable
enum class ResourceStatus(val label: String) {
    NOT_STARTED("To read"),
    IN_PROGRESS("In progress"),
    DONE("Finished"),
}

@Serializable
data class Resource(
    val id: String = newId(),
    val type: ResourceType = ResourceType.BOOK,
    val title: String,
    val author: String = "",
    val url: String = "",
    val required: Boolean = true,
    val status: ResourceStatus = ResourceStatus.NOT_STARTED,
    val notes: String = "",
)

@Serializable
enum class AssignmentType(val label: String) {
    READING("Reading"),
    PROBLEM_SET("Problem set"),
    ESSAY("Essay"),
    PROJECT("Project"),
    QUIZ("Quiz"),
    EXAM("Exam"),
    PRESENTATION("Presentation"),
    OTHER("Other"),
}

@Serializable
data class Assignment(
    val id: String = newId(),
    val title: String,
    val type: AssignmentType = AssignmentType.READING,
    val dueEpochDay: Long? = null,
    val done: Boolean = false,
    val grade: String = "",
    val notes: String = "",
)

@Serializable
data class StudySession(
    val id: String = newId(),
    val epochDay: Long,
    val minutes: Int,
    val notes: String = "",
)

@Serializable
enum class ProgramKind(val label: String, val diplomaPhrase: String) {
    CERTIFICATE("Certificate", "the Certificate in"),
    MINOR("Minor", "a Minor in"),
    MAJOR("Major", "a Major in"),
    ASSOCIATE("Associate's degree", "the degree of Associate of Arts in"),
    BACHELOR("Bachelor's degree", "the degree of Bachelor of Arts in"),
    MASTER("Master's degree", "the degree of Master of Arts in"),
    DOCTORATE("Doctorate", "the degree of Doctor of Philosophy in"),
}

/**
 * A degree, major, minor or certificate. Credits count from [courseIds] plus every course in
 * [departmentId]; with neither set, every completed course counts.
 */
@Serializable
data class Program(
    val id: String = newId(),
    val name: String,
    val kind: ProgramKind = ProgramKind.CERTIFICATE,
    val creditsRequired: Int = 12,
    val courseIds: List<String> = emptyList(),
    val departmentId: String? = null,
    val conferredEpochDay: Long? = null,
)

@Serializable
data class ActiveTimer(
    val courseId: String,
    val startedAtMillis: Long,
)

fun <T> List<T>.upsert(item: T, key: (T) -> String): List<T> {
    val k = key(item)
    return if (any { key(it) == k }) map { if (key(it) == k) item else it } else this + item
}
