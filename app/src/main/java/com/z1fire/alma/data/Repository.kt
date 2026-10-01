package com.z1fire.alma.data

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.Executors

/** Single source of truth. State lives in memory and is written to a JSON file on every change. */
class Repository(private val file: File) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }
    private val writer = Executors.newSingleThreadExecutor()
    private val _state = MutableStateFlow(load())
    val state: StateFlow<AppData> = _state.asStateFlow()
    val current: AppData get() = _state.value

    private fun load(): AppData = try {
        if (file.exists()) json.decodeFromString(AppData.serializer(), file.readText()) else AppData()
    } catch (e: Exception) {
        Log.e(TAG, "Could not read data; keeping a copy and starting fresh", e)
        runCatching { file.copyTo(File(file.parentFile, "curriculum-unreadable-${System.currentTimeMillis()}.json")) }
        AppData()
    }

    fun update(transform: (AppData) -> AppData) {
        val next = _state.updateAndGet(transform)
        val text = json.encodeToString(AppData.serializer(), next)
        writer.execute {
            try {
                val tmp = File(file.parentFile, file.name + ".tmp")
                tmp.writeText(text)
                if (!tmp.renameTo(file)) {
                    file.delete()
                    tmp.renameTo(file)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save", e)
            }
        }
    }

    fun updateProfile(f: (Profile) -> Profile) = update { it.copy(profile = f(it.profile)) }

    fun saveCourse(course: Course) = update { it.copy(courses = it.courses.upsert(course) { c -> c.id }) }

    fun updateCourse(id: String, f: (Course) -> Course) =
        update { d -> d.copy(courses = d.courses.map { if (it.id == id) f(it) else it }) }

    fun deleteCourse(id: String) = update { d ->
        d.copy(
            courses = d.courses.filter { it.id != id }.map { it.copy(prerequisiteIds = it.prerequisiteIds - id) },
            programs = d.programs.map { it.copy(courseIds = it.courseIds - id) },
            activeTimer = d.activeTimer?.takeIf { it.courseId != id },
        )
    }

    /** Copies a course as a fresh, unstarted plan. Returns the new id. */
    fun duplicateCourse(id: String): String? {
        val src = current.course(id) ?: return null
        val copy = src.copy(
            id = newId(),
            title = src.title + " (copy)",
            status = CourseStatus.PLANNED,
            meetings = src.meetings.map { it.copy(id = newId()) },
            modules = src.modules.map { it.copy(id = newId(), done = false) },
            resources = src.resources.map { it.copy(id = newId(), status = ResourceStatus.NOT_STARTED) },
            assignments = src.assignments.map { it.copy(id = newId(), done = false, grade = "") },
            sessions = emptyList(),
            finalGrade = null,
            reflection = "",
            completedEpochDay = null,
            createdAtMillis = System.currentTimeMillis(),
        )
        saveCourse(copy)
        return copy.id
    }

    fun saveDepartment(dep: Department) = update { it.copy(departments = it.departments.upsert(dep) { d -> d.id }) }

    fun deleteDepartment(id: String) = update { d ->
        d.copy(
            departments = d.departments.filter { it.id != id },
            courses = d.courses.map { if (it.departmentId == id) it.copy(departmentId = null) else it },
            programs = d.programs.map { if (it.departmentId == id) it.copy(departmentId = null) else it },
        )
    }

    fun saveProgram(p: Program) = update { it.copy(programs = it.programs.upsert(p) { x -> x.id }) }

    fun deleteProgram(id: String) = update { d -> d.copy(programs = d.programs.filter { it.id != id }) }

    fun startTimer(courseId: String) =
        update { it.copy(activeTimer = ActiveTimer(courseId, System.currentTimeMillis())) }

    fun cancelTimer() = update { it.copy(activeTimer = null) }

    /** Stops the running session timer, logging [minutes] against its course. */
    fun finishTimer(minutes: Int, notes: String) = update { d ->
        val t = d.activeTimer ?: return@update d
        val day = Instant.ofEpochMilli(t.startedAtMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        val session = StudySession(epochDay = day.toEpochDay(), minutes = minutes, notes = notes)
        d.copy(
            activeTimer = null,
            courses = d.courses.map {
                if (it.id == t.courseId && minutes > 0) it.copy(sessions = it.sessions + session) else it
            },
        )
    }

    fun exportJson(): String = json.encodeToString(AppData.serializer(), current)

    fun importJson(text: String): Boolean = try {
        val d = json.decodeFromString(AppData.serializer(), text)
        update { d.copy(profile = d.profile.copy(onboarded = true)) }
        true
    } catch (e: Exception) {
        Log.e(TAG, "Import failed", e)
        false
    }

    fun loadSample(today: LocalDate = LocalDate.now()) = update { SampleData.addTo(it, today) }

    companion object {
        private const val TAG = "AlmaRepository"
    }
}
