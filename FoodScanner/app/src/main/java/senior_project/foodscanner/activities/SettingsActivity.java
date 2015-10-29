package senior_project.foodscanner.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.MenuItem;
import android.view.View;

import senior_project.foodscanner.R;

/**
 * Created by Evan on 10/24/2015.
 */
public class SettingsActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		View hourFormat = findViewById(R.id.hour_format_layout);
		final SwitchCompat hourSwitch = (SwitchCompat) findViewById(R.id.hour_format_switch);
		View logout = findViewById(R.id.logout_layout);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);


		hourFormat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hourSwitch.setChecked(!hourSwitch.isChecked());
			}
		});

		logout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LoginActivity.logout(SettingsActivity.this);
			}
		});
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
