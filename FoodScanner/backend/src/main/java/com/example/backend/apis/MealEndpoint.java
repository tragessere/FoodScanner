package com.example.backend.apis;

/**
 * Created by mlenarto on 10/29/15.
 */
import com.example.backend.Constants;
import com.example.backend.model.BackendMeal;
import com.example.backend.utils.AuthUtil;
import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.Date;
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
    public BackendMeal saveMeal(BackendMeal meal, User user) throws ServiceException {
        //AuthUtil.throwIfNotAuthenticated(user);

        if (findBackendMeal(meal.getId()) != null) throw new ConflictException("Meal already exists.");

        meal.setId(null);

        ofy().save().entity(meal).now();    //A synchronous save() will populate the generated id value on the entity instance
        return meal;
    }

    @ApiMethod(name = "deleteMeal")
    public void deleteMeal(@Named("id") Long id, User user) throws ServiceException {
        AuthUtil.throwIfNotAuthenticated(user);

        if (findBackendMeal(id) == null) throw new NotFoundException("Meal does not exist.");

        BackendMeal backendMeal = ofy().load().type(BackendMeal.class).id(id).now();
        ofy().delete().entity(backendMeal).now();
        //ofy().delete().entity(meal).now();
    }

    @ApiMethod(name = "getAllMeals")
    public CollectionResponse<BackendMeal> getAllMeals (User user) throws ServiceException {
        Query<BackendMeal> query = ofy().load().type(BackendMeal.class);
        List<BackendMeal> results = new ArrayList<BackendMeal>();
        QueryResultIterator<BackendMeal> iterator = query.iterator();

        while (iterator.hasNext()) {
            results.add(iterator.next());
        }

        return CollectionResponse.<BackendMeal>builder().setItems(results).build();
    }

    @ApiMethod(name = "getMealsWithinDates")
    public CollectionResponse<BackendMeal> getMealsWithinDates (@Named("startDate")Date startDate, @Named("endDate")Date endDate, User user) throws ServiceException {
        Query<BackendMeal> query = ofy().load().type(BackendMeal.class).filter("date >=", startDate.getTime()).filter("date <=", endDate.getTime());
        List<BackendMeal> results = new ArrayList<BackendMeal>();
        QueryResultIterator<BackendMeal> iterator = query.iterator();

        while (iterator.hasNext()) {
            results.add(iterator.next());
        }

        return CollectionResponse.<BackendMeal>builder().setItems(results).build();
    }

    private BackendMeal findBackendMeal(Long id) {
        return ofy().load().type(BackendMeal.class).id(id).now();
    }
}