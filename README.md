# Alma — a simple study tracker

An Android app for self-directed learning. Each "course" is just something you're learning — Mandarin, guitar,
cell biology — with a checklist of what to study and a log of the time you put in. When you're done, mark it
finished and get a certificate of completion.

**Download:** grab `Alma.apk` from the [latest release](../../releases/latest) and install it on your phone
(allow "install unknown apps" for your browser or file manager).

## What it does

- **Courses** with a name, optional subject, notes and color. Start from a starter idea (languages, biology,
  logic, art, guitar, fitness, space, engineering, computer science) or type your own.
- **What to study:** a checklist of books, chapters, topics or videos, with optional links. Check items off as you go.
- **Track time:** a study timer (with a notification you can stop from) or log sessions after the fact.
  See this week's hours against your goal and your daily streak.
- **Studying / Up next / Finished:** keep what you're working on now separate from what's next. Finishing a
  course asks what you learned and gives you a certificate with your total study time.
- **Daily reminder** (optional): one evening nudge, only on days you haven't studied.
- **Home-screen widget:** this week's study time and your current courses.
- **Backups:** everything stays on the device; save and restore a JSON backup from Settings.

Upgrading from 1.x converts your data automatically: syllabus units, readings and assignments become checklist
items, and study sessions carry over.

## Building

Kotlin + Jetpack Compose, minSdk 26. `./gradlew assembleRelease` builds a minified APK (signed with the debug key
unless `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` are set). GitHub Actions builds every push
and attaches `Alma.apk` to a release for each `v*` tag.
