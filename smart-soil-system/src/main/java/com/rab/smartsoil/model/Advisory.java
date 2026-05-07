package com.rab.smartsoil.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Advisory — personalized crop recommendation generated per plot.
 * Created by AdvisoryEngine using the Strategy pattern.
 */
@Entity
@Table(name = "advisories")
public class Advisory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String advisoryId;

    @Column(nullable = false)
    private String plotId;

    private String district;

    @ElementCollection
    @CollectionTable(name = "advisory_crops", joinColumns = @JoinColumn(name = "advisory_id"))
    @Column(name = "crop")
    private List<String> recommendedCrops;

    @Column(nullable = false)
    private String fertilizerType;

    private double dosageKgPerHectare;

    private String applicationSchedule;

    @Column(length = 1000)
    private String soilHealthSummary;

    private double nutrientScore;

    private String riskLevel;  // LOW, MEDIUM, HIGH

    @Column(nullable = false, updatable = false)
    private LocalDateTime generatedDate = LocalDateTime.now();

    private String generatedByEmail;

    // ── Constructors ──────────────────────────────────────
    public Advisory() {}

    public Advisory(String plotId, String district, List<String> recommendedCrops,
                    String fertilizerType, double dosageKgPerHectare,
                    String soilHealthSummary, double nutrientScore, String riskLevel) {
        this.plotId              = plotId;
        this.district            = district;
        this.recommendedCrops    = recommendedCrops;
        this.fertilizerType      = fertilizerType;
        this.dosageKgPerHectare  = dosageKgPerHectare;
        this.soilHealthSummary   = soilHealthSummary;
        this.nutrientScore       = nutrientScore;
        this.riskLevel           = riskLevel;
        this.applicationSchedule = buildSchedule(fertilizerType);
    }

    private String buildSchedule(String fertilizer) {
        if (fertilizer.contains("Urea"))
            return "Apply at planting (50%) and top-dressing 4 weeks after germination (50%)";
        if (fertilizer.contains("Lime"))
            return "Apply 2-3 weeks before planting. Incorporate into top 15cm of soil.";
        if (fertilizer.contains("Organic") || fertilizer.contains("Compost"))
            return "Apply 1 week before planting. Mix thoroughly into planting holes.";
        return "Apply at planting time. Follow label dosage instructions.";
    }

    // ── Getters & Setters ─────────────────────────────────
    public String getAdvisoryId()                       { return advisoryId; }
    public String getPlotId()                           { return plotId; }
    public String getDistrict()                         { return district; }
    public List<String> getRecommendedCrops()           { return recommendedCrops; }
    public String getFertilizerType()                   { return fertilizerType; }
    public double getDosageKgPerHectare()               { return dosageKgPerHectare; }
    public String getApplicationSchedule()              { return applicationSchedule; }
    public String getSoilHealthSummary()                { return soilHealthSummary; }
    public double getNutrientScore()                    { return nutrientScore; }
    public String getRiskLevel()                        { return riskLevel; }
    public LocalDateTime getGeneratedDate()             { return generatedDate; }
    public String getGeneratedByEmail()                 { return generatedByEmail; }
    public void   setGeneratedByEmail(String e)         { this.generatedByEmail = e; }
    public void   setApplicationSchedule(String s)      { this.applicationSchedule = s; }
}
