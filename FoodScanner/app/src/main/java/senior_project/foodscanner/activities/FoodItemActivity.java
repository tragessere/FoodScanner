package senior_project.foodscanner.activities;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import senior_project.foodscanner.R;

import com.example.backend.foodScannerBackendAPI.FoodScannerBackendAPI;
import com.example.backend.foodScannerBackendAPI.model.FoodItem;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import senior_project.foodscanner.backend_helpers.EndpointBuilderHelper;
/**
 * Activity for manually adding a food item.
 *
 * Food Item Details:
 *  Size - Volume/Mass/Servings/Portions/Quantity
 *  Calories per unit of size
 *  Other Nutrition facts per unit of size
 *
 * Actions:
 *  Cancel - return to Meal Details
 *  Ok - return to Meal Details with new food item added
 */
public class FoodItemActivity extends AppCompatActivity {

    private FoodScannerBackendAPI foodScannerBackendAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_item);

        // Handle to the GAE endpoints in the backend
        foodScannerBackendAPI = EndpointBuilderHelper.getEndpoints();

        new FoodItemsTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_food_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * AsyncTask for calling Food Scanner Backend API for getting all food items
     */
    private class FoodItemsTask extends AsyncTask<Void, Void, List<FoodItem>> {

        /**
         * Calls appropriate CloudEndpoint to indicate that user checked into a
         * place.
         *
         * @param params the place where the user is checking in.
         */
        @Override
        protected List<FoodItem> doInBackground(Void... params) {
            try {
                return foodScannerBackendAPI.getAllFoodItems().execute().getItems();
            } catch (IOException e) {
                return Collections.emptyList();
            }
        }

        @Override
        protected void onPostExecute(List<FoodItem> results) {
            Toast.makeText(getApplicationContext(), "PostExecute!", Toast.LENGTH_LONG).show();
            for (FoodItem f : results)
                Toast.makeText(getApplicationContext(), f.getName(), Toast.LENGTH_LONG).show();
        }
    }
}
