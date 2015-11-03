package senior_project.foodscanner.activities;

import android.Manifest;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;


import senior_project.foodscanner.Constants;
import senior_project.foodscanner.R;
import senior_project.foodscanner.Settings;
import senior_project.foodscanner.backend_helpers.EndpointsHelper;
import senior_project.foodscanner.database.SQLHelper;

/**
 * Created by Evan on 9/16/2015.
 */
public class LoginActivity extends AppCompatActivity {
	private static final int REQUEST_ACCOUNT_PICKER = 2;
	private static final int REQUEST_READ_CONTACTS = 3;

	SharedPreferences prefs;
	GoogleAccountCredential credential;

	Button googleButton;
	View progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		googleButton = (Button) findViewById(R.id.login_google_plus_button);
		progressBar = findViewById(R.id.loading);

		//Get permission to get user account for login on Android M
		if(ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS)
				!= PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.GET_ACCOUNTS},
					REQUEST_READ_CONTACTS);
		}


		prefs = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
		credential = GoogleAccountCredential.usingAudience(this.getApplicationContext(), "server:client_id:" + Constants.WEB_CLIENT_ID);
		credential.setSelectedAccountName(prefs.getString(Constants.PREF_ACCOUNT_NAME, null));

		googleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
			}
		});

		progressBar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//consumes touches
			}
		});
	}

	private void setSelectedAccountName(String accountName) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(Constants.PREF_ACCOUNT_NAME, accountName);
		editor.apply();
		credential.setSelectedAccountName(accountName);
		//TODO: add API call to create account in backend database
		finishLogin(this, credential, progressBar);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case REQUEST_ACCOUNT_PICKER:
				if (data != null && data.getExtras() != null) {
					String accountName =
							data.getExtras().getString(
									AccountManager.KEY_ACCOUNT_NAME);
					if (accountName != null) {
						progressBar.setVisibility(View.VISIBLE);
						setSelectedAccountName(accountName);
						// User is authorized.
					}
				}
				break;
		}
	}


	public static void finishLogin(final AppCompatActivity activity, final GoogleAccountCredential credential, final View progressBar) {
		progressBar.setVisibility(View.VISIBLE);

		//Create endpoints helper singleton on login to set the user's credentials
		EndpointsHelper helper = EndpointsHelper.initEndpoints(credential);
		SQLHelper.initialize(activity);

		Settings.initialize(activity);

		//Example usage of an API call
		helper.new ExampleTask(new EndpointsHelper.TaskCompletionListener() {
			@Override
			public void onTaskCompleted(Bundle b) {
				progressBar.setVisibility(View.GONE);
				Toast.makeText(activity, b.getString("test", "failure"), Toast.LENGTH_SHORT).show();

				Intent intent = new Intent(activity, MealCalendarActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				activity.startActivity(intent);
				activity.finish();
			}
		}).execute();

	}

	public static void logout(AppCompatActivity activity) {
		SQLHelper.clear();
		EndpointsHelper.clearInstance();
		SharedPreferences.Editor editor = activity.getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE).edit();
		editor.remove(Constants.PREF_ACCOUNT_NAME);
		editor.apply();

		Intent intent = new Intent(activity, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		activity.startActivity(intent);
		activity.finish();
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if(requestCode == REQUEST_READ_CONTACTS) {
			if(grantResults.length > 0
					&& grantResults[0] != PackageManager.PERMISSION_GRANTED) {
				//TODO: SHOW ERROR
			}
		}
	}

}
