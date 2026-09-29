# Screens and presentation

Project creator and developer: **PolarDredd**.

Each feature folder groups its fragments and, where present, ViewModels and adapters. XML counterparts are in `app/src/main/res/layout/`; routes and arguments are in `res/navigation/nav_graph.xml`.

| Folder | Responsibility | Maintenance note |
| --- | --- | --- |
| `alerts/` | Alert list and adapter. | Review inbox data mapping and empty/error states. |
| `applications/` | Applicant submissions and statuses. | Keep backend job IDs and account ownership intact. |
| `auth/` | Login, registration, verification, phone entry, onboarding, validation. | Preserve consent checks, verification flow, and session-aware routing. |
| `base/` | Shared fragment behavior. | Review affected subclasses when changing shared behavior. |
| `home/` | Dashboard, greeting, and job cards. | Reload profile and job data for the active account. |
| `jobs/` | Search/filter list and job detail. | Keep detail IDs and published status consistent with repository results. |
| `messages/` | Inbox list and message adapter. | Handle unavailable backend tables and empty states without inventing messages. |
| `profile/` | Profile display/editing, experience editor, resume card. | Preserve existing fields when saving partial updates. |
| `recommendations/` | Ranked jobs and matching cards. | Recompute from available profile/resume data; empty data must remain understandable. |
| `resume/` | File selection, parsing, upload states, local fallback. | Preserve extracted text when cloud sync fails; review cancellation before deleting an uploaded file. |
| `savedjobs/` | Saved list and save/remove state. | Keep caches scoped by user and resolve against published jobs. |
| `settings/` | Settings and sign-out interactions. | Verify navigation and local account state after sign-out. |

After changing a screen, check loading, empty, success, and failure states as applicable. Observe state with the correct lifecycle and release view references when the fragment view is destroyed. Document any new feature folder here.
