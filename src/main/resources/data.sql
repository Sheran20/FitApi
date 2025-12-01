INSERT INTO exercises (name, muscle_group, equipment, is_isometric, movement_type)
VALUES ('Bench Press', 'Chest', 'barbell', false, 'Push')
    ON CONFLICT (name) DO NOTHING;

INSERT INTO exercises (name, muscle_group, equipment, is_isometric, movement_type)
VALUES ('Squat', 'Legs', 'barbell', false, 'Push')
    ON CONFLICT (name) DO NOTHING;

INSERT INTO exercises (name, muscle_group, equipment, is_isometric, movement_type)
VALUES ('Deadlift', 'Back', 'barbell', false, 'Pull')
    ON CONFLICT (name) DO NOTHING;

INSERT INTO exercises (name, muscle_group, equipment, is_isometric, movement_type)
VALUES ('Overhead Press', 'Shoulders', 'barbell', false, 'Push')
    ON CONFLICT (name) DO NOTHING;

INSERT INTO exercises (name, muscle_group, equipment, is_isometric, movement_type)
VALUES ('Plank', 'Core', 'bodyweight', true, 'Other')
    ON CONFLICT (name) DO NOTHING;
