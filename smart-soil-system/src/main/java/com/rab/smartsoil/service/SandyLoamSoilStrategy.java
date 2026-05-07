package com.rab.smartsoil.service;
import com.rab.smartsoil.model.SoilSample;
import java.util.List;
public class SandyLoamSoilStrategy implements FertilizerStrategy {
    public String recommendFertilizer(SoilSample s) {
        if(s.getMoisture()<25.0) return "Water-retaining Polymer + Slow-release NPK";
        if(s.getNitrogen()<SoilSample.MIN_NITROGEN) return "CAN (Calcium Ammonium Nitrate)";
        return "Balanced NPK 20-10-10 + Organic Mulch";
    }
    public double calculateDosage(double ha, SoilSample s) { return ha*120.0; }
    public List<String> getCompatibleCrops(SoilSample s) {
        return List.of("Beans","Groundnuts","Sorghum","Millet","Cowpea");
    }
}
