package senior_project.foodscanner.activities;

import android.Manifest;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;


import senior_project.foodscanner.Constants;
import senior_project.foodscanner.R;
import senior_project.foodscanner.backend_helpers.EndpointsHelper;

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

	Button loginButton;
	Button facebookButton;
	EditText emailEdit;
	TextInputLayout emailHolder;
	EditText passwordEdit;
	TextInputLayout passwordHolder;

	Rect loginButtonStartBounds;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);

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

	private void showLoginText() {

		Rect statusBarBorder = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(statusBarBorder);
		int statusBarHeight = statusBarBorder.top;


		if(loginButtonStartBounds == null) {
			loginButtonStartBounds = new Rect();
			loginButton.getGlobalVisibleRect(loginButtonStartBounds);
			loginButtonStartBounds.top -= statusBarHeight;
			loginButtonStartBounds.bottom -= statusBarHeight;
		}


		Rect endBounds = new Rect();
		facebookButton.getGlobalVisibleRect(endBounds);
		endBounds.top -= statusBarHeight;
		endBounds.bottom -= statusBarHeight;

		ObjectAnimator translate = ObjectAnimator.ofFloat(loginButton, View.TRANSLATION_Y, endBounds.top - loginButtonStartBounds.top);
		translate.setInterpolator(new DecelerateInterpolator());
		translate.setDuration(250);

		final ObjectAnimator fadeTextOut = ObjectAnimator.ofInt(loginButton, "textColor", Color.WHITE, Color.TRANSPARENT);
		fadeTextOut.setEvaluator(new ArgbEvaluator());
		fadeTextOut.setDuration(125);
		fadeTextOut.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				loginButton.setText(getString(R.string.login));
				fadeTextOut.removeAllListeners();
			}
		});
		ObjectAnimator fadeTextIn = ObjectAnimator.ofInt(loginButton, "textColor", Color.TRANSPARENT, Color.WHITE);
		fadeTextIn.setEvaluator(new ArgbEvaluator());
		fadeTextIn.setDuration(125);
		fadeTextIn.setStartDelay(125);


		final ObjectAnimator fadeEmailIn = ObjectAnimator.ofFloat(emailHolder, View.ALPHA, 0, 1);
		fadeEmailIn.setDuration(200);
		fadeEmailIn.setStartDelay(100);
		fadeEmailIn.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				super.onAnimationStart(animation);
				emailHolder.setVisibility(View.VISIBLE);
				passwordHolder.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				emailEdit.setFocusable(true);
				emailEdit.setFocusableInTouchMode(true);
				passwordEdit.setFocusable(true);
				passwordEdit.setFocusableInTouchMode(true);

				fadeEmailIn.removeAllListeners();
			}
		});
		ObjectAnimator fadePasswordIn = ObjectAnimator.ofFloat(passwordHolder, View.ALPHA, 0, 1);
		fadePasswordIn.setDuration(200);
		fadePasswordIn.setStartDelay(100);

		final ObjectAnimator googleFadeOut = ObjectAnimator.ofFloat(googleButton, View.ALPHA, 1, 0);
		googleFadeOut.setDuration(125);
		googleFadeOut.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				super.onAnimationStart(animation);
				googleButton.setClickable(false);
				facebookButton.setClickable(false);
				googleFadeOut.removeAllListeners();
			}
		});
		ObjectAnimator facebookFadeOut = ObjectAnimator.ofFloat(facebookButton, View.ALPHA, 1, 0);
		facebookFadeOut.setDuration(125);

		AnimatorSet set = new AnimatorSet();
		set.playTogether(translate, fadeTextOut, fadeTextIn, fadeEmailIn, fadePasswordIn, googleFadeOut, facebookFadeOut);
		set.start();
	}


	private void hideLoginText() {
		Rect startBounds = new Rect();
		facebookButton.getGlobalVisibleRect(startBounds);

		ObjectAnimator translate = ObjectAnimator.ofFloat(loginButton, View.TRANSLATION_Y, 0);
		translate.setInterpolator(new DecelerateInterpolator());
		translate.setDuration(250);

		final ObjectAnimator fadeTextOut = ObjectAnimator.ofInt(loginButton, "textColor", Color.WHITE, Color.TRANSPARENT);
		fadeTextOut.setEvaluator(new ArgbEvaluator());
		fadeTextOut.setDuration(125);
		fadeTextOut.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				loginButton.setText(getString(R.string.login_food_scanner));
				fadeTextOut.removeAllListeners();
			}
		});
		ObjectAnimator fadeTextIn = ObjectAnimator.ofInt(loginButton, "textColor", Color.TRANSPARENT, Color.WHITE);
		fadeTextIn.setEvaluator(new ArgbEvaluator());
		fadeTextIn.setDuration(125);
		fadeTextIn.setStartDelay(125);


		final ObjectAnimator fadeEmailOut = ObjectAnimator.ofFloat(emailHolder, View.ALPHA, 1, 0);
		fadeEmailOut.setInterpolator(new DecelerateInterpolator());
		fadeEmailOut.setDuration(125);
		fadeEmailOut.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				super.onAnimationStart(animation);
				emailEdit.setFocusable(false);
				passwordEdit.setFocusable(false);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				emailHolder.setVisibility(View.INVISIBLE);
				passwordHolder.setVisibility(View.INVISIBLE);

				fadeEmailOut.removeAllListeners();
			}
		});
		ObjectAnimator fadePasswordOut = ObjectAnimator.ofFloat(passwordHolder, View.ALPHA, 1, 0);
		fadePasswordOut.setInterpolator(new DecelerateInterpolator());
		fadePasswordOut.setDuration(125);

		final ObjectAnimator googleFadeIn = ObjectAnimator.ofFloat(googleButton, View.ALPHA, 0, 1);
		googleFadeIn.setDuration(200);
		googleFadeIn.setStartDelay(100);
		googleFadeIn.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				super.onAnimationStart(animation);
				googleButton.setClickable(true);
				facebookButton.setClickable(true);
				googleFadeIn.removeAllListeners();
			}
		});
		ObjectAnimator facebookFadeIn = ObjectAnimator.ofFloat(facebookButton, View.ALPHA, 0, 1);
		facebookFadeIn.setDuration(200);
		facebookFadeIn.setStartDelay(100);

		AnimatorSet set = new AnimatorSet();
		set.playTogether(translate, fadeTextOut, fadeTextIn, fadeEmailOut, fadePasswordOut, googleFadeIn, facebookFadeIn);
		set.start();
	}
}
