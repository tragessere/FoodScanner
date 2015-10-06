package senior_project.foodscanner.backend_helpers;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.backend.foodScannerBackendAPI.FoodScannerBackendAPI;
import com.example.backend.foodScannerBackendAPI.model.MyBean;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.io.IOException;


/**
 * Created by Evan on 10/3/2015.
 */
public class EndpointsHelper {
	private static EndpointsHelper mEndpoints;
	private FoodScannerBackendAPI mAPI;

	private EndpointsHelper() { }

	/**
	 * Use when logging in to allow API requests
	 *
	 * @param credential	Google account returned when logging in
	 * @return				Instance of the requests helper
	 */
	public static EndpointsHelper getInstance(GoogleAccountCredential credential) {
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

	public void startExampleTask(Context context) {
		new ExampleTask(context).execute();
	}


	/**
	 * Google Endpoints uses <code>AsyncTask</code> to make network calls to the backend server
	 */
	public class ExampleTask extends AsyncTask<Void, Void, MyBean> {
		private Context mContext;

		public ExampleTask(Context context) {
			mContext = context;
		}

		@Override
		protected MyBean doInBackground(Void... params) {
			try {
				return mAPI.sayHi("example!").execute();
			} catch (IOException e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(MyBean foodItems) {
			if(!isCancelled())
				Toast.makeText(mContext, foodItems.getData(), Toast.LENGTH_SHORT).show();
		}
	}


}
