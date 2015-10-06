package senior_project.foodscanner.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by Evan on 9/16/2015.
 */
public class SQLHelper extends SQLiteOpenHelper {
	private static SQLHelper mDbHelper;

	private static final String DATABASE_NAME = "foodScanner.db";
	//Update database version whenever changing tables or columns
	private static final int DATABASE_VERSION = 1;
	private static final String DROP = "DROP TABLE IF EXISTS ";

	public static final String TABLE_MEALS = "table_meals";
	public static final String TABLE_FOOD_ITEMS = "food_items";


	//Needs this exact name for some classes (e.g. CursorAdapter)
	public static final String COLUMN_ID = "_id";


	public static final String COLUMN_TIME = "time";
	public static final String COLUMN_IMAGE_TOP_PATH = "image_top_path";
	public static final String COLUMN_IMAGE_SIDE_PATH = "image_side_path";
	public static final String COLUMN_FINISHED = "finished";

	private static final String TABLE_MEALS_CREATE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_MEALS + "("
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_TIME + " INT, "
			+ COLUMN_IMAGE_TOP_PATH + " TEXT, "
			+ COLUMN_IMAGE_SIDE_PATH + " TEXT, "
			+ COLUMN_FINISHED + " INT);";


	public static final String COLUMN_MEAL_ID = "meal_id";
	public static final String COLUMN_FOOD_NAME = "food_name";
	public static final String COLUMN_VOLUME = "volume";

	private static final String TABLE_FOOD_ITEM_CREATE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_FOOD_ITEMS + "("
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "FOREIGN KEY(" + COLUMN_MEAL_ID + ") REFERENCES " + TABLE_MEALS + "(" + COLUMN_ID + "), "
			+ COLUMN_FOOD_NAME + " TEXT, "
			+ COLUMN_VOLUME + " INT);";



	private SQLHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static SQLHelper getInstance(Context context) {
		if(mDbHelper == null)
			mDbHelper = new SQLHelper(context.getApplicationContext());

		return mDbHelper;
	}


	/**
	 * Convenience method for quickly inserting several rows into a single table
	 *
	 * @param tableName		Name of the table where the records will be inserted
	 * @param vals			Array of ContentValues to be inserted
	 */
	public static void bulkInsert(@NonNull String tableName, @NonNull ContentValues[] vals) {
		if(tableName == null)
			throw new NullPointerException("SQLHelper:bulkInsert() - table name cannot be null");
		if(vals == null)
			throw new NullPointerException("SQLHelper:bulkInsert() - ContentValues cannot be null");


		SQLiteDatabase mDb = mDbHelper.getWritableDatabase();
		mDb.beginTransaction();
		try {
			for (ContentValues val : vals) {
				mDb.insert(tableName, null, val);
			}
			mDb.setTransactionSuccessful();
		} finally {
			mDb.endTransaction();
		}
	}


	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_MEALS_CREATE);
		db.execSQL(TABLE_FOOD_ITEM_CREATE);
	}

	/**
	 * Upgrades the database when <code>DATABASE_VERSION</code> is incremented.
	 *
	 * This operation currently deletes all data stored in the database!
	 *
	 * To setup the upgrade to preserve data see:
	 * http://stackoverflow.com/questions/8425861/how-do-i-upgrade-a-database-without-removing-the-data-that-the-user-input-in-the
	 *
	 * @param db 			The database to be updated
	 * @param oldVersion	The current version of <code>db</code>
	 * @param newVersion	The desired final version of <code>db</code>
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(SQLHelper.class.getSimpleName(), "Upgrading database from version " + oldVersion
				+ " to " + newVersion);
		clearDatabase(db);
		onCreate(db);
	}

	public void clearDatabase(SQLiteDatabase db) {
		db.execSQL(DROP + TABLE_MEALS);
		db.execSQL(DROP + TABLE_FOOD_ITEMS);
	}
}
