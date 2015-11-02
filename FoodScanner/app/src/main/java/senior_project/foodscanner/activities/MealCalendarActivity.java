package senior_project.foodscanner.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ListView;

import java.util.Date;
import java.util.GregorianCalendar;

import senior_project.foodscanner.Meal;
import senior_project.foodscanner.R;
import senior_project.foodscanner.Settings;
import senior_project.foodscanner.ui.components.CalendarDialog;
import senior_project.foodscanner.ui.components.mealcalendar.MealArrayAdapter;

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
public class MealCalendarActivity extends AppCompatActivity implements View.OnClickListener, CalendarDialog.CalendarDialogListener, AdapterView.OnItemClickListener, MealArrayAdapter.OnDeleteListener {
    private Button button_calendar;
    private Button button_total;
    private MealArrayAdapter adapter;
    private int lastClickedMealPos;
    private Meal lastClickedMeal;

    private static final long msInDay = 24 * 60 * 60 * 1000;
    private static final int VIEW_MEAL = 0;
    private long currentDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_calendar);

        button_calendar = (Button) findViewById(R.id.button_calendar);
        button_total = (Button) findViewById(R.id.button_total_day);
        ListView mealListView = (ListView) findViewById(R.id.listView_meals);

        button_calendar.setOnClickListener(this);
        button_total.setOnClickListener(this);
        findViewById(R.id.imageButton_prev).setOnClickListener(this);
        findViewById(R.id.imageButton_next).setOnClickListener(this);
        adapter = new MealArrayAdapter(MealCalendarActivity.this);
        adapter.setOnDeleteListener(this);
        mealListView.setAdapter(adapter);
        mealListView.setOnItemClickListener(this);

        currentDate = new Date().getTime();


        // TODO determine when to upload meals to server
        // TODO indicator on meals that aren't uploaded
        // TODO make UI pretty
        // TODO display logged in user?
    }

    @Override
    protected void onStart(){
        loadMeals();
        super.onStart();
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

        if(id == R.id.action_login) {
            LoginActivity.logout(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Meal createMeal() {
        Meal newMeal = new Meal(new GregorianCalendar(), Meal.MealType.LUNCH);
        adapter.add(newMeal);
        //TODO save meal to device here
        return newMeal;
    }

    private void updateMeal(Meal updatedMeal) {
        adapter.remove(lastClickedMeal);
        adapter.insert(updatedMeal, lastClickedMealPos);
        //TODO update meal on device here
    }

    private void viewMeal(Meal meal) {
        // Note: uses Serializable to pass the meal which may have high overhead. Consider Parcelable as alternative.
        Intent intent = new Intent(MealCalendarActivity.this, MealDetailsActivity.class);
        intent.putExtra("meal", meal);
        startActivityForResult(intent, VIEW_MEAL);
    }

    private void changeSelectedDay(long date) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(date);
        changeSelectedDay(cal);
    }

    private void changeSelectedDay(GregorianCalendar cal) {
        currentDate = cal.getTimeInMillis();
        button_calendar.setText(new Settings().formatDate(cal));//TODO reference global Settings object
        loadMeals(cal);
    }

    private void loadMeals(){
        loadMeals(currentDate);
    }

    private void loadMeals(long date){
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(date);
        loadMeals(cal);
    }

    private void loadMeals(GregorianCalendar cal){
        //TODO
        //TODO make sure going to details and back updates data here
        //adapter.clear();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button_calendar:
                CalendarDialog.show(this, this, currentDate);
                break;
            case R.id.button_total_day:
                // TODO display dialog
                break;
            case R.id.button_total_week:
                // TODO display dialog
                break;
            case R.id.button_total_month:
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
        adapter.remove(adapter.getItem(position));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case VIEW_MEAL:
                if (resultCode == RESULT_OK) {
                    updateMeal((Meal) data.getSerializableExtra("meal"));
                }
                break;
            default:
                break;
        }
    }

}
