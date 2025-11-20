MERGE INTO exercises (name, muscle_group, equipment, is_isometric, movement_type)
    KEY(name) VALUES ('Bench Press', 'Chest', 'barbell', false, 'Push');

MERGE INTO exercises (name, muscle_group, equipment, is_isometric, movement_type)
    KEY(name) VALUES ('Squat', 'Legs', 'barbell', false, 'Push');

MERGE INTO exercises (name, muscle_group, equipment, is_isometric, movement_type)
    KEY(name) VALUES ('Deadlift', 'Back', 'barbell', false, 'Pull');

MERGE INTO exercises (name, muscle_group, equipment, is_isometric, movement_type)
    KEY(name) VALUES ('Overhead Press', 'Shoulders', 'barbell', false, 'Push');

MERGE INTO exercises (name, muscle_group, equipment, is_isometric, movement_type)
    KEY(name) VALUES ('Plank', 'Core', 'bodyweight', true, 'Other');
