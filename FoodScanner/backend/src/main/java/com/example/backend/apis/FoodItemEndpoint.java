package com.example.backend.apis;

/**
 * Created by mlenarto on 9/17/15.
 */

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

import java.util.List;

import javax.inject.Named;

import com.example.backend.Constants;
import static com.example.backend.OfyService.ofy;
import com.example.backend.model.FoodItem;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "foodScannerBackendAPI",
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(
                ownerDomain = "backend.example.com",
                ownerName = "backend.example.com",
                packagePath = ""
        )
)

public class FoodItemEndpoint {

    /**
     * Endpoint method that takes in a name, queries the database for that name,
     * and returns a FoodItem from the query.
     */
    @ApiMethod(name = "getDensity")
    public FoodItem getFoodItem(@Named("name") String name) {
        return ofy().load().type(FoodItem.class).filter("name", name).list().get(0);
    }
}
