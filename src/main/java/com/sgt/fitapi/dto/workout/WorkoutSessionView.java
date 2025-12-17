package com.sgt.fitapi.dto.workout;

import java.time.OffsetDateTime;

public class WorkoutSessionView {
    public Long id;
    public Long userId;
    public OffsetDateTime startedAt;
    public OffsetDateTime endedAt;
    public String timezone;
    public String notes;
}
