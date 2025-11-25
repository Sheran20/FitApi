package com.sgt.fitapi.dto.workout;

import java.time.LocalDateTime;
import java.util.List;

public class WorkoutFullView {
    public Long id;
    public String userId;
    public LocalDateTime startedAt;
    public LocalDateTime endedAt;
    public String timezone;
    public String notes;

    public List<WorkoutSetView> sets;
}
