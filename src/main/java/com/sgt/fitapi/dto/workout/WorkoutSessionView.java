package com.sgt.fitapi.dto.workout;

import java.time.LocalDateTime;

public class WorkoutSessionView {
    public Long id;
    public String userId;
    public LocalDateTime startedAt;
    public LocalDateTime endedAt;
    public String timezone;
    public String notes;
}
