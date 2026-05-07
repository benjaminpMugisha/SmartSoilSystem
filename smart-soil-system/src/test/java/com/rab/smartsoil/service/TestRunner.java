package com.rab.smartsoil.test;

import com.rab.smartsoil.model.SoilSample;
import com.rab.smartsoil.model.DiseaseAlert;
import com.rab.smartsoil.service.NutrientAnalyzer;
import com.rab.smartsoil.service.FertilizerStrategy;
import com.rab.smartsoil.service.FertilizerStrategyFactory;
import com.rab.smartsoil.alerts.AlertObserver;
import java.util.*;

/**
 * TestRunner — Manual test suite for Smart Soil System core logic.
 * Verifies all design patterns and business rules without Spring context.
 * AUCA SENG 8240 | Benjamin Mugisha Prince | 26979
 */
public class TestRunner {

    static int passed = 0, failed = 0;

    public static void main(String[] args) {
        System.out.println("═".repeat(65));
        System.out.println("  SMART SOIL SYSTEM — TEST SUITE");
        System.out.println("  AUCA SENG 8240 | Benjamin Mugisha Prince | 26979");
        System.out.println("═".repeat(65));

        testNutrientAnalyzer();
        testStrategyPattern();
        testObserverPattern();
        testDiseaseRiskDetection();
        testNutrientScore();

        System.out.println("\n" + "═".repeat(65));
        System.out.printf("  RESULTS: %d PASSED  |  %d FAILED  |  %d TOTAL%n",
                          passed, failed, passed + failed);
        System.out.println("═".repeat(65));
        if (failed > 0) System.exit(1);
    }

    // ─────────────────────────────────────────────────────
    static void testNutrientAnalyzer() {
        section("NUTRIENT ANALYZER — Boundary Value Tests");
        NutrientAnalyzer a = new NutrientAnalyzer();

        assertEq("TC-N01: Nitrogen 25 (below 30)  → DEFICIENT",
            NutrientAnalyzer.DEFICIENT, a.analyzeNitrogen(25.0));
        assertEq("TC-N02: Nitrogen 30 (at min)    → OPTIMAL",
            NutrientAnalyzer.OPTIMAL,   a.analyzeNitrogen(30.0));
        assertEq("TC-N03: Nitrogen 90 (mid range) → OPTIMAL",
            NutrientAnalyzer.OPTIMAL,   a.analyzeNitrogen(90.0));
        assertEq("TC-N04: Nitrogen 150 (at max)   → OPTIMAL",
            NutrientAnalyzer.OPTIMAL,   a.analyzeNitrogen(150.0));
        assertEq("TC-N05: Nitrogen 180 (above max)→ TOXIC",
            NutrientAnalyzer.TOXIC,     a.analyzeNitrogen(180.0));

        assertThrows("TC-N06: Nitrogen -5 → IllegalArgumentException",
            () -> a.analyzeNitrogen(-5.0));

        assertEq("TC-P01: Phosphorus 10 → DEFICIENT",
            NutrientAnalyzer.DEFICIENT, a.analyzePhosphorus(10.0));
        assertEq("TC-P02: Phosphorus 40 → OPTIMAL",
            NutrientAnalyzer.OPTIMAL,   a.analyzePhosphorus(40.0));
        assertEq("TC-P03: Phosphorus 100→ TOXIC",
            NutrientAnalyzer.TOXIC,     a.analyzePhosphorus(100.0));

        assertEq("TC-K01: Potassium 50  → DEFICIENT",
            NutrientAnalyzer.DEFICIENT, a.analyzePotassium(50.0));
        assertEq("TC-K02: Potassium 250 → OPTIMAL",
            NutrientAnalyzer.OPTIMAL,   a.analyzePotassium(250.0));
        assertEq("TC-K03: Potassium 500 → TOXIC",
            NutrientAnalyzer.TOXIC,     a.analyzePotassium(500.0));

        assertEq("TC-pH1: pH 4.2 (< 5.5) → ACIDIC",
            "ACIDIC",                   a.analyzePH(4.2));
        assertEq("TC-pH2: pH 6.5 (5.5-7.5)→ OPTIMAL",
            NutrientAnalyzer.OPTIMAL,   a.analyzePH(6.5));
        assertEq("TC-pH3: pH 8.2 (> 7.5) → ALKALINE",
            "ALKALINE",                 a.analyzePH(8.2));

        assertThrows("TC-pH4: pH 15 (invalid) → IllegalArgumentException",
            () -> a.analyzePH(15.0));

        // Risk classification
        assertEq("TC-R01: Score 85 → LOW risk",    "LOW",    a.classifyRisk(85.0));
        assertEq("TC-R02: Score 70 → LOW risk",    "LOW",    a.classifyRisk(70.0));
        assertEq("TC-R03: Score 55 → MEDIUM risk", "MEDIUM", a.classifyRisk(55.0));
        assertEq("TC-R04: Score 40 → MEDIUM risk", "MEDIUM", a.classifyRisk(40.0));
        assertEq("TC-R05: Score 25 → HIGH risk",   "HIGH",   a.classifyRisk(25.0));
        assertEq("TC-R06: Score 0  → HIGH risk",   "HIGH",   a.classifyRisk(0.0));
    }

