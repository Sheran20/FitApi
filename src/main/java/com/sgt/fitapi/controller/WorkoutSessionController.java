package com.sgt.fitapi.controller;

import com.sgt.fitapi.dto.workout.*;
import com.sgt.fitapi.mapper.WorkoutMapper;
import com.sgt.fitapi.model.Exercise;
import com.sgt.fitapi.model.WorkoutSession;
import com.sgt.fitapi.model.WorkoutSet;
import com.sgt.fitapi.repository.ExerciseRepository;
import com.sgt.fitapi.repository.WorkoutSessionRepository;
import com.sgt.fitapi.repository.WorkoutSessionSpecs;
import com.sgt.fitapi.repository.WorkoutSetRepository;
import com.sgt.fitapi.service.WorkoutSummaryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/workouts")
public class WorkoutSessionController {

    private final WorkoutSessionRepository sessionRepo;
    private final WorkoutSetRepository workoutSetRepo;
    private final ExerciseRepository exerciseRepo;
    private final WorkoutSummaryService summaryService;

    public WorkoutSessionController(WorkoutSessionRepository sessionRepo,
                                    WorkoutSetRepository workoutSetRepo,
                                    ExerciseRepository exerciseRepo,
                                    WorkoutSummaryService summaryService) {
        this.sessionRepo = sessionRepo;
        this.workoutSetRepo = workoutSetRepo;
        this.exerciseRepo = exerciseRepo;
        this.summaryService = summaryService;
    }

    // ========= Core CRUD =========

    // POST /workouts
    @PostMapping
    public WorkoutSessionView create(@Valid @RequestBody CreateWorkoutSessionRequest body) {

        // Build entity using mapper
        WorkoutSession session = WorkoutMapper.fromCreateRequest(body);

        // Validate date order
        if (session.getEndedAt() != null &&
                session.getEndedAt().isBefore(session.getStartedAt())) {
            throw new IllegalArgumentException("endedAt must be >= startedAt");
        }

        // Save
        WorkoutSession saved = sessionRepo.save(session);
        return WorkoutMapper.toSessionView(saved);
    }

    // GET /workouts/{id}
    @GetMapping("/{id}")
    public WorkoutSessionView get(@PathVariable Long id) {
        var session = sessionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("WorkoutSession not found"));
        return WorkoutMapper.toSessionView(session);
    }

    // GET /workouts?userId=&from=&to=&page=&size=&sort=startedAt,desc
    @GetMapping
    public Page<WorkoutSessionView> list(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            Pageable pageable
    ) {
        var spec = Specification.allOf(
                WorkoutSessionSpecs.userEquals(userId),
                WorkoutSessionSpecs.startedAtFrom(from),
                WorkoutSessionSpecs.startedAtTo(to)
        );

        Page<WorkoutSession> page = sessionRepo.findAll(spec, pageable);
        return page.map(WorkoutMapper::toSessionView);
    }

    // PUT /workouts/{id}
    @PutMapping("/{id}")
    public WorkoutSessionView update(@PathVariable Long id,
                                     @Valid @RequestBody UpdateWorkoutSessionRequest body) {
        var existing = sessionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("WorkoutSession not found"));

        // Apply changes from DTO to entity
        WorkoutMapper.applyUpdate(existing, body);

        // Guard: endedAt >= startedAt if present
        if (existing.getEndedAt() != null &&
                existing.getEndedAt().isBefore(existing.getStartedAt())) {
            throw new IllegalArgumentException("endedAt must be >= startedAt");
        }

        WorkoutSession saved = sessionRepo.save(existing);
        return WorkoutMapper.toSessionView(saved);
    }

    // DELETE /workouts/{id}
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        sessionRepo.deleteById(id);
    }

    // ========= Nested workout sets =========

    // GET /workouts/{id}/sets[?exerciseId=]
    @GetMapping("/{id}/sets")
    public List<WorkoutSet> listSets(@PathVariable Long id,
                                     @RequestParam(required = false) Long exerciseId) {
        // ensure session exists
        sessionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("WorkoutSession not found: " + id));

        if (exerciseId != null) {
            return workoutSetRepo.findByWorkoutSessionIdAndExerciseId(id, exerciseId);
        }
        return workoutSetRepo.findByWorkoutSessionId(id);
    }

    // POST /workouts/{id}/sets
    @PostMapping("/{id}/sets")
    public WorkoutSet addSet(@PathVariable Long id,
                             @Valid @RequestBody CreateWorkoutSetRequest body) {

        WorkoutSession session = sessionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("WorkoutSession not found: " + id));

        Exercise exercise = exerciseRepo.findById(body.exerciseId)
                .orElseThrow(() -> new RuntimeException("Exercise not found: " + body.exerciseId));

        WorkoutSet set = new WorkoutSet();
        set.setWorkoutSession(session);
        set.setExercise(exercise);
        set.setSetNumber(body.setNumber);
        set.setReps(body.reps);
        set.setWeight(body.weight);
        set.setRpe(body.rpe);
        set.setRestSeconds(body.restSeconds);
        set.setNotes(body.notes);

        return workoutSetRepo.save(set);
    }

    // ========= Analytics / views =========

    // GET /workouts/{id}/full
    @GetMapping("/{id}/full")
    public WorkoutFullView getFull(@PathVariable Long id) {
        var session = sessionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("WorkoutSession not found"));
        var sets = workoutSetRepo.findByWorkoutSessionId(id);
        return WorkoutMapper.toFullView(session, sets);
    }

    // GET /workouts/{id}/summary
    @GetMapping("/{id}/summary")
    public WorkoutSummaryView getSummary(@PathVariable Long id) {
        return summaryService.calculateSummary(id);
    }
}
