package senior_project.foodscanner.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import senior_project.foodscanner.R;

/**
 * Shows details of the meal and allows editing.
 *
 * Details:
 *  Date and time
 *  Meal Type
 *  Total nutrition
 *  List of food items and each nutrition and volume
 *
 * Actions:
 *  Add Food Button
 *      - Pop up menu
 *          - Scan Food - takes user to Food Scanner activity
 *          - Manually add Food Item - takes user to Food Item activity
 *  Delete Food
 *  Edit Date and time
 *  Edit Meal Type
 *  Delete Meal
 *  Back Button - return to Meal Calendar
 */
public class MealDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_details);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
