package com.sgt.fitapi.controller;

import com.sgt.fitapi.model.Exercise;
import com.sgt.fitapi.repository.ExerciseRepository;
import com.sgt.fitapi.repository.ExerciseSpecs;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exercises")
public class ExerciseController {

    private final ExerciseRepository repo;

    public ExerciseController(ExerciseRepository repo) {
        this.repo = repo;
    }

    // GET /exercises?search=&muscleGroup=&equipment=&isIsometric=
    @GetMapping
    @Operation(
            summary = "List exercises",
            description = "Returns exercises filtered by optional search, muscle group, equipment, or isometric flag."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    public List<Exercise> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String muscleGroup,
            @RequestParam(required = false) String equipment,
            @RequestParam(required = false) Boolean isIsometric
    ) {
        var spec = Specification.allOf(
                ExerciseSpecs.nameContains(search),
                ExerciseSpecs.muscleGroupEquals(muscleGroup),
                ExerciseSpecs.equipmentEquals(equipment),
                ExerciseSpecs.isIsometricEquals(isIsometric)
        );

        return repo.findAll(spec);
    }

    // GET /exercises/{id}
    @GetMapping("/{id}")
    @Operation(
            summary = "Get an exercise by ID",
            description = "Returns an exercise by its ID, or 404 if it does not exist."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    public ResponseEntity<Exercise> get(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
