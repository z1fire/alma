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
) {
    val studying: List<Course> get() = courses.filter { !it.finished }
    val done: List<Course> get() = courses.filter { it.finished }.sortedByDescending { it.finishedEpochDay }

    fun course(id: String): Course? = courses.find { it.id == id }

    companion object {
        /** Older formats are read by Legacy.kt. */
        const val CURRENT_VERSION = 3
    }
}

@Serializable
data class Profile(
    /** Shown on certificates. */
    val name: String = "",
    /** Evening nudge on days nothing was studied. */
    val reminderEnabled: Boolean = false,
    val reminderMinute: Int = 19 * 60,
)

@Serializable
data class Course(
    val id: String = newId(),
    val title: String,
    val subject: String = "",
    val notes: String = "",
    val colorIndex: Int = 0,
    /** The "what to study" checklist: books, chapters, topics, videos… */
    val items: List<StudyItem> = emptyList(),
    /** Total study time. */
    val minutes: Int = 0,
    val lastStudiedEpochDay: Long? = null,
    val finishedEpochDay: Long? = null,
    /** "What did you learn?" — written when finishing. */
    val reflection: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
) {
    val finished: Boolean get() = finishedEpochDay != null
    val doneCount: Int get() = items.count { it.done }
    val progress: Float get() = if (items.isEmpty()) 0f else doneCount.toFloat() / items.size
}

@Serializable
data class StudyItem(
    val id: String = newId(),
    val text: String,
    val link: String = "",
    val done: Boolean = false,
)
