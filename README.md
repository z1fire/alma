# Alma — a simple study tracker

A small Android app for self-directed learning. Each course is something you're learning — Mandarin, guitar,
cell biology — with a checklist of what to study and a running total of the time you've put in. When you're done,
mark it finished.

**Download:** grab `Alma.apk` from the [latest release](../../releases/latest) and install it on your phone
(allow "install unknown apps" for your browser or file manager).

## How it works

- **One list.** What you're studying at the top; finished courses collect under **Done**.
- **A course** has a name, an optional subject, notes and color, and a **What to study** checklist
  (books, chapters, topics — with optional links). Start from a starter idea or type your own.
- **Time:** tap **+15m / +30m / +1h** after a study session (with Undo). Tap the total to correct it.
- **Finish** a course to note what you learned and get a certificate of completion.
- From the **⋮** menu: an optional daily reminder (only on days you haven't studied), your name for certificates,
  a home-screen widget, and backup / restore.

Upgrading from an older version converts your data automatically; a copy of the old file is kept on the device.

## Building

Kotlin + Jetpack Compose, minSdk 26. `./gradlew assembleRelease` builds a minified APK (signed with the debug key
unless `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` are set). GitHub Actions builds every push
and attaches `Alma.apk` to a release for each `v*` tag.
