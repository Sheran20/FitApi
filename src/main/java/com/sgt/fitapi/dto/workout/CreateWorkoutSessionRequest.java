package com.sgt.fitapi.dto.workout;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public class CreateWorkoutSessionRequest {

    @NotNull
    @PastOrPresent
    public OffsetDateTime startedAt;

    public OffsetDateTime endedAt;

    @NotBlank
    @Size(max = 64)
    public String timezone;

    @Size(max = 500)
    public String notes;
}
