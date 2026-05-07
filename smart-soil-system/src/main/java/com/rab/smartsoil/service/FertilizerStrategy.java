package com.rab.smartsoil.service;

import com.rab.smartsoil.model.SoilSample;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════
 * DESIGN PATTERN: STRATEGY
 * ═══════════════════════════════════════════════════════════════
 * Intent: Define a family of fertilizer recommendation algorithms,
 *         encapsulate each one, and make them interchangeable at
 *         runtime based on the plot's soil type.
 *
 * Why needed: Rwanda has 4 distinct soil types across its provinces —
 * each requiring a completely different fertilizer algorithm.
 * Hardcoding if-else chains violates OCP and makes adding new soil
 * types require editing existing tested classes.
 *
 * Solution: Each soil type strategy is an independent class.
 * AdvisoryEngine selects the correct one at runtime.
 * ═══════════════════════════════════════════════════════════════
 */
public interface FertilizerStrategy {

    /**
     * Recommends the appropriate fertilizer for a given soil sample.
     *
     * @param sample the SoilSample with N, P, K, pH, moisture readings
     * @return String fertilizer recommendation (e.g., "Urea 46%", "Lime + DAP")
     */
    String recommendFertilizer(SoilSample sample);

    /**
     * Calculates the required fertilizer dosage in kg/hectare.
     *
     * @param hectares  land size in hectares
     * @param sample    soil reading with current nutrient levels
     * @return double dosage in kg/hectare
     */
    double calculateDosage(double hectares, SoilSample sample);

    /**
     * Returns crops compatible with this soil type.
     *
     * @param sample the soil sample for compatibility check
     * @return List of compatible crop names
     */
    List<String> getCompatibleCrops(SoilSample sample);
}


// ═══════════════════════════════════════════════════════════════
// CONCRETE STRATEGY 1 — Volcanic Soil (Northern Province: Musanze)
// ═══════════════════════════════════════════════════════════════
public class VolcanicSoilStrategy implements FertilizerStrategy {

    private static final double MIN_PH       = 5.5;
    private static final double MIN_NITROGEN  = SoilSample.MIN_NITROGEN;
    private static final double MIN_PHOSPH    = SoilSample.MIN_PHOSPHORUS;

    @Override
    public String recommendFertilizer(SoilSample sample) {
        if (sample.getPh() < MIN_PH && sample.getNitrogen() < MIN_NITROGEN) {
            return "Lime + DAP (Di-Ammonium Phosphate) — dual correction";
        }
        if (sample.getPh() < MIN_PH)          return "Agricultural Lime — pH correction";
        if (sample.getNitrogen() < MIN_NITROGEN) return "Urea 46% Nitrogen";
        if (sample.getPhosphorus() < MIN_PHOSPH) return "DAP (Di-Ammonium Phosphate)";
        return "Organic Compost — soil in good health";
    }

    @Override
    public double calculateDosage(double hectares, SoilSample sample) {
        double nitrogenDeficit = Math.max(0, MIN_NITROGEN - sample.getNitrogen());
        return hectares * (100 + nitrogenDeficit * 0.8);
    }

    @Override
    public List<String> getCompatibleCrops(SoilSample sample) {
        if (sample.getPh() >= MIN_PH) {
            return List.of("Irish Potato", "Wheat", "Maize", "Pyrethrum", "Peas");
        }
        return List.of("Irish Potato", "Pyrethrum"); // acid-tolerant
    }
}


// ═══════════════════════════════════════════════════════════════
// CONCRETE STRATEGY 2 — Laterite Soil (Eastern Province: Bugesera)
// ═══════════════════════════════════════════════════════════════
public class LateriteSoilStrategy implements FertilizerStrategy {

    private static final double MIN_PHOSPH   = SoilSample.MIN_PHOSPHORUS;
    private static final double MIN_POTASSIUM = SoilSample.MIN_POTASSIUM;

