package com.example.backend.apis;

/**
 * Created by mlenarto on 9/26/15.
 */

import com.example.backend.Constants;
import com.example.backend.model.Calculation;
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

public class CalculationEndpoint {

    /**
     * Saves to provided calculation for the specified user.
     * @param calculation
     * @param user
     * @throws ServiceException
     */
    @ApiMethod(name = "recordCalculation")
    public void recordCalculation(Calculation calculation, User user) throws ServiceException {
        AuthUtil.throwIfNotAuthenticated(user);
        ofy().save().entity(calculation).now();
    }

    /**
     * Get list of calculation items for the specified user
     * @param user
     * @return CollectionResponse of calculation items
     * @throws ServiceException
     */
    @ApiMethod(name = "getCalculationHistory")
    public CollectionResponse<Calculation> getCalculationHistory(User user) throws ServiceException {
        AuthUtil.throwIfNotAuthenticated(user);

        Query<Calculation> query = ofy().load().type(Calculation.class);
        List<Calculation> results = new ArrayList<Calculation>();
        QueryResultIterator<Calculation> iterator = query.iterator();

        while (iterator.hasNext()) {
            results.add(iterator.next());
        }

        return CollectionResponse.<Calculation>builder().setItems(results).build();
    }
}
