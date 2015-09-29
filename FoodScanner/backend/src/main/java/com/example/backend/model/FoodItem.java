package com.example.backend.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by mlenarto on 9/17/15.
 */

@Entity
public class FoodItem {

    @Id
    private Long id;
    private String name;
    private Integer density;

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public Integer getDensity() { return density; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDensity(Integer density) { this.density = density; }
}
