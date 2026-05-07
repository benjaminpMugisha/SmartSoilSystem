package com.rab.smartsoil.service;

import com.rab.smartsoil.model.Advisory;
import com.rab.smartsoil.model.SoilSample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AdvisoryEngine — generates personalized crop advisories.
 *
 * Uses the STRATEGY pattern: selects the appropriate FertilizerStrategy
 * at runtime based on the soil type in the SoilSample.
 *
 * Dependency Inversion: depends on FertilizerStrategy interface,
 * not on any concrete strategy class.
 */
@Service
public class AdvisoryEngine {

    private final NutrientAnalyzer nutrientAnalyzer;

    @Autowired
    public AdvisoryEngine(NutrientAnalyzer nutrientAnalyzer) {
        this.nutrientAnalyzer = nutrientAnalyzer;
    }

    /**
     * Generates a fully personalized crop advisory for a soil sample.
     * Selects the correct FertilizerStrategy at runtime via FertilizerStrategyFactory.
     *
     * @param sample the validated SoilSample entity
     * @return Advisory with fertilizer recommendation, dosage, crops, and schedule
     */
    public Advisory generateAdvisory(SoilSample sample) {
        // STRATEGY PATTERN: select algorithm at runtime based on soil type
        FertilizerStrategy strategy = FertilizerStrategyFactory.getStrategy(sample.getSoilType());

        String fertilizerType      = strategy.recommendFertilizer(sample);
        double dosage              = strategy.calculateDosage(sample.getLandSizeHectares(), sample);
        List<String> compatibleCrops = strategy.getCompatibleCrops(sample);

        double healthScore = nutrientAnalyzer.getOverallHealthScore(sample);
        String riskLevel   = nutrientAnalyzer.classifyRisk(healthScore);
        String summary     = nutrientAnalyzer.generateHealthSummary(sample);

        return new Advisory(
            sample.getPlotId(),
            sample.getDistrict(),
            compatibleCrops,
            fertilizerType,
            dosage,
            summary,
            healthScore,
            riskLevel
        );
    }
}
