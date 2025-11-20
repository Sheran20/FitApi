package com.sgt.fitapi.controller;

import com.sgt.fitapi.model.WorkoutSession;
import com.sgt.fitapi.repository.WorkoutSessionRepository;
import com.sgt.fitapi.repository.WorkoutSessionSpecs;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/workouts")
public class WorkoutSessionController {

    private final WorkoutSessionRepository repo;

    public WorkoutSessionController(WorkoutSessionRepository repo) {
        this.repo = repo;
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
