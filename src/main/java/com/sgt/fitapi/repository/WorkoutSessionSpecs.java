package com.sgt.fitapi.repository;

import com.sgt.fitapi.model.WorkoutSession;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class WorkoutSessionSpecs {
    private WorkoutSessionSpecs() {}

    public static Specification<WorkoutSession> userEquals(String userId) {
        if (userId == null || userId.isBlank()) return Specification.unrestricted();
        return (root, cq, cb) -> cb.equal(root.get("userId"), userId);
    }

    public static Specification<WorkoutSession> startedAtFrom(LocalDateTime from) {
        if (from == null) return Specification.unrestricted();
        return (root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("startedAt"), from);
    }

    public static Specification<WorkoutSession> startedAtTo(LocalDateTime to) {
        if (to == null) return Specification.unrestricted();
        return (root, cq, cb) -> cb.lessThanOrEqualTo(root.get("startedAt"), to);
    }
}
