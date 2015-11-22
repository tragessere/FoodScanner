package senior_project.foodscanner.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import senior_project.foodscanner.Constants;
import senior_project.foodscanner.FoodItem;
import senior_project.foodscanner.Meal;
import senior_project.foodscanner.Nutritious;
import senior_project.foodscanner.R;
import senior_project.foodscanner.Settings;
import senior_project.foodscanner.backend_helpers.EndpointsHelper;
import senior_project.foodscanner.database.SQLHelper;
import senior_project.foodscanner.database.SQLQueryHelper;
import senior_project.foodscanner.ui.components.ErrorDialogFragment;
import senior_project.foodscanner.ui.components.mealcalendar.CalendarDialog;
import senior_project.foodscanner.ui.components.mealcalendar.MealArrayAdapter;
import senior_project.foodscanner.ui.components.mealcalendar.TextDialog;

/**
 * Displays list or calendar of meals.
 * <p/>
 * Actions:
 * Add Meal - In Action Bar
 * View Meal - Click on an item in the list
 * Delete Meal - Swipe left on an item in the list
 * Log In - In Action Bar
 * - Takes user to Login/Register Activity
 * - When user is logged in:
 * - Button displays username
 * - Clicking this will log out the user
 * - Maybe a drop down menu for account settings
 */
public class MealCalendarActivity extends AppCompatActivity implements View.OnClickListener, CalendarDialog.CalendarDialogListener, AdapterView.OnItemClickListener, MealArrayAdapter.MealArrayAdapterListener, ErrorDialogFragment.ErrorDialogListener {
    private static final String SAVE_DATE = "currentDate";
    private static final int VIEW_MEAL = 0;

    private String username;

    private Button button_calendar;
    private Button button_totalDay;
    private Button button_totalWeek;
    private Button button_totalMonth;
    private View container_warning;

    private MealArrayAdapter adapter;
    private int lastClickedMealPos;
    private Meal lastClickedMeal;

