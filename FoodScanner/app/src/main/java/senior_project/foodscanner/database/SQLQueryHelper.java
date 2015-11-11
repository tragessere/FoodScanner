package senior_project.foodscanner.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import senior_project.foodscanner.FoodItem;
import senior_project.foodscanner.Meal;

/**
 * Created by Evan on 10/19/2015.
 */
public class SQLQueryHelper {

	private SQLQueryHelper() { }

	private static final String[] ALL_COLUMNS = new String[] {
			SQLHelper.COLUMN_ID, SQLHelper.COLUMN_MEAL_TYPE,
			SQLHelper.COLUMN_TIME, SQLHelper.COLUMN_FOOD_LIST,
			SQLHelper.COLUMN_NEW, SQLHelper.COLUMN_CHANGED};

	/**
	 * Insert a meal object into the database
	 *
	 * @param meal	<code>Meal</code> object to insert
	 * @return		Id of the newly inserted <code>Meal</code>
	 */
	public static long insertMeal(Meal meal) {
		SQLiteDatabase db = SQLHelper.getInstance().getWritableDatabase();

		meal.setId(db.insert(SQLHelper.TABLE_MEALS, null, meal.toContentValues()));

		return meal.getId();
	}

	
	/**
	 * Insert a list of meals into the database at one time
	 *
	 * @param meals	List of <code>Meal</code> objects to insert
	 *
	 * @return	number of rows inserted into the database
	 */
	public static int insertMeals(List<Meal> meals) {
		ContentValues[] cvs = new ContentValues[meals.size()];

		for(int i = 0; i < meals.size(); i++) {
			cvs[i] = meals.get(i).toContentValues();
		}

		return SQLHelper.bulkInsert(SQLHelper.TABLE_MEALS, cvs);
	}


	/**
	 * Insert an array of meals into the database at one time
	 *
	 * @param meals	Array of <code>Meal</code> objects to insert
	 *
	 * @return	number of rows inserted into the database
	 */
	public static int insertMeals(Meal[] meals) {
		ContentValues[] cvs = new ContentValues[meals.length];

		for(int i = 0; i < meals.length; i++) {
			cvs[i] = meals[i].toContentValues();
		}

		return SQLHelper.bulkInsert(SQLHelper.TABLE_MEALS, cvs);
	}


	/**
	 * Update an existing <code>Meal</code> object in the database. The <code>Meal</code> should
	 * have a valid ID set.
	 *
	 * @param meal	<code>Meal</code> object to update
	 */
	public static void updateMeal(Meal meal) {
		SQLiteDatabase db = SQLHelper.getInstance().getWritableDatabase();

		db.update(SQLHelper.TABLE_MEALS, meal.toContentValues(), SQLHelper.COLUMN_ID + " = " + meal.getId(), null);
	}


	/**
	 * Retrieve a <code>Meal</code> object from the database
	 *
	 * @param mealId	Database ID representing the desired <code>Meal</code> object
	 * @return			Meal object from the database or null if the ID is not found
	 */
	public static Meal getMeal(long mealId) {
		SQLiteDatabase db = SQLHelper.getInstance().getReadableDatabase();

		Cursor c = db.query(SQLHelper.TABLE_MEALS, ALL_COLUMNS,
				SQLHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(mealId)},
				null, null, null);

		if(c != null && c.moveToFirst()) {
			Meal meal = new Meal(c.getLong(0), c.getLong(2), c.getString(1), (ArrayList<FoodItem>) bytesToFoodList(c.getBlob(3)), c.getInt(4) == 1, c.getInt(5) == 1);

			c.close();

			return meal;
		} else if(c != null) {
			c.close();
		}

		return null;
	}


	/**
	 * return a list of all meals in a given date interval.
	 *
	 * @param startDay		The earliest date to return meal entries
	 * @param endDay		The latest date to return meal entries
	 * @param useFullDay	Return meals from the full day regardless of the time of day
	 *                      on the <code>Calendar</code> objects. (This will modify the <code>startDay</code>
	 *                      and <code>endDay</code> times)
	 * @return				List of all meal items within the given time interval
	 */
	public static List<Meal> getMeals(Calendar startDay, Calendar endDay, boolean useFullDay) {
		if(useFullDay) {
			startDay.set(Calendar.HOUR_OF_DAY, 0);
			startDay.set(Calendar.MINUTE, 0);
			startDay.set(Calendar.SECOND, 0);
			startDay.set(Calendar.MILLISECOND, 0);

			endDay.add(Calendar.DATE, 1);
			endDay.set(Calendar.HOUR_OF_DAY, 0);
			endDay.set(Calendar.MINUTE, 0);
			endDay.set(Calendar.SECOND, 0);
			endDay.set(Calendar.MILLISECOND, 0);
		}

		List<Meal> mealList = new ArrayList<>();
		SQLiteDatabase db = SQLHelper.getInstance().getReadableDatabase();

		Cursor c = db.query(SQLHelper.TABLE_MEALS, ALL_COLUMNS,
				SQLHelper.COLUMN_TIME + " >= " + startDay.getTimeInMillis() + " AND " + SQLHelper.COLUMN_TIME + " <= " + endDay.getTimeInMillis(),
				null, null, null, SQLHelper.COLUMN_TIME);

		if(c != null && c.moveToFirst()) {
			while(!c.isAfterLast()) {
				mealList.add(new Meal(c.getLong(0), c.getLong(2), c.getString(1), (ArrayList<FoodItem>) bytesToFoodList(c.getBlob(3)), c.getInt(4) == 1, c.getInt(5) == 1));
				c.moveToNext();
			}
			c.close();

			return mealList;
		} else if(c != null) {
			c.close();
			return mealList;
		}

		return null;
	}


	/**
	 * Get all of the meals that have not been uploaded to the backend.
	 *
	 * @return List of <code>Meal</code> objects
	 */
	public static List<Meal> getChangedMeals() {
		List<Meal> mealList = new ArrayList<>();
		SQLiteDatabase db = SQLHelper.getInstance().getReadableDatabase();

		Cursor c = db.query(SQLHelper.TABLE_MEALS, ALL_COLUMNS, SQLHelper.COLUMN_CHANGED + " = 1", null, null, null, SQLHelper.COLUMN_TIME);

		if(c != null && c.moveToFirst()) {
			while(!c.isAfterLast()) {
				mealList.add(new Meal(c.getLong(0), c.getLong(2), c.getString(1), (ArrayList<FoodItem>) bytesToFoodList(c.getBlob(3)), c.getInt(4) == 1, c.getInt(5) == 1));
				c.moveToNext();
			}
			c.close();

			return mealList;
		} else if(c != null) {
			c.close();
			return mealList;
		}

		return null;
	}


	public static byte[] foodListToBytes(List<FoodItem> foodItems) {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

		try (ObjectOutputStream objectOut = new ObjectOutputStream(byteOut)) {
			objectOut.writeObject(foodItems);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return byteOut.toByteArray();
	}

	public static List<FoodItem> bytesToFoodList(byte[] bytes) {
		ArrayList<FoodItem> foodItems;
		ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);

		try (ObjectInputStream objectIn = new ObjectInputStream(byteIn)) {
			//noinspection unchecked
			foodItems = (ArrayList<FoodItem>) objectIn.readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
			return null;
		}

		return foodItems;
	}


}
