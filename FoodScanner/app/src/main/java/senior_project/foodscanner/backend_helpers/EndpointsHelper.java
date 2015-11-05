package senior_project.foodscanner.backend_helpers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.example.backend.foodScannerBackendAPI.FoodScannerBackendAPI;
import com.example.backend.foodScannerBackendAPI.model.DensityEntry;
import com.example.backend.foodScannerBackendAPI.model.FoodItem;
import com.example.backend.foodScannerBackendAPI.model.Meal;
import com.example.backend.foodScannerBackendAPI.model.MyBean;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.DateTime;

import java.io.IOException;
import java.util.Date;
import java.util.List;


/**
 * Created by Evan on 10/3/2015.
 */
public class EndpointsHelper
{
	public static EndpointsHelper mEndpoints;
	public FoodScannerBackendAPI mAPI;

	public EndpointsHelper() { }

	/**
	 * Use when logging in to allow API requests
	 *
	 * @param credential	Google account returned when logging in
	 * @return				Instance of the requests helper
	 */
	public static EndpointsHelper initEndpoints(GoogleAccountCredential credential) {
		if(mEndpoints == null) {
			mEndpoints = new EndpointsHelper();
			mEndpoints.mAPI = EndpointBuilderHelper.getEndpoints(credential);
		}

		return mEndpoints;
	}

	/**
	 * use when logging out to clear the users credentials from any future requests.
	 */
	public static void clearInstance() {
		mEndpoints.mAPI = null;
		mEndpoints = null;
	}

	/**
	 * Google Endpoints uses <code>AsyncTask</code> to make network calls to the backend server
	 */
	public class ExampleTask extends AsyncTask<Void, Void, MyBean> {
		private TaskCompletionListener mListener;

		public ExampleTask(TaskCompletionListener listener) {
			mListener = listener;
		}

		@Override
		protected MyBean doInBackground(Void... params) {
			try {
				return mAPI.sayHi("example!").execute();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(MyBean testBean) {
			if(!isCancelled()) {
				Bundle b = new Bundle();
				if(testBean != null)
					b.putString("test", testBean.getData());
				mListener.onTaskCompleted(b);
			}
		}
	}

	public class GetAllDensityEntriesTask extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog dialog;
		private Activity act;

		public GetAllDensityEntriesTask(Activity act) {
			this.act = act;
		}

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(act);
			dialog.setMessage("Loading...");
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (dialog.isShowing()) {
				dialog.dismiss();
			}

			if (result == false) {
				act.runOnUiThread(new Runnable() {
					public void run() {
						Toast butteredToast = Toast.makeText(act.getApplicationContext(),
								"Error: Could not retrieve densities.", Toast.LENGTH_LONG);
						butteredToast.show();
					}
				});
			}
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			List<DensityEntry> results;
			try {
				results = mAPI.getAllDensityEntries().execute().getItems();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}

			if (results == null) {
				return false;
			}

			// Save results to density map
			for (DensityEntry entry : results) {
				// TODO: Fix database so no entries should have null density values
				if (entry.getDensity() != null) {
					senior_project.foodscanner.FoodItem.addDensity(entry.getName(),
							(double) (entry.getDensity()));
				}
			}

			// TODO: Save densities locally, in case a later query fails

			return true;
		}
	}

	public class SaveMealTask extends AsyncTask<Meal, Void, Meal> {
		@Override
		protected Meal doInBackground(Meal... meals) {
			try {
				mAPI.saveMeal(meals[0]).execute();
				return meals[0];
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public class GetMealsWithinDates extends AsyncTask<Date, Void, List<Meal>> {
		@Override
		protected List<Meal> doInBackground(Date... dates) {
			try {
				Date startDate = dates[0];
				Date endDate = dates[1];
				return mAPI.getMealsWithinDates(new DateTime(startDate), new DateTime(endDate)).execute().getItems();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * Callback for API calls finishing.
	 *
	 * Bundle is optional and can hold any primitive making the callback flexible.
	 */
	public interface TaskCompletionListener {
		void onTaskCompleted(Bundle b);
	}
}
