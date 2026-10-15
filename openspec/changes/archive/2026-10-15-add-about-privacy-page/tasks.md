## 1. Navigation

- [x] 1.1 Add `data object About : Screen` to the `Screen` sealed interface in `PindouController.kt`
- [x] 1.2 Add `fun goAbout() { nav.go(Screen.About) }` to `PindouController`
- [x] 1.3 Route `Screen.About -> AboutScreen(controller)` in `PindouApp.kt`

## 2. About & privacy page

- [x] 2.1 Create `ui/AboutScreen.kt`: `Scaffold` + `TopAppBar` titled 「关于与隐私」 with a back `IconButton` and `BackHandler { controller.goHome() }`
- [x] 2.2 Show the app name 「拼豆助手」 and the version from `PackageManager` when it can be read (omit otherwise)
- [x] 2.3 Add the privacy note: on-device processing; only the user-picked/shot picture (no full-gallery access); network only on demand for optional features, never in the background

## 3. Home entry

- [x] 3.1 In `HomeScreen.kt`, add a centered 「关于与隐私」 entry below the tip card that calls `controller.goAbout()`

## 4. Verification

- [x] 4.1 `:core:test :app:assembleDebug` passes
- [x] 4.2 Install the APK and have the user verify: the 关于与隐私 entry sits below the tip card, opens the page, the note reads correctly, and back returns home
- [x] 4.3 Re-validate the change `--strict`
