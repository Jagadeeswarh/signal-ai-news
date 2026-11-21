# VANTAGE monorepo

This repository now hosts the VANTAGE publication and mobile application work in one place.

| Area | Location | Technology |
| --- | --- | --- |
| Web publication | Repository root | Existing HTML/CSS/JavaScript publication |
| Android | `apps/android` | Kotlin, Jetpack Compose |
| iOS | `apps/ios` | Reserved for future native client |
| Shared edition feed | `content/editions` | JSON contract |
| Historical web editions | `archive` | Existing HTML archive |

## Publishing contract

The daily publishing workflow should continue producing the web edition and archive as it does today. As a separate final publishing step, it should also produce `content/editions/latest.json` using the schema documented in `apps/android/README.md`. Mobile clients should consume this stable data contract rather than scrape the HTML publication.

Do not move or rename the current web root: the existing Netlify site depends on the current repository layout. Keep `index.html`, `archive/`, and `netlify.toml` at their existing paths.

## Next engineering steps

1. Convert completed editorial stories into the shared JSON contract during the existing single-publication workflow.
2. Add robust background sync with WorkManager, retry policy and sync metadata.
3. Replace the first local bookmark store with Room and persist reading progress.
4. Add accessibility settings, font controls and theme accent preferences.
5. Define authentication only when a real account-backed feature requires it; the current reader does not require sign-in.
6. Add the iOS client against the same edition contract.
