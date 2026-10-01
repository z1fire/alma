package com.z1fire.alma.data

import java.time.DayOfWeek
import java.time.LocalDate

fun AppData.course(id: String): Course? = courses.find { it.id == id }

fun weekStart(date: LocalDate): LocalDate = date.with(DayOfWeek.MONDAY)

fun Course.minutesBetween(start: LocalDate, endInclusive: LocalDate): Int {
    val range = start.toEpochDay()..endInclusive.toEpochDay()
    return sessions.filter { it.epochDay in range }.sumOf { it.minutes }
}

fun Course.weekMinutes(today: LocalDate): Int = minutesBetween(weekStart(today), today)

fun AppData.weekMinutes(today: LocalDate): Int = courses.sumOf { it.weekMinutes(today) }

fun AppData.minutesOn(day: LocalDate): Int = courses.sumOf { it.minutesBetween(day, day) }

fun AppData.totalMinutes(): Int = courses.sumOf { it.totalMinutes }

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

val AppData.active: List<Course> get() = courses.filter { it.status == CourseStatus.ACTIVE }
val AppData.upNext: List<Course> get() = courses.filter { it.status == CourseStatus.SOMEDAY }
val AppData.finished: List<Course>
    get() = courses.filter { it.status == CourseStatus.FINISHED }.sortedByDescending { it.finishedEpochDay ?: 0 }
