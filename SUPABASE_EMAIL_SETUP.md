# Registration email delivery

The Android client uses Supabase Auth for registration. Creating a row in `auth.users` does not guarantee delivery unless the Supabase Auth email provider is configured.

In the Supabase project used by the app:

1. Open **Authentication → Providers → Email** and keep email confirmation enabled.
2. Open **Project Settings → Auth → SMTP Settings** and configure a verified SMTP sender (host, port, username, password, and sender email). The built-in email service is rate-limited and is not intended for production delivery.
3. In **Authentication → URL Configuration**, add this redirect URL exactly:
   `mrdiy://login-callback`
4. Send a test registration and check the Auth logs for SMTP response codes. A successful user insert with no SMTP log indicates the provider is not configured or the sender is not verified.

The app requests a verification-email resend after signup as a delivery fallback and also provides a manual **Resend** action on the verification screen. Resend rate-limit failures are handled without exposing provider details to the user.
