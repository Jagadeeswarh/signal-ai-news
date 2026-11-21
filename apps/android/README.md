# VANTAGE for Android

Native Android reader built with Kotlin and Jetpack Compose.

## Current foundation

- Edition, Sections and Saved destinations
- Charcoal / burgundy VANTAGE visual system
- Story detail view with a “The Bigger Picture” context layer
- GitHub-hosted JSON edition feed
- Cached latest edition for offline reopening
- Device-local bookmarks
- Manual refresh; WorkManager background sync is the next integration step

## Build

Open the repository root in Android Studio (Giraffe or newer), let Gradle sync, then run the `apps.android` application configuration or select the `android` module.

Requirements: JDK 17, Android SDK 35, minimum Android 8.0 (API 26).

## Edition contract

The app reads:

`content/editions/latest.json`

The JSON object contains `dateLabel`, `kicker`, `title`, `description`, `bigPicture`, and a `stories` array. Each story has `id`, `title`, `section`, `summary`, `context`, `source`, `url`, and `readTime`.

The current JSON is explicitly labelled as an app preview, not live news. The publishing workflow must generate this contract from each completed VANTAGE edition before the mobile app can display production stories.

## Monorepo direction

- Existing root website: current web publication (kept at repository root)
- `apps/android`: native Android app
- `apps/ios`: reserved for the future iOS app
- `content/editions`: platform-neutral edition feed consumed by mobile clients
