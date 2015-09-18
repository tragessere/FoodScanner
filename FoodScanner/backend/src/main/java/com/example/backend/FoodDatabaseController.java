package com.example.backend;

/**
 * Created by mlenarto on 9/17/15.
 */

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

import javax.inject.Named;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "foodScannerBackendAPI",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "backend.example.com",
                ownerName = "backend.example.com",
                packagePath = ""
        )
)

public class FoodDatabaseController {

    /**
     * Endpoint method that takes in a name, queries the database for that name,
     * and returns a FoodItem from the query.
     */
    @ApiMethod(name = "getDensity")
    public FoodItem getDensity(@Named("name") String name) {
        //TODO: look food item up in database

        // Create mock food item
        FoodItem item = new FoodItem();
        item.setName(name);
        item.setDensity(1212);

        return item;
    }

}
