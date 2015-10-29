package senior_project.foodscanner.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import senior_project.foodscanner.Meal;

import java.io.File;
import java.util.Calendar;

import senior_project.foodscanner.R;

/**
 * Shows details of the meal and allows editing.
 * <p/>
 * Details:
 * Date and time
 * Meal Type
 * Total nutrition
 * List of food items and each nutrition and volume
 * <p/>
 * Actions:
 * Add Food Button
 * - Pop up menu
 * - Scan Food - takes user to Food Scanner activity
 * - Manually add Food Item - takes user to Food Item activity
 * Delete Food
 * Edit Date and time
 * Edit Meal Type
 * Delete Meal
 * Back Button - return to Meal Calendar
 */
public class MealDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private Meal meal;
    private static final int REQUEST_FOODSCANNER = 0;

    private String[] meals;
    private Spinner mealSpinner;

    // region Times that meal spinner will start defaulting to each meal (in min, 24-hour format)
    // TODO: Allow user to set these times
    public int breakfastTime = 240; // 3:00 (am)
    public int brunchTime = 600;    // 10:00 (am)
    public int lunchTime = 720;     // 12:00 (am)
    public int snackTime = 900;     // 15:00 (3:00pm)
    public int dinnerTime = 1080;   // 18:00 (6:00pm)
    public int dessertTime = 1260;  // 21:00 (9:00pm)
    // endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        meal = (Meal) getIntent().getSerializableExtra("meal");

        setContentView(R.layout.activity_meal_details);

        // Set up FoodScanner button
        Button scan_button = (Button) findViewById(R.id.button_foodscanner);
        scan_button.setOnClickListener(this);

        // Set up Add Food button
        Button add_button = (Button) findViewById(R.id.button_addfood);
        add_button.setOnClickListener(this);

        // Set up meal selection spinner
        mealSpinner = (Spinner) findViewById(R.id.spinner_meal);
        meals = getResources().getStringArray(R.array.meal_list);
        ArrayAdapter<String> mealAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, meals);
        mealAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mealSpinner.setAdapter(mealAdapter);

        // Set default meal, based on time of day
        Calendar cal = Calendar.getInstance();
        int minute = cal.get(Calendar.MINUTE);
        int hour = cal.get(Calendar.HOUR_OF_DAY); //24-hour format
        int time = minute + (hour * 60);
        String defaultMeal;

        if(time >= breakfastTime && time < brunchTime) {
            defaultMeal = "Breakfast";
        } else if(time >= brunchTime && time < lunchTime) {
            defaultMeal = "Brunch";
        } else if(time >= lunchTime && time < snackTime) {
            defaultMeal = "Lunch";
        } else if(time >= snackTime && time < dinnerTime) {
            defaultMeal = "Snack";
        } else if(time >= dinnerTime && time < dessertTime) {
            defaultMeal = "Dinner";
        } else {
            defaultMeal = "Dessert";
        }

        mealSpinner.setSelection(mealAdapter.getPosition(defaultMeal));

        // Set up what meal selection does
        mealSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Currently, do nothing. Below was for testing.
                //String text = mealSpinner.getSelectedItem().toString();
                //(Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT)).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // Do nothing
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_meal_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button_foodscanner) {
            Intent intent = new Intent(MealDetailsActivity.this, PhotoTakerActivity.class);
            intent.putExtra("pic_names", new String[]{"Top", "Side"});
            startActivityForResult(intent, REQUEST_FOODSCANNER);
        } else if(v.getId() == R.id.button_addfood) {
            startActivity(new Intent(MealDetailsActivity.this, FoodItemActivity.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_FOODSCANNER:
                if(resultCode == Activity.RESULT_OK) {
                    if(data.hasExtra(PhotoTakerActivity.RESULT_IMAGE_FILES)) {
                        File[] imgFiles = (File[]) data.getSerializableExtra(PhotoTakerActivity.RESULT_IMAGE_FILES);
                        //TODO go to food drawing activity with these file, or you can find them with ImageDirectoryManager.getImageDirectory().list()
                        //TODO delete files after they are not needed anymore. To do this use ImageDirectoryManager.clearImageDirectory()
                    }
                }
                break;
            default:
                break;
        }
    }

}
