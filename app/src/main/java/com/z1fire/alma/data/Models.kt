package com.z1fire.alma.data

import kotlinx.serialization.Serializable
import java.util.UUID

fun newId(): String = UUID.randomUUID().toString()

/** Everything the app knows, persisted as a single JSON document. */
@Serializable
data class AppData(
    val version: Int = CURRENT_VERSION,
    val profile: Profile = Profile(),
    val courses: List<Course> = emptyList(),
    val activeTimer: ActiveTimer? = null,
) {
    companion object {
        /** Version 1 was the full college model (credits, terms, departments…); see Legacy.kt. */
        const val CURRENT_VERSION = 2
    }
}

@Serializable
data class Profile(
    val name: String = "",
    val weeklyGoalMinutes: Int = 300,
    /** Evening nudge on days with no study logged. */
    val reminderEnabled: Boolean = false,
    val reminderMinute: Int = 19 * 60,
    val sessionNotificationEnabled: Boolean = true,
    /** The "want a daily reminder?" card was answered. */
    val reminderPrompted: Boolean = false,
)

@Serializable
enum class CourseStatus(val label: String) {
    ACTIVE("Studying"),
    SOMEDAY("Up next"),
    FINISHED("Finished"),
}

@Serializable
data class Course(
    val id: String = newId(),
    val title: String,
    val subject: String = "",
    val notes: String = "",
    val colorIndex: Int = 0,
    val status: CourseStatus = CourseStatus.ACTIVE,
    /** The "what to study" checklist: books, chapters, topics, videos… */
    val items: List<StudyItem> = emptyList(),
    val sessions: List<StudySession> = emptyList(),
    val startedEpochDay: Long? = null,
    val finishedEpochDay: Long? = null,
    /** "What did you learn?" — written when finishing. */
    val reflection: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
) {
    val doneCount: Int get() = items.count { it.done }

    val progress: Float get() = if (items.isEmpty()) 0f else doneCount.toFloat() / items.size

    val totalMinutes: Int get() = sessions.sumOf { it.minutes }
}

@Serializable
data class StudyItem(
    val id: String = newId(),
    val text: String,
    val link: String = "",
    val done: Boolean = false,
)

@Serializable
data class StudySession(
    val id: String = newId(),
    val epochDay: Long,
    val minutes: Int,
    val notes: String = "",
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
