package com.example.backend.model;

import com.google.appengine.repackaged.com.google.common.base.Flag;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mlenarto on 11/3/15.
 */
@Entity
public class BackendFoodItem {

    @Id
    private Long id;
    @Index
    private String name;
    private Double density;
    private String brand;
    private Double servingSize;
    private Double numServings;
    private Double volume;
    private Double cubicVolume;
    private Double mass;
    private String servingSizeUnit;
    private String actualServingSizeUnit;
    private boolean usesVol;
    private boolean usesMass;
    private boolean needConvertVol;

    private boolean needCalculateServings;
    private Map<String, Double> calculatedNutrition;
    private Map<String, Double> uncalculatedNutrition;

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public Double getDensity() { return density; }
    public String getBrand() { return brand; }
    public Double getServingSize() { return servingSize; }
    public Double getNumServings() { return numServings; }
    public Double getVolume() { return volume; }
    public Double getCubicVolume() { return cubicVolume; }
    public Double getMass() { return mass; }
    public String getServingSizeUnit() { return servingSizeUnit; }
    public String getActualServingSizeUnit() { return actualServingSizeUnit; }
    public boolean getUsesVol() { return usesVol; }
    public boolean getUsesMass() { return usesMass; }
    public boolean getNeedConvertVol() { return needConvertVol; }
    public boolean getNeedCalculateServings() { return  needCalculateServings; }
    public Map<String, Double> getCalculatedNutrition() { return calculatedNutrition; }
    public Map<String, Double> getUncalculatedNutrition() { return uncalculatedNutrition; }


    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDensity(Double density) { this.density = density; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setServingSize (Double servingSize) { this.servingSize = servingSize; }
    public void setUsesVol(boolean usesVol) { this.usesVol = usesVol; }
    public void setNumServings(Double numServings) { this.numServings = numServings; }
    public void setVolume(Double volume) { this.volume = volume; }
    public void setCubicVolume(Double cubicVolume) { this.cubicVolume = cubicVolume; }
    public void setMass(Double mass) { this.mass = mass; }
    public void setServingSizeUnit(String servingSizeUnit) { this.servingSizeUnit = servingSizeUnit; }
    public void setUsesMass(boolean usesMass) { this.usesMass = usesMass; }
    public void setNeedConvertVol(boolean needConvertVol) { this.needConvertVol = needConvertVol; }
    public void setNeedCalculateServings(boolean needCalculateServings) { this.needCalculateServings = needCalculateServings; }
    public void setActualServingSizeUnit(String actualServingSizeUnit) { this.actualServingSizeUnit = actualServingSizeUnit; }
    public void setCalculatedNutrition(Map<String, Double> nutritionFields) { this.calculatedNutrition = new HashMap<String, Double>(nutritionFields); }
    public void setUncalculatedNutrition(Map<String, Double> nutritionFields) { this.uncalculatedNutrition = new HashMap<String, Double>(nutritionFields); }
}