package com.example.backend.model;

import com.example.backend.Constants.MealType;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.Date;
import java.util.List;

/**
 * Created by mlenarto on 10/29/15.
 */
@Entity
public class Meal {
    @Id
    private Long id;
    private Date date;
    private MealType type;
    private List<FoodItem> foodItems;

    // Getters
    public Long getId() { return id; }
    public Date getDate() { return date; }
    public MealType getType() { return type; }
    public List<FoodItem> getFoodItems() { return foodItems; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setDate(Date date) {this.date = date; }
    public void setMealType(MealType type) { this.type = type; }
    public void setFoodItems(List<FoodItem> foodItems) { this.foodItems = foodItems; }

    public Meal(Date date, MealType type, List<FoodItem> foodItems) {
        this.date = date;
        this.type = type;
        this.foodItems = foodItems;
    }
}
