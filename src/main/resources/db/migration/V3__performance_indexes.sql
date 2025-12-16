-- Indexes to speed up common user/time queries and set lookups
CREATE INDEX IF NOT EXISTS idx_workout_sessions_user_started
    ON workout_sessions (user_id, started_at DESC);

CREATE INDEX IF NOT EXISTS idx_workout_sets_session_setnum
    ON workout_sets (workout_session_id, set_number);
