-- Basic seed data for common exercises
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
    ('Hanging Leg Raise', 'Core', 'Bodyweight', FALSE, 'ISOLATION')
ON CONFLICT (name) DO NOTHING;
