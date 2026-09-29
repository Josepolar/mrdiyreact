# JVM regression tests

Project creator and developer: **PolarDredd**.

Tests live in `java/com/mrdiy/careers/`. Existing suites cover account/resume rules, geographic scope, published-job pagination and filtering, resume skills, and search ranking.

Run from the repository root: `.\gradlew.bat :app:testDebugUnitTest`.

Add behavior-focused cases to the relevant suite when changing these rules. Test reports are generated under `app/build/reports/tests/`. Unit test success does not establish live Supabase policy behavior or email delivery.
