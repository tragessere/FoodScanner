package senior_project.foodscanner.activities;

import android.app.TimePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

import senior_project.foodscanner.R;
import senior_project.foodscanner.Settings;

/**
 * Created by Evan on 10/30/2015.
 */
public class SettingsManualTimes extends AppCompatActivity {

	View manualTimeButton;
	SwitchCompat manualTimeSwitch;
	View breakfastStartButton;
	TextView breakfastStartPrompt;
	TextView breakfastStartTime;
	View breakfastEndButton;
	TextView breakfastEndPrompt;
	TextView breakfastEndTime;
	View lunchStartButton;
	TextView lunchStartPrompt;
	TextView lunchStartTime;
	View lunchEndButton;
	TextView lunchEndPrompt;
	TextView lunchEndTime;
	View dinnerStartButton;
	TextView dinnerStartPrompt;
	TextView dinnerStartTime;
	View dinnerEndButton;
	TextView dinnerEndPrompt;
	TextView dinnerEndTime;

	int breakfastStart;
	int breakfastEnd;
	int lunchStart;
	int lunchEnd;
	int dinnerStart;
	int dinnerEnd;

	Settings settings;
	Calendar timeHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings_manual_times);

		manualTimeButton = findViewById(R.id.manual_time_button);
		manualTimeSwitch = (SwitchCompat) findViewById(R.id.manual_time_switch);
		breakfastStartButton = findViewById(R.id.breakfast_start);
		breakfastStartPrompt = (TextView) findViewById(R.id.breakfast_start_prompt);
		breakfastStartTime = (TextView) findViewById(R.id.breakfast_start_current);
		breakfastEndButton = findViewById(R.id.breakfast_end);
		breakfastEndPrompt = (TextView) findViewById(R.id.breakfast_end_prompt);
		breakfastEndTime = (TextView) findViewById(R.id.breakfast_end_current);
		lunchStartButton = findViewById(R.id.lunch_start);
		lunchStartPrompt = (TextView) findViewById(R.id.lunch_start_prompt);
		lunchStartTime = (TextView) findViewById(R.id.lunch_start_current);
		lunchEndButton = findViewById(R.id.lunch_end);
		lunchEndPrompt = (TextView) findViewById(R.id.lunch_end_prompt);
		lunchEndTime = (TextView) findViewById(R.id.lunch_end_current);
		dinnerStartButton = findViewById(R.id.dinner_start);
		dinnerStartPrompt = (TextView) findViewById(R.id.dinner_start_prompt);
		dinnerStartTime = (TextView) findViewById(R.id.dinner_start_current);
		dinnerEndButton = findViewById(R.id.dinner_end);
		dinnerEndPrompt = (TextView) findViewById(R.id.dinner_end_prompt);
		dinnerEndTime = (TextView) findViewById(R.id.dinner_end_current);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		settings = Settings.getInstance();
		timeHelper = Calendar.getInstance();
		timeHelper.set(2000, 0, 0);
		timeHelper.set(Calendar.SECOND, 0);
		timeHelper.set(Calendar.MILLISECOND, 0);

		breakfastStart = settings.getBreakfastStartManual();
		breakfastEnd = settings.getBreakfastEndManual();
		lunchStart = settings.getLunchStartManual();
		lunchEnd = settings.getLunchEndManual();
		dinnerStart = settings.getDinnerStartManual();
		dinnerEnd = settings.getDinnerEndManual();

		setManualTime(settings.isUsingManualTimes());

		setupListeners();
	}


	private void setupListeners() {
		manualTimeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setManualTime(!manualTimeSwitch.isChecked());
			}
		});

		final int theme;
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			theme = R.style.TimePicker;
		else
			theme = 0;

		breakfastStartButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TimePickerDialog timePicker = new TimePickerDialog(SettingsManualTimes.this, theme, new TimePickerDialog.OnTimeSetListener() {
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						breakfastStart = settings.timeToMillis(hourOfDay, minute);
						breakfastStartTime.setText(settings.formatHour(hourOfDay, minute));
					}
				}, settings.getBreakfastStartManualHour(), settings.getBreakfastStartManualMinute(), settings.isUsing24HourFormat());
				timePicker.show();
			}
		});

		breakfastEndButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TimePickerDialog timePicker = new TimePickerDialog(SettingsManualTimes.this, theme, new TimePickerDialog.OnTimeSetListener() {
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						breakfastEnd = settings.timeToMillis(hourOfDay, minute);
						breakfastEndTime.setText(settings.formatHour(hourOfDay, minute));
					}
				}, settings.getBreakfastEndManualHour(), settings.getBreakfastEndManualMinute(), settings.isUsing24HourFormat());
				timePicker.show();
			}
		});

		lunchStartButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TimePickerDialog timePicker = new TimePickerDialog(SettingsManualTimes.this, theme, new TimePickerDialog.OnTimeSetListener() {
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						lunchStart = settings.timeToMillis(hourOfDay, minute);
						lunchStartTime.setText(settings.formatHour(hourOfDay, minute));
					}
				}, settings.getLunchStartManualHour(), settings.getLunchStartManualMinute(), settings.isUsing24HourFormat());
				timePicker.show();
			}
		});

		lunchEndButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TimePickerDialog timePicker = new TimePickerDialog(SettingsManualTimes.this, theme, new TimePickerDialog.OnTimeSetListener() {
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						lunchEnd = settings.timeToMillis(hourOfDay, minute);
						lunchEndTime.setText(settings.formatHour(hourOfDay, minute));
					}
				}, settings.getLunchEndManualHour(), settings.getLunchEndManualMinute(), settings.isUsing24HourFormat());
				timePicker.show();
			}
		});

		dinnerStartButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TimePickerDialog timePicker = new TimePickerDialog(SettingsManualTimes.this, theme, new TimePickerDialog.OnTimeSetListener() {
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						dinnerStart = settings.timeToMillis(hourOfDay, minute);
						dinnerStartTime.setText(settings.formatHour(hourOfDay, minute));
					}
				}, settings.getDinnerStartManualHour(), settings.getDinnerStartManualMinute(), settings.isUsing24HourFormat());
				timePicker.show();
			}
		});

		dinnerEndButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TimePickerDialog timePicker = new TimePickerDialog(SettingsManualTimes.this, theme, new TimePickerDialog.OnTimeSetListener() {
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						dinnerEnd = settings.timeToMillis(hourOfDay, minute);
						dinnerEndTime.setText(settings.formatHour(hourOfDay, minute));
					}
				}, settings.getDinnerEndManualHour(), settings.getDinnerEndManualMinute(), settings.isUsing24HourFormat());
				timePicker.show();
			}
		});
	}


	private void setManualTime(boolean useManualTime) {
		manualTimeSwitch.setChecked(useManualTime);


		int primaryColor;
		int secondaryColor;
		if(useManualTime) {
			primaryColor = ContextCompat.getColor(this, R.color.Text_Primary);
			secondaryColor = ContextCompat.getColor(this, R.color.Text_Secondary);

			breakfastStartTime.setText(settings.formatHour(breakfastStart));
			breakfastEndTime.setText(settings.formatHour(breakfastEnd));
			lunchStartTime.setText(settings.formatHour(lunchStart));
			lunchEndTime.setText(settings.formatHour(lunchEnd));
			dinnerStartTime.setText(settings.formatHour(dinnerStart));
			dinnerEndTime.setText(settings.formatHour(dinnerEnd));
		} else {
			primaryColor = ContextCompat.getColor(this, R.color.Text_Primary_Disabled);
			secondaryColor = ContextCompat.getColor(this, R.color.Text_Secondary_Disabled);

			breakfastStartTime.setText(settings.formatHour(settings.getBreakfastStartAuto()));
			breakfastEndTime.setText(settings.formatHour(settings.getBreakfastEndAuto()));
			lunchStartTime.setText(settings.formatHour(settings.getLunchStartAuto()));
			lunchEndTime.setText(settings.formatHour(settings.getLunchEndAuto()));
			dinnerStartTime.setText(settings.formatHour(settings.getDinnerStartAuto()));
			dinnerEndTime.setText(settings.formatHour(settings.getDinnerEndAuto()));
		}

		breakfastStartButton.setEnabled(useManualTime);
		breakfastStartPrompt.setTextColor(primaryColor);
		breakfastStartTime.setTextColor(secondaryColor);

		breakfastEndButton.setEnabled(useManualTime);
		breakfastEndPrompt.setTextColor(primaryColor);
		breakfastEndTime.setTextColor(secondaryColor);

		lunchStartButton.setEnabled(useManualTime);
		lunchStartPrompt.setTextColor(primaryColor);
		lunchStartTime.setTextColor(secondaryColor);

		lunchEndButton.setEnabled(useManualTime);
		lunchEndPrompt.setTextColor(primaryColor);
		lunchEndTime.setTextColor(secondaryColor);

		dinnerStartButton.setEnabled(useManualTime);
		dinnerStartPrompt.setTextColor(primaryColor);
		dinnerStartTime.setTextColor(secondaryColor);

		dinnerEndButton.setEnabled(useManualTime);
		dinnerEndPrompt.setTextColor(primaryColor);
		dinnerEndTime.setTextColor(secondaryColor);

		settings.setUseManualTimes(useManualTime);
	}


	private void checkTimeOverlaps() {

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
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
