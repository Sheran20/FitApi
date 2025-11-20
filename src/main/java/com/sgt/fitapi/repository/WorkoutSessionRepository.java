package com.sgt.fitapi.repository;

import com.sgt.fitapi.model.WorkoutSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;

public interface WorkoutSessionRepository extends
        JpaRepository<WorkoutSession, Long>,
        JpaSpecificationExecutor<WorkoutSession> {

    // convenience method if you want simple date filtering without specs
    Page<WorkoutSession> findByUserIdAndStartedAtBetween(String userId, LocalDateTime from, LocalDateTime to, Pageable pageable);
    Page<WorkoutSession> findByUserId(String userId, Pageable pageable);
}
