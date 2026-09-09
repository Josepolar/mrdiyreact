-- ==========================================================================
-- RLS POLICIES for MrDIY Careers Supabase Project
-- 
-- user_id is stored as TEXT (not UUID), so auth.uid() must be cast to ::text
--
-- Run in Supabase SQL Editor: your project → SQL Editor → paste → RUN
-- ==========================================================================

-- ──────────────────────────────────────────────────────────────────────────
-- 1. ENABLE RLS ON PROFILES TABLE
-- ──────────────────────────────────────────────────────────────────────────
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;

-- ──────────────────────────────────────────────────────────────────────────
-- 2. profiles TABLE POLICIES (user_id is TEXT, auth.uid() cast to ::text)
-- ──────────────────────────────────────────────────────────────────────────

DROP POLICY IF EXISTS "Users can insert own profile" ON profiles;
CREATE POLICY "Users can insert own profile"
    ON profiles
    FOR INSERT
    WITH CHECK (auth.uid()::text = user_id);

DROP POLICY IF EXISTS "Users can view own profile" ON profiles;
CREATE POLICY "Users can view own profile"
    ON profiles
    FOR SELECT
    USING (auth.uid()::text = user_id);

DROP POLICY IF EXISTS "Users can update own profile" ON profiles;
CREATE POLICY "Users can update own profile"
    ON profiles
    FOR UPDATE
    USING (auth.uid()::text = user_id)
    WITH CHECK (auth.uid()::text = user_id);

DROP POLICY IF EXISTS "Users can delete own profile" ON profiles;
CREATE POLICY "Users can delete own profile"
    ON profiles
    FOR DELETE
    USING (auth.uid()::text = user_id);


-- ──────────────────────────────────────────────────────────────────────────
-- 3. STORAGE BUCKET POLICIES (resumes bucket)
-- ──────────────────────────────────────────────────────────────────────────

DROP POLICY IF EXISTS "Users can upload own resume" ON storage.objects;
CREATE POLICY "Users can upload own resume"
    ON storage.objects
    FOR INSERT
    WITH CHECK (
        bucket_id = 'resumes'
        AND auth.role() = 'authenticated'
        AND (storage.foldername(name))[1] = auth.uid()::text
    );

DROP POLICY IF EXISTS "Users can view own resume" ON storage.objects;
CREATE POLICY "Users can view own resume"
    ON storage.objects
    FOR SELECT
    USING (
        bucket_id = 'resumes'
        AND auth.role() = 'authenticated'
        AND (storage.foldername(name))[1] = auth.uid()::text
    );

DROP POLICY IF EXISTS "Users can update own resume" ON storage.objects;
CREATE POLICY "Users can update own resume"
    ON storage.objects
    FOR UPDATE
    USING (
        bucket_id = 'resumes'
        AND auth.role() = 'authenticated'
        AND (storage.foldername(name))[1] = auth.uid()::text
    );

DROP POLICY IF EXISTS "Users can delete own resume" ON storage.objects;
CREATE POLICY "Users can delete own resume"
    ON storage.objects
    FOR DELETE
    USING (
        bucket_id = 'resumes'
        AND auth.role() = 'authenticated'
        AND (storage.foldername(name))[1] = auth.uid()::text
    );


-- ──────────────────────────────────────────────────────────────────────────
-- 4. CREATE THE RESUMES BUCKET (if not already exists)
-- ──────────────────────────────────────────────────────────────────────────

INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
    'resumes',
    'resumes',
    true,
    10485760,
    '{application/pdf,text/plain}'
)
ON CONFLICT (id) DO NOTHING;


-- ──────────────────────────────────────────────────────────────────────────
-- 5. VERIFY SETUP
-- ──────────────────────────────────────────────────────────────────────────

SELECT tablename, rowsecurity FROM pg_tables
WHERE tablename = 'profiles' AND schemaname = 'public';

SELECT policyname, cmd FROM pg_policies
WHERE tablename = 'profiles' AND schemaname = 'public';

SELECT policyname, cmd FROM pg_policies
WHERE tablename = 'objects' AND schemaname = 'storage';

SELECT id, name, public FROM storage.buckets WHERE id = 'resumes';