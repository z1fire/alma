package com.z1fire.alma.data.bulletin

import com.z1fire.alma.data.AppData
import com.z1fire.alma.data.Assignment
import com.z1fire.alma.data.AssignmentType
import com.z1fire.alma.data.Course
import com.z1fire.alma.data.CourseStatus
import com.z1fire.alma.data.Department
import com.z1fire.alma.data.Meeting
import com.z1fire.alma.data.Module
import com.z1fire.alma.data.Program
import com.z1fire.alma.data.ProgramKind
import com.z1fire.alma.data.Resource
import com.z1fire.alma.data.ResourceType
import com.z1fire.alma.data.codeOf
import com.z1fire.alma.data.enrolled
import com.z1fire.alma.data.newId
import java.time.LocalDate

/**
 * The Course Bulletin: ready-made course designs the student can copy into their own catalog.
 * Templates are plain values; [addFromBulletin] turns them into real, editable courses.
 */
data class TemplateDept(
    val name: String,
    val code: String,
    val colorIndex: Int,
    val blurb: String,
    val courses: List<TemplateCourse>,
    val programs: List<TemplateProgram>,
)

data class TemplateCourse(
    val deptCode: String,
    val number: String,
    val title: String,
    val credits: Int,
    val weeks: Int,
    val description: String,
    val objectives: List<String>,
    val units: List<Pair<String, String>>,
    val resources: List<Resource>,
    val tasks: List<TemplateTask>,
    /** Full course codes, e.g. "MATH 151". */
    val prereqs: List<String>,
) {
    val code: String get() = "$deptCode $number"
}

data class TemplateTask(val title: String, val type: AssignmentType, val week: Int)

data class TemplateProgram(val name: String, val kind: ProgramKind, val numbers: List<String>)

// ---------------- builder DSL ----------------

@DslMarker
annotation class BulletinDsl

@BulletinDsl
class DeptBuilder(private val name: String, private val code: String, private val color: Int, private val blurb: String) {
    private val courses = mutableListOf<TemplateCourse>()
    private val programs = mutableListOf<TemplateProgram>()

    /** [prereqs] may be bare numbers in this department ("101") or full codes ("MATH 151"). */
    fun course(
        number: String,
        title: String,
        credits: Int = 3,
        weeks: Int = 15,
        prereqs: List<String> = emptyList(),
        block: CourseBuilder.() -> Unit,
    ) {
        val b = CourseBuilder().apply(block)
        courses += TemplateCourse(
            deptCode = code,
            number = number,
            title = title,
            credits = credits,
            weeks = weeks,
            description = b.about.trimIndent().replace("\n", " "),
            objectives = b.objectiveList,
            units = b.unitList,
            resources = b.resourceList,
            tasks = b.taskList,
            prereqs = prereqs.map { if (it.contains(' ')) it else "$code $it" },
        )
    }

    /** Requires every listed course; the credit total is their sum. */
    fun program(name: String, kind: ProgramKind, vararg numbers: String) {
        programs += TemplateProgram(name, kind, numbers.toList())
    }

    fun build() = TemplateDept(name, code, color, blurb, courses.toList(), programs.toList())
}

@BulletinDsl
class CourseBuilder {
    var about: String = ""
    internal val objectiveList = mutableListOf<String>()
    internal val unitList = mutableListOf<Pair<String, String>>()
    internal val resourceList = mutableListOf<Resource>()
    internal val taskList = mutableListOf<TemplateTask>()

    fun objectives(vararg items: String) {
        objectiveList += items
    }

    /** Each unit is "Title — topics" (the topics part is optional). */
    fun units(vararg items: String) {
        unitList += items.map { item ->
            val parts = item.split(" — ", limit = 2)
            parts[0].trim() to parts.getOrElse(1) { "" }.trim()
        }
    }

    fun read(type: ResourceType, title: String, author: String = "", url: String = "", required: Boolean = true, notes: String = "") {
        resourceList += Resource(type = type, title = title, author = author, url = url, required = required, notes = notes)
    }

    fun book(title: String, author: String, required: Boolean = true, notes: String = "") =
        read(ResourceType.BOOK, title, author, required = required, notes = notes)

    fun textbook(title: String, author: String, url: String = "", required: Boolean = true, notes: String = "") =
        read(ResourceType.TEXTBOOK, title, author, url, required, notes)

    fun site(title: String, author: String, url: String, required: Boolean = false, notes: String = "") =
        read(ResourceType.WEBSITE, title, author, url, required, notes)

