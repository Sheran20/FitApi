package com.sgt.fitapi.mapper;

import com.sgt.fitapi.dto.workout.*;
import com.sgt.fitapi.model.WorkoutSession;
import com.sgt.fitapi.model.WorkoutSet;

import java.util.List;
import java.util.stream.Collectors;
import java.time.ZoneOffset;

public class WorkoutMapper {

    public static WorkoutSetView toSetView(WorkoutSet ws) {
        WorkoutSetView view = new WorkoutSetView();
        view.id = ws.getId();
        view.exerciseId = ws.getExercise().getId();
        view.exerciseName = ws.getExercise().getName();
        view.setNumber = ws.getSetNumber();
        view.reps = ws.getReps();
        view.weight = ws.getWeight();
        view.rpe = ws.getRpe();
        view.restSeconds = ws.getRestSeconds();
        view.notes = ws.getNotes();
        return view;
    }

    public static WorkoutFullView toFullView(WorkoutSession session, List<WorkoutSet> sets) {
        WorkoutFullView view = new WorkoutFullView();

        view.id = session.getId();
        view.userId = session.getUserId();
        view.startedAt = session.getStartedAt() != null ? session.getStartedAt().atOffset(ZoneOffset.UTC) : null;
        view.endedAt = session.getEndedAt() != null ? session.getEndedAt().atOffset(ZoneOffset.UTC) : null;
        view.timezone = session.getTimezone();
        view.notes = session.getNotes();

        view.sets = sets.stream()
                .map(WorkoutMapper::toSetView)
                .collect(Collectors.toList());

        return view;
    }

    public static WorkoutSessionView toSessionView(WorkoutSession session) {
        WorkoutSessionView view = new WorkoutSessionView();
        view.id = session.getId();
        view.userId = session.getUserId();
        view.startedAt = session.getStartedAt() != null ? session.getStartedAt().atOffset(ZoneOffset.UTC) : null;
        view.endedAt = session.getEndedAt() != null ? session.getEndedAt().atOffset(ZoneOffset.UTC) : null;
        view.timezone = session.getTimezone();
        view.notes = session.getNotes();
        return view;
    }

    // DTO -> Entity (for create)
    public static WorkoutSession fromCreateRequest(CreateWorkoutSessionRequest body, Long userId) {
        WorkoutSession session = new WorkoutSession();
        session.setUserId(userId);       // now comes from authenticated user
        session.setStartedAt(body.startedAt != null ? body.startedAt.toInstant() : null);
        session.setEndedAt(body.endedAt != null ? body.endedAt.toInstant() : null);
        session.setTimezone(body.timezone);
        session.setNotes(body.notes);
        return session;
    }


    public static void applyUpdate(WorkoutSession session, UpdateWorkoutSessionRequest body) {
        session.setStartedAt(body.startedAt != null ? body.startedAt.toInstant() : null);
        session.setEndedAt(body.endedAt != null ? body.endedAt.toInstant() : null);
        session.setTimezone(body.timezone);
        session.setNotes(body.notes);
    }

}
