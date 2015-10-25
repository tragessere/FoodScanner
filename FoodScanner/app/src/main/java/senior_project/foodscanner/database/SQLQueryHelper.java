package senior_project.foodscanner.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import senior_project.foodscanner.FoodItem;
import senior_project.foodscanner.Meal;

/**
 * Created by Evan on 10/19/2015.
 */
public class SQLQueryHelper {

	private SQLQueryHelper() { }

	public static void insertMeal(Meal meal) {
		SQLiteDatabase db = SQLHelper.getInstance().getWritableDatabase();

		db.insert(SQLHelper.TABLE_MEALS, null, meal.toContentValues());
	}


	/**
	 * Retrieve a <code>Meal</code> object from the database
	 *
	 * @param mealId	Database ID representing the desired <code>Meal</code> object
	 * @return			Meal object from the database or null if the ID is not found
	 */
	public static Meal getMeal(long mealId) {
		SQLiteDatabase db = SQLHelper.getInstance().getReadableDatabase();

		Cursor c = db.query(SQLHelper.TABLE_MEALS, new String[]{SQLHelper.COLUMN_MEAL_TYPE, SQLHelper.COLUMN_TIME, SQLHelper.COLUMN_FOOD_LIST},
				SQLHelper.COLUMN_MEAL_ID + " = ?", new String[]{String.valueOf(mealId)},
				null, null, null);

		if(c != null && c.moveToFirst()) {
			GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTimeInMillis(c.getLong(1));
			Meal.MealType type = Meal.MealType.valueOf(c.getString(0));

			Meal meal = new Meal(calendar, type);
			meal.setFoodList(bytesToFoodList(c.getBlob(2)));

			c.close();

			return meal;
		} else if(c != null) {
			c.close();
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
