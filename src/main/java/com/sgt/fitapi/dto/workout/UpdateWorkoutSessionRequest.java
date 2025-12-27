package com.sgt.fitapi.dto.workout;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

public class UpdateWorkoutSessionRequest {

    @NotNull
    @PastOrPresent
    @Schema(description = "Updated workout start time in ISO-8601 format.", example = "2025-01-15T10:00:00Z")
    public OffsetDateTime startedAt;

    @Schema(description = "Updated workout end time in ISO-8601 format.", example = "2025-01-15T11:15:00Z")
    public OffsetDateTime endedAt;

    @NotNull
    @Size(max = 64)
    @Schema(description = "IANA timezone identifier for the session.", example = "America/Los_Angeles")
    public String timezone;

    @Size(max = 500)
    @Schema(description = "Optional notes about the session.", example = "Updated notes")
    public String notes;
}
