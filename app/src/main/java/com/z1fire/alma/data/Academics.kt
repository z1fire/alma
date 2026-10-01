package com.z1fire.alma.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

object Grades {
    val letterPoints: Map<String, Double> = linkedMapOf(
        "A+" to 4.0, "A" to 4.0, "A-" to 3.7,
        "B+" to 3.3, "B" to 3.0, "B-" to 2.7,
        "C+" to 2.3, "C" to 2.0, "C-" to 1.7,
        "D+" to 1.3, "D" to 1.0, "D-" to 0.7,
        "F" to 0.0,
    )
    private val special: Map<String, String> = linkedMapOf(
        "P" to "Pass", "NP" to "No pass", "AU" to "Audit",
    )
    val all: List<String> = letterPoints.keys.toList() + special.keys

    fun points(grade: String?): Double? = grade?.let { letterPoints[it] }

    fun earnsCredit(grade: String?): Boolean = grade != null && grade !in setOf("F", "NP", "AU")

    fun label(grade: String): String =
        special[grade]?.let { "$grade — $it" }
            ?: letterPoints[grade]?.let { "$grade  (${String.format(Locale.US, "%.1f", it)})" }
            ?: grade
}

object Terms {
    private val seasons = listOf("Spring", "Summer", "Fall")

    private fun seasonOf(month: Int) = when (month) {
        in 1..5 -> "Spring"
        in 6..8 -> "Summer"
        else -> "Fall"
    }

    fun forDate(date: LocalDate): String = "${seasonOf(date.monthValue)} ${date.year}"

    fun sortKey(term: String): Int {
        val parts = term.trim().split(" ")
        val year = parts.lastOrNull()?.toIntOrNull() ?: return Int.MAX_VALUE
        val s = seasons.indexOf(parts.first())
        return year * 10 + if (s < 0) 9 else s
    }

    fun options(today: LocalDate, include: String = ""): List<String> {
        val list = (today.year - 1..today.year + 2).flatMap { y -> seasons.map { "$it $y" } }
        return if (include.isNotBlank() && include !in list) (list + include).sortedBy { sortKey(it) } else list
    }
}

fun standingFor(credits: Int): String = when {
    credits < 30 -> "Freshman"
    credits < 60 -> "Sophomore"
    credits < 90 -> "Junior"
    credits < 120 -> "Senior"
    else -> "Graduate scholar"
}

fun latinHonors(gpa: Double?): String? = when {
    gpa == null -> null
    gpa >= 3.9 -> "summa cum laude"
    gpa >= 3.7 -> "magna cum laude"
    gpa >= 3.5 -> "cum laude"
    else -> null
}

fun levelLabel(number: String): String? = when (number.trim().firstOrNull()) {
    '0' -> "Foundations"
    '1' -> "Introductory"
    '2' -> "Intermediate"
    '3' -> "Advanced"
    '4' -> "Senior seminar"
    '5', '6' -> "Graduate"
    '7', '8', '9' -> "Doctoral"
    else -> null
}

data class RecordStats(
    val gpa: Double?,
    val creditsEarned: Int,
    val gradedCredits: Int,
    val completedCount: Int,
)

fun recordStats(courses: List<Course>): RecordStats {
    val done = courses.filter { it.status == CourseStatus.COMPLETED }
    val graded = done.filter { Grades.points(it.finalGrade) != null && it.credits > 0 }
    val gradedCredits = graded.sumOf { it.credits }
    val gpa = if (gradedCredits > 0) {
        graded.sumOf { Grades.points(it.finalGrade)!! * it.credits } / gradedCredits
    } else null
    return RecordStats(
        gpa = gpa,
        creditsEarned = done.filter { Grades.earnsCredit(it.finalGrade) }.sumOf { it.credits },
        gradedCredits = gradedCredits,
        completedCount = done.size,
    )
}

fun AppData.stats(): RecordStats = recordStats(courses)

fun AppData.department(id: String?): Department? = id?.let { d -> departments.find { it.id == d } }

fun AppData.course(id: String): Course? = courses.find { it.id == id }

fun AppData.codeOf(course: Course): String {
    val code = department(course.departmentId)?.code ?: "GEN"
    return if (course.number.isBlank()) code else "$code ${course.number}"
}

fun Course.isActiveOn(date: LocalDate): Boolean {
    val day = date.toEpochDay()
    return status == CourseStatus.ENROLLED &&
        (startEpochDay == null || day >= startEpochDay) &&
        (endEpochDay == null || day <= endEpochDay)
}

