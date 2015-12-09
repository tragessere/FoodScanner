package senior_project.foodscanner.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import senior_project.foodscanner.Constants;
import senior_project.foodscanner.FoodItem;
import senior_project.foodscanner.ImageDirectoryManager;
import senior_project.foodscanner.Meal;

import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import senior_project.foodscanner.Nutritious;
import senior_project.foodscanner.R;
import senior_project.foodscanner.Settings;
import senior_project.foodscanner.backend_helpers.EndpointsHelper;
import senior_project.foodscanner.database.SQLQueryHelper;
import senior_project.foodscanner.fragments.FoodInfoFragment;
import senior_project.foodscanner.fragments.FoodServingFragment;
import senior_project.foodscanner.fragments.MessageDialogFragment;
import senior_project.foodscanner.fragments.PreviousPhotoFragment;
import senior_project.foodscanner.ui.components.fooditem.FoodArrayAdapter;
import senior_project.foodscanner.ui.components.tutorial.TutorialBaseActivity;
import senior_project.foodscanner.ui.components.tutorial.TutorialCard;

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
public class MealDetailsActivity extends TutorialBaseActivity implements View.OnClickListener,
        FoodInfoFragment.FoodInfoDialogListener, FoodServingFragment.FoodServingDialogListener,
        PreviousPhotoFragment.PreviousPhotoDialogListener, FoodArrayAdapter.FoodArrayAdapterListener {

    private Meal meal;
    private static final int REQUEST_FOODSCANNER = 0;
    private static final int NEW_FOOD_ITEM = 1;
    private static final int REPLACE_FOOD_ITEM = 2;
    private static final int REQUEST_DENSITY = 3;
    public static final int RESULT_FOOD_SCANNER = 4;

    public static final String RESULT_VOLUME = "volume";

    private String[] meals;
    private Spinner mealSpinner;
    private ListView mealList;
    private FoodItem removedFood;
    private FoodItem lastClickedFood;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dialog = new ProgressDialog(this);

        meal = (Meal) getIntent().getSerializableExtra("meal");

        setContentView(R.layout.activity_meal_details);

        // Set up Total Nutrition button
        Button total_button = (Button) findViewById(R.id.button_total_meal);
        total_button.setOnClickListener(this);

        // Set up Finish button
        Button finish_button = (Button) findViewById(R.id.button_finish);
        finish_button.setOnClickListener(this);

        // Set up date text
        TextView meal_date = (TextView) findViewById(R.id.textView_date);
        GregorianCalendar day = new GregorianCalendar();
        day.setTimeInMillis(meal.getDate());
        String date_text = "<b>" + Settings.getInstance().
                formatDate(day) + "</b>";
        meal_date.setText(Html.fromHtml(date_text));

        // Set up meal selection spinner
        mealSpinner = (Spinner) findViewById(R.id.spinner_meal);
        meals = getResources().getStringArray(R.array.meal_list);
        ArrayAdapter<String> mealAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, meals);
        mealAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mealSpinner.setAdapter(mealAdapter);
        mealSpinner.setSelection(mealAdapter.getPosition(meal.getType().getName()));

        mealList = (ListView) findViewById(R.id.food_list);

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
                SQLQueryHelper.updateMeal(meal);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // Do nothing
            }
        });
    }

    @Override
    public void setupTutorial() {
        TutorialCard page1 = new TutorialCard(mealList, getString(R.string.tutorial_meal_details_title), getString(R.string.tutorial_meal_details_list))
                .setHighlightPadding(-15)
                .setPosition(TutorialCard.POSITION_BOTTOM);
        TutorialCard page2 = new TutorialCard(mealList, getString(R.string.tutorial_meal_details_scan_title), getString(R.string.tutorial_meal_details_scan))
                .setHighlightPadding(-15)
                .setPosition(TutorialCard.POSITION_BOTTOM);
        TutorialCard page3 = new TutorialCard(mealList, getString(R.string.tutorial_meal_details_servings_title), getString(R.string.tutorial_meal_details_servings))
                .setHighlightPadding(-15)
                .setPosition(TutorialCard.POSITION_BOTTOM);
        TutorialCard page4 = new TutorialCard(mealList, getString(R.string.tutorial_meal_details_replace_title), getString(R.string.tutorial_meal_details_replace))
                .setHighlightPadding(-15)
                .setPosition(TutorialCard.POSITION_BOTTOM);

        sequence.addCard(page1);
        sequence.addCard(page2);
        sequence.addCard(page3);
        sequence.addCard(page4);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tutorial, menu);
        return true;
    }

    @Override
    public void onResume(){
        super.onResume();

        // Query density database, if necessary
        synchronized (EndpointsHelper.mEndpoints) {
            if (EndpointsHelper.getDownloadStatus() == Constants.DENSITY_NOT_DOWNLOADED || EndpointsHelper.getDownloadStatus() == Constants.DENSITY_DOWNLOADING) {
                dialog.setMessage("Loading...");
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                EndpointsHelper.registerDensityObserver(new EndpointsHelper.densityDownloadObserver() {
                    @Override
                    public void densityDownloaded() {
                        EndpointsHelper.registerDensityObserver(null);
                        dialog.dismiss();
                    }
                });

                if (EndpointsHelper.getDownloadStatus() == Constants.DENSITY_NOT_DOWNLOADED)
                    EndpointsHelper.mEndpoints.new GetAllDensityEntriesTask(this).execute();
            }
        }

        // Set up list of food items
        List<FoodItem> tempFood = new ArrayList<>(meal.getFood());
        FoodArrayAdapter adapter = new FoodArrayAdapter(MealDetailsActivity.this, tempFood);
        adapter.setOnDeleteListener(this);
        mealList.setAdapter(adapter);

        // Set up background
        if (meal.getFood().size() == 0) {
            mealList.setBackgroundResource(R.drawable.background_add_food);
        } else {
            mealList.setBackgroundResource(0);
        }

        // Set up what happens when you click a list item
        mealList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Check if null, i.e. the "+" was pressed
                if (position >= meal.getFood().size() || meal.getFoodItem(position) == null) {
                    // Add new food item
                    Intent intent = new Intent(MealDetailsActivity.this, FoodItemActivity.class);
                    intent.putExtra("meal", meal);
                    intent.putExtra("requestCode", NEW_FOOD_ITEM);
                    startActivityForResult(intent, NEW_FOOD_ITEM);
                } else {
                    // View nutrition info & give option to scan/enter servings, or replace
                    lastClickedFood = meal.getFoodItem(position);
                    FoodItem food = meal.getFoodItem(position);
                    DialogFragment dialog = FoodInfoFragment.newInstance(food, true);
                    dialog.show(getFragmentManager(), "FoodInfoFragment");
                }

            }
        });

