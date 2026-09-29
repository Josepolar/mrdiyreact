# Gradle tooling

Project creator and developer: **PolarDredd**. Gradle itself and its wrapper remain third-party tooling.

`wrapper/` contains the wrapper JAR and distribution properties used by the root `gradlew.bat`. `gradle-daemon-jvm.properties` contains generated daemon JVM criteria and download locations.

Coordinate Gradle, Android Gradle Plugin, Kotlin, and JVM changes with the root and app build files. Use Gradle tooling to regenerate wrapper/daemon files, then verify a build and tests. Machine caches belong in `.gradle/`, not here.