    @Override
    public String recommendFertilizer(SoilSample sample) {
        if (sample.getPhosphorus() < MIN_PHOSPH && sample.getPotassium() < MIN_POTASSIUM) {
            return "NPK 17-17-17 Compound Fertilizer — balanced correction";
        }
        if (sample.getPhosphorus() < MIN_PHOSPH) return "TSP (Triple Super Phosphate)";
        if (sample.getPotassium() < MIN_POTASSIUM) return "MOP (Muriate of Potash)";
        return "NPK 17-17-17 maintenance application";
    }

    @Override
    public double calculateDosage(double hectares, SoilSample sample) {
        double phosphDeficit = Math.max(0, MIN_PHOSPH - sample.getPhosphorus());
        return hectares * (150 + phosphDeficit * 1.5);
    }

    @Override
    public List<String> getCompatibleCrops(SoilSample sample) {
        return List.of("Cassava", "Sorghum", "Sunflower", "Groundnuts", "Sweet Potato");
    }
}


// ═══════════════════════════════════════════════════════════════
// CONCRETE STRATEGY 3 — Organic/Peat Soil (Bugesera Wetlands)
// ═══════════════════════════════════════════════════════════════
public class OrganicSoilStrategy implements FertilizerStrategy {

    private static final double GOOD_ORGANIC_MATTER = 3.0;

    @Override
    public String recommendFertilizer(SoilSample sample) {
        if (sample.getOrganicMatter() > GOOD_ORGANIC_MATTER) {
            return "Rhizobium Bio-Fertilizer Inoculant — organic enhancement";
        }
        return "Green Manure + Compost Mix — organic matter restoration";
    }

    @Override
    public double calculateDosage(double hectares, SoilSample sample) {
        return hectares * 80.0; // lighter dosage for already nutrient-rich organic soil
    }

    @Override
    public List<String> getCompatibleCrops(SoilSample sample) {
        return List.of("Rice", "Taro", "Vegetables", "Banana", "Sugar Cane");
    }
}


// ═══════════════════════════════════════════════════════════════
// CONCRETE STRATEGY 4 — Sandy Loam (Western Province hillsides)
// ═══════════════════════════════════════════════════════════════
public class SandyLoamSoilStrategy implements FertilizerStrategy {

    @Override
    public String recommendFertilizer(SoilSample sample) {
        if (sample.getMoisture() < 25.0) {
            return "Water-retaining Polymer + Slow-release NPK";
        }
        if (sample.getNitrogen() < SoilSample.MIN_NITROGEN) {
            return "CAN (Calcium Ammonium Nitrate) — fast nitrogen";
        }
        return "Balanced NPK 20-10-10 + Organic Mulch";
    }

    @Override
    public double calculateDosage(double hectares, SoilSample sample) {
        return hectares * 120.0;
    }

    @Override
    public List<String> getCompatibleCrops(SoilSample sample) {
        return List.of("Beans", "Groundnuts", "Sorghum", "Millet", "Cowpea");
    }
}


// ═══════════════════════════════════════════════════════════════
// STRATEGY FACTORY — selects the right strategy at runtime
// ═══════════════════════════════════════════════════════════════
public class FertilizerStrategyFactory {

    /**
     * Returns the appropriate FertilizerStrategy based on soil type.
     * Adding a new soil type requires ONLY a new case here — no other
     * class needs to change (Open/Closed Principle).
     *
     * @param soilType soil type string from SoilSample entity
     * @return FertilizerStrategy implementation
     */
    public static FertilizerStrategy getStrategy(String soilType) {
        return switch (soilType.toUpperCase()) {
            case "VOLCANIC"   -> new VolcanicSoilStrategy();
            case "LATERITE"   -> new LateriteSoilStrategy();
            case "ORGANIC"    -> new OrganicSoilStrategy();
            case "SANDY_LOAM" -> new SandyLoamSoilStrategy();
            default           -> new VolcanicSoilStrategy(); // Rwanda default
        };
    }
}
