package com.sgt.fitapi.controller;

import com.sgt.fitapi.model.Exercise;
import com.sgt.fitapi.model.WorkoutSession;
import com.sgt.fitapi.model.WorkoutSet;
import com.sgt.fitapi.repository.ExerciseRepository;
import com.sgt.fitapi.repository.WorkoutSessionRepository;
import com.sgt.fitapi.repository.WorkoutSetRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workout-sets")
public class WorkoutSetController {

    private final WorkoutSetRepository workoutSetRepo;
    private final WorkoutSessionRepository workoutSessionRepo;
    private final ExerciseRepository exerciseRepo;

    public WorkoutSetController(WorkoutSetRepository workoutSetRepo,
                                WorkoutSessionRepository workoutSessionRepo,
                                ExerciseRepository exerciseRepo) {
        this.workoutSetRepo = workoutSetRepo;
        this.workoutSessionRepo = workoutSessionRepo;
        this.exerciseRepo = exerciseRepo;
    }

    // POST /workout-sets
    @PostMapping
    public WorkoutSet create(@Valid @RequestBody WorkoutSet body) {
        // Ensure session + exercise actually exist (and reattach them)
        Long sessionId = body.getWorkoutSession().getId();
        Long exerciseId = body.getExercise().getId();

        WorkoutSession session = workoutSessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("WorkoutSession not found: " + sessionId));
        Exercise exercise = exerciseRepo.findById(exerciseId)
                .orElseThrow(() -> new RuntimeException("Exercise not found: " + exerciseId));

        body.setWorkoutSession(session);
        body.setExercise(exercise);

        return workoutSetRepo.save(body);
    }

    // GET /workout-sets?workoutSessionId=&exerciseId=
    @GetMapping
    public List<WorkoutSet> list(
            @RequestParam(required = false) Long workoutSessionId,
            @RequestParam(required = false) Long exerciseId
    ) {
        if (workoutSessionId != null && exerciseId != null) {
            return workoutSetRepo.findByWorkoutSessionIdAndExerciseId(workoutSessionId, exerciseId);
        } else if (workoutSessionId != null) {
            return workoutSetRepo.findByWorkoutSessionId(workoutSessionId);
        } else {
            return workoutSetRepo.findAll();
        }
    }

    // GET /workout-sets/{id}
    @GetMapping("/{id}")
    public WorkoutSet get(@PathVariable Long id) {
        return workoutSetRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("WorkoutSet not found"));
    }

    // DELETE /workout-sets/{id}
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        workoutSetRepo.deleteById(id);
    }
}
