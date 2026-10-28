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

## Current status
The first archived HTML file reflects the saved SIGNAL prototype dated 2026-10-28. It is a static sample, not an automatically refreshed feed. Daily content generation has not yet been connected to Git commits or Netlify deployment.
