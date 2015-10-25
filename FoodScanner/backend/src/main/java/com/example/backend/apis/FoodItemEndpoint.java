package com.example.backend.apis;

/**
 * Created by mlenarto on 9/17/15.
 */

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import com.example.backend.Constants;
import static com.example.backend.OfyService.ofy;
import com.example.backend.model.FoodItem;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.cmd.Query;

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
     * Gets food item with specified name.
     * @param name
     * @return food item with name equal to the specified name.
     */
    @ApiMethod(name = "getDensity")
    public FoodItem getFoodItem(@Named("name") String name) {
        return ofy().load().type(FoodItem.class).filter("name", name).list().get(0);
    }

    /**
     * Gets all of the food items in the data store.
     * @return CollectionResponse of all food items.
     */
    @ApiMethod(name = "getAllFoodItems")
    public CollectionResponse<FoodItem> getFoodItems() {

        Query<FoodItem> query = ofy().load().type(FoodItem.class);
        List<FoodItem> results = new ArrayList<FoodItem>();
        QueryResultIterator<FoodItem> iterator = query.iterator();

        while (iterator.hasNext()) {
            results.add(iterator.next());
        }

        return CollectionResponse.<FoodItem>builder().setItems(results).build();
    }

}
