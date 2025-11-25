package com.sgt.fitapi.dto.workout;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class UpdateWorkoutSessionRequest {

    @NotNull
    public LocalDateTime startedAt;

    public LocalDateTime endedAt;

    @NotNull
    @Size(max = 64)
    public String timezone;

    @Size(max = 500)
    public String notes;
}
