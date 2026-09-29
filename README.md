# MR.D.I.Y. Careers

Created and developed by **PolarDredd**. See [project credits](CREDITS.md) for attribution and existing contributors.

This repository contains a native Kotlin Android careers app and a static APK download website. The directory name `mrdiyreact` is historical; the application here is built with Android Gradle, XML layouts, ViewBinding, and ViewModels.

## Start here

- [Android app and build instructions](app/README.md)
- [Source code and feature directory map](app/src/main/java/com/mrdiy/careers/README.md)
- [Download website](website/README.md)
- [Database maintenance](supabase/README.md)
- [Testing and handoff evidence](qa/README.md)
- [Commit comments and contributor workflow](CONTRIBUTING.md)

Open this directory in Android Studio. Install Android SDK 36 and configure your own SDK path in `local.properties`. Build versions are defined in `build.gradle`, `app/build.gradle`, and `gradle/wrapper/gradle-wrapper.properties`.

From PowerShell at the repository root:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`. `run-local.ps1` also detects the SDK, updates the local SDK path, and installs/launches on a connected device.

## Handoff notes

Read [admin integration](ADMIN_APP_INTEGRATION.md), [email setup](SUPABASE_EMAIL_SETUP.md), and [stabilization evidence](STABILIZATION.md) before changing backend behavior. These documents describe previous checks and unresolved live acceptance work; they do not certify the current deployment.

Other repository folders:

| Folder | Purpose and maintenance note |
| --- | --- |
| `gradle/` | Gradle wrapper and daemon settings; see its README. |
| `scripts/` | Backend diagnostic tooling; see its README. |
| `.vscode/` | Editor task configuration; inspect command paths before running on another machine. |
| `.idea/` | Android Studio project settings and machine-specific state. |
| `.integration/` | Local screenshots, inspection output, and an admin checkout; not the Android source of truth. |
| `.gradle/`, `.kotlin/` | Tool caches and diagnostic output. |
| `build/`, `app/build/` | Generated reports, bindings, compiled classes, and APKs. Change source files rather than generated output. |

Some generated files and `local.properties` are already tracked despite ignore rules. Stage intended files explicitly; avoid mixing cache changes into a source or documentation commit.
