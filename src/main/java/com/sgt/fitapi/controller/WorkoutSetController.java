package com.sgt.fitapi.controller;

import com.sgt.fitapi.dto.workout.WorkoutSetView;
import com.sgt.fitapi.mapper.WorkoutMapper;
import com.sgt.fitapi.model.WorkoutSet;
import com.sgt.fitapi.repository.WorkoutSetRepository;
import org.springframework.http.ResponseEntity;
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
    public List<WorkoutSetView> list(
            @RequestParam(required = false) Long workoutSessionId,
            @RequestParam(required = false) Long exerciseId
    ) {
        List<WorkoutSet> sets;

        if (workoutSessionId != null && exerciseId != null) {
            sets = workoutSetRepo.findByWorkoutSessionIdAndExerciseId(workoutSessionId, exerciseId);
        } else if (workoutSessionId != null) {
            sets = workoutSetRepo.findByWorkoutSessionId(workoutSessionId);
        } else {
            sets = workoutSetRepo.findAll();
        }

        return sets.stream()
                .map(WorkoutMapper::toSetView)
                .toList();
    }

    // GET /workout-sets/{id}
    @GetMapping("/{id}")
    public WorkoutSetView get(@PathVariable Long id) {
        var set = workoutSetRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("WorkoutSet not found"));
        return WorkoutMapper.toSetView(set);
    }

    // DELETE /workout-sets/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!workoutSetRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        workoutSetRepo.deleteById(id);
        return ResponseEntity.noContent().build(); // 204
    }
}