//        // Set up what happens when you long click a list item
//        mealList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                //View nutrition info & give option to replace or delete
//                FoodItem food = meal.getFoodItem(position);
//                DialogFragment dialog = FoodInfoFragment.newInstance(food, true);
//                dialog.show(getFragmentManager(), "FoodInfoFragment");
//                return true;
//            }
//        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        EndpointsHelper.registerDensityObserver(null);
    }


    @Override
    public boolean backButtonPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("meal", meal);
        setResult(Activity.RESULT_OK, resultIntent);

        return true;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button_total_meal) {
            // Display total nutrition for the meal
            if (meal.getFood() == null || meal.getFood().size() == 0) {
                Toast butteredToast = Toast.makeText(this,
                        "No nutrition info to show.", Toast.LENGTH_SHORT);
                butteredToast.show();
                return;
            }

            List<FoodItem> foodList = meal.getFood();

            if (foodList.size() == 1) {
                FoodItem onlyFood = foodList.get(0);
                if ((onlyFood.usesVolume() || onlyFood.usesMass()) && onlyFood.getVolume() == 0.0) {
                    Toast butteredToast = Toast.makeText(this,
                            "Scan food item first.", Toast.LENGTH_SHORT);
                    butteredToast.setGravity(Gravity.CENTER, 0, 0);
                    butteredToast.show();
                    return;
                }
            }

            String title = "<b>Total Meal Nutrition</b>";
            MessageDialogFragment dialog = MessageDialogFragment.newInstance(Nutritious.nutritionText
                    (Nutritious.calculateTotalNutrition(foodList)), Html.fromHtml(title), 0);
            dialog.show(getFragmentManager(), "Total Meal");

        } else if (v.getId() == R.id.button_finish) {
            onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_FOODSCANNER:
                // User took new pictures, then used painting
                if(resultCode == Activity.RESULT_OK) {
                    if(data.hasExtra(PhotoTakerActivity.RESULT_IMAGE_FILES)) {
                        File[] imgFiles = (File[]) data.getSerializableExtra(PhotoTakerActivity.RESULT_IMAGE_FILES);
                        //TODO go to food drawing activity with these file, or you can find them with ImageDirectoryManager.getImageDirectory().list()
                        //TODO delete files after they are not needed anymore. To do this use ImageDirectoryManager.clearImageDirectory()
                    }

                    lastClickedFood.setVolume(data.getDoubleExtra(RESULT_VOLUME, -1.0));
                    SQLQueryHelper.updateMeal(meal);
                }
                break;
            case RESULT_FOOD_SCANNER:
                // User did painting with previous images
                if(resultCode == RESULT_OK) {
                    lastClickedFood.setVolume(data.getDoubleExtra(RESULT_VOLUME, -1.0));
                    SQLQueryHelper.updateMeal(meal);
                }
                break;
            case REQUEST_DENSITY:
                //fall through
            case NEW_FOOD_ITEM:
                //fall through
            case REPLACE_FOOD_ITEM:
                if (resultCode == RESULT_OK) {
                    meal = (Meal) data.getSerializableExtra("meal");
                    SQLQueryHelper.updateMeal(meal);
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
        // User touched the dialog's positive button - "Scan" or "Servings"
        FoodInfoFragment foodFrag = (FoodInfoFragment)dialog;
        if (foodFrag.food.usesMass() || foodFrag.food.usesVolume()) {
            // User touched "Scan" -> open dialog to use old photos or go to photo taker

            File topImage = new File(ImageDirectoryManager.
                    getImageDirectory(getApplicationContext()).getPath() + "/Top.png");
            File sideImage = new File(ImageDirectoryManager.
                    getImageDirectory(getApplicationContext()).getPath() + "/Side.png");

            if (!topImage.exists() || !sideImage.exists()) {
                // No previous images, go straight to photo taker
                Intent intent = new Intent(MealDetailsActivity.this, PhotoTakerActivity.class);
                intent.putExtra("pic_names", new String[]{"Top", "Side"});
                startActivityForResult(intent, REQUEST_FOODSCANNER);
            } else {
                // Ask user if they want to use previous images before proceeding
                DialogFragment photoDialog = PreviousPhotoFragment.newInstance();
                photoDialog.show(getFragmentManager(), "PreviousPhotoFragment");
            }

        } else {
            // User touched "Servings" -> open servings dialog
            DialogFragment servingsDialog = FoodServingFragment.newInstance(foodFrag.food);
            servingsDialog.show(getFragmentManager(), "FoodServingFragment");
        }
    }

    // This is for the food info dialog
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
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
    public void onDialogNeutralClick(DialogFragment dialog) {
        // User touched the dialog's neutral button - "Cancel"
        // Do nothing, besides exit dialog.
    }

    // This is for the food serving dialog
    @Override
    public void onServingDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button - "Save"
        // Servings already updated in dialog; just update meal in db
        SQLQueryHelper.updateMeal(meal);
        Toast butteredToast = Toast.makeText(this,
                "Saved servings.", Toast.LENGTH_SHORT);
        butteredToast.show();
    }

    // This is for the food serving dialog
    @Override
    public void onServingDialogNeutralClick(DialogFragment dialog) {
        // User touched the dialog's neutral button - "Cancel"
        // Do nothing, besides exit dialog.
    }

    // This is for previous photo dialog
    @Override
    public void onPhotoDialogNeutralClick(DialogFragment dialog) {
        // User touched "Cancel"
        // Do nothing
    }

    // This is for previous photo dialog
    @Override
    public void onPhotoDialogPositiveClick(DialogFragment dialog) {
        // User touched "New" - take new photos
        // Open photo taker activity
        Intent intent = new Intent(MealDetailsActivity.this, PhotoTakerActivity.class);
        intent.putExtra("pic_names", new String[]{"Top", "Side"});
        startActivityForResult(intent, REQUEST_FOODSCANNER);
    }

    // This is for previous photo dialog
    @Override
    public void onPhotoDialogNegativeClick(DialogFragment dialog) {
        // User touched "Keep" - keep the previous photos
        // Go straight to drawing activity
        Intent paintingIntent = new Intent(this, PaintingActivity.class);
        startActivityForResult(paintingIntent, RESULT_FOOD_SCANNER);
    }

    //endregion

    // This is for handling food items
    @Override
    public void onDelete(FoodArrayAdapter adapter, int position) {
        // Prompt for deleting of the select FoodItem

        removedFood = adapter.getItem(position);

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
                List<FoodItem> tempFood = new ArrayList<>(meal.getFood());
                FoodArrayAdapter adapter = new FoodArrayAdapter(MealDetailsActivity.this, tempFood);
                adapter.setOnDeleteListener(MealDetailsActivity.this);
                lv.setAdapter(adapter);

                // Set up background
                if (meal.getFood().size() == 0) {
                    lv.setBackgroundResource(R.drawable.background_add_food);
                } else {
                    lv.setBackgroundResource(0);
                }

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

    // This is for handling food items
    @Override
    public void onWarning(FoodArrayAdapter adapter, int position) {
        // Go to foodscanner; same as tapping "Scan" in FoodInfoFragment

        lastClickedFood = adapter.getItem(position);

        File topImage = new File(ImageDirectoryManager.
                getImageDirectory(getApplicationContext()).getPath() + "/Top.png");
        File sideImage = new File(ImageDirectoryManager.
                getImageDirectory(getApplicationContext()).getPath() + "/Side.png");

        if (!topImage.exists() || !sideImage.exists()) {
            // No previous images, go straight to photo taker
            Intent intent = new Intent(MealDetailsActivity.this, PhotoTakerActivity.class);
            intent.putExtra("pic_names", new String[]{"Top", "Side"});
            startActivityForResult(intent, REQUEST_FOODSCANNER);
        } else {
            // Ask user if they want to use previous images before proceeding
            DialogFragment photoDialog = PreviousPhotoFragment.newInstance();
            photoDialog.show(getFragmentManager(), "PreviousPhotoFragment");
        }
    }

}
