package senior_project.foodscanner.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import senior_project.foodscanner.R;

/**
 * Arbitrary first activity to be seen by the user.
 * Currently a splash screen that takes user to the calendar.
 *
 * Possible changes:
 * -tutorial
 * -login
 * -delete this activity
 *
 * Ideas:
 *  Allow user to save Meal and Food Item presets so they don't have to input all the details if they eat the same stuff often.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int wait = 1000;// wait this many milliseconds before moving on to next activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this, MealCalendarActivity.class));
                finish();
            }
        }, wait);

    }

}
