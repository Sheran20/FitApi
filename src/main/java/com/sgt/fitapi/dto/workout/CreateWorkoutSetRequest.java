package com.sgt.fitapi.dto.workout;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CreateWorkoutSetRequest {

    @NotNull
    public Long exerciseId;

    @NotNull
    @Positive
    public Integer setNumber;

    @NotNull
    @Positive
    public Integer reps;

    @NotNull
    @Positive
    public Double weight;

    @Min(1)
    @Max(10)
    public Double rpe;
    @Min(0)
    public Integer restSeconds;

    @Size(max = 500)
    public String notes;
}
