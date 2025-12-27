package com.sgt.fitapi.dto.workout;

public class WorkoutSetView {
    @io.swagger.v3.oas.annotations.media.Schema(description = "Workout set ID.", example = "987")
    public Long id;
    @io.swagger.v3.oas.annotations.media.Schema(description = "Exercise ID for this set.", example = "15")
    public Long exerciseId;
    @io.swagger.v3.oas.annotations.media.Schema(description = "Exercise name for display.", example = "Barbell Squat")
    public String exerciseName;
    @io.swagger.v3.oas.annotations.media.Schema(description = "Set number within the session.", example = "1")
    public Integer setNumber;
    @io.swagger.v3.oas.annotations.media.Schema(description = "Repetitions completed.", example = "8")
    public Integer reps;
    @io.swagger.v3.oas.annotations.media.Schema(description = "Weight used in kilograms.", example = "100.0")
    public Double weight;
    @io.swagger.v3.oas.annotations.media.Schema(description = "Rate of perceived exertion (1-10).", example = "8.5")
    public Double rpe;
    @io.swagger.v3.oas.annotations.media.Schema(description = "Rest time after the set, in seconds.", example = "120")
    public Integer restSeconds;
    @io.swagger.v3.oas.annotations.media.Schema(description = "Optional notes about the set.", example = "Felt strong, last rep grinder")
    public String notes;
}
