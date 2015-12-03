package com.example.backend.model;

import com.example.backend.Constants.MealType;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.List;

/**
 * Created by mlenarto on 10/29/15.
 */
@Entity
public class BackendMeal {
    @Id
    private Long id;
    private Long date;
    private MealType type;
    private List<BackendFoodItem> foodItems;

    // Getters
    public Long getId() { return id; }
    public Long getDate() { return date; }
    public MealType getType() { return type; }
    public List<BackendFoodItem> getFoodItems() { return foodItems; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setDate(Long date) {this.date = date; }
    public void setMealType(MealType type) { this.type = type; }
    public void setFoodItems(List<BackendFoodItem> foodItems) { this.foodItems = foodItems; }

    public BackendMeal(Long date, String type, List<BackendFoodItem> foodItems) {
        this.date = date;
        this.type = MealType.valueOf(type);
        this.foodItems = foodItems;
    }
}
