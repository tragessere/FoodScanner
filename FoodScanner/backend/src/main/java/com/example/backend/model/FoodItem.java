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
    private NutritionResult nutritionTotals;

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public Float getDensity() { return density; }
    public NutritionResult getNutritionTotals() { return nutritionTotals; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDensity(Float density) { this.density = density; }
    public void setNutritionTotals(NutritionResult nutritionTotals) { this.nutritionTotals = nutritionTotals; }

    public FoodItem() {}

    public FoodItem(String name, Float density, NutritionResult nutritionTotals) {
        this.name = name;
        this.density = density;
        this.nutritionTotals = nutritionTotals;
    }
}