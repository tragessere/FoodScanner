package com.example.backend.apis;

import com.example.backend.Constants;
import com.example.backend.model.DensityEntry;
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

public class DensityEntryEndpoint {

    /**
     * Gets density entry with specified name.
     * @param name
     * @return density entry with name equal to the specified name.
     */
    @ApiMethod(name = "getDensity")
    public DensityEntry getDensityEntry(@Named("name") String name) {
        return ofy().load().type(DensityEntry.class).filter("name", name).list().get(0);
    }

    /**
     * Gets all of the density entries in the data store.
     * @return CollectionResponse of all density entries.
     */
    @ApiMethod(name = "getAllDensityEntries")
    public CollectionResponse<DensityEntry> getAllDensityEntries() {

        Query<DensityEntry> query = ofy().load().type(DensityEntry.class);
        List<DensityEntry> results = new ArrayList<DensityEntry>();
        QueryResultIterator<DensityEntry> iterator = query.iterator();

        while (iterator.hasNext()) {
            results.add(iterator.next());
        }

        return CollectionResponse.<DensityEntry>builder().setItems(results).build();
    }

    /**
     * Gets density entries with name similar to the name provided.
     * @param name name of the density entry
     * @return CollectionResponse of of density entries partially matching the provided name
     */
    @ApiMethod(name = "getDensitiesWithNameSimilarTo")
    public CollectionResponse<DensityEntry> getDensitiesWithNameSimilarTo(@Named("name") String name) {

        Query<DensityEntry> query = ofy().load().type(DensityEntry.class);
        List<DensityEntry> results = new ArrayList<DensityEntry>();
        QueryResultIterator<DensityEntry> iterator = query.iterator();

        DensityEntry entry;
        while (iterator.hasNext())
        {
            entry = iterator.next();
            String[] words = name.split("\\s+");
            for (String word : words)
            {
                String cleanedWord = word.replaceAll("[^\\w]","");
                boolean contains = entry.getName().toLowerCase().matches(".*\\b" + cleanedWord.toLowerCase() + "\\b.*");
                if(contains) {
                    results.add(entry);
                }
            }
        }

        return CollectionResponse.<DensityEntry>builder().setItems(results).build();
    }
}
