package com.example.backend.apis;

import com.example.backend.Constants;
import com.example.backend.model.BackendFoodItem;
import com.example.backend.model.BackendMeal;
import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;
import com.google.appengine.api.users.User;

import com.example.backend.utils.AuthUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

import static com.googlecode.objectify.ObjectifyService.ofy;

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
public class BackendMealEndpoint {

    private static final Logger logger = Logger.getLogger(BackendMealEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(BackendMeal.class);
    }

    /**
     * Returns the {@link BackendMeal} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code BackendMeal} with the provided ID.
     */
    @ApiMethod(
            name = "getBackendMeal",
            path = "backendMeal/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public BackendMeal getBackendMeal(@Named("id") Long id, User user) throws ServiceException
    {
        AuthUtil.throwIfNotAuthenticated(user);

        logger.info("Getting BackendMeal with ID: " + id);
        BackendMeal backendMeal = ofy().load().type(BackendMeal.class).id(id).now();

        if (backendMeal == null) {
            throw new NotFoundException("Could not find BackendMeal with ID: " + id);
        }

        return backendMeal;
    }

    /**
     * Inserts a new {@code BackendMeal}.
     */
    @ApiMethod(
            name = "insertBackendMeal",
            path = "backendMeal",
            httpMethod = ApiMethod.HttpMethod.POST)
    public BackendMeal insertBackendMeal(BackendMeal backendMeal, User user) throws ServiceException
    {
        AuthUtil.throwIfNotAuthenticated(user);

        //validate that backendMeal.id has not been set.
        if (backendMeal.getId().longValue() == 0) backendMeal.setId(null);
        List<BackendFoodItem> newList = new ArrayList<BackendFoodItem>();
        for (BackendFoodItem item : backendMeal.getFoodItems())
        {
            ofy().save().entity(item).now();
            newList.add(ofy().load().entity(item).now());

        }

        backendMeal.setFoodItems(newList);
        ofy().save().entity(backendMeal).now();
        logger.info("Created BackendMeal with ID: " + backendMeal.getId());

        return ofy().load().entity(backendMeal).now();
    }

    /**
     * Updates an existing {@code BackendMeal}.
     *
     * @param id          the ID of the entity to be updated
     * @param backendMeal the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code BackendMeal}
     */
    @ApiMethod(
            name = "updateBackendMeal",
            path = "backendMeal/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public BackendMeal updateBackendMeal(@Named("id") Long id, BackendMeal backendMeal, User user) throws ServiceException
    {
        AuthUtil.throwIfNotAuthenticated(user);

        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExists(id);
        ofy().save().entity(backendMeal).now();
        logger.info("Updated BackendMeal: " + backendMeal);
        return ofy().load().entity(backendMeal).now();
    }

    /**
     * Deletes the specified {@code BackendMeal}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code BackendMeal}
     */
    @ApiMethod(
            name = "removeBackendMeal",
            path = "backendMeal/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void removeBackendMeal(@Named("id") Long id, User user) throws ServiceException
    {
        AuthUtil.throwIfNotAuthenticated(user);

        checkExists(id);
        ofy().delete().type(BackendMeal.class).id(id).now();
        logger.info("Deleted BackendMeal with ID: " + id);
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "listBackendMeals",
            path = "backendMeal",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<BackendMeal> listBackendMeals(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit, User user) throws ServiceException
    {
        AuthUtil.throwIfNotAuthenticated(user);

        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;

        Query<BackendMeal> query = ofy().load().type(BackendMeal.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }

        QueryResultIterator<BackendMeal> queryIterator = query.iterator();
        List<BackendMeal> backendMealList = new ArrayList<BackendMeal>(limit);

        while (queryIterator.hasNext()) {
            backendMealList.add(queryIterator.next());
        }
        return CollectionResponse.<BackendMeal>builder().setItems(backendMealList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    @ApiMethod(
            name = "getBackendMealsBetweenDates",
            path = "backendMeal/dates",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<BackendMeal> getBackendMealsBetweenDates(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit, @Named("startDate") long startDate, @Named("endDate") long endDate, User user) throws ServiceException
    {
        AuthUtil.throwIfNotAuthenticated(user);

        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;

        Query<BackendMeal> query = ofy().load().type(BackendMeal.class).limit(limit).filter("date >=", startDate).filter("date <=", endDate);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }

        QueryResultIterator<BackendMeal> queryIterator = query.iterator();
        List<BackendMeal> backendMealList = new ArrayList<BackendMeal>(limit);

        while (queryIterator.hasNext()) {
            backendMealList.add(queryIterator.next());
        }
        return CollectionResponse.<BackendMeal>builder().setItems(backendMealList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    private void checkExists(Long id) throws NotFoundException
    {
        try {
            ofy().load().type(BackendMeal.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find BackendMeal with ID: " + id);
        }
    }
}