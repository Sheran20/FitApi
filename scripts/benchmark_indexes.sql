-- ============================================================
-- INDEX PERFORMANCE BENCHMARK SCRIPT
-- ============================================================
-- This script measures the performance difference between
-- queries with and without indexes on your fitapi database.
--
-- USAGE:
--   psql -d fitapi -f benchmark_indexes.sql
--   OR run sections manually in your PostgreSQL client
--
-- WARNING: This script will temporarily DROP indexes!
--          Run on a dev/test database, not production.
-- ============================================================

\echo '============================================================'
\echo 'INDEX PERFORMANCE BENCHMARK'
\echo '============================================================'

-- Enable timing
\timing on

-- ============================================================
-- STEP 1: Generate test data (skip if you have enough data)
-- ============================================================
\echo ''
\echo '>>> Step 1: Checking current data volume...'

SELECT 'users' as table_name, COUNT(*) as row_count FROM users
UNION ALL
SELECT 'workout_sessions', COUNT(*) FROM workout_sessions
UNION ALL
SELECT 'workout_sets', COUNT(*) FROM workout_sets
UNION ALL
SELECT 'exercises', COUNT(*) FROM exercises;

-- Data generation enabled
-- Adjust the numbers based on how much data you want

\echo '>>> Generating test users...'
INSERT INTO users (email, password, display_name, role)
SELECT
    'testuser' || i || '@example.com',
    '$2a$10$dummyhashedpassword1234567890',
    'Test User ' || i,
    'ROLE_USER'
FROM generate_series(1, 1000) AS i
ON CONFLICT (email) DO NOTHING;

\echo '>>> Generating workout sessions...'
INSERT INTO workout_sessions (user_id, started_at, ended_at, timezone, notes)
SELECT
    (SELECT id FROM users ORDER BY RANDOM() LIMIT 1),
    NOW() - (random() * interval '365 days'),
    NOW() - (random() * interval '365 days') + interval '1 hour',
    'UTC',
    'Benchmark session ' || i
FROM generate_series(1, 50000) AS i;

\echo '>>> Generating workout sets...'
INSERT INTO workout_sets (workout_session_id, exercise_id, set_number, reps, weight, rpe)
SELECT
    ws.id,
    (SELECT id FROM exercises ORDER BY RANDOM() LIMIT 1),
    (random() * 5 + 1)::int,
    (random() * 12 + 1)::int,
    (random() * 100 + 20)::double precision,
    (random() * 4 + 6)::double precision
FROM workout_sessions ws
CROSS JOIN generate_series(1, 5) AS set_num
WHERE ws.id IN (SELECT id FROM workout_sessions ORDER BY RANDOM() LIMIT 10000);

-- ============================================================
-- STEP 2: Verify indexes exist
-- ============================================================
\echo ''
\echo '>>> Step 2: Current indexes on relevant tables...'

SELECT
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename IN ('users', 'workout_sessions', 'workout_sets', 'exercises')
ORDER BY tablename, indexname;

-- ============================================================
-- STEP 3: Benchmark WITH indexes (current state)
-- ============================================================
\echo ''
\echo '============================================================'
\echo '>>> Step 3: BENCHMARKING WITH INDEXES'
\echo '============================================================'

-- Clear caches for fair comparison (requires superuser)
-- DISCARD ALL;

\echo ''
\echo '--- Query 1: User lookup by email ---'
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT * FROM users WHERE email = 'testuser500@example.com';

\echo ''
\echo '--- Query 2: Workout sessions for user (sorted by date) ---'
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT * FROM workout_sessions
WHERE user_id = 1
ORDER BY started_at DESC
LIMIT 20;

\echo ''
\echo '--- Query 3: Workout sessions in date range ---'
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT * FROM workout_sessions
WHERE user_id = 1
  AND started_at BETWEEN NOW() - interval '30 days' AND NOW()
ORDER BY started_at DESC;

\echo ''
\echo '--- Query 4: All sets for a workout session ---'
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT * FROM workout_sets
WHERE workout_session_id = 1;

\echo ''
\echo '--- Query 5: Sets for session + specific exercise ---'
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT * FROM workout_sets
WHERE workout_session_id = 1 AND exercise_id = 1;

\echo ''
\echo '--- Query 6: Aggregate - total volume per session ---'
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT
    workout_session_id,
    SUM(reps * weight) as total_volume,
    COUNT(*) as set_count
FROM workout_sets
WHERE workout_session_id = 1
GROUP BY workout_session_id;

-- ============================================================
-- STEP 4: Store index definitions for recreation
-- ============================================================
\echo ''
\echo '>>> Step 4: Saving index definitions before dropping...'

