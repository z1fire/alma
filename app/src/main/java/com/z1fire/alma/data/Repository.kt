package com.z1fire.alma.data

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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
        if (file.exists()) {
            val text = file.readText()
            if (versionOf(text) < AppData.CURRENT_VERSION) {
                // Keep the pre-migration file, just in case.
                runCatching { file.copyTo(File(file.parentFile, "curriculum-v1-backup.json"), overwrite = false) }
            }
            decode(text)
        } else {
            AppData()
        }
    } catch (e: Exception) {
        Log.e(TAG, "Could not read data; keeping a copy and starting fresh", e)
        runCatching { file.copyTo(File(file.parentFile, "curriculum-unreadable-${System.currentTimeMillis()}.json")) }
        AppData()
    }

    private fun versionOf(text: String): Int =
        runCatching { json.parseToJsonElement(text).jsonObject["version"]?.jsonPrimitive?.int }.getOrNull() ?: 1

    /** Reads either format: the current one, or version 1 which is migrated. */
    private fun decode(text: String): AppData {
        return if (versionOf(text) >= AppData.CURRENT_VERSION) {
            json.decodeFromString(AppData.serializer(), text)
        } else {
            json.decodeFromString(LegacyAppData.serializer(), text).toCurrent()
        }
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
        d.copy(courses = d.courses.filter { it.id != id }, activeTimer = d.activeTimer?.takeIf { it.courseId != id })
    }

    fun setStatus(id: String, status: CourseStatus, reflection: String? = null) = updateCourse(id) { c ->
        val today = LocalDate.now().toEpochDay()
        c.copy(
            status = status,
            startedEpochDay = if (status == CourseStatus.ACTIVE) c.startedEpochDay ?: today else c.startedEpochDay,
            finishedEpochDay = if (status == CourseStatus.FINISHED) today else null,
            reflection = reflection ?: c.reflection,
        )
    }

    fun toggleItem(courseId: String, itemId: String) = updateCourse(courseId) { c ->
        c.copy(items = c.items.map { if (it.id == itemId) it.copy(done = !it.done) else it })
    }

    fun logSession(courseId: String, session: StudySession) =
        updateCourse(courseId) { it.copy(sessions = it.sessions + session) }

    fun startTimer(courseId: String) =
        update { it.copy(activeTimer = ActiveTimer(courseId, System.currentTimeMillis())) }

    fun cancelTimer() = update { it.copy(activeTimer = null) }

    /** Stops the running timer, logging [minutes] against its course on the day it started. */
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
        val d = decode(text)
        update { d }
        true
    } catch (e: Exception) {
        Log.e(TAG, "Import failed", e)
        false
    }

    companion object {
        private const val TAG = "AlmaRepository"
    }
}
