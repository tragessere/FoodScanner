package senior_project.foodscanner.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import senior_project.foodscanner.R;

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
public class FoodItemActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_item);

        // Set up Search button
        Button searchButton = (Button) findViewById(R.id.foodSearchButton);
        searchButton.setOnClickListener(this);
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
            String searchTerm = ((EditText)findViewById(R.id.searchTerm)).getText().toString();

            //Create a new async task for searching
            new FoodSearch(this, searchTerm).execute();
        }
    }

}

class FoodSearch extends AsyncTask<String, Void, Boolean> {

    private Activity act;
    private String searchTerm;
    private ProgressDialog dialog;
    private SearchResults results;

    //region Strings for nutritionix API
    static final String API_ID = "4d784f94";
    static final String API_KEY = "a3e15aa81cdbe791ad05625dcc3d417a";
    //endregion

    FoodSearch (Activity act, String searchTerm) {
        this.act = act;
        this.searchTerm = searchTerm;
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(act);
        dialog.setMessage("Loading...");
        //dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if(dialog.isShowing()) {
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
            displayToast("Error: Please try again.");
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
            displayToast("Error: Network connection failed.");
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
        } catch (Exception e){
            e.printStackTrace();
            displayToast("Error: Please try again.");
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e){
                    e.printStackTrace();
                    displayToast("Error: Please try again.");
                }
            }
        }

        urlConnection.disconnect();

        if (results.count == 0) {
            displayToast("No results found.");
        }
        else {
            //TODO: Make a custom ListView layout, w/ better text color & brand in subtext.
            displayToast("Found " + results.count + " matches.");
            act.runOnUiThread(new Runnable() {
                public void run() {
                    ListView lv = (ListView)act.findViewById(R.id.resultList);
                    ArrayAdapter<FoodResult> arrayAdapter = new ArrayAdapter<FoodResult>(
                            act.getApplicationContext(),
                            android.R.layout.simple_list_item_1,
                            results.list);
                    lv.setAdapter(arrayAdapter);
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
            } else if (name.equals("max_score")){
                double max_score = reader.nextDouble();
                // we currently do nothing with this value
            } else if (name.equals("hits")){
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
        while(reader.hasNext()) {
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

    private void displayToast(final String message) {
        act.runOnUiThread(new Runnable() {
            public void run() {
                Toast butteredToast = Toast.makeText(act.getApplicationContext(), message, Toast.LENGTH_SHORT);
                butteredToast.setGravity(Gravity.CENTER, 0, 0);
                butteredToast.show();
            }
        });
    }

}

/**
 * Object representing search results
 */
class SearchResults {
    int count; // total number of hits from search term(s)
    List<FoodResult> list; // list of food items matching term(s)
}

/**
 * Object for each food item result.
 * ID used later to retrieve all nutrition info.
 */
class FoodResult {
    String id; // food id, used for retrieving info
    String name; // name of food
    String brand; // name of brand associated with food

    @Override
    public String toString() {
        return name;
    }
}