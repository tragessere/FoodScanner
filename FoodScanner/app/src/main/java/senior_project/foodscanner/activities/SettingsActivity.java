package senior_project.foodscanner.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import senior_project.foodscanner.R;
import senior_project.foodscanner.Settings;

/**
 * Created by Evan on 10/24/2015.
 */
public class SettingsActivity extends AppCompatActivity {
	private static final String format12Hour = "h:mm a";
	private static final String format24Hour = "HH:mm";

	View hourFormat;
	TextView hourExample;
	SwitchCompat hourSwitch;
	View logout;

	Settings settings;
	Calendar calendarExample;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		hourFormat = findViewById(R.id.hour_format_layout);
		hourExample = (TextView) findViewById(R.id.hour_format_example);
		hourSwitch = (SwitchCompat) findViewById(R.id.hour_format_switch);
		logout = findViewById(R.id.logout_layout);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		settings = Settings.getInstance();

		calendarExample = Calendar.getInstance();
		calendarExample.set(0, 0, 0, 13, 0);
		setHourFormat(settings.getTimeFormat().equals(Settings.TimeFormat._24));

		setListeners();
	}

	private void setListeners() {
		hourFormat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setHourFormat(!hourSwitch.isChecked());
			}
		});

		logout.setOnClickListener(new View.OnClickListener() {
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
		SimpleDateFormat format = new SimpleDateFormat(use24HourTime ? format24Hour : format12Hour, Locale.getDefault());
		hourExample.setText(format.format(calendarExample.getTimeInMillis()));
		settings.setTimeFormat(use24HourTime? Settings.TimeFormat._24 : Settings.TimeFormat._12);
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
