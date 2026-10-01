package com.z1fire.alma.data

import com.z1fire.alma.data.bulletin.Bulletin
import com.z1fire.alma.data.bulletin.EnrollPlan
import com.z1fire.alma.data.bulletin.addFromBulletin
import java.time.LocalDate

/**
 * The starter example, drawn from the bulletin: PHIL 110 enrolled with a weekly schedule,
 * PHIL 210 planned next, and a certificate tying them together. Safe to run twice.
 */
object SampleData {
    fun addTo(data: AppData, today: LocalDate): AppData {
        val phil = Bulletin.department("PHIL") ?: return data
        val logic = phil.courses.filter { it.number == "110" }
        val symbolic = phil.courses.filter { it.number == "210" }
        val schedule = EnrollPlan(
            start = today,
            meetings = listOf(
                Meeting(dayOfWeek = 2, startMinute = 19 * 60, durationMinutes = 60, label = "Seminar", location = "Study desk"),
                Meeting(dayOfWeek = 4, startMinute = 19 * 60, durationMinutes = 60, label = "Seminar", location = "Study desk"),
                Meeting(dayOfWeek = 6, startMinute = 10 * 60, durationMinutes = 90, label = "Reading lab", location = "Library"),
            ),
        )
        var d = data.addFromBulletin(phil, logic, enroll = schedule).first
        d = d.addFromBulletin(phil, symbolic).first
        if (d.programs.none { it.name == "Critical Thinking" }) {
            val ids = listOf("PHIL 110", "PHIL 210").mapNotNull { code -> d.courses.find { d.codeOf(it) == code }?.id }
            d = d.copy(programs = d.programs + Program(name = "Critical Thinking", kind = ProgramKind.CERTIFICATE, creditsRequired = 6, courseIds = ids))
        }
        return d
    }
}
