# SIGNAL. — AI Daily deployment notes

## Current deployment layout
- `index.html` is the homepage.
- `archive/index.html` lists saved issues.
- `archive/YYYY-MM-DD.html` is an immutable, dated edition file.
- `netlify.toml` tells Netlify to publish the repository root as a static site.
- Existing README.md has been left unchanged.

## Preservation rules
1. Never deploy this site into the existing `jagadeeswarh.github.io` site.
2. Add new editions under `archive/YYYY-MM-DD.html`; do not overwrite an existing dated path.
3. Update the archive index with each verified new edition.
4. Before changing `index.html`, validate the new HTML and source links.
5. Only publish a newly generated edition after it passes validation; if validation fails, keep the last known-good homepage.

## Current status (updated 2026-10-28)
- `index.html` is the current source-checked 28 October edition with 20 ranked story cards.
- `archive/2026-10-28-current-v2.html` is the latest corrected edition for 28 October.
- `archive/2026-10-28-current.html` is the first source-checked build, retained unchanged for history.
- `archive/2026-10-28.html` is the original starter prototype, retained unchanged.
- `archive/index.html` links all three; future editions must be added rather than replacing any existing dated file.
- The daily SIGNAL automation is scheduled for 8:00 AM Asia/Kolkata and has been instructed to publish to this repository, but a successful end-to-end scheduled run is not yet verified.
- Netlify is expected to build this repository's `main` branch. Check the Netlify Deploys page to confirm the production deploy follows the latest Git commit. Do not claim live deployment until verified.
