package com.sgt.fitapi.dto.workout;

import java.time.OffsetDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

public class WorkoutSessionView {
    @Schema(description = "Workout session ID.", example = "123")
    public Long id;
    @Schema(description = "Owner user ID.", example = "42")
    public Long userId;
    @Schema(description = "Workout start time in ISO-8601 format.", example = "2025-01-15T10:00:00Z")
    public OffsetDateTime startedAt;
    @Schema(description = "Workout end time in ISO-8601 format.", example = "2025-01-15T11:15:00Z")
    public OffsetDateTime endedAt;
    @Schema(description = "IANA timezone identifier for the session.", example = "America/Los_Angeles")
    public String timezone;
    @Schema(description = "Optional notes about the session.", example = "Leg day strength focus")
    public String notes;
}
