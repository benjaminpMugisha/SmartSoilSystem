package com.rab.smartsoil.service;

import com.rab.smartsoil.model.SoilSample;
import org.springframework.stereotype.Service;

/**
 * NutrientAnalyzer — analyses soil nutrient readings against RAB thresholds.
 *
 * Single Responsibility: ONLY analyses nutrients — no report generation,
 * no advisory logic, no persistence concerns.
 *
 * All threshold values are named constants (Google Java Style — no magic numbers).
 */
@Service
public class NutrientAnalyzer {

    // ── Nutrient Status Constants ──────────────────────────
    public static final String DEFICIENT = "DEFICIENT";
    public static final String OPTIMAL   = "OPTIMAL";
    public static final String TOXIC     = "TOXIC";

    /**
     * Analyses nitrogen level against RAB standards.
     *
     * @param nitrogenMgKg nitrogen reading in mg/kg
     * @return status string: DEFICIENT, OPTIMAL, or TOXIC
     * @throws IllegalArgumentException if reading is negative
     */
    public String analyzeNitrogen(double nitrogenMgKg) {
        validateReading("Nitrogen", nitrogenMgKg);
        if (nitrogenMgKg < SoilSample.MIN_NITROGEN) return DEFICIENT;
        if (nitrogenMgKg > SoilSample.MAX_NITROGEN) return TOXIC;
        return OPTIMAL;
    }

    /**
     * Analyses phosphorus level against RAB standards.
     *
     * @param phosphorusMgKg phosphorus reading in mg/kg
     * @return status string: DEFICIENT, OPTIMAL, or TOXIC
     */
    public String analyzePhosphorus(double phosphorusMgKg) {
        validateReading("Phosphorus", phosphorusMgKg);
        if (phosphorusMgKg < SoilSample.MIN_PHOSPHORUS) return DEFICIENT;
        if (phosphorusMgKg > SoilSample.MAX_PHOSPHORUS) return TOXIC;
        return OPTIMAL;
    }

    /**
     * Analyses potassium level against RAB standards.
     *
     * @param potassiumMgKg potassium reading in mg/kg
     * @return status string: DEFICIENT, OPTIMAL, or TOXIC
     */
    public String analyzePotassium(double potassiumMgKg) {
        validateReading("Potassium", potassiumMgKg);
        if (potassiumMgKg < SoilSample.MIN_POTASSIUM) return DEFICIENT;
        if (potassiumMgKg > SoilSample.MAX_POTASSIUM) return TOXIC;
        return OPTIMAL;
    }

    /**
     * Analyses soil pH level.
     *
     * @param ph soil pH value (0–14 scale)
     * @return status string: ACIDIC, OPTIMAL, or ALKALINE
     */
    public String analyzePH(double ph) {
        if (ph < 0 || ph > 14)
            throw new IllegalArgumentException("pH must be between 0 and 14, got: " + ph);
        if (ph < SoilSample.MIN_PH) return "ACIDIC";
        if (ph > SoilSample.MAX_PH) return "ALKALINE";
        return OPTIMAL;
    }

    /**
     * Computes the overall soil health score (0–100) for a complete sample.
     * Delegates to SoilSample.calculateNutrientScore() which uses weighted ranges.
     *
     * @param sample the complete SoilSample entity
     * @return double health score (0 = critically unhealthy, 100 = perfect)
     */
    public double getOverallHealthScore(SoilSample sample) {
        return sample.calculateNutrientScore();
    }

    /**
     * Generates a human-readable soil health summary for farmer dashboards.
     *
     * @param sample the complete SoilSample entity
     * @return multi-line summary string
     */
    public String generateHealthSummary(SoilSample sample) {
        double score = getOverallHealthScore(sample);
        StringBuilder sb = new StringBuilder();

        sb.append("Plot: ").append(sample.getPlotId())
          .append(" | District: ").append(sample.getDistrict())
          .append(" | Soil Type: ").append(sample.getSoilType()).append("\n");

        sb.append("Nitrogen  : ").append(analyzeNitrogen(sample.getNitrogen()))
          .append(" (").append(sample.getNitrogen()).append(" mg/kg)\n");
        sb.append("Phosphorus: ").append(analyzePhosphorus(sample.getPhosphorus()))
          .append(" (").append(sample.getPhosphorus()).append(" mg/kg)\n");
        sb.append("Potassium : ").append(analyzePotassium(sample.getPotassium()))
          .append(" (").append(sample.getPotassium()).append(" mg/kg)\n");
        sb.append("pH        : ").append(analyzePH(sample.getPh()))
          .append(" (").append(sample.getPh()).append(")\n");
        sb.append("Moisture  : ").append(sample.getMoisture()).append("%\n");
        sb.append("Overall Health Score: ").append(String.format("%.1f", score)).append("/100\n");
        sb.append("Risk Level: ").append(classifyRisk(score));

        return sb.toString();
    }

    /**
     * Classifies risk level based on overall health score.
     *
     * @param score the nutrient health score (0–100)
     * @return risk level string: LOW, MEDIUM, or HIGH
     */
    public String classifyRisk(double score) {
        if (score >= 70) return "LOW";
        if (score >= 40) return "MEDIUM";
        return "HIGH";
    }

    /** Validates that a nutrient reading is not negative */
    private void validateReading(String nutrientName, double value) {
        if (value < 0) {
            throw new IllegalArgumentException(
                nutrientName + " reading cannot be negative. Received: " + value);
        }
    }
}
