package senior_project.foodscanner.activities;

import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Map;

import senior_project.foodscanner.FoodItem;
import senior_project.foodscanner.Meal;
import senior_project.foodscanner.Nutritious;
import senior_project.foodscanner.R;
import senior_project.foodscanner.Settings;
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
public class MealCalendarActivity extends AppCompatActivity implements View.OnClickListener, CalendarDialog.CalendarDialogListener, AdapterView.OnItemClickListener, MealArrayAdapter.MealArrayAdapterListener {
    private static final String SAVE_DATE = "currentDate";
    private static final int VIEW_MEAL = 0;
    private static final long msInDay = 24 * 60 * 60 * 1000;

    private String username;

    private Button button_calendar;
    private Button button_totalDay;
    private MealArrayAdapter adapter;
    private int lastClickedMealPos;
    private Meal lastClickedMeal;

    private long currentDate;
    private ArrayList<Meal> meals = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_calendar);

        button_calendar = (Button) findViewById(R.id.button_calendar);
        button_totalDay = (Button) findViewById(R.id.button_total_day);
        ListView mealListView = (ListView) findViewById(R.id.listView_meals);

        button_calendar.setOnClickListener(this);
        button_totalDay.setOnClickListener(this);
        findViewById(R.id.imageButton_prev).setOnClickListener(this);
        findViewById(R.id.imageButton_next).setOnClickListener(this);
        adapter = new MealArrayAdapter(MealCalendarActivity.this, meals);
        adapter.setOnDeleteListener(this);
        mealListView.setAdapter(adapter);
        mealListView.setOnItemClickListener(this);

        if(savedInstanceState == null) {
            changeSelectedDay(new GregorianCalendar().getTimeInMillis());
        } else {
            changeSelectedDay(savedInstanceState.getLong(SAVE_DATE));
        }

        username = getIntent().getStringExtra(LoginActivity.EXTRA_ACCOUNT_NAME);

        // TODO determine when to upload meals to server
        // TODO indicator on meals that aren't uploaded
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_meal_calendar, menu);
        menu.findItem(R.id.action_logout).setTitle(username);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_logout) {
            //TODO confirm dialog
            LoginActivity.logout(this);
        }
        if(id == R.id.action_settings) {
            //TODO go to login screen here
            //TODO upon returning from logging in, update UI here to indicate user is logged in and has the ability to log out
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

        Meal newMeal = new Meal(mealTime.getTimeInMillis(), Meal.MealType.LUNCH);
        adapter.add(newMeal);
        //TODO save meal to device here
        return newMeal;
    }

    private void updateMeal(Meal updatedMeal) {
        adapter.remove(lastClickedMeal);
        adapter.insert(updatedMeal, lastClickedMealPos);
        updateDayTotal();
        //TODO update meal on device here
    }

    private void viewMeal(Meal meal) {
        // Note: uses Serializable to pass the meal which may have high overhead. Consider Parcelable as alternative.
        Intent intent = new Intent(MealCalendarActivity.this, MealDetailsActivity.class);
        intent.putExtra("meal", meal);
        startActivityForResult(intent, VIEW_MEAL);
    }

    private void changeSelectedDay(long date) {
        currentDate = date;
        button_calendar.setText(Settings.getInstance().formatDate(date));
        loadMeals(date);
    }

    private void changeSelectedDay(GregorianCalendar cal) {
        changeSelectedDay(cal.getTimeInMillis());
    }

    private void loadMeals() {
        loadMeals(currentDate);
    }

    private void loadMeals(long date) {
        //adapter.clear();
        //TODO load meals from local
        //TODO if empty load from backend into local, handle no connection case
        //TODO load from local again
        //TODO make sure going to details and back updates data here
        //TODO update total button
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
            case R.id.button_total_week:
                // TODO query week
                // TODO display dialog
                break;
            case R.id.button_total_month:
                // TODO query month
                // TODO display dialog
                break;
            case R.id.imageButton_prev:
                changeSelectedDay(currentDate - msInDay);
                break;
            case R.id.imageButton_next:
                changeSelectedDay(currentDate + msInDay);
                break;
            default:
                // Do nothing
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
        //TODO confirm dialog
        //TODO delete from device
        //TODO somehow delete from backend and handle case of no connection
        adapter.remove(adapter.getItem(position));
        updateDayTotal();
    }

    @Override
    public void onWarning(MealArrayAdapter adapter, int position) {
        //TODO confirm dialog
        //TODO upload to backend
        for(Meal meal : meals) {
            if(meal != null) {
                meal.setIsChanged(false);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case VIEW_MEAL:
                if(resultCode == RESULT_OK) {
                    updateMeal((Meal) data.getSerializableExtra("meal"));
                }
                break;
            default:
                break;
        }
    }

}
