# Data and business logic

Project creator and developer: **PolarDredd**. See [project credits](../../../../../../../../CREDITS.md).

| Folder or file | Purpose | Next developer's maintenance note |
| --- | --- | --- |
| `auth/` | Supabase client, authentication, session/account rules, request gate. | `AuthManager.kt` defines backend configuration. Preserve session-derived identity and account isolation. |
| `repository/` | Profiles, published jobs, saved jobs, applications, inbox, resume parsing and file rules. | Keep Supabase column mapping and account-scoped caches aligned. `JobRepository` contains legacy sample data and `PhpJobRepository` is the legacy API path; applicant lists use `PublishedJobsRepository`. |
| `ml/` | Local job matching and resume skill extraction. | `HybridMatchingService` currently delegates to local matching even when its optional AI flag is supplied. Scores express similarity, not hiring probability. |
| `search/` | Search ranking/filtering and geographic scope. | Keep geographic rules centralized in `MetroManilaScope`; run the search and geography tests after changes. |
| `SafeDiagnostics.kt` | Restricted diagnostic logging. | Preserve sanitized messages rather than logging tokens, resumes, or raw server responses. |

Resume processing spans `ResumeFiles`, `ResumeRepository`, `ProfileRepository`, and `ui/resume/ResumeUploadViewModel.kt`. Text extraction and local preservation must remain useful when cloud persistence fails. Review cancellation and uncertain database commits before changing uploaded-object cleanup.

Backend policy requirements and previous verification limits are documented in the root integration and stabilization guides.
