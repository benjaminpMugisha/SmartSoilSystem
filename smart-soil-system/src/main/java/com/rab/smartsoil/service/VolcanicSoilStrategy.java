package com.rab.smartsoil.service;
import com.rab.smartsoil.model.SoilSample;
import java.util.List;
public class VolcanicSoilStrategy implements FertilizerStrategy {
    public String recommendFertilizer(SoilSample s) {
        if(s.getPh()<5.5 && s.getNitrogen()<SoilSample.MIN_NITROGEN) return "Lime + DAP";
        if(s.getPh()<5.5) return "Agricultural Lime — pH correction";
        if(s.getNitrogen()<SoilSample.MIN_NITROGEN) return "Urea 46% Nitrogen";
        if(s.getPhosphorus()<SoilSample.MIN_PHOSPHORUS) return "DAP (Di-Ammonium Phosphate)";
        return "Organic Compost — soil in good health";
    }
    public double calculateDosage(double ha, SoilSample s) {
        return ha*(100+Math.max(0,SoilSample.MIN_NITROGEN-s.getNitrogen())*0.8);
    }
    public List<String> getCompatibleCrops(SoilSample s) {
        if(s.getPh()>=5.5) return List.of("Irish Potato","Wheat","Maize","Pyrethrum","Peas");
        return List.of("Irish Potato","Pyrethrum");
    }
}