fun AppData.meetingsOn(date: LocalDate): List<Pair<Course, Meeting>> =
    courses.filter { it.isActiveOn(date) }
        .flatMap { c -> c.meetings.filter { it.dayOfWeek == date.dayOfWeek.value }.map { c to it } }
        .sortedBy { it.second.startMinute }

/** Enrolls a course starting [today] (unless it already has a start), filling in dates from its plan. */
fun Course.enrolled(today: LocalDate): Course {
    val start = startEpochDay ?: today.toEpochDay()
    return copy(
        status = CourseStatus.ENROLLED,
        term = term.ifBlank { Terms.forDate(LocalDate.ofEpochDay(start)) },
        startEpochDay = start,
        endEpochDay = endEpochDay ?: durationWeeks?.let { start + it * 7L - 1 },
        assignments = assignments.map { a ->
            if (a.dueEpochDay == null && a.dueOffsetDays != null) a.copy(dueEpochDay = start + a.dueOffsetDays) else a
        },
    )
}

data class UpcomingMeeting(val date: LocalDate, val course: Course, val meeting: Meeting)

/** The first class meeting starting after [now], looking up to [days] ahead. */
fun AppData.nextMeetingAfter(now: LocalDateTime, days: Int = 14): UpcomingMeeting? {
    for (offset in 0..days) {
        val date = now.toLocalDate().plusDays(offset.toLong())
        val nowMinute = if (offset == 0) now.hour * 60 + now.minute else -1
        meetingsOn(date).firstOrNull { it.second.startMinute > nowMinute }
            ?.let { (c, m) -> return UpcomingMeeting(date, c, m) }
    }
    return null
}

fun AppData.assignmentsDueOn(date: LocalDate): List<Pair<Course, Assignment>> =
    courses.filter { it.status != CourseStatus.DROPPED }
        .flatMap { c -> c.assignments.filter { it.dueEpochDay == date.toEpochDay() }.map { c to it } }

fun AppData.sessionsOn(date: LocalDate): List<Pair<Course, StudySession>> =
    courses.flatMap { c -> c.sessions.filter { it.epochDay == date.toEpochDay() }.map { c to it } }

fun AppData.minutesBetween(start: LocalDate, endInclusive: LocalDate): Int {
    val range = start.toEpochDay()..endInclusive.toEpochDay()
    return courses.sumOf { c -> c.sessions.filter { it.epochDay in range }.sumOf { it.minutes } }
}

fun weekStart(date: LocalDate): LocalDate = date.with(DayOfWeek.MONDAY)

fun AppData.weekMinutes(today: LocalDate): Int = minutesBetween(weekStart(today), today)

/** Consecutive days with logged study, counting today or (if nothing yet today) yesterday. */
fun AppData.streak(today: LocalDate): Int {
    val days = courses.flatMap { it.sessions }.filter { it.minutes > 0 }.map { it.epochDay }.toSet()
    var d = today.toEpochDay()
    if (d !in days) d -= 1
    var n = 0
    while (d in days) {
        n++
        d--
    }
    return n
}

fun AppData.dueSoon(today: LocalDate, horizonDays: Int = 14): List<Pair<Course, Assignment>> =
    courses.filter { it.status == CourseStatus.ENROLLED }
        .flatMap { c ->
            c.assignments
                .filter { !it.done && (it.dueEpochDay ?: Long.MAX_VALUE) <= today.toEpochDay() + horizonDays }
                .map { c to it }
        }
        .sortedBy { it.second.dueEpochDay }

private fun Course.earnedCredit() = status == CourseStatus.COMPLETED && Grades.earnsCredit(finalGrade)

fun Program.countingCourses(data: AppData): List<Course> = data.courses.filter { c ->
    c.earnedCredit() && (
        c.id in courseIds ||
            (departmentId != null && c.departmentId == departmentId) ||
            (departmentId == null && courseIds.isEmpty())
        )
}

fun Program.earnedCredits(data: AppData): Int = countingCourses(data).sumOf { it.credits }

fun Program.requiredDone(data: AppData): Int = courseIds.count { id -> data.course(id)?.earnedCredit() == true }

fun Program.isComplete(data: AppData): Boolean =
    earnedCredits(data) >= creditsRequired && requiredDone(data) == courseIds.size
