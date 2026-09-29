# Maintenance scripts

Project creator and developer: **PolarDredd**.

`check-backend.cjs` is a read-only Supabase probe. It reads the public backend configuration from Android's `AuthManager.kt`, requests Auth settings and published jobs, and prints summarized results.

Run from the repository root with a Node.js version that supports built-in `fetch`:

```powershell
node scripts/check-backend.cjs
```

Inspect the printed HTTP statuses and counts; a successful process exit alone does not establish backend health. A zero-row result can reflect missing published jobs or row-level policies. The probe does not validate authenticated writes, SMTP delivery, or private resume access.

Keep its parsing aligned if Android configuration changes, and preserve its restriction against printing tokens, full profiles, or raw responses.
