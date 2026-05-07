package com.rab.smartsoil.controller;

import com.rab.smartsoil.dto.*;
import com.rab.smartsoil.facade.SmartSoilFacade;
import com.rab.smartsoil.model.*;
import com.rab.smartsoil.repository.*;
import com.rab.smartsoil.service.NutrientAnalyzer;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controllers — expose SmartSoilFacade as HTTP endpoints.
 *
 * FACADE PATTERN benefit: each controller method is a thin
 * delegation to one Facade method — controllers never touch
 * repositories or service internals directly.
 */

// ════════════════════════════════════════════════════════════
// SOIL CONTROLLER — core soil submission and analysis
// ════════════════════════════════════════════════════════════
@RestController
@RequestMapping("/api/soil")
public class SoilController {

    private final SmartSoilFacade facade;
    private final UserRepository  userRepository;

    @Autowired
    SoilController(SmartSoilFacade facade, UserRepository userRepository) {
        this.facade         = facade;
        this.userRepository = userRepository;
    }

    /**
     * POST /api/soil/submit
     * Farmer submits soil data — triggers full analysis pipeline via Facade.
     * Returns personalized Advisory in response.
     */
    @PostMapping("/submit")
    public ResponseEntity<Advisory> submitSoilData(
            @Valid @RequestBody SoilSubmissionRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        SoilSample sample = new SoilSample(
            req.getPlotId(), req.getDistrict(), req.getSoilType(),
            req.getNitrogen(), req.getPhosphorus(), req.getPotassium(),
            req.getPh(), req.getMoisture(), req.getOrganicMatter(),
            req.getLandSizeHectares()
        );

        Advisory advisory = facade.submitSoilAndGetAdvisory(
            sample, user.getEmail(), user.getPhone()
        );

        return ResponseEntity.ok(advisory);
    }

    /**
     * GET /api/soil/history/{plotId}
     * Returns all soil samples submitted for a given plot.
     */
    @GetMapping("/history/{plotId}")
    public ResponseEntity<List<SoilSample>> getSoilHistory(@PathVariable String plotId) {
        return ResponseEntity.ok(facade.getSoilHistoryForPlot(plotId));
    }
}


// ════════════════════════════════════════════════════════════
// ADVISORY CONTROLLER — retrieve crop recommendations
// ════════════════════════════════════════════════════════════
@RestController
@RequestMapping("/api/advisory")
public class AdvisoryController {

    private final SmartSoilFacade facade;

    @Autowired
    AdvisoryController(SmartSoilFacade facade) {
        this.facade = facade;
    }

    /**
     * GET /api/advisory/{plotId}
     * Returns all advisories for a plot, newest first.
     */
    @GetMapping("/{plotId}")
    public ResponseEntity<List<Advisory>> getAdvisories(@PathVariable String plotId) {
        List<Advisory> advisories = facade.getAdvisoriesForPlot(plotId);
        if (advisories.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(advisories);
    }
}


// ════════════════════════════════════════════════════════════
// ALERT CONTROLLER — disease alerts management
// ════════════════════════════════════════════════════════════
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final SmartSoilFacade facade;

    @Autowired
    AlertController(SmartSoilFacade facade) {
        this.facade = facade;
    }

    /**
     * GET /api/alerts/district/{district}
     * Returns all untreated disease alerts for a district (Admin/Agronomist view).
     */
    @GetMapping("/district/{district}")
    public ResponseEntity<List<DiseaseAlert>> getDistrictAlerts(@PathVariable String district) {
        return ResponseEntity.ok(facade.getActiveAlertsForDistrict(district));
    }

    /**
     * PATCH /api/alerts/{alertId}/treat
     * Agronomist marks an alert as treated after field intervention.
     */
    @PatchMapping("/{alertId}/treat")
    public ResponseEntity<DiseaseAlert> markAsTreated(@PathVariable String alertId) {
        return ResponseEntity.ok(facade.markAlertAsTreated(alertId));
    }
}


// ════════════════════════════════════════════════════════════
// HEALTH CONTROLLER — system status
// ════════════════════════════════════════════════════════════
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * GET /api/health
     * Returns system health status — used by Docker HEALTHCHECK.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status",  "UP",
            "system",  "Smart Soil Nutrient Monitoring & Crop Advisory System",
            "version", "1.0.0",
            "author",  "Benjamin Mugisha Prince | 26979 | AUCA SENG 8240"
        ));
    }

    /**
     * GET /api/demo
     * Demonstrates the system pipeline without authentication (for prototype demo).
     */
    @GetMapping("/demo")
    public ResponseEntity<Map<String, Object>> demo() {
        NutrientAnalyzer analyzer = new NutrientAnalyzer();
        SoilSample demo = new SoilSample(
            "DEMO-PLT-001", "Musanze", "VOLCANIC",
            25.0, 10.0, 90.0, 5.0, 35.0, 2.5, 1.5
        );
        double score  = analyzer.getOverallHealthScore(demo);
        String risk   = analyzer.classifyRisk(score);
        String summary = analyzer.generateHealthSummary(demo);
        return ResponseEntity.ok(Map.of(
            "plotId",        demo.getPlotId(),
            "district",      demo.getDistrict(),
            "soilType",      demo.getSoilType(),
            "nutrientScore", String.format("%.1f / 100", score),
            "riskLevel",     risk,
            "hasDiseaseRisk", demo.hasDiseaseRisk(),
            "summary",       summary
        ));
    }
}
