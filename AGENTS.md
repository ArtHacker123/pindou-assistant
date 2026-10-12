# AGENTS.md — 拼豆助手 (Pindou Assistant)

## File editing — never use PowerShell to write files
Do NOT use PowerShell cmdlets that write file contents on this repo: `Set-Content`, `Out-File`, `>`/`>>`, or pipes like `... | Set-Content`. PowerShell 5.1 re-encodes files and corrupts the UTF-8 Chinese text, breaking sources and UI strings (this has already happened once).

Always change file contents with the dedicated `read`, `write`, and `edit` tools. The `bash` tool is for build / test / git / adb only.

## Build / test
- `JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"`
- Gradle: `C:\Users\artli\.gradle\wrapper\dists\gradle-8.5-bin\5hry6tgzq0wontdz18qo6fdj9\gradle-8.5\bin\gradle.bat`
- Build: `:core:test :app:assembleDebug --console=plain --no-daemon`
- APK: `G:\pindou-build\app\outputs\apk\debug\app-debug.apk`
- adb: `C:\Users\artli\AppData\Local\Android\Sdk\platform-tools\adb.exe`, test device `642cc491`

## Workflow
- Propose and confirm before coding; keep OpenSpec in sync (`npx openspec ...`) and commit only when explicitly asked.
- Compose quirks (1.5.4): no `InkEraser`; no `lastVisibleItemIndex`; `animateScrollBy` needs an explicit import; `0xFFEEEEEE` needs `.toInt()`.
