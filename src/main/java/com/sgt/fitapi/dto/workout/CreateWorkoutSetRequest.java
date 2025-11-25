package com.sgt.fitapi.dto.workout;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateWorkoutSetRequest {

    @NotNull
    public Long exerciseId;

    @NotNull
    public Integer setNumber;

    @NotNull
    public Integer reps;

    @NotNull
    public Double weight;

    public Double rpe;
    public Integer restSeconds;

    @Size(max = 500)
    public String notes;
}
