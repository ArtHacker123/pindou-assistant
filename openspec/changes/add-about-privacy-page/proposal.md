## Why

The app handles the user's photos but gives them no way to see what it does with them or what data it uses; there is no about/privacy surface at all. A visible privacy note is expected of any app that touches the gallery/camera, and it becomes essential once community/network features are added. The first step is a reachable entry point to an about & privacy page.

## What Changes

- Add an **about & privacy page** that shows the app name and version and a privacy note covering: images are processed on-device, the app only receives the picture the user picks/shoots (system photo picker / system camera), and any network use is on demand (future community/share features), not background.
- Add an **entry** to that page at the bottom of the home screen, below the tip card.
- Make **back** (system back gesture and the page's on-screen back control) return to the home screen.

## Capabilities

### New Capabilities
- `privacy`: an about & privacy page presenting app identity and a privacy note; reachable from the home screen and dismissible back to home.

### Modified Capabilities
- `home`: the home section order gains a footer entry (about & privacy) placed below the tip card.
- `navigation`: the back gesture on the about & privacy page returns to the home screen.

## Impact

- `app/.../PindouController.kt` — add `Screen.About` and `goAbout()`.
- `app/.../ui/AboutScreen.kt` — new screen (top bar, app name/version, privacy note).
- `app/.../ui/PindouApp.kt` — route `Screen.About` to the new screen.
- `app/.../ui/HomeScreen.kt` — footer entry below the tip card.
- `openspec/specs/privacy/spec.md` (new), `openspec/specs/home/spec.md`, `openspec/specs/navigation/spec.md`.
- No data, model, pipeline, persistence, or permission changes.
