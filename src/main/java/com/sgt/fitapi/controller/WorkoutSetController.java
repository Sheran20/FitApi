package com.sgt.fitapi.controller;

import com.sgt.fitapi.dto.workout.WorkoutSetView;
import com.sgt.fitapi.mapper.WorkoutMapper;
import com.sgt.fitapi.model.WorkoutSession;
import com.sgt.fitapi.model.WorkoutSet;
import com.sgt.fitapi.repository.WorkoutSessionRepository;
import com.sgt.fitapi.repository.WorkoutSetRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    @Operation(
            summary = "List workout sets",
            description = "Returns sets for a specific session owned by the authenticated user. Requires workoutSessionId and supports optional exerciseId filtering."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))
            )
    })
    public ResponseEntity<List<WorkoutSetView>> list(
            @RequestParam(required = false) Long workoutSessionId,
            @RequestParam(required = false) Long exerciseId,
            @AuthenticationPrincipal com.sgt.fitapi.model.User user
    ) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
        Long userId = user.getId();

        // For security, we now require workoutSessionId.
        if (workoutSessionId == null) {
            // No session specified -> ambiguous / unsafe in a multi-tenant app
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // ensure session exists and belongs to this user
        WorkoutSession session = sessionRepo.findByIdAndUserId(workoutSessionId, userId)
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
    @Operation(
            summary = "Get a workout set by ID",
            description = "Returns a set if it belongs to a session owned by the authenticated user. Returns 404 if not found."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    public ResponseEntity<WorkoutSetView> get(@PathVariable Long id,
                                              @AuthenticationPrincipal com.sgt.fitapi.model.User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
        Long userId = user.getId();

        return workoutSetRepo.findById(id)
                .filter(set -> {
                    WorkoutSession session = set.getWorkoutSession();
                    return session != null && userId.equals(session.getUserId());
                })
                .map(WorkoutMapper::toSetView)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // DELETE /workout-sets/{id}
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a workout set",
            description = "Deletes a set if it belongs to a session owned by the authenticated user. Returns 204 on success."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "No Content")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal com.sgt.fitapi.model.User user) {

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
        Long userId = user.getId();

        return workoutSetRepo.findById(id)
                .filter(set -> {
                    var session = set.getWorkoutSession();
                    return session != null && userId.equals(session.getUserId());
                })
                .map(set -> {
                    workoutSetRepo.delete(set);
                    return ResponseEntity.noContent().<Void>build();   // <<-- FIXED
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).<Void>build()); // <<-- FIXED
    }

}
