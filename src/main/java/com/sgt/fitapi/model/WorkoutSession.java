package com.sgt.fitapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "workout_sessions")
public class WorkoutSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // later this will come from JWT
    @NotBlank
    @Size(max = 64)
    @Column(nullable = false, length = 64)
    private String userId;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    @NotBlank
    @Size(max = 64)
    @Column(nullable = false, length = 64)
    private String timezone; // e.g., "America/Toronto"

    @Size(max = 500)
    @Column(length = 500)
    private String notes;

    public WorkoutSession() {}

    public WorkoutSession(String userId, LocalDateTime startedAt, String timezone, String notes) {
        this.userId = userId;
        this.startedAt = startedAt;
        this.timezone = timezone;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
