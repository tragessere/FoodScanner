package senior_project.foodscanner.backend_helpers;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.backend.foodScannerBackendAPI.FoodScannerBackendAPI;
import com.example.backend.foodScannerBackendAPI.model.BackendMeal;
import com.example.backend.foodScannerBackendAPI.model.BackendFoodItem;
import com.example.backend.foodScannerBackendAPI.model.DensityEntry;
import com.example.backend.foodScannerBackendAPI.model.JsonMap;
import com.example.backend.foodScannerBackendAPI.model.MyBean;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import senior_project.foodscanner.Constants;
import senior_project.foodscanner.Meal;
import senior_project.foodscanner.FoodItem;

/**
 * Created by Evan on 10/3/2015.
 */
public class EndpointsHelper
{
	public static final String TASKID_MEAL_SAVE = "save_meal";
	public static final String TASKID_MEAL_SAVE_EXCEPTION = "save_meal_exception";
	public static final String TASKID_MEAL_SAVE_SUCCESS = "save_meal_success";
	public static final String TASKID_MEAL_DELETE = "delete_meal";
	public static final String TASKID_MEAL_DELETE_EXCEPTION = "delete_meal_exception";
	public static final String TASKID_MEAL_DELETE_SUCCESS = "delete_meal_success";
	public static final String TASKID_MEALS_GET = "meals";
	public static final String TASKID_MEALS_GET_EXCEPTION = "meals_exception";


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


	private static densityDownloadObserver observer;
	private static Integer densityDownloaded = Constants.DENSITY_NOT_DOWNLOADED;

	public static void registerDensityObserver(EndpointsHelper.densityDownloadObserver register) {
		observer = register;
	}

	public static int getDownloadStatus() {
		return densityDownloaded;
	}

	public class GetAllDensityEntriesTask extends AsyncTask<Void, Void, Boolean> {
		private Activity act;

		public GetAllDensityEntriesTask(Activity act) {
			this.act = act;
		}

