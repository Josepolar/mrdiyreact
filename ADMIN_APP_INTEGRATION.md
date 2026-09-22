# Admin and Android integration

The admin repository writes `public.jobs` in Supabase. Android now reads that
same table using its existing authenticated Supabase client. The old PHP API
and sample jobs are no longer used by applicant job screens.

## Shared project

Android currently uses `https://sfjpiyevasnmvddgtofz.supabase.co`, configured in
`app/src/main/java/com/mrdiy/careers/data/auth/AuthManager.kt`.
The admin's `NEXT_PUBLIC_SUPABASE_URL` must point to this same project (or change
the Android URL and public key together to match the admin project).
Never put a service-role key in Android.

The admin deployment's environment variables were not available in its Git
repository, so project identity has not been verified. A read-only public query
against the Android project's jobs endpoint returned HTTP 200 and an empty
published-job list. That does not prove the table is empty: RLS can hide rows.

## Job publishing

- Set the admin job status to exactly `published`.
- Home, Jobs, Recommendations, Saved Jobs and job details use those published
  rows. A successful empty result remains empty; sample jobs are not substituted.
- Lists refresh on opening and every 60 seconds while their ViewModels remain
  alive. This is polling, not a Realtime subscription.
- All pages are loaded, including catalogs with more than 200 jobs.
- The admin currently supplies title, description, location, salary, salary
  period, status and creation date. It does not supply category, employment type,
  experience level or separate requirements. Android does not invent these.
  Filters for unspecified categories/types will have no matches.
- If RLS blocks applicant reads, review and run `supabase_published_jobs.sql`
  in the shared project's SQL editor. The script has not been applied remotely.

## Recommendations

The existing TF-IDF/cosine algorithm ranks published jobs using skills, desired
position, headline, about text, resume text and available experience/education.
Cards display similarity scores and matching skills mentioned in job descriptions.
Scores are text similarity, not validated hiring probabilities.
An empty profile receives zero scores and a prompt to add profile information.

Resume upload now persists extracted text and merges parsed skills and experience
years with the applicant's profile, preserving their personal information.
Previously uploaded resumes need to be uploaded again if their extracted content
was never saved. Profile edits and resume uploads are used when recommendations
reload.

## Applications and saved jobs

Android now submits to the admin's `applications` table, with the original job ID,
authenticated user ID and `Pending` status. IDs are no longer hashed into a
different UUID. The table must generate its own primary key, accept these status
values and have user IDs compatible with Supabase Auth IDs. Applicant RLS must
permit inserting and reading only their own applications; admin policies must
permit review. A unique constraint on `(user_id, job_id)` is needed to prevent
duplicates across concurrent devices. Client-side duplicate checks alone cannot
guarantee this. Those existing table definitions and policies were not available
in the admin repository and have not been changed remotely.

Saved jobs resolve against the published catalog, retain original job IDs, and
cache saved IDs separately for each account. Existing `saved_jobs` columns and
RLS must allow the signed-in user to manage their own records.

## Validation

Build and unit tests:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest --offline
```

Complete the live acceptance check with an admin and an applicant account:

1. Publish a job in admin. Open Android Home and Jobs, and confirm its ID/details.
2. Edit its description/salary; reopen a list or wait up to 60 seconds.
3. Set the applicant's desired position/skills or upload a text-based resume.
   Confirm matching roles rank above unrelated jobs with visible scores.
4. Save the job; open Saved Jobs and then its details.
5. Apply and confirm the record appears in admin Applications with the same job ID.
6. Change its status in admin; reopen Android Applications to see the update.
7. Unpublish/delete the job; verify it disappears from published lists and its
   details no longer allow a new application.

Live publishing, authenticated reads and application submission have not yet been
verified. No admin records were created or changed during development.
