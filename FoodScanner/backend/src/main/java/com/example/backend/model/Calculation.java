package com.example.backend.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by mlenarto on 9/26/15.
 */
@Entity
public class Calculation {

    @Id
    private Long id;
    private FoodItem foodItemUsed;
    private NutritionResult nutritionResults;

    // Getters
    public Long getId() { return id; }
    public FoodItem getFoodItemUsed() { return foodItemUsed; }
    public NutritionResult getNutritionResults() { return nutritionResults; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setFoodItemUsed(FoodItem item) { this.foodItemUsed = item; }
    public void setNutritionResults(NutritionResult results) { this.nutritionResults = results; }

}
