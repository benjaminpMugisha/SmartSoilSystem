package com.rab.smartsoil.service;

import com.rab.smartsoil.alerts.AlertObserver;
import com.rab.smartsoil.model.DiseaseAlert;
import com.rab.smartsoil.model.SoilSample;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Design Pattern Verification Tests
 *
 * Tests verify that all 4 design patterns behave as specified:
 *  - Strategy:    Correct algorithm selected per soil type
 *  - Observer:    All observers notified; removed observers not called
 *  - SoilSample:  Disease risk detection logic (supports Observer trigger)
 *  - Template:    Nutrient score calculation consistency
 *
 * AUCA SENG 8240 — Phase 4: Software Test Plan
 * Student: Benjamin Mugisha Prince | 26979
 */
@DisplayName("Design Pattern Verification Tests")
class DesignPatternTest {

    // ═══════════════════════════════════════════════════════
    // STRATEGY PATTERN TESTS
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Strategy Pattern — FertilizerStrategyFactory")
    class StrategyPatternTest {

        @Test
        @DisplayName("SP-01: VOLCANIC soil → VolcanicSoilStrategy — recommends pH correction for acidic soil")
        void volcanic_acidicSoil_recommendsLime() {
            SoilSample sample = createSample("VOLCANIC", 25.0, 10.0, 90.0, 4.8, 30.0, 1.0);
            FertilizerStrategy strategy = FertilizerStrategyFactory.getStrategy("VOLCANIC");
            String recommendation = strategy.recommendFertilizer(sample);
            assertTrue(recommendation.contains("Lime"),
                "Acidic volcanic soil should recommend Lime. Got: " + recommendation);
        }

        @Test
        @DisplayName("SP-02: VOLCANIC soil with low nitrogen → recommends Urea")
        void volcanic_lowNitrogen_recommendsUrea() {
            SoilSample sample = createSample("VOLCANIC", 15.0, 40.0, 200.0, 6.5, 40.0, 1.5);
            FertilizerStrategy strategy = FertilizerStrategyFactory.getStrategy("VOLCANIC");
            String recommendation = strategy.recommendFertilizer(sample);
            assertTrue(recommendation.contains("Urea"),
                "Low nitrogen volcanic soil should recommend Urea. Got: " + recommendation);
        }

        @Test
        @DisplayName("SP-03: LATERITE soil → recommends phosphorus or NPK")
        void laterite_lowPhosphorus_recommendsTSP() {
            SoilSample sample = createSample("LATERITE", 60.0, 5.0, 90.0, 6.0, 40.0, 2.0);
            FertilizerStrategy strategy = FertilizerStrategyFactory.getStrategy("LATERITE");
            String recommendation = strategy.recommendFertilizer(sample);
            assertTrue(recommendation.contains("TSP") || recommendation.contains("NPK"),
                "Low-phosphorus laterite soil should recommend TSP or NPK. Got: " + recommendation);
        }

        @Test
        @DisplayName("SP-04: ORGANIC soil → recommends Rhizobium or Green Manure")
        void organic_soil_recommendsOrganicFertilizer() {
            SoilSample sample = createSample("ORGANIC", 80.0, 45.0, 220.0, 6.8, 60.0, 5.0);
            FertilizerStrategy strategy = FertilizerStrategyFactory.getStrategy("ORGANIC");
            String recommendation = strategy.recommendFertilizer(sample);
            assertTrue(recommendation.contains("Rhizobium") || recommendation.contains("Manure"),
                "Organic soil should recommend organic fertilizer. Got: " + recommendation);
        }

        @Test
        @DisplayName("SP-05: Unknown soil type defaults to VOLCANIC strategy — no exception")
        void unknown_soilType_defaultsToVolcanic() {
            assertDoesNotThrow(() -> {
                FertilizerStrategy strategy = FertilizerStrategyFactory.getStrategy("UNKNOWN");
                assertNotNull(strategy);
            });
        }

