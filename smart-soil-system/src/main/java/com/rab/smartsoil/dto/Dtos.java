package com.rab.smartsoil.dto;

import jakarta.validation.constraints.*;

/**
 * Data Transfer Objects — decouples the API layer from domain entities.
 * Follows Google Java Style: immutable where possible, clear field naming.
 */

// ── Request: Soil Sample Submission ──────────────────────────
public class SoilSubmissionRequest {

    @NotBlank(message = "Plot ID is required")
    private String plotId;

    @NotBlank(message = "District is required")
    private String district;

    @NotBlank(message = "Soil type is required — VOLCANIC, LATERITE, ORGANIC, SANDY_LOAM")
    private String soilType;

    @Min(value = 0, message = "Nitrogen cannot be negative")
    @Max(value = 500, message = "Nitrogen value out of range")
    private double nitrogen;

    @Min(value = 0)
    @Max(value = 500)
    private double phosphorus;

    @Min(value = 0)
    @Max(value = 1000)
    private double potassium;

    @DecimalMin("0.0") @DecimalMax("14.0")
    private double ph;

    @Min(0) @Max(100)
    private double moisture;

    @Min(0)
    private double organicMatter;

    @DecimalMin(value = "0.01", message = "Land size must be positive")
    private double landSizeHectares;

    // ── Getters & Setters ──────────────────────────────────
    public String getPlotId()              { return plotId; }
    public void   setPlotId(String p)      { this.plotId = p; }
    public String getDistrict()            { return district; }
    public void   setDistrict(String d)    { this.district = d; }
    public String getSoilType()            { return soilType; }
    public void   setSoilType(String s)    { this.soilType = s; }
    public double getNitrogen()            { return nitrogen; }
    public void   setNitrogen(double n)    { this.nitrogen = n; }
    public double getPhosphorus()          { return phosphorus; }
    public void   setPhosphorus(double p)  { this.phosphorus = p; }
    public double getPotassium()           { return potassium; }
    public void   setPotassium(double k)   { this.potassium = k; }
    public double getPh()                  { return ph; }
    public void   setPh(double ph)         { this.ph = ph; }
    public double getMoisture()            { return moisture; }
    public void   setMoisture(double m)    { this.moisture = m; }
    public double getOrganicMatter()       { return organicMatter; }
    public void   setOrganicMatter(double o){ this.organicMatter = o; }
    public double getLandSizeHectares()    { return landSizeHectares; }
    public void   setLandSizeHectares(double l) { this.landSizeHectares = l; }
}


// ── Request: User Registration ────────────────────────────────
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Must be a valid email")
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank
    private String phone;

    @NotBlank
    private String role;  // FARMER, AGRONOMIST, ADMIN

    // Farmer-specific
    private String location;
    private String district;
    private double landSizeHectares;

    // Agronomist-specific
    private String assignedZone;
    private String specialization;
    private int yearsExperience;

    // ── Getters & Setters ──────────────────────────────────
    public String getFullName()              { return fullName; }
    public void   setFullName(String f)      { this.fullName = f; }
    public String getEmail()                 { return email; }
    public void   setEmail(String e)         { this.email = e; }
    public String getPassword()              { return password; }
    public void   setPassword(String p)      { this.password = p; }
    public String getPhone()                 { return phone; }
    public void   setPhone(String p)         { this.phone = p; }
    public String getRole()                  { return role; }
    public void   setRole(String r)          { this.role = r; }
    public String getLocation()              { return location; }
    public void   setLocation(String l)      { this.location = l; }
    public String getDistrict()              { return district; }
    public void   setDistrict(String d)      { this.district = d; }
    public double getLandSizeHectares()      { return landSizeHectares; }
    public void   setLandSizeHectares(double l) { this.landSizeHectares = l; }
    public String getAssignedZone()          { return assignedZone; }
    public void   setAssignedZone(String z)  { this.assignedZone = z; }
    public String getSpecialization()        { return specialization; }
    public void   setSpecialization(String s){ this.specialization = s; }
    public int    getYearsExperience()       { return yearsExperience; }
    public void   setYearsExperience(int y)  { this.yearsExperience = y; }
}


// ── Request: Login ────────────────────────────────────────────
public class LoginRequest {

    @Email @NotBlank
    private String email;

    @NotBlank
    private String password;

    public String getEmail()            { return email; }
    public void   setEmail(String e)    { this.email = e; }
    public String getPassword()         { return password; }
    public void   setPassword(String p) { this.password = p; }
}


// ── Response: Authentication ──────────────────────────────────
public class AuthResponse {
    private String token;
    private String role;
    private String fullName;
    private String message;

    public AuthResponse(String token, String role, String fullName, String message) {
        this.token    = token;
        this.role     = role;
        this.fullName = fullName;
        this.message  = message;
    }

    public String getToken()    { return token; }
    public String getRole()     { return role; }
    public String getFullName() { return fullName; }
    public String getMessage()  { return message; }
}


// ── Response: API Error ───────────────────────────────────────
public class ApiError {
    private int status;
    private String error;
    private String message;
    private String timestamp;

    public ApiError(int status, String error, String message) {
        this.status    = status;
        this.error     = error;
        this.message   = message;
        this.timestamp = java.time.LocalDateTime.now().toString();
    }

    public int    getStatus()    { return status; }
    public String getError()     { return error; }
    public String getMessage()   { return message; }
    public String getTimestamp() { return timestamp; }
}
