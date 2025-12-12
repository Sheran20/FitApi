package com.sgt.fitapi.service;

import com.sgt.fitapi.dto.workout.ExerciseVolumeView;
import com.sgt.fitapi.dto.workout.WorkoutSummaryView;
import com.sgt.fitapi.model.WorkoutSession;
import com.sgt.fitapi.model.WorkoutSet;
import com.sgt.fitapi.repository.WorkoutSessionRepository;
import com.sgt.fitapi.repository.WorkoutSetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class WorkoutSummaryService {

    private final WorkoutSessionRepository sessionRepo;
    private final WorkoutSetRepository setRepo;

    public WorkoutSummaryService(WorkoutSessionRepository sessionRepo,
                                 WorkoutSetRepository setRepo) {
        this.sessionRepo = sessionRepo;
        this.setRepo = setRepo;
    }

    public WorkoutSummaryView calculateSummary(Long workoutId, String userId) {
        // Enforce ownership here to prevent cross-tenant access if new callers skip controller checks.
        WorkoutSession session = sessionRepo.findByIdAndUserId(workoutId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "WorkoutSession not found: " + workoutId
                ));

        List<WorkoutSet> sets = setRepo.findByWorkoutSessionId(workoutId);

        WorkoutSummaryView summary = new WorkoutSummaryView();
        summary.id = session.getId();
        summary.userId = session.getUserId();
        summary.startedAt = session.getStartedAt();
        summary.endedAt = session.getEndedAt();
        summary.timezone = session.getTimezone();
        summary.notes = session.getNotes();

        double totalVolume = 0.0;
        Map<Long, ExerciseVolumeView> perExercise = new HashMap<>();

        for (WorkoutSet ws : sets) {
            Integer reps = ws.getReps();
            Double weight = ws.getWeight();

            if (reps == null || weight == null) continue;

            double setVolume = reps * weight;
            totalVolume += setVolume;

            Long exerciseId = ws.getExercise().getId();
            String exerciseName = ws.getExercise().getName();

            ExerciseVolumeView ev = perExercise.getOrDefault(exerciseId, null);
            if (ev == null) {
                ev = new ExerciseVolumeView();
                ev.exerciseId = exerciseId;
                ev.exerciseName = exerciseName;
                ev.volume = 0.0;
                ev.setsCount = 0;
                perExercise.put(exerciseId, ev);
            }

            ev.volume += setVolume;
            ev.setsCount++;
        }

        summary.totalVolume = totalVolume;
        summary.setsCount = sets.size();
        summary.uniqueExercises = perExercise.size();
        summary.exerciseBreakdown = new ArrayList<>(perExercise.values());

        return summary;
    }
}
