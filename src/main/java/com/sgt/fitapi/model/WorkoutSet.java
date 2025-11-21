package com.sgt.fitapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "workout_sets")
public class WorkoutSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // each set belongs to a workout session
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_session_id", nullable = false)
    @NotNull
    private WorkoutSession workoutSession;

    // and to a specific exercise
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    @NotNull
    private Exercise exercise;

    // set number within that workout (1,2,3…)
    @NotNull
    @Column(nullable = false)
    private Integer setNumber;

    @NotNull
    @Column(nullable = false)
    private Integer reps;

    // kg or lb, your choice – just be consistent at the API level
    @NotNull
    @Column(nullable = false)
    private Double weight;

    // optional RPE
    private Double rpe;

    private Integer restSeconds;

    @Size(max = 500)
    @Column(length = 500)
    private String notes;

    public WorkoutSet() {}

    public WorkoutSet(WorkoutSession workoutSession,
                      Exercise exercise,
                      Integer setNumber,
                      Integer reps,
                      Double weight) {
        this.workoutSession = workoutSession;
        this.exercise = exercise;
        this.setNumber = setNumber;
        this.reps = reps;
        this.weight = weight;
    }

    public Long getId() { return id; }

    public WorkoutSession getWorkoutSession() { return workoutSession; }
    public void setWorkoutSession(WorkoutSession workoutSession) { this.workoutSession = workoutSession; }

    public Exercise getExercise() { return exercise; }
    public void setExercise(Exercise exercise) { this.exercise = exercise; }

    public Integer getSetNumber() { return setNumber; }
    public void setSetNumber(Integer setNumber) { this.setNumber = setNumber; }

    public Integer getReps() { return reps; }
    public void setReps(Integer reps) { this.reps = reps; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Double getRpe() { return rpe; }
    public void setRpe(Double rpe) { this.rpe = rpe; }

    public Integer getRestSeconds() { return restSeconds; }
    public void setRestSeconds(Integer restSeconds) { this.restSeconds = restSeconds; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
