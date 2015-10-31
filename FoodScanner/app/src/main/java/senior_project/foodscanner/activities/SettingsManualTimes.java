package senior_project.foodscanner.activities;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

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

	Settings settings;

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


	}


	private void setManualTime(boolean useManualTime) {
		manualTimeSwitch.setChecked(useManualTime);


		int primaryColor;
		int secondaryColor;
		if(useManualTime) {
			primaryColor = ContextCompat.getColor(this, R.color.Text_Primary);
			secondaryColor = ContextCompat.getColor(this, R.color.Text_Secondary);
		} else {
			primaryColor = ContextCompat.getColor(this, R.color.Text_Primary_Disabled);
			secondaryColor = ContextCompat.getColor(this, R.color.Text_Secondary_Disabled);
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