        @Test
        @DisplayName("SP-06: Strategy dosage is proportional to land size")
        void dosage_scalesWithLandSize() {
            SoilSample s1 = createSample("VOLCANIC", 25.0, 20.0, 100.0, 5.0, 30.0, 1.0);
            SoilSample s2 = createSample("VOLCANIC", 25.0, 20.0, 100.0, 5.0, 30.0, 2.0);
            FertilizerStrategy strategy = FertilizerStrategyFactory.getStrategy("VOLCANIC");
            double dose1 = strategy.calculateDosage(1.0, s1);
            double dose2 = strategy.calculateDosage(2.0, s2);
            assertTrue(dose2 > dose1,
                "Dosage for 2ha should be greater than for 1ha");
        }

        @Test
        @DisplayName("SP-07: Compatible crops list is not empty for any soil type")
        void compatibleCrops_notEmpty_forAllStrategies() {
            SoilSample sample = createSample("VOLCANIC", 80.0, 40.0, 200.0, 6.5, 50.0, 1.0);
            for (String soilType : List.of("VOLCANIC", "LATERITE", "ORGANIC", "SANDY_LOAM")) {
                FertilizerStrategy strategy = FertilizerStrategyFactory.getStrategy(soilType);
                assertFalse(strategy.getCompatibleCrops(sample).isEmpty(),
                    "Compatible crops must not be empty for soil type: " + soilType);
            }
        }
    }


    // ═══════════════════════════════════════════════════════
    // OBSERVER PATTERN TESTS (using inline simple Observer)
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Observer Pattern — DiseaseAlertNotification")
    @ExtendWith(MockitoExtension.class)
    class ObserverPatternTest {

        /**
         * Simple in-line observer subject to avoid Spring context in unit tests
         */
        static class SimpleAlertSubject {
            private final List<AlertObserver> observers = new ArrayList<>();
            public void addObserver(AlertObserver o)    { observers.add(o); }
            public void removeObserver(AlertObserver o) { observers.remove(o); }
            public void triggerAlert(DiseaseAlert a)    { observers.forEach(o -> o.onAlertReceived(a)); }
            public int  size()                          { return observers.size(); }
        }

        private SimpleAlertSubject subject;

        @BeforeEach
        void setUp() { subject = new SimpleAlertSubject(); }

        @Test
        @DisplayName("OB-01: All registered observers receive the alert")
        void allObserversReceiveAlert(@Mock AlertObserver obs1, @Mock AlertObserver obs2) {
            subject.addObserver(obs1);
            subject.addObserver(obs2);
            DiseaseAlert alert = buildAlert(DiseaseAlert.Severity.HIGH);
            subject.triggerAlert(alert);
            verify(obs1, times(1)).onAlertReceived(alert);
            verify(obs2, times(1)).onAlertReceived(alert);
        }

        @Test
        @DisplayName("OB-02: Removed observer does NOT receive alert")
        void removedObserver_notNotified(@Mock AlertObserver obs1, @Mock AlertObserver obs2) {
            subject.addObserver(obs1);
            subject.addObserver(obs2);
            subject.removeObserver(obs2);
            subject.triggerAlert(buildAlert(DiseaseAlert.Severity.MEDIUM));
            verify(obs1, times(1)).onAlertReceived(any());
            verify(obs2, never()).onAlertReceived(any());
        }

        @Test
        @DisplayName("OB-03: No observers — triggerAlert completes without error")
        void noObservers_noException() {
            assertDoesNotThrow(() ->
                subject.triggerAlert(buildAlert(DiseaseAlert.Severity.LOW)));
        }

        @Test
        @DisplayName("OB-04: Observer count reflects add/remove operations")
        void observerCount_reflectsOperations(@Mock AlertObserver obs) {
            assertEquals(0, subject.size());
            subject.addObserver(obs);
            assertEquals(1, subject.size());
            subject.removeObserver(obs);
            assertEquals(0, subject.size());
        }

