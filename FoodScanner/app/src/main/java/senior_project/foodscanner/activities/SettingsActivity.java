package senior_project.foodscanner.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.util.Calendar;

import senior_project.foodscanner.Constants;
import senior_project.foodscanner.R;
import senior_project.foodscanner.Settings;

/**
 * Created by Evan on 10/24/2015.
 */
public class SettingsActivity extends AppCompatActivity {

	View hourFormatButton;
	TextView hourExample;
	SwitchCompat hourSwitch;
	View setTimesButton;
	TextView currentTimeSetting;
	View logoutButton;
	TextView usernameText;

	Settings settings;
	Calendar calendarExample;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		hourFormatButton = findViewById(R.id.hour_format_layout);
		hourExample = (TextView) findViewById(R.id.hour_format_example);
		hourSwitch = (SwitchCompat) findViewById(R.id.hour_format_switch);
		setTimesButton = findViewById(R.id.manual_meal_time_layout);
		currentTimeSetting = (TextView) findViewById(R.id.meal_time_current);
		logoutButton = findViewById(R.id.logout_layout);
		usernameText = (TextView) findViewById(R.id.logout_username);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		settings = Settings.getInstance();

		calendarExample = Calendar.getInstance();
		calendarExample.set(0, 0, 0, 13, 0);
		setHourFormat(settings.getTimeFormat().equals(Settings.TimeFormat._24));

		SharedPreferences prefs = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
		GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(this.getApplicationContext(), "server:client_id:" + Constants.WEB_CLIENT_ID);
		credential.setSelectedAccountName(prefs.getString(Constants.PREF_ACCOUNT_NAME, null));

		usernameText.setText(credential.getSelectedAccountName());

		setListeners();
	}

	private void setListeners() {
		hourFormatButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setHourFormat(!hourSwitch.isChecked());
			}
		});

		setTimesButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(SettingsActivity.this, SettingsManualTimes.class));
			}
		});

		logoutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(SettingsActivity.this)
						.setMessage(getString(R.string.action_logout_confirm))
						.setPositiveButton(getString(R.string.logout_button), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								LoginActivity.logout(SettingsActivity.this);
							}
						})
						.setNegativeButton(getString(android.R.string.cancel).toUpperCase(), null)
						.show();
			}
		});
	}


	private void setHourFormat(boolean use24HourTime) {
		hourSwitch.setChecked(use24HourTime);
		settings.setTimeFormat(use24HourTime? Settings.TimeFormat._24 : Settings.TimeFormat._12);
		hourExample.setText(settings.formatHour(13, 0));
	}

	@Override
	protected void onResume() {
		currentTimeSetting.setText(settings.isUsingManualTimes() ? getString(R.string.settings_meal_time_manual) : getString(R.string.settings_meal_time_automatic));
		super.onResume();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