-- Save current indexes to temp table
CREATE TEMP TABLE saved_indexes AS
SELECT indexname, indexdef
FROM pg_indexes
WHERE indexname IN (
    -- Note: idx_users_email removed - redundant with UNIQUE constraint index
    'idx_workout_sets_session',
    'idx_workout_sets_exercise',
    'idx_workout_sets_session_exercise',
    'idx_workout_sessions_user_started',
    'idx_workout_sets_session_setnum'
);

SELECT * FROM saved_indexes;

-- ============================================================
-- STEP 5: DROP indexes for comparison
-- ============================================================
\echo ''
\echo '============================================================'
\echo '>>> Step 5: DROPPING INDEXES FOR COMPARISON'
\echo '============================================================'

-- Note: idx_users_email not dropped - it's redundant (UNIQUE constraint provides index)
DROP INDEX IF EXISTS idx_workout_sets_session;
DROP INDEX IF EXISTS idx_workout_sets_exercise;
DROP INDEX IF EXISTS idx_workout_sets_session_exercise;
DROP INDEX IF EXISTS idx_workout_sessions_user_started;
DROP INDEX IF EXISTS idx_workout_sets_session_setnum;

\echo 'Indexes dropped. Verifying...'
SELECT indexname FROM pg_indexes
WHERE tablename IN ('users', 'workout_sessions', 'workout_sets')
  AND indexname LIKE 'idx_%';

-- ============================================================
-- STEP 6: Benchmark WITHOUT indexes
-- ============================================================
\echo ''
\echo '============================================================'
\echo '>>> Step 6: BENCHMARKING WITHOUT INDEXES (Sequential Scans)'
\echo '============================================================'

\echo ''
\echo '--- Query 1: User lookup by email (NO INDEX) ---'
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT * FROM users WHERE email = 'testuser500@example.com';

\echo ''
\echo '--- Query 2: Workout sessions for user (NO INDEX) ---'
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT * FROM workout_sessions
WHERE user_id = 1
ORDER BY started_at DESC
LIMIT 20;

\echo ''
\echo '--- Query 3: Workout sessions in date range (NO INDEX) ---'
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT * FROM workout_sessions
WHERE user_id = 1
  AND started_at BETWEEN NOW() - interval '30 days' AND NOW()
ORDER BY started_at DESC;

\echo ''
\echo '--- Query 4: All sets for a workout session (NO INDEX) ---'
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT * FROM workout_sets
WHERE workout_session_id = 1;

\echo ''
\echo '--- Query 5: Sets for session + specific exercise (NO INDEX) ---'
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT * FROM workout_sets
WHERE workout_session_id = 1 AND exercise_id = 1;

\echo ''
\echo '--- Query 6: Aggregate - total volume (NO INDEX) ---'
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT
    workout_session_id,
    SUM(reps * weight) as total_volume,
    COUNT(*) as set_count
FROM workout_sets
WHERE workout_session_id = 1
GROUP BY workout_session_id;

-- ============================================================
-- STEP 7: Recreate indexes
-- ============================================================
\echo ''
\echo '============================================================'
\echo '>>> Step 7: RECREATING INDEXES'
\echo '============================================================'

-- Note: idx_users_email not recreated - redundant with UNIQUE constraint index (users_email_key)
CREATE INDEX IF NOT EXISTS idx_workout_sets_session ON workout_sets (workout_session_id);
CREATE INDEX IF NOT EXISTS idx_workout_sets_exercise ON workout_sets (exercise_id);
CREATE INDEX IF NOT EXISTS idx_workout_sets_session_exercise
    ON workout_sets (workout_session_id, exercise_id);
CREATE INDEX IF NOT EXISTS idx_workout_sessions_user_started
    ON workout_sessions (user_id, started_at DESC);
CREATE INDEX IF NOT EXISTS idx_workout_sets_session_setnum
    ON workout_sets (workout_session_id, set_number);

\echo 'Indexes recreated. Verifying...'
SELECT indexname FROM pg_indexes
WHERE tablename IN ('users', 'workout_sessions', 'workout_sets')
  AND indexname LIKE 'idx_%';

-- Cleanup
DROP TABLE IF EXISTS saved_indexes;

\echo ''
\echo '============================================================'
\echo 'BENCHMARK COMPLETE'
\echo '============================================================'
\echo 'Compare the "Execution Time" values from WITH vs WITHOUT index sections.'
\echo 'Look for "Index Scan" vs "Seq Scan" in the query plans.'
\echo '============================================================'

\timing off
