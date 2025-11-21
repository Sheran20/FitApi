package com.sgt.fitapi.controller;

import com.sgt.fitapi.model.WorkoutSet;
import com.sgt.fitapi.repository.WorkoutSetRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workout-sets")
public class WorkoutSetController {

    private final WorkoutSetRepository workoutSetRepo;

    public WorkoutSetController(WorkoutSetRepository workoutSetRepo) {
        this.workoutSetRepo = workoutSetRepo;
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
