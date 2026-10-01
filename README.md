# Alma — your personal university

An Android app that treats self-directed learning like an ongoing college education. Alma doesn't hold the
learning material itself — it organizes and schedules it, the way a registrar, a syllabus and a course catalog would.

**Download:** grab `Alma.apk` from the [latest release](../../releases/latest) and install it on your phone
(allow "install unknown apps" for your browser or file manager).

## What you can do

- **Matriculate.** Name yourself, your university, and its motto.
- **Course catalog.** Found departments (PHIL, MATH, HIST…) and design courses with numbers, levels, credits,
  a catalog description, learning objectives and prerequisites.
- **Syllabus.** Break each course into units and check them off.
- **Reading list.** Books, textbooks, articles, papers, lectures, podcasts and sites — required or recommended,
  with links and reading status.
- **Assignments.** Problem sets, essays, projects, self-quizzes and exams with due dates and self-assessed grades.
- **Class schedule.** Weekly meeting times for each course, a week-by-week agenda, and optional reminders before class.
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
