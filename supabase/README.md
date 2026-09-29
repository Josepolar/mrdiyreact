# Supabase database maintenance

Project creator and developer: **PolarDredd**.

| Path | Responsibility |
| --- | --- |
| `migrations/` | Versioned SQL changes; the existing private-resumes migration configures private storage and owner policies. |
| `verify-access.sql` | Queries for inspecting schema, constraints, and policies. |
| Root `supabase_schema_migration.sql` and `supabase_rls_policies.sql` | Earlier schema/policy setup; inspect against the live database before reuse. |
| Root `supabase_published_jobs.sql` | Published-job read policy used by the app/admin integration. |

Read [stabilization notes](../STABILIZATION.md) and [admin integration](../ADMIN_APP_INTEGRATION.md) before applying changes. Inspect the actual target project and existing policies first; repository SQL is not evidence that a migration has run remotely. Review older setup scripts carefully because newer private-storage requirements may supersede their behavior.

Add dated migrations with comments explaining the affected table/bucket, reason, expected behavior, and verification. After storage-policy changes, test owner access, another authenticated account, and anonymous access. Keep privileged credentials out of the Android app.
