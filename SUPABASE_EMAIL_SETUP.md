# Registration email delivery

The Android client uses Supabase Auth for registration. Creating a row in `auth.users` does not guarantee delivery unless the Supabase Auth email provider is configured.

In the Supabase project used by the app:

1. Open **Authentication → Providers → Email** and keep email confirmation enabled.
2. Open **Project Settings → Auth → SMTP Settings** and configure a verified SMTP sender (host, port, username, password, and sender email). The built-in email service is rate-limited and is not intended for production delivery.
3. In **Authentication → URL Configuration**, add this redirect URL exactly:
   `mrdiy://login-callback`
4. Send one registration to a mailbox you control. Inspect Auth logs and the SMTP provider's delivery events, including rejected, suppressed and bounced messages. User creation alone does not establish whether a message was sent or delivered.

The app makes one signup request, which triggers Supabase Auth confirmation. It does **not** automatically resend after signup. The verification screen provides a manual Resend action with a cooldown. Only generic messages appear in the client; no SMTP credentials belong in the APK.

Live check on 2026-09-23: the public Auth settings report email enabled, confirmation required, and signup enabled. This endpoint does not reveal SMTP configuration or delivery results. No mailbox receipt was verified.

The default Supabase email service restricts recipients to organization team members and has very low rate limits. Configure custom SMTP for other recipients. See [Supabase SMTP documentation](https://supabase.com/docs/guides/auth/auth-smtp). Do not disable email confirmation to hide delivery failures.

Verify afterward: register a unique mailbox alias once; confirm exactly one provider event and actual receipt; open the link with the app installed; verify login and name-prefilled onboarding. Also test an expired link and a deliberate manual resend. A web confirmation page must exist if used as the SMTP template's redirect; an APK update cannot repair a missing Vercel page or SMTP/DNS configuration.
