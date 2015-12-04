package com.example.backend.model;

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
    private Map<String, Double> nutritionFields;  //holds all nutrition info

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public Double getDensity() { return density; }
    public String getBrand() { return brand; }
    public Double getServingSize() { return servingSize; }
    public Map<String, Double> getNutritionFields() { return nutritionFields; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDensity(Double density) { this.density = density; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setServingSize (Double servingSize) { this.servingSize = servingSize; }
    public void setNutritionFields(Map<String, Double> nutritionFields) { this.nutritionFields = new HashMap<String, Double>(nutritionFields); }

    public BackendFoodItem() {}

    public BackendFoodItem(String name, Double density, String brand, Double servingSize, Map<String, Double> nutritionFields) {
        this.name = name;
        this.density = density;
        this.brand = brand;
        this.servingSize = servingSize;
        this.nutritionFields = new HashMap<String, Double>(nutritionFields);
    }
}