# Alma — a simple study tracker

A small Android app for self-directed learning. Plan courses in a catalogue, study them from your curriculum, and keep a
record of what you've finished — each with a checklist of what to study, a running total of time, and an optional
hour goal for long-haul subjects.

**Download:** grab `Alma.apk` from the [latest release](../../releases/latest) and install it on your phone
(allow "install unknown apps" for your browser or file manager).

## How it works

- **Curriculum** — what you're studying now.
- **Catalogue** — courses you've planned but not started, grouped by subject. Tap **Start** when you're ready.
- **Finished** — completed courses with dates, hours and a certificate.
- **A course** has a name, optional subject, notes and color, a **What to study** checklist (with optional links),
  and an optional **hour goal** — use a big one (say 10,000h) for ongoing studies like a language.
- **Time:** tap **+15m / +30m / +1h** after studying (with Undo). Tap the total to enter hours you'd already
  studied. Courses with a goal show your daily average and a projected date for reaching it; crossing the goal
  offers to mark the course finished.
- From the **⋮** menu: an optional daily reminder (only on days you haven't studied), your name for certificates,
  a home-screen widget, and backup / restore.

Upgrading from an older version converts your data automatically; a copy of the old file is kept on the device.

## Building

Kotlin + Jetpack Compose, minSdk 26. `./gradlew assembleRelease` builds a minified APK (signed with the debug key
unless `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` are set). GitHub Actions builds every push
and attaches `Alma.apk` to a release for each `v*` tag.
