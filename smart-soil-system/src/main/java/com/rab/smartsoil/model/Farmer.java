package com.rab.smartsoil.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * Farmer — extends User (Inheritance).
 * Represents a smallholder farming household registered with RAB.
 */
@Entity
@Table(name = "farmers")
@PrimaryKeyJoinColumn(name = "farmer_id")
public class Farmer extends User {

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "District is required")
    private String district;

    @Min(value = 0, message = "Land size cannot be negative")
    private double landSizeHectares;

    @ElementCollection
    @CollectionTable(name = "farmer_crop_history", joinColumns = @JoinColumn(name = "farmer_id"))
    @Column(name = "crop_name")
    private List<String> cropHistory = new ArrayList<>();

    // ── Constructors ──────────────────────────────────────
    public Farmer() { super(); }

    public Farmer(String fullName, String email, String password, String phone,
                  String location, String district, double landSizeHectares) {
        super(fullName, email, password, Role.FARMER, phone);
        this.location         = location;
        this.district         = district;
        this.landSizeHectares = landSizeHectares;
    }

    // ── Getters & Setters ─────────────────────────────────
    public String getLocation()               { return location; }
    public void   setLocation(String l)       { this.location = l; }
    public String getDistrict()               { return district; }
    public void   setDistrict(String d)       { this.district = d; }
    public double getLandSizeHectares()       { return landSizeHectares; }
    public void   setLandSizeHectares(double s) { this.landSizeHectares = s; }
    public List<String> getCropHistory()      { return cropHistory; }
    public void   addCropHistory(String crop) { this.cropHistory.add(crop); }
}
