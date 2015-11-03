package com.example.backend.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * Created by mlenarto on 11/3/15.
 */
@Entity
public class DensityEntry {

    @Id
    private Long id;
    @Index
    private String name;
    private Float density;

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public Float getDensity() { return density; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDensity(Float density) { this.density = density; }

    public DensityEntry() {}

    public DensityEntry(String name, String category, Float density, Float specialGravity) {
        this.name = name;
        this.density = density;
    }
}