    // ─────────────────────────────────────────────────────
    static void testStrategyPattern() {
        section("STRATEGY PATTERN — FertilizerStrategy per Soil Type");

        // VOLCANIC — acidic, low nitrogen
        SoilSample vs = sample("VOLCANIC", 20.0, 10.0, 90.0, 4.8, 30.0, 1.5, 1.0);
        FertilizerStrategy volcanic = FertilizerStrategyFactory.getStrategy("VOLCANIC");
        assertContains("SP-01: Volcanic pH<5.5 + lowN → Lime",
            volcanic.recommendFertilizer(vs), "Lime");

        SoilSample vs2 = sample("VOLCANIC", 15.0, 40.0, 200.0, 6.5, 40.0, 2.0, 1.5);
        assertContains("SP-02: Volcanic lowN, good pH → Urea",
            volcanic.recommendFertilizer(vs2), "Urea");

        SoilSample vs3 = sample("VOLCANIC", 90.0, 45.0, 250.0, 6.5, 50.0, 3.0, 1.0);
        assertContains("SP-03: Volcanic healthy → Compost",
            volcanic.recommendFertilizer(vs3), "Compost");

        // LATERITE — low phosphorus
        SoilSample ls = sample("LATERITE", 60.0, 5.0, 90.0, 6.0, 40.0, 2.0, 2.0);
        FertilizerStrategy laterite = FertilizerStrategyFactory.getStrategy("LATERITE");
        assertContains("SP-04: Laterite lowP lowK → NPK or TSP",
            laterite.recommendFertilizer(ls), "NPK", "TSP");

        // ORGANIC — high organic matter
        SoilSample os = sample("ORGANIC", 80.0, 45.0, 220.0, 6.8, 60.0, 4.5, 0.8);
        FertilizerStrategy organic = FertilizerStrategyFactory.getStrategy("ORGANIC");
        assertContains("SP-05: Organic highOM → Rhizobium",
            organic.recommendFertilizer(os), "Rhizobium");

        // SANDY_LOAM — low moisture
        SoilSample sl = sample("SANDY_LOAM", 25.0, 20.0, 120.0, 6.2, 15.0, 1.5, 3.0);
        FertilizerStrategy sandy = FertilizerStrategyFactory.getStrategy("SANDY_LOAM");
        assertContains("SP-06: SandyLoam lowMoisture → Polymer+NPK",
            sandy.recommendFertilizer(sl), "Polymer", "NPK");

        // Dosage scales with hectares
        double dose1 = volcanic.calculateDosage(1.0, vs);
        double dose2 = volcanic.calculateDosage(2.0, vs);
        assertTrue("SP-07: Dosage 2ha > dosage 1ha", dose2 > dose1);

        // All strategies return non-empty crop lists
        for (String soil : List.of("VOLCANIC","LATERITE","ORGANIC","SANDY_LOAM")) {
            FertilizerStrategy st = FertilizerStrategyFactory.getStrategy(soil);
            SoilSample s = sample(soil, 80.0, 40.0, 200.0, 6.5, 50.0, 2.0, 1.0);
            assertTrue("SP-08: "+soil+" compatible crops not empty",
                !st.getCompatibleCrops(s).isEmpty());
        }

        // Unknown soil type defaults without exception
        assertNoThrow("SP-09: Unknown soilType defaults gracefully",
            () -> FertilizerStrategyFactory.getStrategy("MOONSOIL").recommendFertilizer(vs));
    }

