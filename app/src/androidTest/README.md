# Android device tests

Project creator and developer: **PolarDredd**.

`java/com/mrdiy/careers/AuthWorkflowTest.kt` and `ResumeWorkflowTest.kt` exercise Android-dependent authentication and resume behavior.

Connect a dedicated test device/emulator, then run from the repository root: `.\gradlew.bat :app:connectedDebugAndroidTest`.

Tests can change application state. Record device/API details with results, and distinguish instrumented checks from authenticated live acceptance. See the root stabilization guide for the remaining backend, email, and account-isolation checks.
