package com.sgt.fitapi.repository;

import com.sgt.fitapi.model.Exercise;
import org.springframework.data.jpa.domain.Specification;

public final class ExerciseSpecs {
    private ExerciseSpecs() {}

    public static Specification<Exercise> nameContains(String q) {
        if (q == null || q.isBlank()) {
            return Specification.unrestricted();
        }
        return (root, cq, cb) -> cb.like(cb.lower(root.get("name")), "%" + q.toLowerCase() + "%");
    }

    public static Specification<Exercise> muscleGroupEquals(String mg) {
        if (mg == null || mg.isBlank()) {
            return Specification.unrestricted();
        }
        return (root, cq, cb) -> cb.equal(cb.lower(root.get("muscleGroup")), mg.toLowerCase());
    }

    public static Specification<Exercise> equipmentEquals(String eq) {
        if (eq == null || eq.isBlank()) {
            return Specification.unrestricted();
        }
        return (root, cq, cb) -> cb.equal(cb.lower(root.get("equipment")), eq.toLowerCase());
    }

    public static Specification<Exercise> isIsometricEquals(Boolean iso) {
        if (iso == null) {
            return Specification.unrestricted();
        }
        return (root, cq, cb) -> cb.equal(root.get("isIsometric"), iso);
    }
}
