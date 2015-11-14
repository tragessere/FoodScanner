package senior_project.foodscanner.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.backend.foodScannerBackendAPI.model.DensityEntry;

import senior_project.foodscanner.FoodItem;
import senior_project.foodscanner.Meal;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

import senior_project.foodscanner.R;
import senior_project.foodscanner.backend_helpers.EndpointsHelper;
import senior_project.foodscanner.fragments.FoodDensityFragment;
import senior_project.foodscanner.fragments.FoodInfoFragment;
import senior_project.foodscanner.fragments.FoodServingFragment;
import senior_project.foodscanner.fragments.FoodVolumeFragment;

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
public class MealDetailsActivity extends AppCompatActivity implements View.OnClickListener,
        FoodInfoFragment.FoodInfoDialogListener, FoodDensityFragment.FoodDensityDialogListener,
        FoodVolumeFragment.FoodVolumeDialogListener, FoodServingFragment.FoodServingDialogListener {

    private Meal meal;
    private static final int REQUEST_FOODSCANNER = 0;
    private static final int NEW_FOOD_ITEM = 1;
    private static final int REPLACE_FOOD_ITEM = 2;
    private static final int REQUEST_DENSITY = 3;

    private double volume;
    private String[] meals;
    private Spinner mealSpinner;
    private FoodItem removedFood;
    private FoodItem lastClickedFood;

    // TODO add ui to display date of meal (Settings class has some useful functions to format a date)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Query density database, if necessary
        if (FoodItem.getDensityKeys() == null) {
            EndpointsHelper.mEndpoints.new GetAllDensityEntriesTask(this).execute();
        }

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
        mealSpinner.setSelection(mealAdapter.getPosition(meal.getType().getName()));

        // Doesn't do anything now, left in just in case.
        if (meal.isNew()) {
            meal.setIsNew(false);
        }

        // Set up what meal selection does
        mealSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Set the meal type
                String typeStr = mealSpinner.getSelectedItem().toString().toUpperCase();
                meal.setType(Meal.MealType.valueOf(typeStr));
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
    public void onResume(){
        super.onResume();

        // Set up list of food items
        ListView lv = (ListView) findViewById(R.id.food_list);
        ArrayAdapter<FoodItem> arrayAdapter = new ArrayAdapter<>(
                getApplicationContext(),
                R.layout.list_layout_added_food,
                R.id.foodListText,
                meal.getFood());
        lv.setAdapter(arrayAdapter);

        // Set up what happens when you click a list item
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Open appropriate dialog for this food item
                lastClickedFood = meal.getFoodItem(position);
                if (lastClickedFood.usesMass()) {
                    // Bring up density dialog
                    // Check that densities have been successfully retrieved
                    if (FoodItem.getDensityKeys() == null) {
                        Toast butteredToast = Toast.makeText(getApplicationContext(),
                                "Error: Cannot set density at this time.", Toast.LENGTH_LONG);
                        butteredToast.show();
                    } else {
                        //DialogFragment dialog = FoodDensityFragment.newInstance(food);
                        //dialog.show(getFragmentManager(), "FoodDensityFragment");
                        Intent intent = new Intent(MealDetailsActivity.this, FoodDensityActivity.class);
                        intent.putExtra("food", lastClickedFood);
                        intent.putExtra("meal", meal);
                        startActivityForResult(intent, REQUEST_DENSITY);
                    }
                } else if (lastClickedFood.usesVolume()) {
                    // Bring up (temporary) volume dialog
                    DialogFragment dialog = FoodVolumeFragment.newInstance(lastClickedFood);
                    dialog.show(getFragmentManager(), "FoodVolumeFragment");
                } else {
                    // Bring up servings dialog
                    DialogFragment dialog = FoodServingFragment.newInstance(lastClickedFood);
                    dialog.show(getFragmentManager(), "FoodServingFragment");
                }
            }
        });

        // Set up what happens when you long click a list item
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //View nutrition info & give option to replace or delete
                FoodItem food = meal.getFoodItem(position);
                DialogFragment dialog = FoodInfoFragment.newInstance(food, true);
                dialog.show(getFragmentManager(), "FoodInfoFragment");
                return true;
            }
        });

    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("meal", meal);
        setResult(Activity.RESULT_OK, resultIntent);

        super.onBackPressed();
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
//            Intent intent = new Intent(MealDetailsActivity.this, PhotoTakerActivity.class);
//            intent.putExtra("pic_names", new String[]{"Top", "Side"});
//            startActivityForResult(intent, REQUEST_FOODSCANNER);
            Toast butteredToast = Toast.makeText(getApplicationContext(), "Please choose food item first.",
                    Toast.LENGTH_SHORT);
            butteredToast.show();

        } else if(v.getId() == R.id.button_addfood) {
            Intent intent = new Intent(MealDetailsActivity.this, FoodItemActivity.class);
            intent.putExtra("meal", meal);
            intent.putExtra("requestCode", NEW_FOOD_ITEM);
            startActivityForResult(intent, NEW_FOOD_ITEM);
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

                    lastClickedFood.setVolume(data.getDoubleExtra(PhotoTakerActivity.RESULT_VOLUME, -1.0));
                }
                break;
            case REQUEST_DENSITY:
                //fall through
            case NEW_FOOD_ITEM:
                //fall through
            case REPLACE_FOOD_ITEM:
                if (resultCode == RESULT_OK) {
                    meal = (Meal) data.getSerializableExtra("meal");
                }
                break;
            default:
                break;
        }
    }

    //region Dialog click handlers

    // This is for the food info dialog
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button - "Replace"
        // Save FoodItem being edited
        FoodInfoFragment frag = (FoodInfoFragment)dialog;
        removedFood = frag.food;

        // Open food searching page
        Intent intent = new Intent(MealDetailsActivity.this, FoodItemActivity.class);
        intent.putExtra("meal", meal);
        intent.putExtra("requestCode", REPLACE_FOOD_ITEM);
        intent.putExtra("foodItem", removedFood);
        startActivityForResult(intent, REPLACE_FOOD_ITEM);
    }

    // This is for the food info dialog
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button - "Delete"

        FoodInfoFragment frag = (FoodInfoFragment)dialog;
        removedFood = frag.food;

        // Create confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete \"" + removedFood.getName() + " (" +
                removedFood.getBrand() + ")\"?")
                .setTitle("Confirm Food Deletion");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked 'Delete' button
                // Delete food item from meal
                meal.removeFoodItem(removedFood);

                // Update listview
                ListView lv = (ListView) findViewById(R.id.food_list);
                ArrayAdapter<FoodItem> arrayAdapter = new ArrayAdapter<>(
                        getApplicationContext(),
                        R.layout.list_layout_added_food,
                        R.id.foodListText,
                        meal.getFood());
                lv.setAdapter(arrayAdapter);

                Toast butteredToast = Toast.makeText(getApplicationContext(), "Removed from meal.",
                        Toast.LENGTH_SHORT);
                butteredToast.show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        // Create the AlertDialog
        AlertDialog confirmDialog = builder.create();
        confirmDialog.show();
    }

    // This is for the food info dialog
    @Override
    public void onDialogNeutralClick(DialogFragment dialog) {
        // User touched the dialog's neutral button - "Cancel"
        // Do nothing, besides exit dialog.
    }

    // This is for the food density dialog
    @Override
    public void onDensityDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button - "Scan Food"
        // TODO: Start FoodScanner for result. Save volume to selected FoodItem.
    }

    // This is for the food density dialog
    @Override
    public void onDensityDialogNeutralClick(DialogFragment dialog) {
        // User touched the dialog's neutral button - "Cancel"
        // Do nothing, besides exit dialog.
    }

    // This is for the food volume dialog
    @Override
    public void onVolumeDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button - "Scan Food"
        Intent intent = new Intent(MealDetailsActivity.this, PhotoTakerActivity.class);
        intent.putExtra("pic_names", new String[]{"Top", "Side"});
        startActivityForResult(intent, REQUEST_FOODSCANNER);
    }

    // This is for the food volume dialog
    @Override
    public void onVolumeDialogNeutralClick(DialogFragment dialog) {
        // User touched the dialog's neutral button - "Cancel"
        // Do nothing, besides exit dialog.
    }

    // This is for the food serving dialog
    @Override
    public void onServingDialogNeutralClick(DialogFragment dialog) {
        // User touched the dialog's neutral button - "Cancel"
        // Do nothing, besides exit dialog.
    }

    //endregion

}
