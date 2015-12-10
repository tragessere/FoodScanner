package senior_project.foodscanner.backend_helpers;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.os.CountDownTimer;
import android.widget.Toast;

import com.example.backend.foodScannerBackendAPI.FoodScannerBackendAPI;
import com.example.backend.foodScannerBackendAPI.model.BackendMeal;
import com.example.backend.foodScannerBackendAPI.model.BackendFoodItem;
import com.example.backend.foodScannerBackendAPI.model.DensityEntry;
import com.example.backend.foodScannerBackendAPI.model.JsonMap;
import com.example.backend.foodScannerBackendAPI.model.MyBean;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.DateTime;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import senior_project.foodscanner.Constants;
import senior_project.foodscanner.ImageDirectoryManager;
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
				mListener.onTaskCompleted(this, b);
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
		private boolean downloaded = false;

		public GetAllDensityEntriesTask(Activity act) {
			this.act = act;
		}

		@Override
		protected void onPreExecute() {
			densityDownloaded = Constants.DENSITY_DOWNLOADING;

			new CountDownTimer(15000, 15000) {
				public void onTick(long millisUntilFinished) {
					// You can monitor the progress here as well by changing the onTick() time
				}
				public void onFinish() {
					// stop async task if the data hasn't been downloaded yet
					synchronized (mEndpoints) {
						if (GetAllDensityEntriesTask.this.getStatus() == AsyncTask.Status.RUNNING) {
							if (!downloaded) {
								GetAllDensityEntriesTask.this.cancel(true);
								onPostExecute(false);
							}

							// Add any specific task you wish to do as your extended class variable works here as well.
						}
					}
				}
			}.start();
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
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
            // Before querying, check if densities are saved locally,
            // so density queries don't max out our api limit.

            // Open local density file, and check if it exists
            File localDensities = new File(ImageDirectoryManager.
                    getDensityDirectory(act).getPath() + "/densities.txt");
            Boolean downloadDensities = true;

            if (localDensities.exists()) {
                // Read from file and save to density map
                downloadDensities = false;
                try {
                    BufferedReader br = new BufferedReader(new FileReader(localDensities));
                    String line;
                    String name = null;
                    boolean isName = true;
                    while ((line = br.readLine()) != null) {
                        if (isName) {
                            // Line contains name of new density entry
                            isName = false;
                            name = line;
                        } else {
                            // Line contains value of previous density entry
                            isName = true;
                            double value = Double.parseDouble(line);
                            // Add density entry to map
                            senior_project.foodscanner.FoodItem.addDensity(name, value);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    act.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast butteredToast = Toast.makeText(act.getApplicationContext(),
                                    "Error: Could not read cached densities.", Toast.LENGTH_SHORT);
                            butteredToast.show();
                        }
                    });
                    downloadDensities = true;
                    // Clear density list to avoid errors
                    senior_project.foodscanner.FoodItem.clearAllDensities();
                }
            }

            if (!downloadDensities) {
                // Successfully retrieved densities from cache
                // Set downloaded to true, so client thinks it was successful
                synchronized (mEndpoints) {
                    downloaded = true;
                }
                return true;
            }

            // Densities were not retrieved from local cache; query the database

			List<DensityEntry> results;
			try {
				results = mAPI.getAllDensityEntries().execute().getItems();
				synchronized (mEndpoints) {
					downloaded = true;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}

			if (results == null) {
				return false;
			}

			// Save results to density map
			for (DensityEntry entry : results) {
				if (entry.getDensity() != null) {
					senior_project.foodscanner.FoodItem.addDensity(entry.getName(),
							(double) (entry.getDensity()));
				}
				//senior_project.foodscanner.FoodItem.addDensity(entry.getName(), (double) (entry.getDensity()));
			}

			// Save densities locally, so density queries don't max out our api limit

            // Delete local density file, if it already exists
            // NOTE: With our current set up, it should never have to delete previous cache
            if (localDensities.exists()) {
                boolean result = localDensities.delete();
                if (!result) {
                    act.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast butteredToast = Toast.makeText(act.getApplicationContext(),
                                    "Error: Could not replace cached densities.", Toast.LENGTH_SHORT);
                            butteredToast.show();
                        }
                    });
                    return true;  // Still return true, because download was successful at least
                }
            }

            // Write density name, value pairs to file
            try {
                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(localDensities));
                for (DensityEntry entry : results) {
                    if (entry.getDensity() != null) {
                        stream.write(entry.getName().getBytes());
                        stream.write("\n".getBytes());  // Couldn't use 'System.lineSeparator()'
                        stream.write(("" + (double) entry.getDensity()).getBytes());
                        stream.write("\n".getBytes());
                    }
                }
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
                act.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast butteredToast = Toast.makeText(act.getApplicationContext(),
                                "Error: Could not cache densities.", Toast.LENGTH_SHORT);
                        butteredToast.show();
                    }
                });

				if (localDensities.exists()) {
					// Delete file, in case it was created
					localDensities.delete();
				}

                return true;  // Still return true, because download was successful at least
            }

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
		private boolean fakeQuery = false;

		public SaveMealTask(TaskCompletionListener listener) {
			mListener = listener;
		}

		public SaveMealTask(TaskCompletionListener listener, boolean fakeQuery) {
			mListener = listener;
			this.fakeQuery = fakeQuery;
		}

		@Override
		protected void onCancelled(Meal meal) {
			Bundle b = new Bundle();
			b.putSerializable(TASKID_MEAL_SAVE_EXCEPTION, exception);
			b.putSerializable(TASKID_MEAL_SAVE, meal);
			b.putBoolean(TASKID_MEAL_SAVE_SUCCESS, false);
			mListener.onTaskCompleted(this, b);
		}

		@Override
		protected void onPostExecute(Meal meal) {
			Bundle b = new Bundle();
			b.putSerializable(TASKID_MEAL_SAVE_EXCEPTION, exception);
			b.putSerializable(TASKID_MEAL_SAVE, meal);
			b.putBoolean(TASKID_MEAL_SAVE_SUCCESS, success);
			mListener.onTaskCompleted(this, b);
		}

		@Override
		protected Meal doInBackground(Meal... meals) {
			Meal meal = meals[0];
			try {
				BackendMeal backendMealToSave = convertToBackendMeal(meal);
				BackendMeal savedBackendMeal = null;
				if(fakeQuery) {
					Thread.sleep(7000);
				}
				else{
					savedBackendMeal = mAPI.saveMeal(backendMealToSave).execute();
					meal.setServerId(savedBackendMeal.getId());
				}
				success = true;
				return meal;
			} catch (Exception e) {
				if(!isCancelled()) {
					Log.e("EndpointsHelper", "SaveMealTask", e);
					exception = e;
				}
				return meal;
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
		private boolean fakeQuery = false;

		public DeleteMealTask(TaskCompletionListener listener) {
			mListener = listener;
		}

		public DeleteMealTask(TaskCompletionListener listener, boolean fakeQuery) {
			mListener = listener;
			this.fakeQuery = fakeQuery;
		}

		@Override
		protected void onCancelled(Meal meal) {
			Bundle b = new Bundle();
			b.putSerializable(TASKID_MEAL_DELETE_EXCEPTION, exception);
			b.putSerializable(TASKID_MEAL_DELETE, meal);
			b.putBoolean(TASKID_MEAL_DELETE_SUCCESS, false);
			mListener.onTaskCompleted(this, b);
		}

		@Override
		protected void onPostExecute(Meal meal) {
			Bundle b = new Bundle();
			b.putSerializable(TASKID_MEAL_DELETE_EXCEPTION, exception);
			b.putSerializable(TASKID_MEAL_DELETE, meal);
			b.putBoolean(TASKID_MEAL_DELETE_SUCCESS, success);
			mListener.onTaskCompleted(this, b);
		}

		@Override
		protected Meal doInBackground(Meal... meals) {
			try {
				BackendMeal backendMeal = convertToBackendMeal(meals[0]);
				if(fakeQuery) {
					Thread.sleep(7000);
				}
				else{
					mAPI.deleteMeal(backendMeal.getId()).execute();
				}
				success = true;
				return meals[0];
			} catch (Exception e) {
				if(!isCancelled()) {
					Log.e("EndpointsHelper", "DeleteMealTask", e);
					exception = e;
				}
				return meals[0];
			}
		}
	}

	/**
	 * Gets all meals in backend between two dates inclusive. Null and exception on failure.
	 */
	public class GetMealsWithinDatesTask extends AsyncTask<Date, Void, ArrayList<Meal>> {
		private TaskCompletionListener mListener;
		private Exception exception;
		private boolean fakeQuery = false;

		public GetMealsWithinDatesTask(TaskCompletionListener listener) {
			mListener = listener;
		}

		public GetMealsWithinDatesTask(TaskCompletionListener listener, boolean fakeQuery) {
			mListener = listener;
			this.fakeQuery = fakeQuery;
		}

		@Override
		protected void onCancelled(){
			Bundle b = new Bundle();
			b.putSerializable(TASKID_MEALS_GET, null);
			b.putSerializable(TASKID_MEALS_GET_EXCEPTION, exception);
			mListener.onTaskCompleted(this, b);
		}

		@Override
		protected void onPostExecute(ArrayList<Meal> meals) {
			Bundle b = new Bundle();
			if(meals != null){
				b.putSerializable(TASKID_MEALS_GET, meals);
			}
			else{
				b.putSerializable(TASKID_MEALS_GET, null);
			}
			b.putSerializable(TASKID_MEALS_GET_EXCEPTION, exception);
			mListener.onTaskCompleted(this, b);
		}

		@Override
		protected ArrayList<Meal> doInBackground(Date... dates) {
			try {
				Date startDate = dates[0];
				Date endDate = dates[1];
				List<BackendMeal> backendMeals = null;
				if(fakeQuery){
					Thread.sleep(7000);
				}
				else {
					backendMeals = mAPI.getMealsWithinDates(new DateTime(startDate), new DateTime(endDate)).execute().getItems();
				}
				if(backendMeals == null) {
					backendMeals = new ArrayList<>();
				}
				return convertToFrontEndMeals(backendMeals);
			} catch (Exception e) {
				if(!isCancelled()) {
					Log.e("EndpointsHelper", "GetMealsWithinDatesTask", e);
					exception = e;
				}
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
		void onTaskCompleted(AsyncTask task, Bundle b);
	}

	public interface densityDownloadObserver {
		void densityDownloaded();
	}

	/* Converters */

	public Meal convertToFrontEndMeal(BackendMeal backendMeal)
	{
		Meal meal = new Meal(
				backendMeal.getClientDBId(),
				backendMeal.getDate(),
				backendMeal.getType(),
				convertToFrontEndFoodItems(backendMeal.getFoodItems()),
				backendMeal.getIsNew(),
				backendMeal.getIsChanged(),
				false,
				backendMeal.getId()
		);

		return meal;
	}

	public BackendMeal convertToBackendMeal(Meal meal)
	{
		BackendMeal backendMeal = new BackendMeal();
		//backendMeal.setId(new Long(meal.getServerId()));
		//backendMeal.setId(new Long(610));
		backendMeal.setClientDBId(new Long(meal.getId()));
		backendMeal.setDate(new Long(meal.getDate()));
		backendMeal.setType(meal.getType().getName());
		backendMeal.setIsNew(meal.isNew());
		backendMeal.setIsChanged(meal.isChangedCount());
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
		backendFoodItem.setServingSizeUnit(foodItem.getServingSizeUnit());
		backendFoodItem.setActualServingSizeUnit(foodItem.getActualServingSizeUnit());
		backendFoodItem.setUsesVol(foodItem.usesVolume());
		backendFoodItem.setUsesMass(foodItem.usesMass());
		backendFoodItem.setNeedConvertVol(foodItem.needConvertVol());
		backendFoodItem.setNeedCalculateServings(foodItem.isNeedCalculateServings());
		backendFoodItem.setNumServings(foodItem.getNumServings());
		backendFoodItem.setVolume(foodItem.getVolume());
		backendFoodItem.setCubicVolume(foodItem.getCubicVolume());
		backendFoodItem.setMass(foodItem.getMass());

		JsonMap nutritionMap = new JsonMap();
		nutritionMap.putAll(foodItem.getNutrition());
		backendFoodItem.setCalculatedNutrition(nutritionMap);

		JsonMap uncalcNutritionMap = new JsonMap();
		uncalcNutritionMap.putAll(foodItem.getFields());
		backendFoodItem.setUncalculatedNutrition(uncalcNutritionMap);

		return backendFoodItem;
	}

	public FoodItem convertToFrontEndFoodItem(BackendFoodItem backendFoodItem)
	{
		FoodItem foodItem = new FoodItem();
		foodItem.setName(backendFoodItem.getName());
		foodItem.setBrand(backendFoodItem.getBrand());
		foodItem.setDensity(backendFoodItem.getDensity());
		foodItem.setServingSize(backendFoodItem.getServingSize());
		foodItem.setServingSizeUnit(backendFoodItem.getServingSizeUnit());
		foodItem.setActualServingSizeUnit(backendFoodItem.getActualServingSizeUnit());
		foodItem.setUsesMass(backendFoodItem.getUsesMass());
		foodItem.setUsesVol(backendFoodItem.getUsesVol());
		foodItem.setNeedConvertVol(backendFoodItem.getNeedConvertVol());
		foodItem.setNeedCalculateServings(backendFoodItem.getNeedCalculateServings());
		foodItem.setNumServings(backendFoodItem.getNumServings());
		foodItem.setVolume(backendFoodItem.getVolume());
		foodItem.setCubicVolume(backendFoodItem.getCubicVolume());
		foodItem.setMass(backendFoodItem.getMass());

		JsonMap calcNutritionMap = backendFoodItem.getCalculatedNutrition();

		Iterator itForCalc = calcNutritionMap.entrySet().iterator();
		while (itForCalc.hasNext()) {
			JsonMap.Entry pair = (JsonMap.Entry)itForCalc.next();
			foodItem.setField((String)pair.getKey(), (Double)pair.getValue());
			itForCalc.remove();
		}

		JsonMap uncalcNutritionMap = backendFoodItem.getUncalculatedNutrition();

		Iterator itForUncalc = uncalcNutritionMap.entrySet().iterator();
		while (itForUncalc.hasNext()) {
			JsonMap.Entry pair = (JsonMap.Entry)itForUncalc.next();
			foodItem.setField((String)pair.getKey(), (Double)pair.getValue());
			itForUncalc.remove();
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
