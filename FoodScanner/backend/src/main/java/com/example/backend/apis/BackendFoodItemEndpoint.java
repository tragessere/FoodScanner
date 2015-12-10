package com.example.backend.apis;

import com.example.backend.Constants;
import com.example.backend.model.BackendFoodItem;
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
public class BackendFoodItemEndpoint {

    private static final Logger logger = Logger.getLogger(BackendFoodItemEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(BackendFoodItem.class);
    }

    /**
     * Returns the {@link BackendFoodItem} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code BackendFoodItem} with the provided ID.
     */
    @ApiMethod(
            name = "getFoodItem",
            path = "backendFoodItem/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public BackendFoodItem getFoodItem(@Named("id") Long id, User user) throws ServiceException
    {
        AuthUtil.throwIfNotAuthenticated(user);

        logger.info("Getting BackendFoodItem with ID: " + id);
        BackendFoodItem backendFoodItem = ofy().load().type(BackendFoodItem.class).id(id).now();
        if (backendFoodItem == null) {
            throw new NotFoundException("Could not find BackendFoodItem with ID: " + id);
        }
        return backendFoodItem;
    }

    /**
     * Inserts a new {@code BackendFoodItem}.
     */
    @ApiMethod(
            name = "insertFoodItem",
            path = "backendFoodItem",
            httpMethod = ApiMethod.HttpMethod.POST)
    public BackendFoodItem insertFoodItem(BackendFoodItem backendFoodItem, User user) throws ServiceException
    {
        AuthUtil.throwIfNotAuthenticated(user);

        ofy().save().entity(backendFoodItem).now();
        //logger.info("Created BackendFoodItem with ID: " + backendFoodItem.getId());

        return ofy().load().entity(backendFoodItem).now();
    }

    /**
     * Updates an existing {@code BackendFoodItem}.
     *
     * @param id              the ID of the entity to be updated
     * @param backendFoodItem the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code BackendFoodItem}
     */
    @ApiMethod(
            name = "updateFoodItem",
            path = "backendFoodItem/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public BackendFoodItem updateFoodItem(@Named("id") Long id, BackendFoodItem backendFoodItem, User user) throws ServiceException
    {
        AuthUtil.throwIfNotAuthenticated(user);

        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExists(id);
        ofy().save().entity(backendFoodItem).now();
        logger.info("Updated BackendFoodItem: " + backendFoodItem);
        return ofy().load().entity(backendFoodItem).now();
    }

    /**
     * Deletes the specified {@code BackendFoodItem}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code BackendFoodItem}
     */
    @ApiMethod(
            name = "removeFoodItem",
            path = "backendFoodItem/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void removeFoodItem(@Named("id") Long id) throws NotFoundException {
        checkExists(id);
        ofy().delete().type(BackendFoodItem.class).id(id).now();
        logger.info("Deleted BackendFoodItem with ID: " + id);
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "listFoodItem",
            path = "backendFoodItem",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<BackendFoodItem> listFoodItem(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit, User user) throws ServiceException
    {
        AuthUtil.throwIfNotAuthenticated(user);

        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<BackendFoodItem> query = ofy().load().type(BackendFoodItem.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<BackendFoodItem> queryIterator = query.iterator();
        List<BackendFoodItem> backendFoodItemList = new ArrayList<BackendFoodItem>(limit);
        while (queryIterator.hasNext()) {
            backendFoodItemList.add(queryIterator.next());
        }
        return CollectionResponse.<BackendFoodItem>builder().setItems(backendFoodItemList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    private void checkExists(Long id) throws NotFoundException {
        try {
            ofy().load().type(BackendFoodItem.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find BackendFoodItem with ID: " + id);
        }
    }
}