package com.z1fire.alma.data

import java.time.LocalDate

/** A ready-made example: an intro logic course, its follow-on, and a certificate tying them together. */
object SampleData {
    fun addTo(data: AppData, today: LocalDate): AppData {
        val phil = data.departments.find { it.code == "PHIL" }
            ?: Department(name = "Philosophy", code = "PHIL", colorIndex = 1)
        val t = today.toEpochDay()

        val logic = Course(
            departmentId = phil.id,
            number = "110",
            title = "Logic & the Art of Argument",
            credits = 3,
            description = "An introduction to reasoning well. We learn to find the argument hiding in " +
                "everyday speech, test whether it holds up, name the ways it can go wrong, and build " +
                "sound, charitable arguments of our own — first informally, then with the tools of " +
                "propositional logic.",
            objectives = listOf(
                "Identify premises and conclusions in everyday arguments",
                "Distinguish deductive validity from inductive strength",
                "Recognize and name common informal fallacies",
                "Symbolize arguments in propositional logic and test them with truth tables",
                "Write a clear, charitable, well-structured argumentative essay",
            ),
            status = CourseStatus.ENROLLED,
            term = Terms.forDate(today),
            startEpochDay = t,
            endEpochDay = t + 55,
            meetings = listOf(
                Meeting(dayOfWeek = 2, startMinute = 19 * 60, durationMinutes = 60, label = "Seminar", location = "Study desk"),
                Meeting(dayOfWeek = 4, startMinute = 19 * 60, durationMinutes = 60, label = "Seminar", location = "Study desk"),
                Meeting(dayOfWeek = 6, startMinute = 10 * 60, durationMinutes = 90, label = "Reading lab", location = "Library"),
            ),
            modules = listOf(
                Module(title = "What is an argument?", notes = "Premises, conclusions, and the difference between arguing and asserting."),
                Module(title = "Mapping arguments", notes = "Standard form, argument diagrams, hidden premises."),
                Module(title = "Deduction & induction", notes = "Validity, soundness, strength, cogency."),
                Module(title = "Informal fallacies", notes = "Fallacies of relevance, ambiguity, and presumption."),
                Module(title = "Propositional logic", notes = "Symbolization, connectives, truth tables."),
                Module(title = "Testing validity", notes = "Truth-table tests and the basics of natural deduction."),
                Module(title = "Reasoning under uncertainty", notes = "Analogy, causal reasoning, inference to the best explanation."),
                Module(title = "Capstone: building your own case", notes = "Plan, draft, and critique an argumentative essay."),
            ),
            resources = listOf(
                Resource(type = ResourceType.BOOK, title = "A Rulebook for Arguments", author = "Anthony Weston", required = true, notes = "Short and practical — the backbone of the first half."),
                Resource(type = ResourceType.TEXTBOOK, title = "forall x: Calgary — An Introduction to Formal Logic", author = "P. D. Magnus, Tim Button et al.", url = "https://forallx.openlogicproject.org/", required = true, notes = "Free, open-access textbook for the formal units."),
                Resource(type = ResourceType.TEXTBOOK, title = "Introduction to Logic", author = "Irving M. Copi & Carl Cohen", required = false),
                Resource(type = ResourceType.BOOK, title = "Being Logical: A Guide to Good Thinking", author = "D. Q. McInerny", required = false),
                Resource(type = ResourceType.ARTICLE, title = "Informal Logic", author = "Stanford Encyclopedia of Philosophy", url = "https://plato.stanford.edu/entries/logic-informal/", required = false),
                Resource(type = ResourceType.WEBSITE, title = "The Fallacy Files", author = "Gary N. Curtis", url = "https://www.fallacyfiles.org/", required = false),
            ),
            assignments = listOf(
                Assignment(title = "Read the first half of A Rulebook for Arguments", type = AssignmentType.READING, dueEpochDay = t + 6),
                Assignment(title = "Map five arguments from op-eds", type = AssignmentType.PROBLEM_SET, dueEpochDay = t + 13),
                Assignment(title = "Midterm self-quiz: fallacies & validity", type = AssignmentType.EXAM, dueEpochDay = t + 27),
                Assignment(title = "Fallacy field journal (10 entries)", type = AssignmentType.PROJECT, dueEpochDay = t + 30),
                Assignment(title = "Truth-table problem set", type = AssignmentType.PROBLEM_SET, dueEpochDay = t + 38),
                Assignment(title = "Final paper: 1,500-word argumentative essay", type = AssignmentType.ESSAY, dueEpochDay = t + 55),
            ),
        )

        val symbolic = Course(
            departmentId = phil.id,
            number = "210",
            title = "Symbolic Logic",
            credits = 3,
            description = "Natural deduction for propositional and first-order logic, quantifiers, " +
                "identity, and an introduction to metalogic.",
            objectives = listOf(
                "Construct natural-deduction proofs in propositional and predicate logic",
                "Translate English sentences with quantifiers into first-order logic",
                "Explain soundness and completeness informally",
            ),
            prerequisiteIds = listOf(logic.id),
            status = CourseStatus.PLANNED,
            term = Terms.forDate(today.plusMonths(4)),
            resources = listOf(
                Resource(type = ResourceType.TEXTBOOK, title = "forall x: Calgary — An Introduction to Formal Logic", author = "P. D. Magnus, Tim Button et al.", url = "https://forallx.openlogicproject.org/"),
            ),
        )

        val certificate = Program(
            name = "Critical Thinking",
            kind = ProgramKind.CERTIFICATE,
            creditsRequired = 6,
            courseIds = listOf(logic.id, symbolic.id),
        )

        return data.copy(
            departments = data.departments.upsert(phil) { it.id },
            courses = data.courses + logic + symbolic,
            programs = data.programs + certificate,
        )
    }
}
