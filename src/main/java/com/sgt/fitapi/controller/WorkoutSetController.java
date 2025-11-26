package com.sgt.fitapi.controller;

import com.sgt.fitapi.dto.workout.WorkoutSetView;
import com.sgt.fitapi.mapper.WorkoutMapper;
import com.sgt.fitapi.model.WorkoutSession;
import com.sgt.fitapi.model.WorkoutSet;
import com.sgt.fitapi.repository.WorkoutSessionRepository;
import com.sgt.fitapi.repository.WorkoutSetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workout-sets")
public class WorkoutSetController {

    private final WorkoutSetRepository workoutSetRepo;
    private final WorkoutSessionRepository sessionRepo;

    public WorkoutSetController(WorkoutSetRepository workoutSetRepo,
                                WorkoutSessionRepository sessionRepo) {
        this.workoutSetRepo = workoutSetRepo;
        this.sessionRepo = sessionRepo;
    }

    // GET /workout-sets?workoutSessionId=&exerciseId=
    @GetMapping
    public ResponseEntity<List<WorkoutSetView>> list(
            @RequestParam(required = false) Long workoutSessionId,
            @RequestParam(required = false) Long exerciseId,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();

        // For security, we now require workoutSessionId.
        if (workoutSessionId == null) {
            // No session specified -> ambiguous / unsafe in a multi-tenant app
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // ensure session exists and belongs to this user
        WorkoutSession session = sessionRepo.findByIdAndUserId(workoutSessionId, userEmail)
                .orElse(null);

        if (session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<WorkoutSet> sets;

        if (exerciseId != null) {
            sets = workoutSetRepo.findByWorkoutSessionIdAndExerciseId(session.getId(), exerciseId);
        } else {
            sets = workoutSetRepo.findByWorkoutSessionId(session.getId());
        }

        List<WorkoutSetView> views = sets.stream()
                .map(WorkoutMapper::toSetView)
                .toList();

        return ResponseEntity.ok(views);
    }

    // GET /workout-sets/{id}
    @GetMapping("/{id}")
    public ResponseEntity<WorkoutSetView> get(@PathVariable Long id,
                                              Authentication authentication) {
        String userEmail = authentication.getName();

        return workoutSetRepo.findById(id)
                .filter(set -> {
                    WorkoutSession session = set.getWorkoutSession();
                    return session != null && userEmail.equals(session.getUserId());
                })
                .map(WorkoutMapper::toSetView)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // DELETE /workout-sets/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       Authentication authentication) {

        String userEmail = authentication.getName();

        return workoutSetRepo.findById(id)
                .filter(set -> {
                    var session = set.getWorkoutSession();
                    return session != null && userEmail.equals(session.getUserId());
                })
                .map(set -> {
                    workoutSetRepo.delete(set);
                    return ResponseEntity.noContent().<Void>build();   // <<-- FIXED
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).<Void>build()); // <<-- FIXED
    }

}
