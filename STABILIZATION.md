# Stabilization evidence — 2026-09-23

This is a candidate, **not a certified production release**. Remote Supabase policies, SMTP delivery, admin publishing, and the complete authenticated journey have not been verified. No production deployment or website APK replacement was made during this pass.

## Architecture and baseline

This is a native Kotlin Android application, despite the directory name. MainActivity hosts a Navigation Component graph; XML/ViewBinding fragments use LiveData/ViewModels. Supabase Auth, PostgREST and Storage share one client. Profiles and saved IDs have user-specific preference caches. Jobs come from public.jobs, status=published, with pagination. Recommendations use the existing local TF-IDF/cosine algorithm. The admin clone reads its Supabase URL and publishable key from deployment environment variables; its deployed project identity is unavailable here.

Baseline on the connected emulator: installed APK was cleared with `pm clear`; launch still showed Demo User and the dashboard. Current source also retained demo login paths and trusted preference flags. Baseline screenshot: `.integration/baseline-login.png`. After rebuilding and clearing data, Login appeared (`.integration/stabilized-login.png`). Test fixtures are confined to test source sets.

## Issue reports

### 1. Resume upload / credential exposure
Root Cause: app could rely on a preference/demo identity instead of a restored authenticated session; upload errors were logged verbatim; the legacy SQL created a public bucket and the app persisted public URLs. The exact live rejecting RLS policy cannot be inspected with the public key.
Fix: session-derived ownership, bounded 10 MB read before parsing, PDF signature/type checks, private user paths, temporary signed URLs, no upsert collisions, cancellable upload, safe messages and diagnostics, partial profile update. A failed/uncertain DB commit preserves its object to avoid deleting a possibly referenced resume. Replacement keeps previous objects; administrative orphan retention cleanup is still a deployment concern.
Test Performed: unit checks for PDF/type mismatch, oversized unknown-length streams, and path ownership/traversal; emulator PDF extraction test added. Live upload/retry/cancellation/cross-account access requires authenticated test accounts and the migration below.
Result: BLOCKED for live acceptance. Do not call storage fixed until the migration and two-user checks pass.

### 2. Terms bypass
Root Cause: registration logic had no terms argument/guard; a screen-only checkbox was bypassable by calling the auth method.
Fix: validate consent before any signup call; shared request gate prevents overlapping requests across fragment instances; registration metadata records acceptance. The Google registration button also checks consent.
Test Performed: direct auth method with unchecked terms, emulator unchecked submission and activity recreation, valid/invalid rule tests. No email is sent by these tests.
Result: PASS for the tested app registration guards, including direct method invocation and recreation. Supabase Auth remains a public API; server-enforced legal consent for arbitrary non-app clients would require a verified Auth hook, outside this client guard.

### 3. Authentication bypass
Root Cause: demo navigation and preference-based login/session identity; SDK initialization was not awaited.
Fix: remove demo paths and BuildConfig switch; await SDK initialization; validate/refresh sessions; route unauthenticated protected navigation to Login; observe signout and clear the back stack; disable Android credential backup.
Test Performed: clear app data and launch, stale preference flag, activity recreation, session expiry rules. Live valid/expired-refresh and cross-account restoration require test credentials.
Result: local logged-out checks are recorded below; authenticated end-to-end acceptance BLOCKED.

### 4 / 7. Name greeting and onboarding
Root Cause: global name preference and stale demo state; signup ignored fullName; profile setup consulted a different preference key.
Fix: signup metadata provides initial name; saved profile is the source after setup; greeting reloads the current profile and shows Hello, first name; cache/results are checked against the active user. Onboarding pre-fills the registered name and preserves existing profile fields.
Test Performed: greeting/fallback rules; code trace registration metadata → profile prefill → profile repository → greeting. Live registration/profile update/relogin acceptance BLOCKED on test account.
Result: BLOCKED for full workflow.

