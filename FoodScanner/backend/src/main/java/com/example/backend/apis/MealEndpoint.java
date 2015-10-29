package com.example.backend.apis;

/**
 * Created by mlenarto on 10/29/15.
 */
import com.example.backend.Constants;
import com.example.backend.model.Meal;
import com.example.backend.utils.AuthUtil;
import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import static com.example.backend.OfyService.ofy;

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

public class MealEndpoint {

    @ApiMethod(name = "saveMeal")
    public void saveMeal(Meal meal, User user) throws ServiceException {
        AuthUtil.throwIfNotAuthenticated(user);
        ofy().save().entity(meal).now();
    }

    @ApiMethod(name = "getAllMeals")
    public CollectionResponse<Meal> getAllMeals (User user) throws ServiceException {
        Query<Meal> query = ofy().load().type(Meal.class);
        List<Meal> results = new ArrayList<Meal>();
        QueryResultIterator<Meal> iterator = query.iterator();

        while (iterator.hasNext()) {
            results.add(iterator.next());
        }

        return CollectionResponse.<Meal>builder().setItems(results).build();
    }
}