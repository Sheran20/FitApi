package com.sgt.fitapi.dto.workout;

import java.time.LocalDateTime;
import java.util.List;

public class WorkoutSummaryView {
    public Long id;
    public String userId;
    public LocalDateTime startedAt;
    public LocalDateTime endedAt;
    public String timezone;
    public String notes;

    public double totalVolume;
    public int setsCount;
    public int uniqueExercises;

    public List<ExerciseVolumeView> exerciseBreakdown;
}
