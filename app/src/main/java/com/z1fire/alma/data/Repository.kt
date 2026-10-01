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
import java.time.LocalDate
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
            val version = versionOf(text)
            if (version < AppData.CURRENT_VERSION) {
                // Keep the pre-upgrade file, just in case.
                runCatching { file.copyTo(File(file.parentFile, "curriculum-v$version-backup.json"), overwrite = false) }
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

    /** Reads the current format or any older one. */
    private fun decode(text: String): AppData = when (versionOf(text)) {
        1 -> json.decodeFromString(V1AppData.serializer(), text).toCurrent()
        2 -> json.decodeFromString(V2AppData.serializer(), text).toCurrent()
        else -> json.decodeFromString(AppData.serializer(), text)
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

    fun saveCourse(course: Course) = update { d ->
        d.copy(courses = if (d.courses.any { it.id == course.id }) d.courses.map { if (it.id == course.id) course else it } else d.courses + course)
    }

    fun updateCourse(id: String, f: (Course) -> Course) =
        update { d -> d.copy(courses = d.courses.map { if (it.id == id) f(it) else it }) }

    fun deleteCourse(id: String) = update { d -> d.copy(courses = d.courses.filter { it.id != id }) }

    fun toggleItem(courseId: String, itemId: String) = updateCourse(courseId) { c ->
        c.copy(items = c.items.map { if (it.id == itemId) it.copy(done = !it.done) else it })
    }

    fun addMinutes(courseId: String, minutes: Int) = updateCourse(courseId) {
        it.copy(minutes = it.minutes + minutes, lastStudiedEpochDay = LocalDate.now().toEpochDay())
    }

    fun finish(courseId: String, reflection: String) = updateCourse(courseId) {
        it.copy(finishedEpochDay = LocalDate.now().toEpochDay(), reflection = reflection)
    }

    fun reopen(courseId: String) = updateCourse(courseId) { it.copy(finishedEpochDay = null) }

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
