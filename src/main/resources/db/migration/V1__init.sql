-- Combined initial schema with constraints, indexes, and seed data

-- Users table (authentication & ownership)
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
    user_id BIGINT NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ended_at TIMESTAMP WITH TIME ZONE,
    timezone VARCHAR(64) NOT NULL,
    notes VARCHAR(500),
    CONSTRAINT fk_workout_sessions_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
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
        ON DELETE RESTRICT,

    CONSTRAINT chk_workout_sets_reps_positive CHECK (reps > 0),
    CONSTRAINT chk_workout_sets_weight_positive CHECK (weight > 0),
    CONSTRAINT chk_workout_sets_rpe_range CHECK (rpe IS NULL OR (rpe >= 1 AND rpe <= 10)),
    CONSTRAINT chk_workout_sets_rest_non_negative CHECK (rest_seconds IS NULL OR rest_seconds >= 0)
);

-- Helpful indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);
CREATE INDEX IF NOT EXISTS idx_workout_sets_session ON workout_sets (workout_session_id);
CREATE INDEX IF NOT EXISTS idx_workout_sets_exercise ON workout_sets (exercise_id);
CREATE INDEX IF NOT EXISTS idx_workout_sets_session_exercise
    ON workout_sets (workout_session_id, exercise_id);
CREATE INDEX IF NOT EXISTS idx_workout_sessions_user_started
    ON workout_sessions (user_id, started_at DESC);
CREATE INDEX IF NOT EXISTS idx_workout_sets_session_setnum
    ON workout_sets (workout_session_id, set_number);

-- Seed data for common exercises
INSERT INTO exercises (name, muscle_group, equipment, is_isometric, movement_type)
VALUES
    ('Barbell Bench Press', 'Chest', 'Barbell', FALSE, 'COMPOUND'),
    ('Incline Dumbbell Press', 'Chest', 'Dumbbells', FALSE, 'COMPOUND'),
    ('Push-Up', 'Chest', 'Bodyweight', FALSE, 'COMPOUND'),

    ('Back Squat', 'Quads', 'Barbell', FALSE, 'COMPOUND'),
    ('Front Squat', 'Quads', 'Barbell', FALSE, 'COMPOUND'),
    ('Leg Press', 'Quads', 'Machine', FALSE, 'COMPOUND'),

    ('Conventional Deadlift', 'Posterior Chain', 'Barbell', FALSE, 'COMPOUND'),
    ('Romanian Deadlift', 'Hamstrings', 'Barbell', FALSE, 'COMPOUND'),
    ('Hip Thrust', 'Glutes', 'Barbell', FALSE, 'COMPOUND'),

    ('Overhead Press', 'Shoulders', 'Barbell', FALSE, 'COMPOUND'),
    ('Lateral Raise', 'Shoulders', 'Dumbbells', FALSE, 'ISOLATION'),

    ('Pull-Up', 'Back', 'Bodyweight', FALSE, 'COMPOUND'),
    ('Barbell Row', 'Back', 'Barbell', FALSE, 'COMPOUND'),
    ('Lat Pulldown', 'Back', 'Machine', FALSE, 'COMPOUND'),

    ('Barbell Curl', 'Biceps', 'Barbell', FALSE, 'ISOLATION'),
    ('Dumbbell Curl', 'Biceps', 'Dumbbells', FALSE, 'ISOLATION'),
    ('Triceps Pushdown', 'Triceps', 'Cable', FALSE, 'ISOLATION'),
    ('Skullcrusher', 'Triceps', 'Barbell', FALSE, 'ISOLATION'),

    ('Plank', 'Core', 'Bodyweight', TRUE, 'ISOMETRIC'),
    ('Hanging Leg Raise', 'Core', 'Bodyweight', FALSE, 'ISOLATION'),

    ('Bench Press', 'Chest', 'barbell', FALSE, 'Push'),
    ('Squat', 'Legs', 'barbell', FALSE, 'Push'),
    ('Deadlift', 'Back', 'barbell', FALSE, 'Pull'),
    ('Overhead Press', 'Shoulders', 'barbell', FALSE, 'Push'),
    ('Plank (Isometric)', 'Core', 'bodyweight', TRUE, 'Other')
ON CONFLICT (name) DO NOTHING;
