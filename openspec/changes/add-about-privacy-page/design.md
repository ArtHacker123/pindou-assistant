## Context

The app is a single-activity Compose app. Screens are modelled as a `sealed interface Screen` (`PindouController.kt`) and routed by a `when (controller.screen)` in `PindouApp.kt`; each secondary screen (History, Crop, Export) owns a `BackHandler` that mirrors its on-screen back control. The home screen is a single scrolling `Column` whose sections end with the tip card (`HomeScreen.kt`). There is currently no about/privacy surface.

The manifest declares no `INTERNET`, `CAMERA`, or gallery permissions; gallery/camera use the system photo picker (`PickVisualMedia`) and the system camera (`TakePicture`), which return only the single picked/captured image. Community/network features are planned but not yet built, so the privacy wording must stay truthful after they land.

## Goals / Non-Goals

**Goals:**
- Give the app a discoverable about & privacy entry reachable in one tap from home.
- State plainly where image data goes and when the app would use the network.
- Keep the page structure that can grow into a full policy page later.

**Non-Goals:**
- A full legal privacy policy / terms of service.
- Any network permission or connectivity code (deferred to the community feature).
- Language/locale switching or a settings screen.

## Decisions

### D1: A dedicated screen, not a dialog
Add `Screen.About` and an `AboutScreen` composable, routed like History/Export, rather than a dialog. A screen already has the back handling and title-bar conventions, and it gives the future full policy a place to live without a rework.
- *Alternative*: a dialog from home — lighter, but cramped for growing policy text and inconsistent with how the app presents multi-paragraph pages.

### D2: Entry at the bottom of the home column
Place a centered text button 「关于与隐私」 after `TipCard()` (the last home section). This keeps the primary flow (generate / free-create / recent / templates) untouched and matches the app's existing text-button entries (e.g. 查看全部).
- *Alternative*: a title-bar icon — competes with the bead logo title area and is less self-describing.

### D3: Version from the package manager
Read `versionName` via `PackageManager.getPackageInfo(packageName, 0)` inside a `runCatching`, showing nothing if it fails. Avoids enabling/`BuildConfig` plumbing and needs no new build config.

### D4: Wording that survives the network feature
The note states (a) images are processed **on-device** to build the pattern, (b) the app only receives the picture the user picks or shoots, and (c) network is used **only on demand** for future community/share features. It deliberately avoids the brittle promise "this app never uses the network", which would become false when the community feature ships. Wording is data-accuracy, not marketing.

## Risks / Trade-offs

- [The note is not yet a full policy] → acceptable: it is honest and future-proof; a full policy can be appended to the same page when community features are designed.
- [Version string unavailable] → the row is simply omitted; the page still renders.
- [Wording drifts from reality as features ship] → D4 is phrased around on-device processing and on-demand network use, which remain true; revisit wording in the community change.
