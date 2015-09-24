package senior_project.foodscanner.backend_helpers;

import com.example.backend.foodScannerBackendAPI.FoodScannerBackendAPI;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.extensions.android.http.AndroidHttp;

/**
 * Created by mlenarto on 9/24/15.
 */
public final class EndpointBuilderHelper {

    /**
     * Default constructor, never called.
     */
    private EndpointBuilderHelper() {
    }

    /**
     * @return FoodScannerBackendAPI endpoints to the GAE backend.
     */
    public static FoodScannerBackendAPI getEndpoints() {

        FoodScannerBackendAPI.Builder builder = new FoodScannerBackendAPI.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                .setRootUrl("https://foodscannerwebapp.appspot.com/_ah/api/");

        return builder.build();
    }
}
