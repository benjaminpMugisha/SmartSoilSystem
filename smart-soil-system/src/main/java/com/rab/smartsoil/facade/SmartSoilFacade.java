package com.rab.smartsoil.facade;

import com.rab.smartsoil.alerts.AlertObserver;
import com.rab.smartsoil.model.Advisory;
import com.rab.smartsoil.model.DiseaseAlert;
import com.rab.smartsoil.model.SoilSample;
import com.rab.smartsoil.repository.AdvisoryRepository;
import com.rab.smartsoil.repository.AlertRepository;
import com.rab.smartsoil.repository.SoilSampleRepository;
import com.rab.smartsoil.service.AdvisoryEngine;
import com.rab.smartsoil.service.DiseaseDetector;
import com.rab.smartsoil.service.NutrientAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════
 * DESIGN PATTERN: FACADE
 * ═══════════════════════════════════════════════════════════════
 * Intent: Provide a single simplified interface to the complex
 *         subsystems of the Smart Soil platform.
 *
 * Without Facade: FarmerController would need to directly coordinate
 *   SoilController, NutrientAnalyzer, AdvisoryEngine, DiseaseDetector,
 *   DiseaseAlertSystem, SoilSampleRepository, AdvisoryRepository, AlertRepository.
 *   That creates 8 dependencies per controller — violating SRP and DIP.
 *
 * With Facade: FarmerController depends on ONE class — SmartSoilFacade.
 *   All coordination happens here. Controllers stay simple and testable.
 * ═══════════════════════════════════════════════════════════════
 */
@Service
public class SmartSoilFacade {

    private static final Logger log = LoggerFactory.getLogger(SmartSoilFacade.class);

    // ── All subsystem dependencies injected ───────────────
    private final NutrientAnalyzer      nutrientAnalyzer;
    private final AdvisoryEngine        advisoryEngine;
    private final DiseaseDetector       diseaseDetector;
    private final SoilSampleRepository  soilSampleRepository;
    private final AdvisoryRepository    advisoryRepository;
    private final AlertRepository       alertRepository;

    // ── Observer list for disease alert chain (Observer pattern) ──
    private final List<AlertObserver> alertObservers = new ArrayList<>();

    @Autowired
    public SmartSoilFacade(NutrientAnalyzer nutrientAnalyzer,
                           AdvisoryEngine advisoryEngine,
                           DiseaseDetector diseaseDetector,
                           SoilSampleRepository soilSampleRepository,
                           AdvisoryRepository advisoryRepository,
                           AlertRepository alertRepository) {
        this.nutrientAnalyzer     = nutrientAnalyzer;
        this.advisoryEngine       = advisoryEngine;
        this.diseaseDetector      = diseaseDetector;
        this.soilSampleRepository = soilSampleRepository;
        this.advisoryRepository   = advisoryRepository;
        this.alertRepository      = alertRepository;
    }

    // ── Observer management ───────────────────────────────
    public void registerAlertObserver(AlertObserver observer) {
        alertObservers.add(observer);
    }

    // ═════════════════════════════════════════════════════
    // HIGH-LEVEL USE CASE 1:
    // Farmer submits soil data → Full advisory pipeline
    // ═════════════════════════════════════════════════════

