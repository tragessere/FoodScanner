package senior_project.foodscanner.activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.JsonToken;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import senior_project.foodscanner.FoodItem;
import senior_project.foodscanner.Meal;
import senior_project.foodscanner.R;

import com.example.backend.foodScannerBackendAPI.FoodScannerBackendAPI;
//import com.example.backend.foodScannerBackendAPI.model.FoodItem;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import senior_project.foodscanner.backend_helpers.EndpointBuilderHelper;
import senior_project.foodscanner.fragments.FoodDensityFragment;
import senior_project.foodscanner.fragments.FoodInfoFragment;
import senior_project.foodscanner.fragments.FoodServingFragment;

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
public class FoodItemActivity extends AppCompatActivity implements View.OnClickListener,
        FoodInfoFragment.FoodInfoDialogListener, FoodServingFragment.FoodServingDialogListener,
        FoodDensityFragment.FoodDensityDialogListener {

    private Meal meal;
    private FoodItem replacedFood = null;

    private static int NEW_FOOD_ITEM = 1;  //TODO: add to constants.java
    private static int REPLACE_FOOD_ITEM = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        replacedFood = null;
        meal = (Meal) getIntent().getSerializableExtra("meal");
        int requestCode = getIntent().getIntExtra("requestCode", 0);  //0 is arbitrary
        if (requestCode == REPLACE_FOOD_ITEM) {
            replacedFood = (FoodItem) getIntent().getSerializableExtra("foodItem");
        }

        setContentView(R.layout.activity_food_item);

        // Set up Search button
        Button searchButton = (Button) findViewById(R.id.foodSearchButton);
        searchButton.setOnClickListener(this);

        // Set up search field
        final Activity thisAct = this;
        EditText editText = (EditText) findViewById(R.id.searchTerm);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    // Get search term(s)
                    String searchTerm = ((EditText) findViewById(R.id.searchTerm)).getText().toString();

                    //Create a new async task for searching
                    new FoodSearch(thisAct, searchTerm).execute();

                    handled = true;
                }
                return handled;
            }
        });
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.foodSearchButton) {
            // User initiates a food search
            // Get search term(s)
            String searchTerm = ((EditText) findViewById(R.id.searchTerm)).getText().toString();

            //Create a new async task for searching
            new FoodSearch(this, searchTerm).execute();
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button - "Add to Meal"
        FoodInfoFragment frag = (FoodInfoFragment)dialog;

        if (replacedFood == null) {
            // First prompt for density or servings
            if (frag.food.usesMass()) {
                if (FoodItem.getAllDensities() == null) {
                    displayToast("Error: Cannot access densities.", this);
                    return;
                }
                findDensityMatches(frag.food);
            } else if (!frag.food.usesVolume()) {
                DialogFragment servingsDialog = FoodServingFragment.newInstance(frag.food, false);
                servingsDialog.show(getFragmentManager(), "FoodServingFragment");
            } else {
                // Only uses volume, nothing else needed
                meal.addFoodItem(frag.food);
                displayToast("Added to meal.", this);
                saveAndFinish();
            }

        } else {
            // Replace previously added food item
            if (frag.food.usesMass()) {
                if (FoodItem.getAllDensities() == null) {
                    displayToast("Error: Cannot access densities.", this);
                    return;
                }
                findDensityMatches(frag.food);
            } else if (!frag.food.usesVolume()) {
                DialogFragment servingsDialog = FoodServingFragment.newInstance(frag.food, false);
                servingsDialog.show(getFragmentManager(), "FoodServingFragment");
            } else {
                // Only uses volume, nothing else needed
                meal.replaceFoodItem(replacedFood, frag.food);
                displayToast("Replaced food item.", this);
                saveAndFinish();
            }
        }

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button - DNE
        // Cannot get called currently
    }

    @Override
    public void onDialogNeutralClick(DialogFragment dialog) {
        // User touched the dialog's neutral button - "Cancel"
        // Do nothing, besides exit dialog
    }

    public static void displayToast(final String message, Activity actArg) {
        final Activity act = actArg;
        act.runOnUiThread(new Runnable() {
            public void run() {
                Toast butteredToast = Toast.makeText(act.getApplicationContext(), message, Toast.LENGTH_SHORT);
                //butteredToast.setGravity(Gravity.CENTER, 0, 0);
                butteredToast.show();
            }
        });
    }

    @Override
    public void onServingDialogNeutralClick(DialogFragment dialog) {
        // Do nothing
    }

    @Override
    public void onServingDialogPositiveClick(DialogFragment dialog) {
        // Clicked "Save" in servings dialog. Add the food to meal.
        FoodServingFragment frag = (FoodServingFragment) dialog;
        if (replacedFood == null) {
            meal.addFoodItem(frag.food);
            displayToast("Added to meal.", this);
            saveAndFinish();
        } else {
            meal.replaceFoodItem(replacedFood, frag.food);
            displayToast("Replaced food item.", this);
            saveAndFinish();
        }
    }

    private void saveAndFinish() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("meal", meal);
        setResult(Activity.RESULT_OK, resultIntent);

        finish();
    }

    private void findDensityMatches(FoodItem food) {
        new getDensityPartialMatches(this, food).execute();
    }

    public void findDensityMatchesCallback(Map<String, Double> matches, FoodItem food) {
        if (matches.size() == 0) {
            displayToast("Error: No density matches found.", this);
        } else if (matches.size() == 1) {
            // Only one match found, add it automatically
            Map.Entry<String, Double> entry = matches.entrySet().iterator().next();
            food.setDensity(entry.getValue());
            food.setDensityName(entry.getKey());
            if (replacedFood == null) {
                meal.addFoodItem(food);
                displayToast("Added to meal.", this);
                saveAndFinish();
            } else {
                meal.replaceFoodItem(replacedFood, food);
                displayToast("Replaced food item.", this);
                saveAndFinish();
            }
        } else {
            // Multiple matches. Display dialog list to allow user to pick
            displayToast("Multiple densities found.", this);
            DialogFragment dialog = FoodDensityFragment.newInstance(food, matches);
            dialog.show(getFragmentManager(), "FoodDensityFragment");
        }
    }

    // This is for the density dialog, if mulitple matches
    @Override
    public void onDensityDialogPositiveClick(DialogFragment dialog, String name, Double value) {
        // User chose a density item for the food item. Save the food item.
        FoodDensityFragment frag = (FoodDensityFragment)dialog;
        frag.food.setDensity(value);
        frag.food.setDensityName(name);
        if (replacedFood == null) {
            meal.addFoodItem(frag.food);
            displayToast("Added to meal.", this);
            saveAndFinish();
        } else {
            meal.replaceFoodItem(replacedFood, frag.food);
            displayToast("Replaced food item.", this);
            saveAndFinish();
        }
    }

    // This is for the density dialog, if mulitple matches
    @Override
    public void onDensityDialogNeutralClick(DialogFragment dialog) {
        // User chose "cancel"; do nothing
    }

    private class FoodSearch extends AsyncTask<String, Void, Boolean> {

        private Activity act;
        private String searchTerm;
        private ProgressDialog dialog;
        private SearchResults results;

        //region Strings for nutritionix API
        //TODO: add to constants.java
        final String API_ID = getResources().getString(R.string.nutritionix_api_id);
        final String API_KEY = getResources().getString(R.string.nutritionix_api_key);
        //endregion

        FoodSearch(Activity act, String searchTerm) {
            this.act = act;
            this.searchTerm = searchTerm;
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(act);
            dialog.setMessage("Loading...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {
            // Format search term(s)
            searchTerm = searchTerm.replace(" ", "%20");

            // Construct GET request
            String urlStr = "https://api.nutritionix.com/v1_1/search/" + searchTerm +
                    "?results=0%3A50&fields=item_name%2Citem_id%2Cbrand_name&appId=" + API_ID +
                    "&appKey=" + API_KEY;
            //System.out.println(urlStr);
            URL url;
            try {
                url = new URL(urlStr);
            } catch (Exception e) {
                e.printStackTrace();
                displayToast("Error: Please try again.", act);
                return false;
            }

            HttpURLConnection urlConnection = null;
            InputStream in = null;

            // Make GET request and save input
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                if (urlConnection.getResponseCode() != 200) {
                    throw new Exception(urlConnection.getErrorStream().toString());
                }
                in = new BufferedInputStream(urlConnection.getInputStream());

            } catch (Exception e) {
                e.printStackTrace();
                displayToast("Error: Please try again.", act);
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                return false;
            }

            JsonReader reader = null;
            results = null;
            try {
                reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
                results = readResults(reader);
            } catch (Exception e) {
                e.printStackTrace();
                displayToast("Error: Please try again.", act);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        displayToast("Error: Please try again.", act);
                    }
                }
            }

            urlConnection.disconnect();

            if (results.count == 0) {
                displayToast("No results found.", act);
            } else {
                displayToast("Found " + results.count + " matches.", act);
                act.runOnUiThread(new Runnable() {
                    public void run() {
                        ListView lv = (ListView) act.findViewById(R.id.resultList);
                        ArrayAdapter<FoodResult> arrayAdapter = new ArrayAdapter<FoodResult>(
                                act.getApplicationContext(),
                                R.layout.list_layout,
                                R.id.foodListText,
                                results.list);
                        lv.setAdapter(arrayAdapter);

                        // set up happens when you click a list item
                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                FoodResult selectedFood = results.list.get(position);

                                // query database for nutrition info in async task
                                // will also display confirmation prompt with info
                                new NutritionInfoQuery(act, selectedFood.id).execute();
                            }
                        });

                    }
                });
            }

            return true;
        }

        private SearchResults readResults(JsonReader reader) throws IOException {
            SearchResults results = new SearchResults();
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("total_hits")) {
                    results.count = reader.nextInt();
                    if (results.count == 0) {
                        return results;
                    }
                } else if (name.equals("max_score")) {
                    double max_score = reader.nextDouble();
                    // we currently do nothing with this value
                } else if (name.equals("hits")) {
                    results.list = readHitsArray(reader);
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            return results;
        }

        private List readHitsArray(JsonReader reader) throws IOException {
            List hits = new ArrayList<FoodResult>();

            reader.beginArray();
            while (reader.hasNext()) {
                hits.add(readHitObject(reader));
            }
            reader.endArray();
            return hits;
        }

        private FoodResult readHitObject(JsonReader reader) throws IOException {
            FoodResult food = new FoodResult();

            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("_index")) {
                    String index = reader.nextString();
                    // we currently do nothing with this value
                } else if (name.equals("_type")) {
                    String type = reader.nextString();
                    // we currently do nothing with this value
                } else if (name.equals("_id")) {
                    food.id = reader.nextString();
                } else if (name.equals("_score")) {
                    double score = reader.nextDouble();
                    // we currently do nothing with this value
                } else if (name.equals("fields")) {
                    readHitFields(reader, food);
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            return food;
        }

        private void readHitFields(JsonReader reader, FoodResult food) throws IOException {
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("item_id")) {
                    String id = reader.nextString();
                    // we already saved the ID of this food
                } else if (name.equals("item_name")) {
                    food.name = reader.nextString();
                } else if (name.equals("brand_name")) {
                    food.brand = reader.nextString();
                } else if (name.equals("nf_serving_size_qty")) {
                    double serving = reader.nextDouble();
                    // we currently do nothing with this value
                } else if (name.equals("nf_serving_size_unit")) {
                    String servingUnit = reader.nextString();
                    // we currently do nothing with this value
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            return;
        }

        /**
         * Object representing search results
         */
        private class SearchResults {
            int count; // total number of hits from search term(s)
            List<FoodResult> list; // list of food items matching term(s)
        }

        /**
         * Object for each food item result.
         * ID used later to retrieve all nutrition info.
         */
        private class FoodResult {
            String id; // food id, used for retrieving info
            String name; // name of food
            String brand; // name of brand associated with food

            @Override
            public String toString() {
                return name + " (" + brand + ")";
            }
        }

    }

    private class NutritionInfoQuery extends AsyncTask<String, Void, Boolean> {

        private Activity act;
        private String id;
        private ProgressDialog dialog;
        private FoodItem food;

        //region Strings for nutritionix API
        final String API_ID = getResources().getString(R.string.nutritionix_api_id);
        final String API_KEY = getResources().getString(R.string.nutritionix_api_key);
        //endregion

        NutritionInfoQuery(Activity act, String id) {
            this.act = act;
            this.id = id;
            food = null;
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(act);
            dialog.setMessage("Loading...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            if (food != null) {
                // display confirmation prompt containing nutrition info
                DialogFragment dialog = FoodInfoFragment.newInstance(food, false);
                dialog.show(getFragmentManager(), "FoodInfoFragment");
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {
            // Construct GET request
            String urlStr = "https://api.nutritionix.com/v1_1/item?id=" + id +
                    "&appId=" + API_ID + "&appKey=" + API_KEY;
            System.out.println(urlStr);
            URL url;
            try {
                url = new URL(urlStr);
            } catch (Exception e) {
                e.printStackTrace();
                displayToast("Error: Please try again.", act);
                return false;
            }

            HttpURLConnection urlConnection = null;
            InputStream in = null;

            // Make GET request and save input
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                if (urlConnection.getResponseCode() != 200) {
                    throw new Exception(urlConnection.getErrorStream().toString());
                }
                in = new BufferedInputStream(urlConnection.getInputStream());

            } catch (Exception e) {
                e.printStackTrace();
                displayToast("Error: Please try again.", act);
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                return false;
            }

            JsonReader reader = null;
            try {
                reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
                food = readFoodInfo(reader);
            } catch (Exception e) {
                e.printStackTrace();
                displayToast("Error: Please try again.", act);
                food = null;  //just to be safe
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        displayToast("Error: Please try again.", act);
                    }
                }
            }

            urlConnection.disconnect();
            return true;
        }

        private FoodItem readFoodInfo(JsonReader reader) throws IOException {
            FoodItem food = new FoodItem();
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("item_name") && reader.peek() != JsonToken.NULL) {
                    food.setName(reader.nextString());
                } else if (name.equals("brand_name") && reader.peek() != JsonToken.NULL) {
                    food.setBrand(reader.nextString());
                } else if (name.equals("nf_serving_size_qty") && reader.peek() != JsonToken.NULL) {
                    food.setServingSize(reader.nextDouble());
                } else if (name.equals("nf_serving_size_unit") && reader.peek() != JsonToken.NULL) {
                    food.setServingSizeUnit(reader.nextString());
                } else if (name.equals("nf_calories")) {
                    if (reader.peek() == JsonToken.NULL) {
                        food.setField("Calories", 0.0);
                        reader.skipValue();
                    }
                    else {
                        food.setField("Calories", reader.nextDouble());
                    }
                } else if (name.equals("nf_total_fat")) {
                    if (reader.peek() == JsonToken.NULL) {
                        food.setField("Total Fat", 0.0);
                        reader.skipValue();
                    }
                    else {
                        food.setField("Total Fat", reader.nextDouble());
                    }
                } else if (name.equals("nf_sodium")) {
                    if (reader.peek() == JsonToken.NULL) {
                        food.setField("Sodium", 0.0);
                        reader.skipValue();
                    }
                    else {
                        food.setField("Sodium", reader.nextDouble());
                    }
                } else if (name.equals("nf_total_carbohydrate")) {
                    if (reader.peek() == JsonToken.NULL) {
                        food.setField("Total Carbohydrate", 0.0);
                        reader.skipValue();
                    }
                    else {
                        food.setField("Total Carbohydrate", reader.nextDouble());
                    }
                } else if (name.equals("nf_sugars")) {
                    if (reader.peek() == JsonToken.NULL) {
                        food.setField("Sugars", 0.0);
                        reader.skipValue();
                    }
                    else {
                        food.setField("Sugars", reader.nextDouble());
                    }
                } else if (name.equals("nf_protein")) {
                    if (reader.peek() == JsonToken.NULL) {
                        food.setField("Protein", 0.0);
                        reader.skipValue();
                    }
                    else {
                        food.setField("Protein", reader.nextDouble());
                    }
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            return food;
        }
    }

    private class getDensityPartialMatches extends AsyncTask<Void, Void, Map<String, Double>> {

        private Activity act;
        private FoodItem food;
        private ProgressDialog dialog;

        getDensityPartialMatches(Activity act, FoodItem food) {
            this.act = act;
            this.food = food;
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(act);
            dialog.setMessage("Loading...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected void onPostExecute(Map<String, Double> results) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            findDensityMatchesCallback(results, food);
        }

        @Override
        protected Map<String, Double> doInBackground(Void... params) {
            Map<String, Double> results = new HashMap<>();

            for (Map.Entry<String, Double> entry : FoodItem.getAllDensities().entrySet()) {
                String[] words = food.getName().split("\\s+");
                for (String word : words) {
                    //boolean contains = entry.getKey().toLowerCase().matches(".*\\b" + word.toLowerCase() + "\\b.*");
                    String cleanedWord = word.replaceAll("[^\\w]", "");
                    if (cleanedWord.equals("")) {
                        continue;
                    }
                    boolean contains = entry.getKey().toLowerCase().matches(".*\\b" + cleanedWord.toLowerCase() + "\\b.*");
                    if (contains) {
                        results.put(entry.getKey(), entry.getValue());
                    }
                }
            }

            return results;
        }
    }
}