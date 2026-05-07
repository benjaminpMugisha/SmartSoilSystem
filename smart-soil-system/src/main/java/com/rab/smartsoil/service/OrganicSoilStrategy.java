package com.rab.smartsoil.service;
import com.rab.smartsoil.model.SoilSample;
import java.util.List;
public class OrganicSoilStrategy implements FertilizerStrategy {
    public String recommendFertilizer(SoilSample s) {
        return s.getOrganicMatter()>3.0 ? "Rhizobium Bio-Fertilizer Inoculant"
                                        : "Green Manure + Compost Mix";
    }
    public double calculateDosage(double ha, SoilSample s) { return ha*80.0; }
    public List<String> getCompatibleCrops(SoilSample s) {
        return List.of("Rice","Taro","Vegetables","Banana","Sugar Cane");
    }
}
