# Alma — your personal university

An Android app that treats self-directed learning like an ongoing college education. Alma doesn't hold the
learning material itself — it organizes and schedules it, the way a registrar, a syllabus and a course catalog would.

**Download:** grab `Alma.apk` from the [latest release](../../releases/latest) and install it on your phone
(allow "install unknown apps" for your browser or file manager).

## What you can do

- **Course Bulletin.** 67 ready-made courses across 15 departments — Mandarin, Japanese, Thai, Spanish, biology
  (cellular & molecular), chemistry, philosophy & logic, art, guitar & music theory, fitness, astronomy & space,
  engineering, computer science, mathematics and physics — each with a syllabus, real reading list, assignments and
  prerequisites. Add one course, a whole department (with its certificates and minors), or enroll on the spot.
- **Matriculate.** Name yourself, your university, and its motto.
- **Course catalog.** Found departments (PHIL, MATH, HIST…) and design courses with numbers, levels, credits,
  a catalog description, learning objectives and prerequisites.
- **Syllabus.** Break each course into units and check them off.
- **Reading list.** Books, textbooks, articles, papers, lectures, podcasts and sites — required or recommended,
  with links and reading status.
- **Assignments.** Problem sets, essays, projects, self-quizzes and exams with due dates and self-assessed grades.
- **Class schedule.** Weekly meeting times for each course, a week-by-week agenda, and a "Today's classes" home-screen widget.
- **Notifications.** Class reminders, a morning briefing (classes, due and overdue work), an evening study nudge on days
  you haven't studied, a Sunday weekly report, and a live session-timer notification with End & log.
- **Attendance.** Start a timed study session or log one afterwards; track weekly hours against a goal and keep a streak.
- **Grades & transcript.** Complete a course with a final grade and a reflection. Earn credits, a GPA, class standing,
  term GPAs and Dean's List.
- **Degrees.** Declare certificates, minors, majors or whole degrees; when requirements are met, confer it and view your diploma.
- **Backups.** Everything stays on the device; export/import a JSON backup from the Registrar's Office.

A sample course — *PHIL 110: Logic & the Art of Argument* — can be loaded to see how it all fits together.

## Building

Kotlin + Jetpack Compose, minSdk 26. `./gradlew assembleRelease` builds a minified APK (signed with the debug key
unless `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` are set). GitHub Actions builds every push
and attaches `Alma.apk` to a release for each `v*` tag.
