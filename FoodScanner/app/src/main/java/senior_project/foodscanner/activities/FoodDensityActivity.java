package senior_project.foodscanner.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import senior_project.foodscanner.FoodItem;
import senior_project.foodscanner.Meal;
import senior_project.foodscanner.R;

/**
 * Created by Tyler on 11/5/2015.
 */
public class FoodDensityActivity extends AppCompatActivity implements View.OnClickListener {

    private FoodItem oldFood;  //not modified until onBackPressed
    private FoodItem newFood;
    private Meal meal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        oldFood = (FoodItem)getIntent().getSerializableExtra("food");
        newFood = oldFood;
        meal = (Meal)getIntent().getSerializableExtra("meal");

        setContentView(R.layout.activity_food_density);

        // Set up FoodScanner button
        Button scan_button = (Button) findViewById(R.id.button_foodscanner);
        scan_button.setOnClickListener(this);

        final TextView densityValue = (TextView)findViewById(R.id.densityValue);
        final TextView densityEntry = (TextView)findViewById(R.id.densityEntry);
        final AutoCompleteTextView autoView = (AutoCompleteTextView)findViewById(R.id.densitySearch);

        // If density not yet found, display default text for density
        if (newFood.getDensity() == 0.0) {
            densityValue.setText(Html.fromHtml("<b>Value:</b> Please search for entry."));
            densityEntry.setText(Html.fromHtml("<b>Entry:</b> Please search for entry."));
        } else {
            densityValue.setText(Html.fromHtml("<b>Value:</b> " + newFood.getDensity() + " g/ml"));
            densityEntry.setText(Html.fromHtml("<b>Entry:</b> " + newFood.getDensityName()));
        }

        // Set up AutoCompleteTextView for densities
        String[] densityKeys = FoodItem.getDensityKeys();

        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, densityKeys);
        autoView.setAdapter(adapter);
        autoView.setThreshold(1);
        autoView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // User selected density entry to use for this FoodItem

                String selectedEntry = autoView.getText().toString();
                newFood.setDensityName(selectedEntry);
                newFood.setDensity(FoodItem.getDensityValue(selectedEntry));

                // Update dialog
                densityValue.setText(Html.fromHtml("<b>Value:</b> " + newFood.getDensity() + " g/ml"));
                densityEntry.setText(Html.fromHtml("<b>Entry:</b> " + newFood.getDensityName()));
            }
        });

        // Set up bottom-right button on keyboard
        autoView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideSoftKeyBoard();
                    handled = true;
                }
                return handled;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_food_density, menu);
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
    public void onBackPressed() {
        meal.replaceFoodItem(oldFood, newFood);
        Intent resultIntent = new Intent();
        resultIntent.putExtra("meal", meal);
        setResult(Activity.RESULT_OK, resultIntent);

        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button_foodscanner) {
//            Intent intent = new Intent(MealDetailsActivity.this, PhotoTakerActivity.class);
//            intent.putExtra("pic_names", new String[]{"Top", "Side"});
//            startActivityForResult(intent, REQUEST_FOODSCANNER);
            //TODO: start FoodScanner
            Toast butteredToast = Toast.makeText(getApplicationContext(),
                    "Error: FoodScanner not available at this time.", Toast.LENGTH_LONG);
            butteredToast.show();
        }
    }

    // Source: http://stackoverflow.com/questions/3553779/android-dismiss-keyboard
    private void hideSoftKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        if(imm.isAcceptingText()) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}
