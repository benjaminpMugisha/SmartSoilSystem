package com.rab.smartsoil.service;

import com.rab.smartsoil.model.SoilSample;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * NutrientAnalyzerTest — Unit tests for NutrientAnalyzer service.
 *
 * Tests cover:
 *  - Boundary value analysis for N, P, K, pH
 *  - Invalid input handling
 *  - Health score calculation
 *  - Risk classification
 *
 * AUCA SENG 8240 — Phase 4: Software Test Plan
 * Student: Benjamin Mugisha Prince | 26979
 */
@DisplayName("NutrientAnalyzer — Unit Test Suite")
class NutrientAnalyzerTest {

    private NutrientAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new NutrientAnalyzer();
    }

    // ── Nitrogen Tests ────────────────────────────────────

    @Test
    @DisplayName("TC-N01: Nitrogen BELOW threshold (25 mg/kg) → DEFICIENT")
    void nitrogen_belowThreshold_returnsDeficient() {
        assertEquals(NutrientAnalyzer.DEFICIENT, analyzer.analyzeNitrogen(25.0));
    }

    @Test
    @DisplayName("TC-N02: Nitrogen AT minimum threshold (30 mg/kg) → OPTIMAL")
    void nitrogen_atMinThreshold_returnsOptimal() {
        assertEquals(NutrientAnalyzer.OPTIMAL, analyzer.analyzeNitrogen(30.0));
    }

    @Test
    @DisplayName("TC-N03: Nitrogen IN optimal range (90 mg/kg) → OPTIMAL")
    void nitrogen_inOptimalRange_returnsOptimal() {
        assertEquals(NutrientAnalyzer.OPTIMAL, analyzer.analyzeNitrogen(90.0));
    }

    @Test
    @DisplayName("TC-N04: Nitrogen AT maximum threshold (150 mg/kg) → OPTIMAL")
    void nitrogen_atMaxThreshold_returnsOptimal() {
        assertEquals(NutrientAnalyzer.OPTIMAL, analyzer.analyzeNitrogen(150.0));
    }

    @Test
    @DisplayName("TC-N05: Nitrogen ABOVE threshold (180 mg/kg) → TOXIC")
    void nitrogen_aboveThreshold_returnsToxic() {
        assertEquals(NutrientAnalyzer.TOXIC, analyzer.analyzeNitrogen(180.0));
    }

    @Test
    @DisplayName("TC-N06: Negative nitrogen → IllegalArgumentException")
    void nitrogen_negative_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> analyzer.analyzeNitrogen(-5.0),
            "Negative nitrogen should throw IllegalArgumentException");
    }

    // ── Phosphorus Tests ──────────────────────────────────

    @Test
    @DisplayName("TC-P01: Phosphorus DEFICIENT (10 mg/kg) → DEFICIENT")
    void phosphorus_deficient() {
        assertEquals(NutrientAnalyzer.DEFICIENT, analyzer.analyzePhosphorus(10.0));
    }

    @Test
    @DisplayName("TC-P02: Phosphorus OPTIMAL (40 mg/kg) → OPTIMAL")
    void phosphorus_optimal() {
        assertEquals(NutrientAnalyzer.OPTIMAL, analyzer.analyzePhosphorus(40.0));
    }

    @Test
    @DisplayName("TC-P03: Phosphorus TOXIC (100 mg/kg) → TOXIC")
    void phosphorus_toxic() {
        assertEquals(NutrientAnalyzer.TOXIC, analyzer.analyzePhosphorus(100.0));
    }

    // ── pH Tests ──────────────────────────────────────────

    @Test
    @DisplayName("TC-pH01: pH 4.2 (below 5.5) → ACIDIC")
    void ph_acidic() {
        assertEquals("ACIDIC", analyzer.analyzePH(4.2));
    }

    @Test
    @DisplayName("TC-pH02: pH 6.5 (5.5–7.5) → OPTIMAL")
    void ph_optimal() {
        assertEquals(NutrientAnalyzer.OPTIMAL, analyzer.analyzePH(6.5));
    }

    @Test
    @DisplayName("TC-pH03: pH 8.2 (above 7.5) → ALKALINE")
    void ph_alkaline() {
        assertEquals("ALKALINE", analyzer.analyzePH(8.2));
    }

    @Test
    @DisplayName("TC-pH04: pH 15 (invalid) → IllegalArgumentException")
    void ph_outOfRange_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> analyzer.analyzePH(15.0));
    }

    // ── Parameterized Tests ───────────────────────────────

    @ParameterizedTest(name = "Score {0} → Risk {1}")
    @CsvSource({
        "85.0, LOW",
        "70.0, LOW",
        "60.0, MEDIUM",
        "40.0, MEDIUM",
        "30.0, HIGH",
        "0.0,  HIGH"
    })
    @DisplayName("TC-RISK: Risk classification based on health score")
    void riskClassification_matchesExpected(double score, String expectedRisk) {
        assertEquals(expectedRisk.trim(), analyzer.classifyRisk(score));
    }

    // ── Health Score Tests ────────────────────────────────

    @Test
    @DisplayName("TC-HS01: Fully optimal soil sample → score >= 90")
    void healthScore_optimalSample_highScore() {
        SoilSample sample = new SoilSample(
            "PLT-001", "Musanze", "VOLCANIC",
            80.0, 40.0, 200.0, 6.5, 50.0, 3.0, 1.5
        );
        double score = analyzer.getOverallHealthScore(sample);
        assertTrue(score >= 90.0,
            "Optimal soil sample should score >= 90, got: " + score);
    }

    @Test
    @DisplayName("TC-HS02: Critically deficient sample → score <= 40")
    void healthScore_criticalSample_lowScore() {
        SoilSample sample = new SoilSample(
            "PLT-002", "Bugesera", "LATERITE",
            5.0, 2.0, 10.0, 4.0, 10.0, 0.5, 1.0
        );
        double score = analyzer.getOverallHealthScore(sample);
        assertTrue(score <= 40.0,
            "Critically deficient sample should score <= 40, got: " + score);
    }

    @Test
    @DisplayName("TC-HS03: Health summary for Musanze plot contains expected fields")
    void healthSummary_containsAllFields() {
        SoilSample sample = new SoilSample(
            "PLT-003", "Musanze", "VOLCANIC",
            25.0, 10.0, 80.0, 5.0, 30.0, 2.0, 1.0
        );
        String summary = analyzer.generateHealthSummary(sample);
        assertAll("Health summary fields",
            () -> assertTrue(summary.contains("PLT-003"), "Should contain plot ID"),
            () -> assertTrue(summary.contains("Musanze"), "Should contain district"),
            () -> assertTrue(summary.contains("VOLCANIC"), "Should contain soil type"),
            () -> assertTrue(summary.contains("Nitrogen"), "Should contain nitrogen status"),
            () -> assertTrue(summary.contains("Overall Health Score"), "Should contain score")
        );
    }
}
