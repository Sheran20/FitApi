package com.sgt.fitapi.controller;

import com.sgt.fitapi.model.Exercise;
import com.sgt.fitapi.model.WorkoutSession;
import com.sgt.fitapi.model.WorkoutSet;
import com.sgt.fitapi.repository.ExerciseRepository;
import com.sgt.fitapi.repository.WorkoutSessionRepository;
import com.sgt.fitapi.repository.WorkoutSessionSpecs;
import com.sgt.fitapi.repository.WorkoutSetRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/workouts")
public class WorkoutSessionController {

    private final WorkoutSessionRepository repo;

    private final WorkoutSetRepository workoutSetRepo;
    private final ExerciseRepository exerciseRepo;

    public WorkoutSessionController(WorkoutSessionRepository repo,
                                    WorkoutSetRepository workoutSetRepo,
                                    ExerciseRepository exerciseRepo) {
        this.repo = repo;
        this.workoutSetRepo = workoutSetRepo;
        this.exerciseRepo = exerciseRepo;
    }

    public static class CreateWorkoutSetRequest {

        @NotNull
        public Long exerciseId;

        @NotNull
        public Integer setNumber;

        @NotNull
        public Integer reps;

        @NotNull
        public Double weight;

        public Double rpe;
        public Integer restSeconds;

        @Size(max = 500)
        public String notes;
    }

    public static class WorkoutSetView {
        public Long id;
        public Long exerciseId;
        public String exerciseName;
        public Integer setNumber;
        public Integer reps;
        public Double weight;
        public Double rpe;
        public Integer restSeconds;
        public String notes;
    }

    public static class WorkoutFullView {
        public Long id;
        public String userId;
        public java.time.LocalDateTime startedAt;
        public java.time.LocalDateTime endedAt;
        public String timezone;
        public String notes;
        public java.util.List<WorkoutSetView> sets;
    }


    // POST /workouts
    @PostMapping
    public WorkoutSession create(@Valid @RequestBody WorkoutSession body) {
        // simple guard: endedAt >= startedAt if present
        if (body.getEndedAt() != null && body.getEndedAt().isBefore(body.getStartedAt())) {
            throw new IllegalArgumentException("endedAt must be >= startedAt");
        }
        return repo.save(body);
    }

    // GET /workouts?userId=&from=&to=&page=&size=&sort=startedAt,desc
    @GetMapping
    public Page<WorkoutSession> list(
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
        return repo.findAll(spec, pageable);
    }

    // GET /workouts/{id}
    @GetMapping("/{id}")
    public WorkoutSession get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("WorkoutSession not found"));
    }

    @GetMapping("/{id}/sets")
    public List<WorkoutSet> listSets(@PathVariable Long id) {
        // ensure session exists (gives 500 with message if not)
        repo.findById(id).orElseThrow(() -> new RuntimeException("WorkoutSession not found"));

        return workoutSetRepo.findByWorkoutSessionId(id);
    }

    @GetMapping("/{id}/full")
    public WorkoutFullView getFullWorkout(@PathVariable Long id) {
        // 1) Load the workout session or fail
        var session = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("WorkoutSession not found: " + id));

        // 2) Load all sets for this workout
        var sets = workoutSetRepo.findByWorkoutSessionId(id);

        // 3) Map to DTO
        WorkoutFullView view = new WorkoutFullView();
        view.id = session.getId();
        view.userId = session.getUserId();
        view.startedAt = session.getStartedAt();
        view.endedAt = session.getEndedAt();
        view.timezone = session.getTimezone();
        view.notes = session.getNotes();

        // Convert each WorkoutSet entity into a WorkoutSetView DTO and collect into a list
        view.sets = sets.stream().map(ws -> {
            WorkoutSetView s = new WorkoutSetView();
            s.id = ws.getId();
            s.exerciseId = ws.getExercise().getId();
            s.exerciseName = ws.getExercise().getName();
            s.setNumber = ws.getSetNumber();
            s.reps = ws.getReps();
            s.weight = ws.getWeight();
            s.rpe = ws.getRpe();
            s.restSeconds = ws.getRestSeconds();
            s.notes = ws.getNotes();
            return s;
        }).toList();

        return view;
    }

    @PostMapping("/{id}/sets")
    public WorkoutSet addSet(@PathVariable Long id,
                             @Valid @RequestBody CreateWorkoutSetRequest body) {

        WorkoutSession session = repo.findById(id)
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

    // PUT /workouts/{id}
    @PutMapping("/{id}")
    public WorkoutSession update(@PathVariable Long id, @Valid @RequestBody WorkoutSession body) {
        var existing = repo.findById(id).orElseThrow(() -> new RuntimeException("WorkoutSession not found"));
        existing.setUserId(body.getUserId());
        existing.setStartedAt(body.getStartedAt());
        existing.setEndedAt(body.getEndedAt());
        existing.setTimezone(body.getTimezone());
        existing.setNotes(body.getNotes());
        return repo.save(existing);
    }

    // DELETE /workouts/{id}
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}
