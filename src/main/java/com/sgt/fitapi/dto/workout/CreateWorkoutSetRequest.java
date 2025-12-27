package com.sgt.fitapi.dto.workout;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public class CreateWorkoutSetRequest {

    @NotNull
    @Schema(description = "Exercise ID to associate with the set.", example = "15")
    public Long exerciseId;

    @NotNull
    @Positive
    @Schema(description = "Set number within the session.", example = "1")
    public Integer setNumber;

    @NotNull
    @Positive
    @Schema(description = "Number of repetitions completed.", example = "8")
    public Integer reps;

    @NotNull
    @Positive
    @Schema(description = "Weight used for the set in kilograms.", example = "100.0")
    public Double weight;

    @Min(1)
    @Max(10)
    @Schema(description = "Rate of perceived exertion (1-10).", example = "8.5")
    public Double rpe;
    @Min(0)
    @Schema(description = "Rest time after the set, in seconds.", example = "120")
    public Integer restSeconds;

    @Size(max = 500)
    @Schema(description = "Optional notes about the set.", example = "Felt strong, last rep grinder")
    public String notes;
}
