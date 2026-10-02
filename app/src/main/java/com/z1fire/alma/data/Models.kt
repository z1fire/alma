package com.z1fire.alma.data

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.util.UUID
import kotlin.math.ceil

fun newId(): String = UUID.randomUUID().toString()

/** Everything the app knows, persisted as a single JSON document. */
@Serializable
data class AppData(
    val version: Int = CURRENT_VERSION,
    val profile: Profile = Profile(),
    val courses: List<Course> = emptyList(),
) {
    /** Started and not finished: what you're studying now. */
    val curriculum: List<Course> get() = courses.filter { it.inCurriculum }

    /** Planned but not started yet. */
    val catalogue: List<Course> get() = courses.filter { it.inCatalogue }.sortedWith(compareBy({ it.subject.lowercase() }, { it.title.lowercase() }))

    val finished: List<Course> get() = courses.filter { it.finished }.sortedByDescending { it.finishedEpochDay }

    fun course(id: String): Course? = courses.find { it.id == id }

    companion object {
        /** Older formats are read by Legacy.kt. */
        const val CURRENT_VERSION = 4
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
    /** [minutes] when the course was started, plus manual corrections; pace counts only time added since. */
    val baselineMinutes: Int = 0,
    /** Optional target, e.g. 10,000 hours for an ongoing language. */
    val goalHours: Int? = null,
    /** Set when moved from the catalogue into the curriculum. */
    val startedEpochDay: Long? = null,
    val lastStudiedEpochDay: Long? = null,
    val finishedEpochDay: Long? = null,
    /** "What did you learn?" — written when finishing. */
    val reflection: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
) {
    val finished: Boolean get() = finishedEpochDay != null
    val inCurriculum: Boolean get() = startedEpochDay != null && !finished
    val inCatalogue: Boolean get() = startedEpochDay == null && !finished

    val doneCount: Int get() = items.count { it.done }
    val itemProgress: Float get() = if (items.isEmpty()) 0f else doneCount.toFloat() / items.size

    val goalMinutes: Int? get() = goalHours?.takeIf { it > 0 }?.let { it * 60 }
    val hourProgress: Float get() = goalMinutes?.let { (minutes.toFloat() / it).coerceAtMost(1f) } ?: 0f
    val goalReached: Boolean get() = goalMinutes?.let { minutes >= it } ?: false

    /** Days since starting, counting today. */
    fun daysStudying(today: LocalDate): Long? = startedEpochDay?.let { (today.toEpochDay() - it + 1).coerceAtLeast(1) }

    /** Average minutes a day logged since starting (time entered as corrections doesn't count). */
    fun averagePerDay(today: LocalDate): Double? =
        daysStudying(today)?.let { (minutes - baselineMinutes).coerceAtLeast(0).toDouble() / it }

    /** When the hour goal would be reached at the current pace; null until there's a week of data. */
    fun projectedGoalDate(today: LocalDate): LocalDate? {
        val goal = goalMinutes ?: return null
        if (minutes >= goal || (daysStudying(today) ?: 0) < 7) return null
        val avg = averagePerDay(today)?.takeIf { it > 0 } ?: return null
        return today.plusDays(ceil((goal - minutes) / avg).toLong())
    }
}

@Serializable
data class StudyItem(
    val id: String = newId(),
    val text: String,
    val link: String = "",
    val done: Boolean = false,
)
