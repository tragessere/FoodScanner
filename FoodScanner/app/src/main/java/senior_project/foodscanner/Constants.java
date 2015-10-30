package senior_project.foodscanner;

/**
 * Created by Evan on 9/23/2015.
 */
public class Constants {
	public static final String APP_PREFERENCES = "senior_project.foodscanner";
	public static final String PREF_ACCOUNT_NAME = "senior_project.foodscanner.account_name";
	public static final String WEB_CLIENT_ID = "419943060428-q8lotlqd42hdl3je6n8ca2oirgc9cnru.apps.googleusercontent.com";

	public static final String SETTINGS_TIME_FORMAT = "senior_project.foodscanner.settings_time_format";
	public static final String SETTINGS_DATE_FORMAT = "senior_project.foodscanner.settings_date_format";
	public static final String SETTINGS_DATE_TYT = "senior_project.foodscanner.settings_date_tyt";
	public static final String SETTINGS_USE_MANUAL_TIMES = "senior_project.foodscanner.settings_use_manual_times";
	public static final String SETTINGS_BREAKFAST_START_MANUAL = "senior_project.foodscanner.settings_breakfast_start_manual";
	public static final String SETTINGS_BREAKFAST_END_MANUAL = "senior_project.foodscanner.settings_breakfast_end_manual";
	public static final String SETTINGS_LUNCH_START_MANUAL = "senior_project.foodscanner.settings_lunch_start_manual";
	public static final String SETTINGS_LUNCH_END_MANUAL = "senior_project.foodscanner.settings_lunch_end_manual";
	public static final String SETTINGS_DINNER_START_MANUAL = "senior_project.foodscanner.settings_dinner_start_manual";
	public static final String SETTINGS_DINNER_END_MANUAL = "senior_project.foodscanner.settings_dinner_end_manual";
	public static final String SETTINGS_BREAKFAST_START_AUTO = "senior_project.foodscanner.settings_breakfast_start_auto";
	public static final String SETTINGS_BREAKFAST_END_AUTO = "senior_project.foodscanner.settings_breakfast_end_auto";
	public static final String SETTINGS_LUNCH_START_AUTO = "senior_project.foodscanner.settings_lunch_start_auto";
	public static final String SETTINGS_LUNCH_END_AUTO = "senior_project.foodscanner.settings_lunch_end_auto";
	public static final String SETTINGS_DINNER_START_AUTO = "senior_project.foodscanner.settings_dinner_start_auto";
	public static final String SETTINGS_DINNER_END_AUTO = "senior_project.foodscanner.settings_dinner_end_auto";

	public static final String DEFAULT_TIME_FORMAT = Settings.TimeFormat._12.toString();
	public static final String DEFAULT_DATE_FORMAT = Settings.DateFormat.dw_mw_dn.toString();
	public static final boolean DEFAULT_DATE_TYT = true;
	public static final boolean DEFAULT_USE_MANUAL_TIMES = false;
	public static final int DEFAULT_BREAKFAST_START = 21600000;	//6 A.M.
	public static final int DEFAULT_BREAKFAST_END = 36000000;	//10 A.M.
	public static final int DEFAULT_LUNCH_START = 39600000;		//11 A.M.
	public static final int DEFAULT_LUNCH_END = 52200000;		//2:30 P.M.
	public static final int DEFAULT_DINNER_START = 61200000;	//5 P.M.
	public static final int DEFAULT_DINNER_END = 72000000;		//8 P.M.

}
