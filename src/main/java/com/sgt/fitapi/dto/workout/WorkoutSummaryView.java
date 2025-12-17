package com.sgt.fitapi.dto.workout;

import java.time.OffsetDateTime;
import java.util.List;

public class WorkoutSummaryView {
    public Long id;
    public Long userId;
    public OffsetDateTime startedAt;
    public OffsetDateTime endedAt;
    public String timezone;
    public String notes;

    public double totalVolume;
    public int setsCount;
    public int uniqueExercises;

    public List<ExerciseVolumeView> exerciseBreakdown;
}
