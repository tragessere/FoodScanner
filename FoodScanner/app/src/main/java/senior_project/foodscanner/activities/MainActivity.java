package senior_project.foodscanner.activities;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Timer;
import java.util.TimerTask;

import senior_project.foodscanner.R;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int wait = 3000;// wait this many milliseconds before moving on to next activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
               // startActivity(new Intent(MainActivity.this, MealDetails.class));
                finish();
            }
        }, wait);

    }
}