		@Override
		protected void onPreExecute() {
			densityDownloaded = Constants.DENSITY_DOWNLOADING;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			synchronized (mEndpoints) {
				if (result) {
					densityDownloaded = Constants.DENSITY_DOWNLOADED;
				} else {
					densityDownloaded = Constants.DENSITY_NOT_DOWNLOADED;

					act.runOnUiThread(new Runnable() {
						public void run() {
							Toast butteredToast = Toast.makeText(act.getApplicationContext(),
									"Error: Could not retrieve densities.", Toast.LENGTH_LONG);
							butteredToast.show();
						}
					});
				}

				if (observer != null)
					observer.densityDownloaded();
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

	public class GetDensitiesWithNameSimilarToTask extends AsyncTask<String, Void, List<DensityEntry>> {
		@Override
		protected  List<DensityEntry> doInBackground(String...strings) {
			try {
				return mAPI.getDensitiesWithNameSimilarTo(strings[0]).execute().getItems();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * Saves/updates meal in backend. Always returns success boolean and meal. Returns exception on failure.
	 */
	public class SaveMealTask extends AsyncTask<Meal, Void, Meal> {
		private TaskCompletionListener mListener;
		private boolean success = false;
		private Exception exception;

		public SaveMealTask(TaskCompletionListener listener) {
			mListener = listener;
		}

		@Override
		protected void onCancelled(Meal meal) {
			Bundle b = new Bundle();
			b.putSerializable(TASKID_MEAL_SAVE_EXCEPTION, exception);
			b.putSerializable(TASKID_MEAL_SAVE, meal);
			b.putBoolean(TASKID_MEAL_SAVE_SUCCESS, false);
			mListener.onTaskCompleted(b);
		}

		@Override
		protected void onPostExecute(Meal meal) {
			Bundle b = new Bundle();
			b.putSerializable(TASKID_MEAL_SAVE_EXCEPTION, exception);
			b.putSerializable(TASKID_MEAL_SAVE, meal);
			b.putBoolean(TASKID_MEAL_SAVE_SUCCESS, success);
			mListener.onTaskCompleted(b);
		}

		@Override
		protected Meal doInBackground(Meal... meals) {
			try {
				BackendMeal backendMeal = convertToBackendMeal(meals[0]);
				mAPI.saveMeal(backendMeal).execute();
				success = true;
				return meals[0];
			} catch (Exception e) {
				Log.e("EndpointsHelper", "SaveMealTask", e);
				exception = e;
				return meals[0];
			}
		}
	}

	/**
	 * Deletes meal from backend. Always returns meal and success boolean and meal. Returns exception on failure.
	 */
	public class DeleteMealTask extends AsyncTask<Meal, Void, Meal> {
		private TaskCompletionListener mListener;
		private boolean success = false;
		private Exception exception;

		public DeleteMealTask(TaskCompletionListener listener) {
			mListener = listener;
		}

		@Override
		protected void onCancelled(Meal meal) {
			Bundle b = new Bundle();
			b.putSerializable(TASKID_MEAL_DELETE_EXCEPTION, exception);
			b.putSerializable(TASKID_MEAL_DELETE, meal);
			b.putBoolean(TASKID_MEAL_DELETE_SUCCESS, false);
			mListener.onTaskCompleted(b);
		}

		@Override
		protected void onPostExecute(Meal meal) {
			Bundle b = new Bundle();
			b.putSerializable(TASKID_MEAL_DELETE_EXCEPTION, exception);
			b.putSerializable(TASKID_MEAL_DELETE, meal);
			b.putBoolean(TASKID_MEAL_DELETE_SUCCESS, success);
			mListener.onTaskCompleted(b);
		}

		@Override
		protected Meal doInBackground(Meal... meals) {
			try {
				BackendMeal backendMeal = convertToBackendMeal(meals[0]);
				mAPI.deleteMeal(backendMeal).execute();//TODO BUG HERE java.lang.IllegalArgumentException: DELETE with non-zero content length is not supported
				success = true;
				return meals[0];
			} catch (Exception e) {
				Log.e("EndpointsHelper", "DeleteMealTask", e);
				exception = e;
				return meals[0];
			}
		}
	}

	/**
	 * Gets all meals in backend between two dates inclusive. Null and exception on failure.
	 */
	public class GetMealsWithinDatesTask extends AsyncTask<Date, Void, List<Meal>> {
		private TaskCompletionListener mListener;
		private Exception exception;

		public GetMealsWithinDatesTask(TaskCompletionListener listener) {
			mListener = listener;
		}


		@Override
		protected void onCancelled(){
			Bundle b = new Bundle();
			b.putSerializable(TASKID_MEALS_GET, null);
			b.putSerializable(TASKID_MEALS_GET_EXCEPTION, exception);
			mListener.onTaskCompleted(b);
		}

		@Override
		protected void onPostExecute(List<Meal> meals) {
			Bundle b = new Bundle();
			if(meals != null){
				b.putSerializable(TASKID_MEALS_GET, meals.toArray());
			}
			else{
				b.putSerializable(TASKID_MEALS_GET, null);
			}
			b.putSerializable(TASKID_MEALS_GET_EXCEPTION, exception);
			mListener.onTaskCompleted(b);
		}

		@Override
		protected List<Meal> doInBackground(Date... dates) {
			try {
				Date startDate = dates[0];
				Date endDate = dates[1];
				List<BackendMeal> backendMeals = mAPI.getMealsWithinDates(new DateTime(startDate), new DateTime(endDate)).execute().getItems();
				return convertToFrontEndMeals(backendMeals);
			} catch (Exception e) {
				Log.e("EndpointsHelper","GetMealsWithinDatesTask",e);
				exception = e;
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

	public interface densityDownloadObserver {
		void densityDownloaded();
	}

	/* Converters */

	public Meal convertToFrontEndMeal(BackendMeal backendMeal)
	{
		Meal meal = new Meal(
				backendMeal.getId(),
				backendMeal.getDate(),
				backendMeal.getType(),
				convertToFrontEndFoodItems(backendMeal.getFoodItems()),
				backendMeal.getIsNew(),
				backendMeal.getIsChanged(),
				false
		);

		return meal;
	}

	public BackendMeal convertToBackendMeal(Meal meal)
	{
		BackendMeal backendMeal = new BackendMeal();
		backendMeal.setDate(meal.getDate());
		backendMeal.setMealType(meal.getType().getName());
		backendMeal.setFoodItems(convertToBackendFoodItems(meal.getFood()));

		return backendMeal;
	}

	public BackendFoodItem convertToBackendFoodItem(FoodItem foodItem)
	{
		BackendFoodItem backendFoodItem = new BackendFoodItem();
		backendFoodItem.setName(foodItem.getName());
		backendFoodItem.setBrand(foodItem.getBrand());
		backendFoodItem.setDensity(foodItem.getDensity());
		backendFoodItem.setServingSize(foodItem.getServingSize());

		JsonMap nutritionMap = new JsonMap();
		nutritionMap.putAll(foodItem.getNutrition());
		backendFoodItem.setNutritionFields(nutritionMap);

		return backendFoodItem;
	}

	public FoodItem convertToFrontEndFoodItem(BackendFoodItem backendFoodItem)
	{
		FoodItem foodItem = new FoodItem();
		foodItem.setName(backendFoodItem.getName());
		foodItem.setBrand(backendFoodItem.getBrand());
		foodItem.setDensity(backendFoodItem.getDensity());
		foodItem.setServingSize(backendFoodItem.getServingSize());

		JsonMap nutritionMap = backendFoodItem.getNutritionFields();

		Iterator it = nutritionMap.entrySet().iterator();
		while (it.hasNext()) {
			JsonMap.Entry pair = (JsonMap.Entry)it.next();
			foodItem.setField((String)pair.getKey(), (Double)pair.getValue());
			it.remove(); // avoids a ConcurrentModificationException
		}

		return foodItem;
	}

	public ArrayList<FoodItem> convertToFrontEndFoodItems(List<BackendFoodItem> backendFoodItems)
	{
		ArrayList<FoodItem> foodItems = new ArrayList<FoodItem>();

		for (BackendFoodItem backendFoodItem : backendFoodItems) {
			foodItems.add(convertToFrontEndFoodItem(backendFoodItem));
		}

		return foodItems;
	}

	public List<BackendFoodItem> convertToBackendFoodItems(List<FoodItem> foodItems)
	{
		List<BackendFoodItem> backendFoodItems = new ArrayList<BackendFoodItem>();

		for (FoodItem foodItem : foodItems) {
			backendFoodItems.add(convertToBackendFoodItem(foodItem));
		}

		return backendFoodItems;
	}

	public ArrayList<Meal> convertToFrontEndMeals(List<BackendMeal> backendMeals)
	{
		ArrayList<Meal> meals = new ArrayList<Meal>();

		for (BackendMeal backendMeal : backendMeals) {
			meals.add(convertToFrontEndMeal(backendMeal));
		}

		return meals;
	}

	public List<BackendMeal> convertToBackendMeals(List<Meal> meals)
	{
		List<BackendMeal> backendMeals = new ArrayList<BackendMeal>();

		for (Meal meal : meals) {
			backendMeals.add(convertToBackendMeal(meal));
		}

		return backendMeals;
	}
}