    // ─────────────────────────────────────────────────────
    static void testObserverPattern() {
        section("OBSERVER PATTERN — DiseaseAlertSystem");

        // Simple in-memory observer subject
        class Subject {
            List<AlertObserver> obs = new ArrayList<>();
            void add(AlertObserver o) { obs.add(o); }
            void remove(AlertObserver o) { obs.remove(o); }
            void trigger(DiseaseAlert a) { obs.forEach(o -> o.onAlertReceived(a)); }
        }

        DiseaseAlert alert = new DiseaseAlert(
            "PLT-001","farmer@rab.rw","+250788000001",
            "Kigali","Root Rot",DiseaseAlert.Severity.HIGH,
            "Apply copper fungicide");

        // OB-01: All observers notified
        Subject subj = new Subject();
        int[] c1 = {0}, c2 = {0};
        subj.add(a -> c1[0]++);
        subj.add(a -> c2[0]++);
        subj.trigger(alert);
        assertTrue("OB-01: Both observers receive alert", c1[0]==1 && c2[0]==1);

        // OB-02: Removed observer not called
        Subject subj2 = new Subject();
        int[] c3 = {0}, c4 = {0};
        AlertObserver obs2 = a -> c4[0]++;
        subj2.add(a -> c3[0]++);
        subj2.add(obs2);
        subj2.remove(obs2);
        subj2.trigger(alert);
        assertTrue("OB-02: Removed observer not notified (c3=1, c4=0)",
            c3[0]==1 && c4[0]==0);

        // OB-03: No observers — no exception
        assertNoThrow("OB-03: Empty observer list — no exception",
            () -> new Subject().trigger(alert));

        // OB-04: Multiple triggers → count increases
        Subject subj3 = new Subject();
        int[] c5 = {0};
        subj3.add(a -> c5[0]++);
        subj3.trigger(alert);
        subj3.trigger(alert);
        subj3.trigger(alert);
        assertTrue("OB-04: 3 triggers → observer called 3 times", c5[0]==3);

        // OB-05: SpreadRisk values correct
        assertEq("OB-05a: LOW severity → spreadRisk 0.15",
            0.15, new DiseaseAlert("","","","","",DiseaseAlert.Severity.LOW,"").getSpreadRisk());
        assertEq("OB-05b: HIGH severity → spreadRisk 0.85",
            0.85, new DiseaseAlert("","","","","",DiseaseAlert.Severity.HIGH,"").getSpreadRisk());
    }

    // ─────────────────────────────────────────────────────
    static void testDiseaseRiskDetection() {
        section("DISEASE RISK DETECTION — SoilSample.hasDiseaseRisk()");

        assertTrue("DR-01: N=10 (critically low) → risk",
            sample("VOLCANIC",10.0,30.0,150.0,6.0,40.0,2.0,1.0).hasDiseaseRisk());
        assertTrue("DR-02: pH=3.5 (very acidic) → risk",
            sample("VOLCANIC",80.0,40.0,200.0,3.5,40.0,2.0,1.0).hasDiseaseRisk());
        assertTrue("DR-03: N=230 (toxic) → risk",
            sample("VOLCANIC",230.0,40.0,200.0,6.5,50.0,2.0,1.0).hasDiseaseRisk());
        assertTrue("DR-04: P=5 (critically low) → risk",
            sample("LATERITE",60.0,5.0,200.0,6.5,40.0,2.0,1.0).hasDiseaseRisk());
        assertTrue("DR-05: pH=9.0 (very alkaline) → risk",
            sample("VOLCANIC",80.0,40.0,200.0,9.0,40.0,2.0,1.0).hasDiseaseRisk());

        assertFalse("DR-06: Healthy soil (all optimal) → no risk",
            sample("VOLCANIC",80.0,40.0,200.0,6.5,50.0,2.0,1.5).hasDiseaseRisk());
        assertFalse("DR-07: Slightly low N (25, not critical) → no risk",
            sample("VOLCANIC",25.0,20.0,120.0,6.0,40.0,2.0,1.0).hasDiseaseRisk());
    }

