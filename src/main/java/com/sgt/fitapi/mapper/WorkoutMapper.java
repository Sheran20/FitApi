package com.sgt.fitapi.mapper;

import com.sgt.fitapi.dto.workout.*;
import com.sgt.fitapi.model.WorkoutSession;
import com.sgt.fitapi.model.WorkoutSet;

import java.util.List;
import java.util.stream.Collectors;

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
        view.startedAt = session.getStartedAt();
        view.endedAt = session.getEndedAt();
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
        view.startedAt = session.getStartedAt();
        view.endedAt = session.getEndedAt();
        view.timezone = session.getTimezone();
        view.notes = session.getNotes();
        return view;
    }

    // DTO -> Entity (for create)
    public static WorkoutSession fromCreateRequest(CreateWorkoutSessionRequest body) {
        WorkoutSession session = new WorkoutSession();
        session.setUserId(body.userId);           // later: override from JWT
        session.setStartedAt(body.startedAt);
        session.setEndedAt(body.endedAt);
        session.setTimezone(body.timezone);
        session.setNotes(body.notes);
        return session;
    }

    public static void applyUpdate(WorkoutSession session, UpdateWorkoutSessionRequest body) {
        session.setStartedAt(body.startedAt);
        session.setEndedAt(body.endedAt);
        session.setTimezone(body.timezone);
        session.setNotes(body.notes);
    }

}
