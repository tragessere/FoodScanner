package com.example.backend.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * Created by mlenarto on 9/17/15.
 */

@Entity
public class FoodItem {

    @Id
    private Long id;
    @Index
    private String name;
    private String category;
    private Float density;
    private Float specialGravity;

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public Float getDensity() { return density; }
    public Float getSpecialGravity() { return specialGravity; }
    public String getCategory() { return category; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDensity(Float density) { this.density = density; }
    public void setSpecialGravity(Float specialGravity) { this.specialGravity = specialGravity; }
    public void setCategory(String category) { this.category = category; }

    public FoodItem() {}

    public FoodItem(String name, String category, Float density, Float specialGravity) {
        this.name = name;
        this.category = category;
        this.density = density;
        this.specialGravity = specialGravity;
    }
}
