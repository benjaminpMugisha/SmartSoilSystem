package com.rab.smartsoil.service;

import com.rab.smartsoil.model.DiseaseAlert;
import com.rab.smartsoil.model.SoilSample;
import org.springframework.stereotype.Service;

/**
 * DiseaseDetector — analyses soil samples for disease and deficiency risks.
 * Creates DiseaseAlert objects that trigger the Observer pattern chain.
 *
 * Single Responsibility: ONLY detects disease risks — does not send
 * notifications (that is the job of DiseaseAlertSystem + Observers).
 */
@Service
public class DiseaseDetector {

    /**
     * Determines if a soil sample indicates a disease or critical deficiency risk.
     *
     * @param sample the soil sample to evaluate
     * @return true if any threshold indicates a significant risk
     */
    public boolean detectRisk(SoilSample sample) {
        return sample.hasDiseaseRisk();
    }

    /**
     * Creates a DiseaseAlert for a risky soil sample.
     * Diagnoses the most likely disease type based on the nutrient profile.
     *
     * @param sample      the soil sample with abnormal readings
     * @param farmerEmail email of the plot owner
     * @param farmerPhone phone of the plot owner
     * @return DiseaseAlert with disease type, severity, and prevention measures
     */
    public DiseaseAlert createAlert(SoilSample sample, String farmerEmail, String farmerPhone) {
        String diseaseType         = diagnoseDiseaseType(sample);
        DiseaseAlert.Severity sev  = classifySeverity(sample);
        String prevention          = buildPreventionMeasures(sample, diseaseType);

        return new DiseaseAlert(
            sample.getPlotId(),
            farmerEmail,
            farmerPhone,
            sample.getDistrict(),
            diseaseType,
            sev,
            prevention
        );
    }

    /** Diagnoses the most likely disease/deficiency based on nutrient pattern */
    private String diagnoseDiseaseType(SoilSample sample) {
        if (sample.getPh() < 4.5)  return "Soil Acidification — Root Damage Risk";
        if (sample.getPh() > 8.5)  return "Alkalinity Stress — Nutrient Lockout";
        if (sample.getNitrogen()   < SoilSample.MIN_NITROGEN * 0.5)
            return "Severe Nitrogen Deficiency — Stunting / Chlorosis";
        if (sample.getPhosphorus() < SoilSample.MIN_PHOSPHORUS * 0.5)
            return "Phosphorus Deficiency — Root Rot Risk";
        if (sample.getPotassium()  < SoilSample.MIN_POTASSIUM * 0.5)
            return "Potassium Deficiency — Leaf Scorch / Blight Risk";
        if (sample.getNitrogen()   > SoilSample.MAX_NITROGEN * 1.5)
            return "Nitrogen Toxicity — Leaf Burn / Runoff Risk";
        return "Multiple Nutrient Imbalance — General Crop Stress";
    }

    /** Classifies severity based on how far readings deviate from safe range */
    private DiseaseAlert.Severity classifySeverity(SoilSample sample) {
        double score = sample.calculateNutrientScore();
        if (score < 25) return DiseaseAlert.Severity.HIGH;
        if (score < 50) return DiseaseAlert.Severity.MEDIUM;
        return DiseaseAlert.Severity.LOW;
    }

    /** Generates prevention measures tailored to the detected disease type */
    private String buildPreventionMeasures(SoilSample sample, String diseaseType) {
        if (diseaseType.contains("Acidification"))
            return "Apply 2-3 tonnes/ha of agricultural lime. Retest soil pH after 3 weeks.";
        if (diseaseType.contains("Alkalinity"))
            return "Apply sulphur or gypsum to lower pH. Irrigate thoroughly.";
        if (diseaseType.contains("Nitrogen"))
            return "Apply Urea 46% at 100kg/ha. Avoid waterlogging.";
        if (diseaseType.contains("Phosphorus"))
            return "Apply DAP or TSP at 80kg/ha. Improve drainage.";
        if (diseaseType.contains("Potassium"))
            return "Apply MOP (Muriate of Potash) at 60kg/ha. Check for fungal infection.";
        if (diseaseType.contains("Toxicity"))
            return "Stop all nitrogen application. Apply heavy irrigation to leach excess.";
        return "Consult your assigned RAB agronomist for a field visit within 48 hours.";
    }
}
