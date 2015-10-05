package senior_project.foodscanner.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ListView;

import java.util.GregorianCalendar;

import senior_project.foodscanner.Meal;
import senior_project.foodscanner.R;
import senior_project.foodscanner.Settings;
import senior_project.foodscanner.ui.components.mealcalendar.MealArrayAdapter;

/**
 * Displays list or calendar of meals.
 *
 * Actions:
 *  Add Meal - In Action Bar
 *  View Meal - Click on an item in the list
 *  Delete Meal - Swipe left on an item in the list
 *  Log In - In Action Bar
 *      - Takes user to Login/Register Activity
 *      - When user is logged in:
 *          - Button displays username
 *          - Clicking this will log out the user
 *          - Maybe a drop down menu for account settings
 *
 */
public class MealCalendarActivity extends AppCompatActivity implements View.OnClickListener, CalendarView.OnDateChangeListener, AdapterView.OnItemClickListener, MealArrayAdapter.OnDeleteListener{
    private Button button_calendar;
    private Button button_total;
    private CalendarView calendar;
    private ListView mealListView;
    private MealArrayAdapter adapter;

    private static final long msInDay = 24*60*60*1000;// TODO come up with more reliable method for prev/next day navigation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_calendar);

        button_calendar = (Button)findViewById(R.id.button_calendar);
        button_total = (Button)findViewById(R.id.button_total_day);
        calendar = (CalendarView)findViewById(R.id.calendarView);
        mealListView = (ListView)findViewById(R.id.listView_meals);

        button_calendar.setOnClickListener(this);
        button_total.setOnClickListener(this);
        calendar.setOnDateChangeListener(this);
        findViewById(R.id.imageButton_prev).setOnClickListener(this);
        findViewById(R.id.imageButton_next).setOnClickListener(this);
        adapter = new MealArrayAdapter(MealCalendarActivity.this);
        adapter.setOnDeleteListener(this);
        mealListView.setAdapter(adapter);
        mealListView.setOnItemClickListener(this);

        calendar.setVisibility(View.GONE);

        // TODO load existing meals into calendar

        // TODO determine when to upload meals to server
        // TODO max history to display/retrieve from server?
            // maybe have the option to load older history
        // TODO make UI pretty
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

        if (id == R.id.action_login) {
            //TODO go to login screen here
            //TODO upon returning from logging in, update UI here to indicate user is logged in and has the ability to log out
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Meal createMeal(){
        Meal newMeal = new Meal(new GregorianCalendar(), Meal.MealType.LUNCH);
        adapter.add(newMeal);
        //TODO save meal to device here
        return newMeal;
    }

    /**
     * Navigates to Meal Details activity for the corresponding meal.
     * @param meal
     */
    private void viewMeal(Meal meal){
        // Note: uses Serializable to pass the meal which may have high overhead. Consider Parcelable as alternative.
        Intent intent = new Intent(MealCalendarActivity.this, MealDetailsActivity.class);
        intent.putExtra("meal", meal);
        startActivity(intent);//TODO get edited meal data and update view
    }

    private void changeSelectedDay(int year, int month, int dayOfMonth){
        changeSelectedDay(new GregorianCalendar(year, month, dayOfMonth));
    }

    private void changeSelectedDay(long date){
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(date);
        changeSelectedDay(cal);
    }

    private void changeSelectedDay(GregorianCalendar cal){
        calendar.setDate(cal.getTimeInMillis());
        button_calendar.setText(new Settings().formatDate(cal));//TODO reference global Settings object
        adapter.clear();
        //TODO update listview with meals for that day
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button_calendar:
                if(calendar.getVisibility() == View.GONE){
                    calendar.setDate(calendar.getDate());
                    calendar.setVisibility(View.VISIBLE);
                }
                else{
                    calendar.setVisibility(View.GONE);
                    calendar.setDate(calendar.getDate());
                }
                break;
            case R.id.button_total_day:
                // TODO display total
                // TODO display week and month totals
                break;
            case R.id.imageButton_prev:
                changeSelectedDay(calendar.getDate() - msInDay);
                break;
            case R.id.imageButton_next:
                changeSelectedDay(calendar.getDate() + msInDay);
                break;
            default:
                // Do nothing
        }
    }

    @Override
    public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
        calendar.setVisibility(View.GONE);
        changeSelectedDay(year, month, dayOfMonth);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Meal meal = (Meal)parent.getItemAtPosition(position);
        if(meal == null){
            meal = createMeal();
        }
        viewMeal(meal);
    }


    @Override
    public void onDelete(MealArrayAdapter adapter, int position) {
        //TODO delete from device
        adapter.remove(adapter.getItem(position));
    }
}
