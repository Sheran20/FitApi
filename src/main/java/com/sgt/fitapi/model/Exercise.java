package com.sgt.fitapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(
        name = "exercises",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_exercise_name", columnNames = {"name"})
        }
)

public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 50)
    @Column(length = 50)
    private String muscleGroup;

    @Size(max = 50)
    @Column(length = 50)
    private String equipment;

    @Column(nullable = false)
    private boolean isIsometric = false;

    @Size(max = 50)
    @Column(nullable = false)
    private String movementType;

    public Exercise() {}

    public Exercise(String name, String muscleGroup, String equipment, boolean isIsometric, String movementType) {
        this.name = name;
        this.muscleGroup = muscleGroup;
        this.equipment = equipment;
        this.isIsometric = isIsometric;
        this.movementType = movementType;
    }
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMuscleGroup() { return muscleGroup; }
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }

    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }

    public boolean isIsometric() { return isIsometric; }
    public void setIsIsometric(boolean isIsometric) { this.isIsometric = isIsometric; }

    public String getMovementType() { return movementType; }
    public void setMovementType(String movementType) { this.movementType = movementType; }
}