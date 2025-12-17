package com.sgt.fitapi.dto.workout;

import java.time.OffsetDateTime;
import java.util.List;

public class WorkoutFullView {
    public Long id;
    public Long userId;
    public OffsetDateTime startedAt;
    public OffsetDateTime endedAt;
    public String timezone;
    public String notes;

    public List<WorkoutSetView> sets;
}
