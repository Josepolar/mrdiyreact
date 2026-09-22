-- Read-only diagnostics. Run as a project administrator. Contains no applicant PII.
select status, count(*) from public.jobs group by status;
select schemaname, tablename, policyname, roles, cmd, qual, with_check
from pg_policies where
    (schemaname = 'storage' and tablename = 'objects') or
    (schemaname = 'public' and tablename in ('jobs', 'profiles', 'saved_jobs', 'applications'));
select id, public, file_size_limit, allowed_mime_types from storage.buckets
where id in ('resumes', 'private-resumes');
select tablename, rowsecurity from pg_tables where schemaname = 'public'
and tablename in ('jobs', 'profiles', 'saved_jobs', 'applications');
select tablename, indexname, indexdef from pg_indexes where schemaname = 'public'
and tablename in ('profiles', 'saved_jobs', 'applications');
