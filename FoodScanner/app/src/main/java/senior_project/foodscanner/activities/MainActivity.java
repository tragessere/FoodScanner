package senior_project.foodscanner.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import senior_project.foodscanner.Constants;
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
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(this.getApplicationContext(), "server:client_id:" + Constants.WEB_CLIENT_ID);
        credential.setSelectedAccountName(prefs.getString(Constants.PREF_ACCOUNT_NAME, null));

        if(credential.getSelectedAccountName() != null) {
            //signed in. Finish activity and continue.
            LoginActivity.finishLogin(this, credential, findViewById(R.id.loading));
            return;
        }

        int wait = 1000;// wait this many milliseconds before moving on to next activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        }, wait);

    }

}