### 5. Admin jobs missing
Root Cause: not fully established remotely. Read-only live query returned HTTP 200 and **zero published rows before geographic filtering**. That cannot distinguish empty published data from RLS filtering. No deployed admin environment access is available.
Fix: keep shared published-job schema and pagination; accept optional metadata; centralize geographic scope at the repository; remove default salary ceiling; preserve query errors as errors. No fake jobs inserted.
Test Performed: 200+ rows/pagination, numeric/string IDs, nullable fields, drafts excluded, empty/error/cancellation results, salary parsing, search and ranking. Live read-only probe is `node scripts/check-backend.cjs`.
Result: BLOCKED. Confirm admin NEXT_PUBLIC_SUPABASE_URL refers to sfjpiyevasnmvddgtofz, inspect policies and publish a real test role through admin, then verify app list/detail/search/save.

### 6. Geographic scope
Root Cause: scattered hidden regional controls, broad substring Quezon match, Metro Manila filter excluding Makati without that literal prefix, inconsistent repository consumers.
Fix: remove Cebu/Davao controls; central MetroManilaScope; explicit Quezon City; shared scope for list/detail/saved; repository accepts configurable location terms. Existing surrounding provinces remain Cavite, Laguna, Bulacan and Rizal; no additional provinces invented.
Test Performed: city/province boundaries, non-serving regions, configurable scope and repository filtering.
Result: PASS for automated logic; live filters BLOCKED on published jobs.

### 8. Experience dropdown
Root Cause: irregular hardcoded options and 10+ parsed as zero.
Fix: numeric 0 through 10 inclusive, including 4 and 9; keep numeric persistence, handle legacy 10+ as 10.
Test Performed: numeric conversion tests; dropdown source inspection. Live onboarding persistence requires an account.
Result: BLOCKED for full live workflow; logic tests pass.

### 9 / 10. Form validation / phone highlight
Root Cause: validation timing lacked focus/touched tracking. Yellow fill on the inspected emulator was Android autofill highlighting, not a phone validation result.
Fix: validate on blur/submit; clear existing errors on correction; neutral autofill highlight in API-qualified theme; normal focus outline remains.
Test Performed: emulator initial edit → blur → corrected email; registration rules; lint verifies supported Android API use.
Result: PASS for tested email validation timing/correction. Phone highlight uses an API-qualified neutral autofill theme; live phone onboarding remains unverified.

### 11. Registration email
Root Cause: exact SMTP/provider failure is externally unverified. Prior code immediately resent after signup and hid resend failures, which did not establish delivery and could duplicate requests.
Fix: one Supabase signup request; manual cooldown-controlled resend; verification uses emailConfirmedAt; safe failure text; callback routes through session/profile restoration. No new competing mail service or client-side mail secret.
Test Performed: live public Auth settings returned signup enabled, email enabled, confirmation required. No provider delivery event or mailbox receipt was available.
Result: BLOCKED. Configure/verify SMTP sender and domain, inspect Auth/provider logs, allow mrdiy://login-callback, register one controlled mailbox alias, verify exactly one receipt and successful link/login. See SUPABASE_EMAIL_SETUP.md. An APK alone cannot repair SMTP or DNS.

### UI and regression
Root Cause: existing styles differed from the textual specification; approved image mockups are not present.
Fix: centralized brand colors; larger square panda, underline login fields, clipboard Jobs / heart Saved, yellow wave header, rectangular red-border filters, accented cards with real references and algorithm scores, square profile avatar/camera and real Applied/Saved/Top Match metrics. Unavailable matches show a dash.
Test Performed: builds/lint, login screenshot, emulator public flows. Authenticated screens and pixel comparison require a test account, published jobs and approved mockups.
Result: BLOCKED for visual acceptance and complete authenticated regression.

## External verification required before release

1. Connect project-owner Supabase access. Run `supabase/verify-access.sql` and inspect active schema/policies. Apply `supabase/migrations/202609230001_private_resumes.sql`. It makes both resume buckets private and restricts folder access to the owner, including against older broad permissive policies. It preserves stored objects. Existing admin access to resumes must be explicitly authorized using the real admin-role model; do not restore public URLs or embed a service key.
2. Authenticate users A and B. Upload a PDF as A to A's folder; verify update/retry/cancel/oversize/type failures; attempt A→B folder writes and B→A read/sign/delete (must fail). Confirm anonymous/public URLs fail and A can request a short-lived signed URL. Confirm profile RLS permits only its owner, including resume partial updates.
3. Verify unique constraints on profiles(user_id), saved_jobs(user_id,job_id), applications(user_id,job_id). Client request locks prevent same-process double taps but do not replace database constraints across devices. Do not delete duplicate production rows automatically.
4. Verify the deployed admin backend identity. Publish a uniquely titled Metro Manila role. Check All/Jobs, recommendations, search, location, details, saved jobs, apply and actual profile metrics. Inspect grants/RLS if live read still returns zero; `supabase_published_jobs.sql` is a narrowly scoped published-job read policy.
5. Verify SMTP and confirmation callback as above. Then complete the fresh-account journey, logout/relaunch/relogin, profile edit/restart, network loss, expired session, slow requests and cross-account isolation.
6. Supply approved mockups for authenticated-screen visual comparison. Configure the production signing key securely before distributing the unsigned release artifact. Do not publish this candidate as verified until the blocked checks pass.

