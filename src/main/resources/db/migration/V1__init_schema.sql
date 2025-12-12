-- Users table (for authentication & ownership)
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(128) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    display_name VARCHAR(64) NOT NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'ROLE_USER'
);

-- Exercises master table
CREATE TABLE IF NOT EXISTS exercises (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    muscle_group VARCHAR(50),
    equipment VARCHAR(50),
    is_isometric BOOLEAN NOT NULL DEFAULT FALSE,
    movement_type VARCHAR(50) NOT NULL,
    CONSTRAINT uk_exercise_name UNIQUE (name)
);

-- Workout sessions (one per workout)
CREATE TABLE IF NOT EXISTS workout_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    started_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    ended_at TIMESTAMP WITHOUT TIME ZONE,
    timezone VARCHAR(64) NOT NULL,
    notes VARCHAR(500)
);

-- Individual sets within a workout session
CREATE TABLE IF NOT EXISTS workout_sets (
    id BIGSERIAL PRIMARY KEY,
    workout_session_id BIGINT NOT NULL,
    exercise_id BIGINT NOT NULL,
    set_number INTEGER NOT NULL,
    reps INTEGER NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    rpe DOUBLE PRECISION,
    rest_seconds INTEGER,
    notes VARCHAR(500),

    CONSTRAINT fk_workout_set_session
        FOREIGN KEY (workout_session_id)
        REFERENCES workout_sessions (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_workout_set_exercise
        FOREIGN KEY (exercise_id)
        REFERENCES exercises (id)
        ON DELETE RESTRICT
);

-- Helpful indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);
CREATE INDEX IF NOT EXISTS idx_workout_sets_session ON workout_sets (workout_session_id);
CREATE INDEX IF NOT EXISTS idx_workout_sets_exercise ON workout_sets (exercise_id);
CREATE INDEX IF NOT EXISTS idx_workout_sets_session_exercise
    ON workout_sets (workout_session_id, exercise_id);
