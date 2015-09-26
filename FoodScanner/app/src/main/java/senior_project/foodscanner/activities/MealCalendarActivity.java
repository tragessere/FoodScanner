package senior_project.foodscanner.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.unnamed.b.atv.model.TreeNode;

import java.util.GregorianCalendar;

import senior_project.foodscanner.Meal;
import senior_project.foodscanner.R;
import senior_project.foodscanner.ui.components.mealcalendar.MealCalendar;
import senior_project.foodscanner.ui.components.mealcalendar.MealClickListener;

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
public class MealCalendarActivity extends AppCompatActivity implements MealClickListener{
    private MealCalendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_calendar);

        RelativeLayout container = (RelativeLayout)findViewById(R.id.container);

        TreeNode root = TreeNode.root();
        calendar = new MealCalendar(MealCalendarActivity.this, root);
        calendar.setMealClickListener(this);
        container.addView(calendar.getView());
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
        else if (id == R.id.action_meal_add) {
            Meal newMeal = new Meal(new GregorianCalendar(), Meal.MealType.LUNCH);
            calendar.addMeal(newMeal);
            viewMeal(newMeal);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Navigates to Meal Details activity for the corresponding meal.
     * @param meal
     */
    private void viewMeal(Meal meal){
        //TODO Parcel or serial
        Intent intent = new Intent(MealCalendarActivity.this, MealDetailsActivity.class);
        intent.putExtra("meal", meal);
        startActivity(intent);
    }

    @Override
    public void onClick(Meal meal) {
        viewMeal(meal);
    }
}