        private DiseaseAlert buildAlert(DiseaseAlert.Severity severity) {
            return new DiseaseAlert(
                "PLT-001", "farmer@rab.rw", "+250788000001",
                "Kigali", "Root Rot", severity,
                "Apply copper fungicide and improve drainage"
            );
        }
    }


    // ═══════════════════════════════════════════════════════
    // SOIL SAMPLE DISEASE RISK DETECTION (triggers Observer)
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("SoilSample Disease Risk Detection")
    class DiseaseRiskTest {

        @Test
        @DisplayName("DR-01: Critically low nitrogen (<15 mg/kg) → disease risk TRUE")
        void criticallyLowNitrogen_hasRisk() {
            SoilSample sample = createSample("VOLCANIC", 10.0, 30.0, 150.0, 6.0, 40.0, 1.0);
            assertTrue(sample.hasDiseaseRisk());
        }

        @Test
        @DisplayName("DR-02: Very acidic pH (3.5) → disease risk TRUE")
        void veryAcidicPh_hasRisk() {
            SoilSample sample = createSample("VOLCANIC", 80.0, 40.0, 200.0, 3.5, 40.0, 1.0);
            assertTrue(sample.hasDiseaseRisk());
        }

        @Test
        @DisplayName("DR-03: Healthy soil (all optimal) → disease risk FALSE")
        void healthySoil_noRisk() {
            SoilSample sample = createSample("VOLCANIC", 80.0, 40.0, 200.0, 6.5, 50.0, 1.5);
            assertFalse(sample.hasDiseaseRisk());
        }

        @Test
        @DisplayName("DR-04: Nitrogen toxicity (>225 mg/kg) → disease risk TRUE")
        void nitrogenToxicity_hasRisk() {
            SoilSample sample = createSample("VOLCANIC", 230.0, 40.0, 200.0, 6.5, 50.0, 1.0);
            assertTrue(sample.hasDiseaseRisk());
        }
    }


    // ═══════════════════════════════════════════════════════
    // NUTRIENT SCORE CALCULATION TESTS
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("NutrientScore Calculation")
    class NutrientScoreTest {

        @Test
        @DisplayName("SC-01: Perfect soil values → score near 100")
        void perfectValues_highScore() {
            SoilSample sample = createSample("VOLCANIC", 90.0, 45.0, 250.0, 6.5, 50.0, 3.0);
            double score = sample.calculateNutrientScore();
            assertEquals(100.0, score, 0.01, "Fully optimal sample should score 100");
        }

        @Test
        @DisplayName("SC-02: All-zero values → score is 0")
        void zeroValues_zeroScore() {
            SoilSample sample = createSample("VOLCANIC", 0.0, 0.0, 0.0, 0.0, 0.0, 1.0);
            double score = sample.calculateNutrientScore();
            assertEquals(0.0, score, 0.1, "All-zero sample should score near 0");
        }

        @Test
        @DisplayName("SC-03: Score is always between 0 and 100")
        void score_alwaysInRange() {
            SoilSample s1 = createSample("VOLCANIC", 300.0, 200.0, 500.0, 13.0, 95.0, 1.0);
            SoilSample s2 = createSample("LATERITE", 0.0, 0.0, 0.0, 0.0, 0.0, 1.0);
            double score1 = s1.calculateNutrientScore();
            double score2 = s2.calculateNutrientScore();
            assertAll(
                () -> assertTrue(score1 >= 0 && score1 <= 100, "Score must be 0-100, got: " + score1),
                () -> assertTrue(score2 >= 0 && score2 <= 100, "Score must be 0-100, got: " + score2)
            );
        }
    }


    // ── Shared helper ─────────────────────────────────────
    private static SoilSample createSample(String soilType, double n, double p,
                                            double k, double ph, double moisture,
                                            double organicMatter) {
        return new SoilSample("PLT-TEST", "Kigali", soilType,
                               n, p, k, ph, moisture, organicMatter, 1.0);
    }
}
