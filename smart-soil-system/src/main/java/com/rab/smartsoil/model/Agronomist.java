package com.rab.smartsoil.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Agronomist — extends User (Inheritance).
 * Field expert responsible for soil data collection and advisory generation.
 */
@Entity
@Table(name = "agronomists")
@PrimaryKeyJoinColumn(name = "agronomist_id")
public class Agronomist extends User {

    @NotBlank(message = "Assigned zone is required")
    private String assignedZone;

    private String specialization;

    @Min(value = 0, message = "Experience cannot be negative")
    private int yearsExperience;

    // ── Constructors ──────────────────────────────────────
    public Agronomist() { super(); }

    public Agronomist(String fullName, String email, String password, String phone,
                      String assignedZone, String specialization, int yearsExperience) {
        super(fullName, email, password, Role.AGRONOMIST, phone);
        this.assignedZone    = assignedZone;
        this.specialization  = specialization;
        this.yearsExperience = yearsExperience;
    }

    // ── Getters & Setters ─────────────────────────────────
    public String getAssignedZone()             { return assignedZone; }
    public void   setAssignedZone(String z)     { this.assignedZone = z; }
    public String getSpecialization()           { return specialization; }
    public void   setSpecialization(String s)   { this.specialization = s; }
    public int    getYearsExperience()          { return yearsExperience; }
    public void   setYearsExperience(int y)     { this.yearsExperience = y; }
}
