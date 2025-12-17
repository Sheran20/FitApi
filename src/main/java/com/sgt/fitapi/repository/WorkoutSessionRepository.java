package com.sgt.fitapi.repository;

import com.sgt.fitapi.model.WorkoutSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.Optional;

public interface WorkoutSessionRepository extends
        JpaRepository<WorkoutSession, Long>,
        JpaSpecificationExecutor<WorkoutSession> {

    Page<WorkoutSession> findByUserIdAndStartedAtBetween(Long userId,
                                                         Instant from,
                                                         Instant to,
                                                         Pageable pageable);

    Page<WorkoutSession> findByUserId(Long userId, Pageable pageable);

    Optional<WorkoutSession> findByIdAndUserId(Long id, Long userId);
}