    /**
     * Executes the complete soil submission → advisory pipeline.
     * This single method replaces 5+ separate subsystem calls in the controller.
     *
     * Steps executed:
     * 1. Persist the soil sample
     * 2. Analyse nutrients via NutrientAnalyzer
     * 3. Check disease risk via DiseaseDetector
     * 4. Trigger Observer chain if disease detected
     * 5. Generate personalized Advisory via AdvisoryEngine (Strategy pattern)
     * 6. Persist and return the Advisory
     *
     * @param sample        the validated SoilSample submitted by the farmer
     * @param farmerEmail   email of the submitting farmer
     * @param farmerPhone   phone of the submitting farmer
     * @return Advisory the generated crop recommendation
     */
    @Transactional
    public Advisory submitSoilAndGetAdvisory(SoilSample sample,
                                              String farmerEmail,
                                              String farmerPhone) {
        log.info("[FACADE] Processing soil submission for plot: {}", sample.getPlotId());

        // Step 1: Persist soil sample
        sample.setCollectedByEmail(farmerEmail);
        SoilSample savedSample = soilSampleRepository.save(sample);
        log.debug("[FACADE] SoilSample saved. ID: {}", savedSample.getSampleId());

        // Step 2: Analyse nutrients
        double healthScore = nutrientAnalyzer.getOverallHealthScore(savedSample);
        log.debug("[FACADE] Nutrient health score: {}", healthScore);

        // Step 3 & 4: Disease risk detection → Observer notification
        if (diseaseDetector.detectRisk(savedSample)) {
            DiseaseAlert alert = diseaseDetector.createAlert(savedSample, farmerEmail, farmerPhone);
            DiseaseAlert savedAlert = alertRepository.save(alert);
            notifyAlertObservers(savedAlert);
            log.warn("[FACADE] Disease alert created and observers notified. AlertID: {}",
                     savedAlert.getAlertId());
        }

        // Step 5: Generate Advisory via AdvisoryEngine (Strategy Pattern internally)
        Advisory advisory = advisoryEngine.generateAdvisory(savedSample);
        advisory.setGeneratedByEmail(farmerEmail);

        // Step 6: Persist and return advisory
        Advisory savedAdvisory = advisoryRepository.save(advisory);
        log.info("[FACADE] Advisory generated successfully for plot: {}", sample.getPlotId());

        return savedAdvisory;
    }

    // ═════════════════════════════════════════════════════
    // HIGH-LEVEL USE CASE 2:
    // Retrieve all advisories for a given plot
    // ═════════════════════════════════════════════════════

    /**
     * Returns all advisories for a given plot, ordered by date descending.
     *
     * @param plotId the plot identifier
     * @return List of Advisory objects
     */
    public List<Advisory> getAdvisoriesForPlot(String plotId) {
        return advisoryRepository.findByPlotIdOrderByGeneratedDateDesc(plotId);
    }

    // ═════════════════════════════════════════════════════
    // HIGH-LEVEL USE CASE 3:
    // Retrieve disease alerts for a district (Admin view)
    // ═════════════════════════════════════════════════════

    /**
     * Returns all active (untreated) disease alerts for a given district.
     *
     * @param district the district name
     * @return List of active DiseaseAlerts
     */
    public List<DiseaseAlert> getActiveAlertsForDistrict(String district) {
        return alertRepository.findByDistrictAndIsTreatedFalse(district);
    }

    // ═════════════════════════════════════════════════════
    // HIGH-LEVEL USE CASE 4:
    // Get all soil samples for a plot (history)
    // ═════════════════════════════════════════════════════

    /**
     * Returns the full soil sample history for a given plot.
     *
     * @param plotId the plot identifier
     * @return List of SoilSample ordered by date descending
     */
    public List<SoilSample> getSoilHistoryForPlot(String plotId) {
        return soilSampleRepository.findByPlotIdOrderBySampleDateDesc(plotId);
    }

    // ═════════════════════════════════════════════════════
    // HIGH-LEVEL USE CASE 5:
    // Agronomist marks an alert as treated
    // ═════════════════════════════════════════════════════

    /**
     * Marks a disease alert as treated by an agronomist.
     *
     * @param alertId the ID of the alert to mark as treated
     * @return updated DiseaseAlert or throws if not found
     */
    @Transactional
    public DiseaseAlert markAlertAsTreated(String alertId) {
        DiseaseAlert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));
        alert.markAsTreated();
        return alertRepository.save(alert);
    }

    /** Notifies all registered observers about a disease alert */
    private void notifyAlertObservers(DiseaseAlert alert) {
        alertObservers.forEach(observer -> {
            try {
                observer.onAlertReceived(alert);
            } catch (Exception e) {
                log.error("[FACADE] Observer {} failed: {}",
                        observer.getClass().getSimpleName(), e.getMessage());
            }
        });
    }
}
