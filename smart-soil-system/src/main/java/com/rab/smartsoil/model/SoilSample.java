package com.rab.smartsoil.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * SoilSample — core data entity representing a single soil test reading.
 * Nutrient thresholds are defined as RAB-standard constants.
 */
@Entity
@Table(name = "soil_samples")
public class SoilSample {

    // ── RAB Nutrient Threshold Constants ──────────────────
    public static final double MIN_NITROGEN   = 30.0;
    public static final double MAX_NITROGEN   = 150.0;
    public static final double MIN_PHOSPHORUS = 15.0;
    public static final double MAX_PHOSPHORUS = 80.0;
    public static final double MIN_POTASSIUM  = 100.0;
    public static final double MAX_POTASSIUM  = 400.0;
    public static final double MIN_PH         = 5.5;
    public static final double MAX_PH         = 7.5;
    public static final double MIN_MOISTURE   = 20.0;
    public static final double MAX_MOISTURE   = 80.0;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String sampleId;

    @NotBlank(message = "Plot ID is required")
    private String plotId;

    @NotBlank(message = "District is required")
    private String district;

    @NotBlank(message = "Soil type is required")
    private String soilType;   // VOLCANIC, LATERITE, ORGANIC, SANDY_LOAM

    @Min(value = 0, message = "Nitrogen cannot be negative")
    @Max(value = 500, message = "Nitrogen reading out of range")
    private double nitrogen;

    @Min(value = 0, message = "Phosphorus cannot be negative")
    @Max(value = 500, message = "Phosphorus reading out of range")
    private double phosphorus;

    @Min(value = 0, message = "Potassium cannot be negative")
    @Max(value = 1000, message = "Potassium reading out of range")
    private double potassium;

    @DecimalMin(value = "0.0", message = "pH cannot be below 0")
    @DecimalMax(value = "14.0", message = "pH cannot exceed 14")
    private double ph;

    @Min(value = 0)
    @Max(value = 100)
    private double moisture;

    @Min(value = 0)
    private double organicMatter;

    private double landSizeHectares;

    @Column(nullable = false, updatable = false)
    private LocalDateTime sampleDate = LocalDateTime.now();

    private String collectedByEmail;

    // ── Constructors ──────────────────────────────────────
    public SoilSample() {}

    public SoilSample(String plotId, String district, String soilType,
                      double nitrogen, double phosphorus, double potassium,
                      double ph, double moisture, double organicMatter,
                      double landSizeHectares) {
        this.plotId           = plotId;
        this.district         = district;
        this.soilType         = soilType;
        this.nitrogen         = nitrogen;
        this.phosphorus       = phosphorus;
        this.potassium        = potassium;
        this.ph               = ph;
        this.moisture         = moisture;
        this.organicMatter    = organicMatter;
        this.landSizeHectares = landSizeHectares;
    }

    /**
     * Calculates an overall nutrient health score (0–100).
     * Score reflects how close each nutrient is to its optimal range.
     *
     * @return double health score — higher is healthier
     */
    public double calculateNutrientScore() {
        double nScore  = scoreRange(nitrogen,   MIN_NITROGEN,   MAX_NITROGEN);
        double pScore  = scoreRange(phosphorus, MIN_PHOSPHORUS, MAX_PHOSPHORUS);
        double kScore  = scoreRange(potassium,  MIN_POTASSIUM,  MAX_POTASSIUM);
        double phScore = scoreRange(ph,         MIN_PH,         MAX_PH);
        double mScore  = scoreRange(moisture,   MIN_MOISTURE,   MAX_MOISTURE);
        return (nScore + pScore + kScore + phScore + mScore) / 5.0;
    }

    /** Scores a value relative to its healthy min-max range (0-100) */
    private double scoreRange(double value, double min, double max) {
        if (value >= min && value <= max) return 100.0;
        if (value < min) return Math.max(0, 100 - ((min - value) / min) * 100);
        return Math.max(0, 100 - ((value - max) / max) * 100);
    }

    /**
     * @return true if any nutrient reading indicates a disease or deficiency risk
     */
    public boolean hasDiseaseRisk() {
        return nitrogen   < MIN_NITROGEN * 0.5
            || phosphorus < MIN_PHOSPHORUS * 0.5
            || potassium  < MIN_POTASSIUM  * 0.5
            || ph < 4.5
            || ph > 8.5
            || nitrogen   > MAX_NITROGEN   * 1.5
            || phosphorus > MAX_PHOSPHORUS * 1.5;
    }

    // ── Getters & Setters ─────────────────────────────────
    public String getSampleId()                     { return sampleId; }
    public String getPlotId()                       { return plotId; }
    public void   setPlotId(String p)               { this.plotId = p; }
    public String getDistrict()                     { return district; }
    public void   setDistrict(String d)             { this.district = d; }
    public String getSoilType()                     { return soilType; }
    public void   setSoilType(String s)             { this.soilType = s; }
    public double getNitrogen()                     { return nitrogen; }
    public void   setNitrogen(double n)             { this.nitrogen = n; }
    public double getPhosphorus()                   { return phosphorus; }
    public void   setPhosphorus(double p)           { this.phosphorus = p; }
    public double getPotassium()                    { return potassium; }
    public void   setPotassium(double k)            { this.potassium = k; }
    public double getPh()                           { return ph; }
    public void   setPh(double ph)                  { this.ph = ph; }
    public double getMoisture()                     { return moisture; }
    public void   setMoisture(double m)             { this.moisture = m; }
    public double getOrganicMatter()                { return organicMatter; }
    public void   setOrganicMatter(double o)        { this.organicMatter = o; }
    public double getLandSizeHectares()             { return landSizeHectares; }
    public void   setLandSizeHectares(double l)     { this.landSizeHectares = l; }
    public LocalDateTime getSampleDate()            { return sampleDate; }
    public String getCollectedByEmail()             { return collectedByEmail; }
    public void   setCollectedByEmail(String e)     { this.collectedByEmail = e; }
}