## Local verification results

Reproduce with `./verify-core.ps1` (add `-Emulator` for connected tests). The backend probe is read-only and prints no keys, auth headers or applicant data. A successful probe is not an SMTP/RLS certification.

Final executed checks:

- Debug APK, Android test APK and minified unsigned release APK: **PASS**. Missing ProGuard configuration and the optional PDFBox JPX decoder shrinker reference were resolved. The app extracts PDF text; it does not render JPEG2000 images. [Library documentation](https://github.com/TomRoush/PdfBox-Android#optional-dependencies).
- Unit tests: **27 passed**, zero failures/errors. Includes registration rules, request gating, session expiry, names, experience values, file validation, geographic scope, published-job DTOs/pagination, search and real matching.
- Instrumentation: **6 passed**, starting after `pm clear com.mrdiy.careers` on a separate MRDIY_QA Android 17 / API 37.2 x86_64 emulator. Tests cover unchecked terms through UI/recreation and direct calls, stale login flags, validation blur/correction, rotation reachability, and actual PDF creation/extraction. See [test output](qa/results/2026-09-23-emulator.txt). Earlier failed runs are superseded: the original test dependency had incompatible input injection; a later run lost focus to another app. The isolated run completed without those failures. [AndroidX Test release notes](https://developer.android.com/jetpack/androidx/releases/test) document the input-injection fix used here.
- Manual: offline cold launch showed Login. A malformed confirmation link showed “Could not verify this link. Please sign in or request a new link.” and left Login usable; no application fatal exception was logged. `.integration/qa-offline-login.png`, `.integration/qa-invalid-link.png`.
- Rotation reproduced clipped login controls. A scroll container fixes reachability; the new rotation test passes. `.integration/qa-rotation.png` records the pre-fix clipping.
- Auth-link import now awaits parsing, user verification and session import in the activity coroutine, instead of racing an SDK helper's separate coroutine. Supabase SDK logging is disabled because its debug logging includes token-bearing fragments. Actual emailed successful callback remains BLOCKED.
- Android lint: **0 errors, 525 warnings**. Warnings remain; no baseline or blanket suppression was used to hide errors. Deprecated Gradle APIs also remain.
- `git diff --check` for edited sources/config/scripts: **PASS**.
- Live public backend probe: Auth HTTP 200, signup enabled, confirmation required; published jobs HTTP 200, zero rows. **Not a pass** for job sync or delivery.

Build and checksum summary: [qa/results/2026-09-23-summary.txt](qa/results/2026-09-23-summary.txt). The debug APK is `app/build/outputs/apk/debug/app-debug.apk`; the unsigned release is `app/build/outputs/apk/release/app-release-unsigned.apk`. Version 1.1-rc1, code 2. Neither replaces the website download. The hidden QA emulator was stopped after testing; the visible emulator was preserved.

Acceptance boundary: local tests pass, but the full fresh-user registration → received email → profile → secure resume upload → admin job → saved/applied → logout/login journey is **BLOCKED**, as are successful-session restart/refresh, live query/storage/provider failures, cross-account RLS tests and authenticated visual review. These were not marked passed based on compilation or unit tests.

Security audit: current application source has no demo login, publicUrl resume calls, raw exception logging, embedded privileged API BuildConfig field, or service-role key found. Public Supabase anon configuration remains intentional. Existing tracked build/cache artifacts and local.properties remain in repository history; .gitignore prevents new untracked outputs. No secret rotation or remote policy change is claimed.
