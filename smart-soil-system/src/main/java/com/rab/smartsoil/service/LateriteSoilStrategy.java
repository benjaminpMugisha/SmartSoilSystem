package com.rab.smartsoil.service;
import com.rab.smartsoil.model.SoilSample;
import java.util.List;
public class LateriteSoilStrategy implements FertilizerStrategy {
    public String recommendFertilizer(SoilSample s) {
        if(s.getPhosphorus()<SoilSample.MIN_PHOSPHORUS && s.getPotassium()<SoilSample.MIN_POTASSIUM)
            return "NPK 17-17-17 Compound Fertilizer";
        if(s.getPhosphorus()<SoilSample.MIN_PHOSPHORUS) return "TSP (Triple Super Phosphate)";
        if(s.getPotassium()<SoilSample.MIN_POTASSIUM) return "MOP (Muriate of Potash)";
        return "NPK 17-17-17 maintenance application";
    }
    public double calculateDosage(double ha, SoilSample s) {
        return ha*(150+Math.max(0,SoilSample.MIN_PHOSPHORUS-s.getPhosphorus())*1.5);
    }
    public List<String> getCompatibleCrops(SoilSample s) {
        return List.of("Cassava","Sorghum","Sunflower","Groundnuts","Sweet Potato");
    }
}
