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
    private Long clientDBId; // ID used by the client DB
    private Long date;
    private MealType type;
    private List<BackendFoodItem> foodItems;
    private boolean isNew;
    private int isChanged;

    // Getters
    public Long getId() { return id; }
    public Long getClientDBId() { return clientDBId; }
    public Long getDate() { return date; }
    public MealType getType() { return type; }
    public List<BackendFoodItem> getFoodItems() { return foodItems; }
    public boolean getIsNew() { return isNew; }
    public int getIsChanged() { return isChanged; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setClientDBId(Long id) { this.clientDBId = id; }
    public void setDate(Long date) {this.date = date; }
    public void setMealType(MealType type) { this.type = type; }
    public void setFoodItems(List<BackendFoodItem> foodItems) { this.foodItems = foodItems; }
    public void setIsNew(boolean isNew) { this.isNew = isNew; }
    public void setIsChanged(int isChanged) { this.isChanged = isChanged; }

}