    private long currentDate;
    private ArrayList<Meal> meals = new ArrayList<>();
    private List<Meal> unsyncedMeals =  new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_calendar);

        if (EndpointsHelper.getDownloadStatus() == Constants.DENSITY_NOT_DOWNLOADED) {
            EndpointsHelper.mEndpoints.new GetAllDensityEntriesTask(this).execute();
        }

        button_calendar = (Button) findViewById(R.id.button_calendar);
        button_totalDay = (Button) findViewById(R.id.button_total_day);
        button_totalWeek = (Button) findViewById(R.id.button_total_week);
        button_totalMonth = (Button) findViewById(R.id.button_total_month);
        ListView mealListView = (ListView) findViewById(R.id.listView_meals);
        container_warning = findViewById(R.id.container_warning);

        button_calendar.setOnClickListener(this);
        button_totalDay.setOnClickListener(this);
        button_totalWeek.setOnClickListener(this);
        button_totalMonth.setOnClickListener(this);
        container_warning.setOnClickListener(this);
        findViewById(R.id.imageButton_prev).setOnClickListener(this);
        findViewById(R.id.imageButton_next).setOnClickListener(this);
        adapter = new MealArrayAdapter(MealCalendarActivity.this, meals);
        adapter.setOnDeleteListener(this);
        mealListView.setAdapter(adapter);
        mealListView.setOnItemClickListener(this);

        if(savedInstanceState == null) {
            changeSelectedDay(System.currentTimeMillis(), false);
        } else {
            changeSelectedDay(savedInstanceState.getLong(SAVE_DATE), false);
        }

        username = getIntent().getStringExtra(LoginActivity.EXTRA_ACCOUNT_NAME);

        // delete Meals over 3 months old (28*3 days) if they are all synced with backend
        GregorianCalendar day = new GregorianCalendar();
        day.add(Calendar.DATE, -28 * 3);
        List<Meal> meals = SQLQueryHelper.getMealsBefore(day, true);
        boolean allSynced = true;
        for(Meal meal:meals){
            if(meal.isChanged()){
                allSynced = false;
                break;
            }
        }
        if(allSynced) {
            SQLQueryHelper.deleteMeals(meals);
        }

        // TODO account for space when downloading and storing locally
    }

    @Override
    protected void onSaveInstanceState(Bundle saved) {
        saved.putLong(SAVE_DATE, currentDate);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMeals();
        uploadMeals();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_meal_calendar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Meal createMeal() {
        // time of meal is now
        GregorianCalendar mealTime = new GregorianCalendar();

        // currently selected date
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.setTimeInMillis(this.currentDate);

        // update time of meal with currently selected date
        mealTime.set(currentDate.get(GregorianCalendar.YEAR), currentDate.get(GregorianCalendar.MONTH), currentDate.get(GregorianCalendar.DAY_OF_MONTH));

        // get settings for 'guessed' meal
        Settings settings = Settings.getInstance();

        // create Meal
        Meal newMeal = new Meal(mealTime.getTimeInMillis(), settings.getCurrentMeal());

        // save meal to local storage
        long newId = SQLQueryHelper.insertMeal(newMeal);
        if(newId < 0) {
            ErrorDialogFragment.showErrorDialog(this, "Failed to write meal to local storage. Id returned = "+newId);
        }
        newMeal.setId(newId);

        // add meal to ui
        adapter.add(newMeal);

        return newMeal;
    }

    private void viewMeal(Meal meal) {
        // Note: uses Serializable to pass the meal which may have high overhead. Consider Parcelable as alternative.
        Intent intent = new Intent(MealCalendarActivity.this, MealDetailsActivity.class);
        intent.putExtra("meal", meal);
        startActivityForResult(intent, VIEW_MEAL);
    }

    private void changeSelectedDay(GregorianCalendar cal) {
        changeSelectedDay(cal.getTimeInMillis());
    }

    private void changeSelectedDay(GregorianCalendar cal, boolean loadMeals) {
        changeSelectedDay(cal.getTimeInMillis(), loadMeals);
    }

    private void changeSelectedDay(long date) {
        changeSelectedDay(date, true);
    }

    private void changeSelectedDay(long date, boolean loadMeals) {
        currentDate = date;
        button_calendar.setText(Settings.getInstance().formatDate(date));
        if(loadMeals) {
            loadMeals(date);
        }
    }

    private void loadMeals() {
        loadMeals(currentDate);
    }

    private void loadMeals(long date) {
        GregorianCalendar day1 = new GregorianCalendar();
        day1.setTimeInMillis(date);
        GregorianCalendar day2 = new GregorianCalendar();
        day2.setTimeInMillis(date);

        // clear ui
        adapter.clear();

        // load meals from local into ui
        List<Meal> meals = SQLQueryHelper.getMeals(day1, day2, true);
        for(Meal meal:meals){
            adapter.add(meal);
        }

        //TODO if empty load from backend into local, handle no connection case
        //TODO load from local again
        //TODO make sure going to details and back updates data here

        updateDayTotal();
    }

    /**
     * Uploads all unsynced meals to server.
     */
    private void uploadMeals(){
        // TODO determine when to upload meals to server

        unsyncedMeals =  SQLQueryHelper.getChangedMeals();

        //TODO upload to backend
        //TODO set is changed false for success only

        for(Meal meal : unsyncedMeals) {
            if(meal != null) {
                meal.setIsChanged(false);
                SQLQueryHelper.updateMeal(meal);
            }
        }
        adapter.notifyDataSetChanged();

        unsyncedMeals =  SQLQueryHelper.getChangedMeals();
        if(unsyncedMeals.isEmpty()){
            container_warning.setVisibility(View.GONE);
        }
        else{
            container_warning.setVisibility(View.VISIBLE);
        }
    }

    private void updateDayTotal() {
        Map<String, Double> nutr = Nutritious.calculateTotalNutrition(meals);
        int cal = 0;
        if(nutr != null) {
            Double calD = nutr.get(FoodItem.KEY_CAL);
            if(calD != null) {
                cal = calD.intValue();
            }
        }
        button_totalDay.setText("Day: " + cal + " Cal");
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button_calendar:
                CalendarDialog.show(this, this, currentDate);
                break;
            case R.id.button_total_day:
                TextDialog.show(this, Nutritious.nutritionText(Nutritious.calculateTotalNutrition(meals)));
                break;
            case R.id.button_total_week:{
                GregorianCalendar day1 = new GregorianCalendar();
                day1.setTimeInMillis(currentDate);
                day1.add(Calendar.DATE, -6);// 7 days ago, including today

                GregorianCalendar day2 = new GregorianCalendar();
                day2.setTimeInMillis(currentDate);// today

                Log.d("MealCalendar", Settings.getInstance().formatDate(day1) + " to " + Settings.getInstance().formatDate(day2));

                List<Meal> meals = SQLQueryHelper.getMeals(day1,day2,true);
                TextDialog.show(this, Nutritious.nutritionText(Nutritious.calculateTotalNutrition(meals)));
                break;}
            case R.id.button_total_month:{
                GregorianCalendar day1 = new GregorianCalendar();
                day1.setTimeInMillis(currentDate);
                day1.add(Calendar.DATE, -27);// 28 days ago, including today

                GregorianCalendar day2 = new GregorianCalendar();
                day2.setTimeInMillis(currentDate);// today

                Log.d("MealCalendar", Settings.getInstance().formatDate(day1)+" to "+Settings.getInstance().formatDate(day2));

                List<Meal> meals = SQLQueryHelper.getMeals(day1,day2,true);
                TextDialog.show(this, Nutritious.nutritionText(Nutritious.calculateTotalNutrition(meals)));
                break;}
            case R.id.imageButton_prev:{
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTimeInMillis(currentDate);
                cal.add(Calendar.DATE, -1);
                changeSelectedDay(cal.getTimeInMillis());
                break;}
            case R.id.imageButton_next:{
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTimeInMillis(currentDate);
                cal.add(Calendar.DATE, 1);
                changeSelectedDay(cal.getTimeInMillis());
                break;}
            case R.id.container_warning:
                Log.d("MealCalendar", "CLICKED WARNING BAR");
                warningClick();
                break;
            default:
                // Do nothing
                break;
        }
    }

    @Override
    public void onCalendarDialogDateSelected(GregorianCalendar date) {
        changeSelectedDay(date);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Meal meal = (Meal) parent.getItemAtPosition(position);
        if(meal == null) {
            meal = createMeal();
        }
        lastClickedMealPos = position;
        lastClickedMeal = meal;
        viewMeal(meal);
    }


    @Override
    public void onDelete(MealArrayAdapter adapter, int position) {
        final MealArrayAdapter tempAdapter = adapter;
        final int tempPostion = position;

        // Create deletion confirmation dialog
        Meal selectedMeal = adapter.getItem(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this " +
                selectedMeal.getType().getName() + " meal entry?")
                .setTitle("Confirm Meal Deletion");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked 'Delete' button
                // Remove meal from calendar
                Meal meal = tempAdapter.getItem(tempPostion);
                tempAdapter.remove(meal);
                updateDayTotal();

                // delete from local storage
                SQLQueryHelper.deleteMeal(meal);

                //TODO delete from backend and handle case of no connection

                Toast butteredToast = Toast.makeText(getApplicationContext(), "Removed from calendar.",
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

    public void warningClick(){
        //TODO confirm dialog
        uploadMeals();
    }

    @Override
    public void onWarning(MealArrayAdapter adapter, int position) {
        warningClick();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case VIEW_MEAL:
                if(resultCode == RESULT_OK) {
                    Meal meal = (Meal) data.getSerializableExtra("meal");
                    SQLQueryHelper.updateMeal(meal);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onErrorDialogClose() {
        finish();
    }

}