    // ─────────────────────────────────────────────────────
    static void testNutrientScore() {
        section("NUTRIENT SCORE CALCULATION — SoilSample.calculateNutrientScore()");

        SoilSample perfect = sample("VOLCANIC",90.0,45.0,250.0,6.5,50.0,3.0,1.0);
        double score = perfect.calculateNutrientScore();
        assertTrue("SC-01: Perfect values → score = 100.0", score == 100.0);

        SoilSample awful = sample("VOLCANIC",0.0,0.0,0.0,0.0,0.0,0.0,1.0);
        double low = awful.calculateNutrientScore();
        assertTrue("SC-02: All zeros → score near 0 (got "+low+")", low < 5.0);

        SoilSample extreme = sample("VOLCANIC",300.0,200.0,600.0,13.0,95.0,0.0,1.0);
        double s3 = extreme.calculateNutrientScore();
        assertTrue("SC-03: Score always >= 0 (got "+s3+")", s3 >= 0);
        assertTrue("SC-04: Score always <= 100 (got "+s3+")", s3 <= 100);

        SoilSample mid = sample("VOLCANIC",50.0,25.0,150.0,6.0,45.0,2.0,1.0);
        double ms = mid.calculateNutrientScore();
        assertTrue("SC-05: Mid-range soil score > 80 (got "+ms+")", ms > 80);

        // Health summary contains all required fields
        NutrientAnalyzer na = new NutrientAnalyzer();
        String summary = na.generateHealthSummary(perfect);
        assertTrue("SC-06: Summary contains plotId", summary.contains("PLT-TEST"));
        assertTrue("SC-07: Summary contains district", summary.contains("Kigali"));
        assertTrue("SC-08: Summary contains Nitrogen", summary.contains("Nitrogen"));
        assertTrue("SC-09: Summary contains health score", summary.contains("Overall Health Score"));
    }

    // ═══════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════
    static SoilSample sample(String soil, double n, double p, double k,
                              double ph, double m, double om, double ha) {
        return new SoilSample("PLT-TEST","Kigali",soil,n,p,k,ph,m,om,ha);
    }

    static void section(String name) {
        System.out.println("\n── " + name + " " + "─".repeat(Math.max(0, 55-name.length())));
    }

    static void assertEq(String name, Object expected, Object actual) {
        boolean ok = expected.equals(actual);
        print(name, ok, expected.toString(), ok ? "" : actual.toString());
    }

    static void assertTrue(String name, boolean cond) {
        print(name, cond, "true", cond ? "" : "false");
    }

    static void assertFalse(String name, boolean cond) {
        print(name, !cond, "false", !cond ? "" : "true");
    }

    @SafeVarargs
    static <T extends CharSequence> void assertContains(String name, String actual, T... anyOf) {
        boolean ok = false;
        for (T s : anyOf) if (actual.contains(s)) { ok = true; break; }
        print(name, ok, "contains one of "+Arrays.toString(anyOf), ok ? "" : actual);
    }

    static void assertThrows(String name, Runnable r) {
        try { r.run(); print(name, false, "exception", "no exception thrown"); }
        catch (Exception e) { print(name, true, "exception", ""); }
    }

    static void assertNoThrow(String name, Runnable r) {
        try { r.run(); print(name, true, "no exception", ""); }
        catch (Exception e) { print(name, false, "no exception", e.getMessage()); }
    }

    static void print(String name, boolean ok, String expected, String got) {
        if (ok) { passed++; System.out.printf("  ✓  %s%n", name); }
        else    { failed++; System.out.printf("  ✗  %s%n     Expected: %s%n     Got: %s%n",
                                               name, expected, got); }
    }
}
