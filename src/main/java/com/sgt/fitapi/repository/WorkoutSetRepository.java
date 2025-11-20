package com.sgt.fitapi.repository;

import com.sgt.fitapi.model.WorkoutSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutSetRepository extends JpaRepository<WorkoutSet, Long> {

    // All sets for a session
    List<WorkoutSet> findByWorkoutSessionId(Long workoutSessionId);

    // All sets for a session + exercise (e.g., all bench sets in that workout)
    List<WorkoutSet> findByWorkoutSessionIdAndExerciseId(Long workoutSessionId, Long exerciseId);
}
