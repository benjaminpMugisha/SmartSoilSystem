package com.rab.smartsoil.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * DiseaseAlert — generated when NutrientAnalyzer detects critical soil conditions.
 * Triggers the Observer pattern notification chain.
 */
@Entity
@Table(name = "disease_alerts")
public class DiseaseAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String alertId;

    @Column(nullable = false)
    private String plotId;

    private String farmerEmail;
    private String district;

    @Column(nullable = false)
    private String diseaseType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(nullable = false, updatable = false)
    private LocalDateTime detectedDate = LocalDateTime.now();

    @Column(length = 1000)
    private String preventionMeasures;

    private boolean isTreated = false;

    private String farmerPhone;

    // ── Constructors ──────────────────────────────────────
    public DiseaseAlert() {}

    public DiseaseAlert(String plotId, String farmerEmail, String farmerPhone,
                        String district, String diseaseType, Severity severity,
                        String preventionMeasures) {
        this.plotId             = plotId;
        this.farmerEmail        = farmerEmail;
        this.farmerPhone        = farmerPhone;
        this.district           = district;
        this.diseaseType        = diseaseType;
        this.severity           = severity;
        this.preventionMeasures = preventionMeasures;
    }

    /**
     * @return double between 0.0 and 1.0 representing how likely the
     *         disease is to spread to neighboring plots
     */
    public double getSpreadRisk() {
        return switch (severity) {
            case LOW    -> 0.15;
            case MEDIUM -> 0.45;
            case HIGH   -> 0.85;
        };
    }

    // ── Getters & Setters ─────────────────────────────────
    public String    getAlertId()                      { return alertId; }
    public String    getPlotId()                       { return plotId; }
    public String    getFarmerEmail()                  { return farmerEmail; }
    public String    getFarmerPhone()                  { return farmerPhone; }
    public String    getDistrict()                     { return district; }
    public String    getDiseaseType()                  { return diseaseType; }
    public Severity  getSeverity()                     { return severity; }
    public LocalDateTime getDetectedDate()             { return detectedDate; }
    public String    getPreventionMeasures()           { return preventionMeasures; }
    public boolean   isTreated()                       { return isTreated; }
    public void      markAsTreated()                   { this.isTreated = true; }
    public void      setSeverity(Severity s)           { this.severity = s; }
    public void      setPreventionMeasures(String p)   { this.preventionMeasures = p; }
    public void      setDistrict(String d)             { this.district = d; }

    public enum Severity { LOW, MEDIUM, HIGH }
}
