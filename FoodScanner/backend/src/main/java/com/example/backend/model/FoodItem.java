package com.example.backend.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * Created by mlenarto on 11/3/15.
 */
@Entity
public class FoodItem {

    @Id
    private Long id;
    @Index
    private String name;
    private Float density;
    private String brand;
    private Integer numProportions;
    private Double servingSize;
    private NutritionResult nutritionTotals;

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public Float getDensity() { return density; }
    public NutritionResult getNutritionTotals() { return nutritionTotals; }
    public String getBrand() { return brand; }
    public Integer getNumProportions() { return numProportions; }
    public Double getServingSize() { return servingSize; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDensity(Float density) { this.density = density; }
    public void setNutritionTotals(NutritionResult nutritionTotals) { this.nutritionTotals = nutritionTotals; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setNumProportions(Integer numProportions) { this.numProportions = numProportions; }
    public void setServingSize (Double servingSize) { this.servingSize = servingSize; }

    public FoodItem() {}

    public FoodItem(String name, Float density, String brand, Integer numProportions, Double servingSize, NutritionResult nutritionTotals) {
        this.name = name;
        this.density = density;
        this.brand = brand;
        this.numProportions = numProportions;
        this.servingSize = servingSize;
        this.nutritionTotals = nutritionTotals;
    }
}