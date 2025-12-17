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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.OffsetDateTime;
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
    public ResponseEntity<WorkoutSessionView> create(@Valid @RequestBody CreateWorkoutSessionRequest body,
                                                     @AuthenticationPrincipal com.sgt.fitapi.model.User user) {

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
        Long userId = user.getId(); // from JWT principal

        // Build entity using mapper + current user
        WorkoutSession session = WorkoutMapper.fromCreateRequest(body, userId);

        // Validate date order
        if (session.getEndedAt() != null &&
                session.getEndedAt().isBefore(session.getStartedAt())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "endedAt must be >= startedAt"
            );
        }

        // Save
        WorkoutSession saved = sessionRepo.save(session);
        WorkoutSessionView view = WorkoutMapper.toSessionView(saved);

        // Return 201 with Location header
        return ResponseEntity
                .created(URI.create("/workouts/" + saved.getId()))
                .body(view);
    }

    // GET /workouts/{id}
    @GetMapping("/{id}")
    public ResponseEntity<WorkoutSessionView> get(@PathVariable Long id,
                                                  @AuthenticationPrincipal com.sgt.fitapi.model.User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
        Long userId = user.getId();

        return sessionRepo.findByIdAndUserId(id, userId)
                .map(WorkoutMapper::toSessionView)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // GET /workouts?from=&to=&page=&size=&sort=startedAt,desc
    @GetMapping
    public Page<WorkoutSessionView> list(
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to,
            Pageable pageable,
            @AuthenticationPrincipal com.sgt.fitapi.model.User user
    ) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
        Long userId = user.getId();

        var spec = Specification.allOf(
                WorkoutSessionSpecs.userEquals(userId),
                WorkoutSessionSpecs.startedAtFrom(from != null ? from.toInstant() : null),
                WorkoutSessionSpecs.startedAtTo(to != null ? to.toInstant() : null)
        );

        Page<WorkoutSession> page = sessionRepo.findAll(spec, pageable);
        return page.map(WorkoutMapper::toSessionView);
    }

    // PUT /workouts/{id}
    @PutMapping("/{id}")
    public ResponseEntity<WorkoutSessionView> update(@PathVariable Long id,
                                                     @Valid @RequestBody UpdateWorkoutSessionRequest body,
                                                     @AuthenticationPrincipal com.sgt.fitapi.model.User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
        Long userId = user.getId();

        var optional = sessionRepo.findByIdAndUserId(id, userId);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        WorkoutSession existing = optional.get();

        // Apply changes from DTO to entity
        WorkoutMapper.applyUpdate(existing, body);

        // Guard: endedAt >= startedAt if present
        if (existing.getEndedAt() != null &&
                existing.getEndedAt().isBefore(existing.getStartedAt())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "endedAt must be >= startedAt"
            );
        }

        WorkoutSession saved = sessionRepo.save(existing);
        return ResponseEntity.ok(WorkoutMapper.toSessionView(saved));
    }

    // DELETE /workouts/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal com.sgt.fitapi.model.User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
        Long userId = user.getId();

        var optional = sessionRepo.findByIdAndUserId(id, userId);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        sessionRepo.delete(optional.get());
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // ========= Nested workout sets =========

    // GET /workouts/{id}/sets[?exerciseId=]
    @GetMapping("/{id}/sets")
    public ResponseEntity<List<WorkoutSetView>> listSets(
            @PathVariable Long id,
            @RequestParam(required = false) Long exerciseId,
            @AuthenticationPrincipal com.sgt.fitapi.model.User user
    ) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
        Long userId = user.getId();

        // ensure session exists and belongs to this user
        WorkoutSession session = sessionRepo.findByIdAndUserId(id, userId)
                .orElse(null);

        if (session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<WorkoutSet> sets;
        Long sessionId = session.getId();

        if (exerciseId != null) {
            sets = workoutSetRepo.findByWorkoutSessionIdAndExerciseId(sessionId, exerciseId);
        } else {
            sets = workoutSetRepo.findByWorkoutSessionId(sessionId);
        }

        List<WorkoutSetView> views = sets.stream()
                .map(WorkoutMapper::toSetView)
                .toList();

        return ResponseEntity.ok(views);
    }

    // POST /workouts/{id}/sets
    @PostMapping("/{id}/sets")
    public ResponseEntity<WorkoutSetView> addSet(@PathVariable Long id,
                                                 @Valid @RequestBody CreateWorkoutSetRequest body,
                                                 @AuthenticationPrincipal com.sgt.fitapi.model.User user) {

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
        Long userId = user.getId();

        WorkoutSession session = sessionRepo.findByIdAndUserId(id, userId)
                .orElse(null);

        if (session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Exercise exercise = exerciseRepo.findById(body.exerciseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Exercise not found: " + body.exerciseId
                ));

        WorkoutSet set = new WorkoutSet();
        set.setWorkoutSession(session);
        set.setExercise(exercise);
        set.setSetNumber(body.setNumber);
        set.setReps(body.reps);
        set.setWeight(body.weight);
        set.setRpe(body.rpe);
        set.setRestSeconds(body.restSeconds);
        set.setNotes(body.notes);

        WorkoutSet saved = workoutSetRepo.save(set);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(WorkoutMapper.toSetView(saved));
    }

    // ========= Analytics / views =========

    // GET /workouts/{id}/full
    @GetMapping("/{id}/full")
    public ResponseEntity<WorkoutFullView> getFull(@PathVariable Long id,
                                                   @AuthenticationPrincipal com.sgt.fitapi.model.User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
        Long userId = user.getId();

        var optional = sessionRepo.findByIdAndUserId(id, userId);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        WorkoutSession session = optional.get();
        var sets = workoutSetRepo.findByWorkoutSessionId(id);

        WorkoutFullView view = WorkoutMapper.toFullView(session, sets);
        return ResponseEntity.ok(view);
    }

    // GET /workouts/{id}/summary
    @GetMapping("/{id}/summary")
    public ResponseEntity<WorkoutSummaryView> getSummary(@PathVariable Long id,
                                                         @AuthenticationPrincipal com.sgt.fitapi.model.User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
        Long userId = user.getId();

        WorkoutSummaryView summary = summaryService.calculateSummary(id, userId);
        return ResponseEntity.ok(summary);
    }
}
