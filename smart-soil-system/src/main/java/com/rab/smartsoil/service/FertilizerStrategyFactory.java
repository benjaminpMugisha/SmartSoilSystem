package com.rab.smartsoil.service;
public class FertilizerStrategyFactory {
    public static FertilizerStrategy getStrategy(String soilType) {
        return switch(soilType.toUpperCase()) {
            case "VOLCANIC"   -> new VolcanicSoilStrategy();
            case "LATERITE"   -> new LateriteSoilStrategy();
            case "ORGANIC"    -> new OrganicSoilStrategy();
            case "SANDY_LOAM" -> new SandyLoamSoilStrategy();
            default           -> new VolcanicSoilStrategy();
        };
    }
}
