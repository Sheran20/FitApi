package com.sgt.fitapi.controller;

import com.sgt.fitapi.model.Exercise;
import com.sgt.fitapi.repository.ExerciseRepository;
import com.sgt.fitapi.repository.ExerciseSpecs;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exercises")
public class ExerciseController {

    private final ExerciseRepository repo;

    public ExerciseController(ExerciseRepository repo) {
        this.repo = repo;
    }

    @GetMapping
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

        return repo.findAll(spec); // returns List<Exercise>
    }

    @GetMapping("/{id}")
    public Exercise get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Exercise not found"));
    }
}
