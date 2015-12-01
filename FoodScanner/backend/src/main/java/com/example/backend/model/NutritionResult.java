package com.example.backend.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by mlenarto on 9/26/15.
 */
@Entity
public class NutritionResult {

    @Id
    private Long id;
    private Integer calories;
    private Integer totalFat;
    private Integer saturatedFat;
    private Integer transFat;
    private Integer cholesterol;
    private Integer sodium;
    private Integer totalCarbohydrate;
    private Integer dietaryFiber;
    private Integer sugars;
    private Integer protein;

    // Getters
    public Long getId() { return id; }
    public Integer getCalories() { return calories; }
    public Integer getTotalFat() { return totalFat; }
    public Integer getSaturatedFat() { return saturatedFat; }
    public Integer getTransFat() { return transFat; }
    public Integer getCholesterol() { return cholesterol; }
    public Integer getSodium() { return sodium; }
    public Integer getTotalCarbohydrate() { return totalCarbohydrate; }
    public Integer getDietaryFiber() { return dietaryFiber; }
    public Integer getSugars() { return sugars; }
    public Integer getProtein() { return protein; }


    // Setters
    public void setId(Long id) { this.id = id; }
    public void setCalories(Integer value) { this.calories = value; }
    public void setTotalFat(Integer value) { this.calories = value; }
    public void setSaturatedFat(Integer value) { this.calories = value; }
    public void setTransFat(Integer value) { this.calories = value; }
    public void setCholesterol(Integer value) { this.calories = value; }
    public void setSodium(Integer value) { this.calories = value; }
    public void setTotalCarbohydrate(Integer value) { this.calories = value; }
    public void setDietaryFiber(Integer value) { this.calories = value; }
    public void setSugars(Integer value) { this.calories = value; }
    public void setProtein(Integer value) { this.calories = value; }


}
