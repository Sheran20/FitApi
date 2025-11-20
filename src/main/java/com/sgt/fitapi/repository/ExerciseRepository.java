package com.sgt.fitapi.repository;

import com.sgt.fitapi.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ExerciseRepository extends
        JpaRepository<Exercise, Long>,
        JpaSpecificationExecutor<Exercise> {
}
