package com.example.backend;

/**
 * Created by mlenarto on 9/17/15.
 */

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

import javax.inject.Named;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Entity;

import java.util.LinkedList;
import java.util.List;


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

    // Get the Datastore Service
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    /**
     * Endpoint method that takes in a name, queries the database for that name,
     * and returns a FoodItem from the query.
     */
    @ApiMethod(name = "getDensity")
    public Entity getDensity(@Named("name") String name) {
        //TODO: look food item up in database

        Filter nameFilter = new FilterPredicate("name", Query.FilterOperator.EQUAL, name);
        Query q = new Query("FoodItem").setFilter(nameFilter);
        PreparedQuery pq = datastore.prepare(q);

        List<Entity> results = new LinkedList<Entity>();
        for (Entity result : pq.asIterable()) {
            results.add(result);
        }

        if (results.size() == 0) {
            // throw exception
        } else if (results.size() > 1) {
            // Uh oh, bad developer, you need to fix the database.
        }

        //TODO: determine if we should return Entity or FoodItem
        return results.get(0);
    }

}