    fun lectures(title: String, author: String, url: String = "", required: Boolean = false, notes: String = "") =
        read(ResourceType.LECTURES, title, author, url, required, notes)

    fun online(title: String, author: String, url: String = "", required: Boolean = false, notes: String = "") =
        read(ResourceType.ONLINE_COURSE, title, author, url, required, notes)

    fun task(type: AssignmentType, title: String, week: Int) {
        taskList += TemplateTask(title, type, week)
    }
}

fun department(name: String, code: String, color: Int, blurb: String, block: DeptBuilder.() -> Unit): TemplateDept =
    DeptBuilder(name, code, color, blurb).apply(block).build()

object Bulletin {
    val departments: List<TemplateDept> by lazy {
        (languageDepartments() + scienceDepartments() + stemDepartments() + artsAndBodyDepartments())
            .sortedBy { it.name }
    }

    fun department(code: String): TemplateDept? = departments.find { it.code == code }

    fun course(code: String): TemplateCourse? =
        departments.firstNotNullOfOrNull { d -> d.courses.find { it.code.equals(code, ignoreCase = true) } }

    val courseCount: Int get() = departments.sumOf { it.courses.size }
}

// ---------------- copying templates into the student's catalog ----------------

data class EnrollPlan(val start: LocalDate, val meetings: List<Meeting>)

fun AppData.hasCourse(code: String): Boolean = courses.any { codeOf(it).equals(code, ignoreCase = true) }

private fun AppData.idOf(code: String): String? = courses.find { codeOf(it).equals(code, ignoreCase = true) }?.id

private fun TemplateCourse.toCourse(departmentId: String): Course = Course(
    departmentId = departmentId,
    number = number,
    title = title,
    credits = credits,
    description = description,
    objectives = objectives,
    status = CourseStatus.PLANNED,
    durationWeeks = weeks,
    modules = units.map { (t, n) -> Module(title = t, notes = n) },
    resources = resources.map { it.copy(id = newId()) },
    assignments = tasks.map {
        Assignment(title = it.title, type = it.type, dueOffsetDays = (it.week * 7 - 1).coerceAtLeast(0))
    },
)

/**
 * Copies [picks] from [dept] into the catalog as planned courses (skipping ones already there),
 * optionally enrolling a single pick per [enroll], and optionally declaring the department's programs.
 * Returns the updated data and the ids of the courses added.
 */
fun AppData.addFromBulletin(
    dept: TemplateDept,
    picks: List<TemplateCourse>,
    enroll: EnrollPlan? = null,
    withPrograms: Boolean = false,
): Pair<AppData, List<String>> {
    val existingDept = departments.find { it.code.equals(dept.code, ignoreCase = true) }
    val department = existingDept ?: Department(name = dept.name, code = dept.code, colorIndex = dept.colorIndex)
    var d = if (existingDept == null) copy(departments = departments + department) else this

    val added = mutableListOf<String>()
    for (t in picks) {
        if (d.hasCourse(t.code)) continue
        var course = t.toCourse(department.id)
        if (enroll != null) {
            course = course.copy(startEpochDay = enroll.start.toEpochDay(), meetings = enroll.meetings).enrolled(enroll.start)
        }
        d = d.copy(courses = d.courses + course)
        added += course.id
    }
    d = d.relinkPrerequisites()

    if (withPrograms) {
        for (p in dept.programs) {
            if (d.programs.any { it.name == p.name && it.kind == p.kind }) continue
            d = d.copy(
                programs = d.programs + Program(
                    name = p.name,
                    kind = p.kind,
                    creditsRequired = p.numbers.sumOf { n -> dept.courses.find { it.number == n }?.credits ?: 0 },
                    courseIds = p.numbers.mapNotNull { d.idOf("${dept.code} $it") },
                ),
            )
        }
    }
    return d to added
}

/**
 * Wires prerequisites for every bulletin-derived course whose prerequisites are now in the catalog,
 * so adding MATH 151 after PHYS 101 still links them.
 */
fun AppData.relinkPrerequisites(): AppData {
    val updated = courses.map { c ->
        val t = Bulletin.course(codeOf(c))?.takeIf { it.title == c.title } ?: return@map c
        val ids = t.prereqs.mapNotNull { idOf(it) }.filter { it != c.id && it !in c.prerequisiteIds }
        if (ids.isEmpty()) c else c.copy(prerequisiteIds = c.prerequisiteIds + ids)
    }
    return copy(courses = updated)
}
