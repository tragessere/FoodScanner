package com.example.backend.apis;

import com.example.backend.Constants;
import com.example.backend.model.BackendFoodItem;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import static com.example.backend.OfyService.ofy;

/**
 * Created by mlenarto on 11/3/15.
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
    @ApiMethod(name = "getFoodItem")
    public BackendFoodItem getFoodItem(@Named("name") String name) {
        return ofy().load().type(BackendFoodItem.class).filter("name", name).list().get(0);
    }

    /**
     * Gets all of the food item in the data store.
     * @return CollectionResponse of all food items.
     */
    @ApiMethod(name = "getAllFoodItems")
    public CollectionResponse<BackendFoodItem> getFoodItems() {

        Query<BackendFoodItem> query = ofy().load().type(BackendFoodItem.class);
        List<BackendFoodItem> results = new ArrayList<BackendFoodItem>();
        QueryResultIterator<BackendFoodItem> iterator = query.iterator();

        while (iterator.hasNext()) {
            results.add(iterator.next());
        }

        return CollectionResponse.<BackendFoodItem>builder().setItems(results).build();
    }
}