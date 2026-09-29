# Android application

Project creator and developer: **PolarDredd**. See [credits](../CREDITS.md).

This module builds `com.mrdiy.careers`. `build.gradle` declares Android SDK levels, app version, dependencies, and build features; `proguard-rules.pro` controls release shrinking.

| Folder | Responsibility |
| --- | --- |
| `src/main/` | Manifest, runtime Kotlin code, and Android resources. |
| `src/main/java/com/mrdiy/careers/` | [Application entry point and feature map](src/main/java/com/mrdiy/careers/README.md). |
| `src/main/res/` | [Layouts, navigation, strings, themes, and artwork](src/main/RESOURCES.md). |
| `src/test/` | [JVM regression tests](src/test/README.md). |
| `src/androidTest/` | [Device/emulator tests](src/androidTest/README.md). |
| `build/` | Generated output. Do not edit generated bindings or classes. |

Run from the repository root:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

For device tests, connect a dedicated test device/emulator and run `.\gradlew.bat :app:connectedDebugAndroidTest`. Tests may alter app state. A release assembly can be built with `:app:assembleRelease`; review signing separately before distribution.

Changes to screen IDs or navigation arguments must stay aligned across Kotlin, XML layouts, and `res/navigation/nav_graph.xml`. Rebuild generated ViewBinding and Safe Args classes instead of editing them. A rebuilt APK does not automatically update `website/downloads/mrdiy-careers.apk`.
