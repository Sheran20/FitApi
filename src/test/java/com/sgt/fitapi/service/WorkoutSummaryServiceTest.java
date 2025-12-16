package com.sgt.fitapi.service;

import com.sgt.fitapi.model.WorkoutSession;
import com.sgt.fitapi.model.WorkoutSet;
import com.sgt.fitapi.repository.WorkoutSessionRepository;
import com.sgt.fitapi.repository.WorkoutSetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutSummaryServiceTest {

    @Mock
    private WorkoutSessionRepository sessionRepo;

    @Mock
    private WorkoutSetRepository setRepo;

    @InjectMocks
    private WorkoutSummaryService service;

    @Test
    void calculateSummaryEnforcesOwnership() {
        when(sessionRepo.findByIdAndUserId(anyLong(), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
                service.calculateSummary(1L, "user@example.com"));
    }

    @Test
    void calculateSummaryAggregatesVolume() {
        WorkoutSession session = new WorkoutSession();
        session.setUserId("user@example.com");

        var exercise = new com.sgt.fitapi.model.Exercise();
        java.lang.reflect.Field idField;
        try {
            idField = com.sgt.fitapi.model.Exercise.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(exercise, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        exercise.setName("Bench Press");

        WorkoutSet set1 = new WorkoutSet();
        set1.setWorkoutSession(session);
        set1.setReps(5);
        set1.setWeight(100.0);
        set1.setExercise(exercise);

        WorkoutSet set2 = new WorkoutSet();
        set2.setWorkoutSession(session);
        set2.setReps(10);
        set2.setWeight(50.0);
        set2.setExercise(exercise);

        when(sessionRepo.findByIdAndUserId(10L, "user@example.com")).thenReturn(Optional.of(session));
        when(setRepo.findByWorkoutSessionId(10L)).thenReturn(List.of(set1, set2));

        var summary = service.calculateSummary(10L, "user@example.com");
        assertEquals(5 * 100.0 + 10 * 50.0, summary.totalVolume, 0.001);
        assertEquals(2, summary.setsCount);
    }
}
