package senior_project.foodscanner.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import senior_project.foodscanner.R;

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
public class MealCalendar extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_calendar);
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
            //TODO
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
