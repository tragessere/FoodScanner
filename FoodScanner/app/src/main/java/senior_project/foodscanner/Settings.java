package senior_project.foodscanner;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.DateFormatSymbols;
import java.util.GregorianCalendar;

/**
 * Represents app settings.
 */
public class Settings {
    private static Settings mSettings;
    private Context mContext;

    /**
     * prefixes:
     *  d = day
     *  m = month
     *  y = year
     * suffixes:
     *  n = number
     *  w = word
     */
    public enum DateFormat{
        mn_dn_yn, dw_mw_dn,
    }

    public enum TimeFormat{
        _12, _24
    }

    // current settings
    private TimeFormat setting_TimeFormat;
    private DateFormat setting_DateFormat;
    private boolean setting_DateFormat_TYT;// whether or not to display today's, yesterday's, and tomorrow's dates as "Today", "Yesterday", "Tomorrow" respectively.
    private boolean useManualTimes;

    private int breakfastStartManual;
    private int breakfastEndManual;
    private int lunchStartManual;
    private int lunchEndManual;
    private int dinnerStartManual;
    private int dinnerEndManual;

    private int breakfastStartAuto;
    private int breakfastEndAuto;
    private int lunchStartAuto;
    private int lunchEndAuto;
    private int dinnerStartAuto;
    private int dinnerEndAuto;

    private static DateFormatSymbols format = new DateFormatSymbols();
    private static final String[] am_pm = {"am", "pm"};
    private static final long msInDay = 24*60*60*1000;// TODO come up with more reliable method for prev/next day navigation

    private Settings(Context context) {
        mContext = context.getApplicationContext();
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);

        breakfastStartManual = prefs.getInt(Constants.SETTINGS_BREAKFAST_START_MANUAL, -1);
        if(breakfastStartManual == -1) {    //No meal times in SharedPreferences
            SharedPreferences.Editor edit = prefs.edit();

            edit.putString(Constants.SETTINGS_TIME_FORMAT, Constants.DEFAULT_TIME_FORMAT);
            edit.putString(Constants.SETTINGS_DATE_FORMAT, Constants.DEFAULT_DATE_FORMAT);
            edit.putBoolean(Constants.SETTINGS_DATE_TYT, Constants.DEFAULT_DATE_TYT);
            edit.putBoolean(Constants.SETTINGS_USE_MANUAL_TIMES, Constants.DEFAULT_USE_MANUAL_TIMES);

            setting_TimeFormat = TimeFormat.valueOf(Constants.DEFAULT_TIME_FORMAT);
            setting_DateFormat = DateFormat.valueOf(Constants.DEFAULT_DATE_FORMAT);
            setting_DateFormat_TYT = Constants.DEFAULT_DATE_TYT;
            useManualTimes = false;

            edit.putInt(Constants.SETTINGS_BREAKFAST_START_MANUAL, Constants.DEFAULT_BREAKFAST_START);
            edit.putInt(Constants.SETTINGS_BREAKFAST_START_AUTO, Constants.DEFAULT_BREAKFAST_START);
            edit.putInt(Constants.SETTINGS_BREAKFAST_END_MANUAL, Constants.DEFAULT_BREAKFAST_END);
            edit.putInt(Constants.SETTINGS_BREAKFAST_END_AUTO, Constants.DEFAULT_BREAKFAST_END);
            edit.putInt(Constants.SETTINGS_LUNCH_START_MANUAL, Constants.DEFAULT_LUNCH_START);
            edit.putInt(Constants.SETTINGS_LUNCH_START_AUTO, Constants.DEFAULT_LUNCH_START);
            edit.putInt(Constants.SETTINGS_LUNCH_END_MANUAL, Constants.DEFAULT_LUNCH_END);
            edit.putInt(Constants.SETTINGS_LUNCH_END_AUTO, Constants.DEFAULT_LUNCH_END);
            edit.putInt(Constants.SETTINGS_DINNER_START_MANUAL, Constants.DEFAULT_DINNER_START);
            edit.putInt(Constants.SETTINGS_DINNER_START_AUTO, Constants.DEFAULT_DINNER_START);
            edit.putInt(Constants.SETTINGS_DINNER_END_MANUAL, Constants.DEFAULT_DINNER_END);
            edit.putInt(Constants.SETTINGS_DINNER_END_AUTO, Constants.DEFAULT_DINNER_END);

            breakfastStartManual = Constants.DEFAULT_BREAKFAST_START;
            breakfastStartAuto = Constants.DEFAULT_BREAKFAST_START;
            breakfastEndManual = Constants.DEFAULT_BREAKFAST_END;
            breakfastEndAuto = Constants.DEFAULT_BREAKFAST_END;
            lunchStartManual = Constants.DEFAULT_LUNCH_START;
            lunchStartAuto = Constants.DEFAULT_LUNCH_START;
            lunchEndManual = Constants.DEFAULT_LUNCH_END;
            lunchEndAuto = Constants.DEFAULT_LUNCH_END;
            dinnerStartManual = Constants.DEFAULT_DINNER_START;
            dinnerStartAuto = Constants.DEFAULT_DINNER_START;
            dinnerEndManual = Constants.DEFAULT_DINNER_END;
            dinnerEndAuto = Constants.DEFAULT_DINNER_END;

            edit.apply();
        } else {
            setting_TimeFormat = TimeFormat.valueOf(prefs.getString(Constants.SETTINGS_TIME_FORMAT, ""));
            setting_DateFormat = DateFormat.valueOf(prefs.getString(Constants.SETTINGS_DATE_FORMAT, ""));
            setting_DateFormat_TYT = prefs.getBoolean(Constants.SETTINGS_DATE_TYT, Constants.DEFAULT_DATE_TYT);
            useManualTimes = prefs.getBoolean(Constants.SETTINGS_USE_MANUAL_TIMES, Constants.DEFAULT_USE_MANUAL_TIMES);

            breakfastStartAuto = prefs.getInt(Constants.SETTINGS_BREAKFAST_START_AUTO, -1);
            breakfastEndManual = prefs.getInt(Constants.SETTINGS_BREAKFAST_END_MANUAL, -1);
            breakfastEndAuto = prefs.getInt(Constants.SETTINGS_BREAKFAST_END_AUTO, -1);
            lunchStartManual = prefs.getInt(Constants.SETTINGS_LUNCH_START_MANUAL, -1);
            lunchStartAuto = prefs.getInt(Constants.SETTINGS_LUNCH_START_AUTO, -1);
            lunchEndManual = prefs.getInt(Constants.SETTINGS_LUNCH_END_MANUAL, -1);
            lunchEndAuto = prefs.getInt(Constants.SETTINGS_LUNCH_END_AUTO, -1);
            dinnerStartManual = prefs.getInt(Constants.SETTINGS_DINNER_START_MANUAL, -1);
            dinnerStartAuto = prefs.getInt(Constants.SETTINGS_DINNER_START_AUTO, -1);
            dinnerEndManual = prefs.getInt(Constants.SETTINGS_DINNER_END_MANUAL, -1);
            dinnerEndAuto = prefs.getInt(Constants.SETTINGS_DINNER_END_AUTO, -1);
        }

    }

    public static Settings initialize(Context context) {
        if(mSettings == null)
            mSettings = new Settings(context);

        return mSettings;
    }

    public static Settings getInstance() {
        return mSettings;
    }

    public static void clearSettings() {
        mSettings = null;
    }


    public void setTimeFormat(TimeFormat tf){
        setting_TimeFormat = tf;
        setSharedPreference(Constants.SETTINGS_TIME_FORMAT, tf.toString());
    }

    public TimeFormat getTimeFormat() {
        return setting_TimeFormat;
    }

    public void setDateFormat(DateFormat df){
        setting_DateFormat = df;
        setSharedPreference(Constants.SETTINGS_DATE_FORMAT, df.toString());
    }

    public DateFormat getDateFormat() {
        return setting_DateFormat;
    }

    /**
     * Returns time as a string formatted according to current settings.
     * @return
     */
    public String formatTime(Meal meal){
        GregorianCalendar date = meal.getDate();

        int hour = date.get(GregorianCalendar.HOUR);
        if(hour == 0){
            hour = 12;
        }
        if(setting_TimeFormat == TimeFormat._24 && date.get(GregorianCalendar.AM_PM) == GregorianCalendar.PM){
            hour+=12;
        }

        String s = ""+hour+":";
        int minute = date.get(GregorianCalendar.MINUTE);
        if(minute < 10){
            s += '0';
        }
        s+=minute;

        if(setting_TimeFormat == TimeFormat._12){
            s+=am_pm[date.get(GregorianCalendar.AM_PM)];
        }

        return s;
    }

    /**
     * Returns date as a string formatted according to current settings.
     * @return
     */
    public String formatDate(Meal meal){
        return formatDate(meal.getDate());
    }

    /**
     * Returns date as a string formatted according to current settings.
     * @return
     */
    public String formatDate(int year, int month, int dayOfMonth){
        return formatDate(new GregorianCalendar(year,month,dayOfMonth));
    }

    /**
     * Returns date as a string formatted according to current settings.
     * @return
     */
    public String formatDate(GregorianCalendar cal){
        int year = cal.get(GregorianCalendar.YEAR);
        int month = cal.get(GregorianCalendar.MONTH);
        int dayOfMonth = cal.get(GregorianCalendar.DAY_OF_MONTH);

        if(setting_DateFormat_TYT){
            GregorianCalendar day = new GregorianCalendar();
            if(day.get(GregorianCalendar.YEAR) == year && day.get(GregorianCalendar.MONTH) == month && day.get(GregorianCalendar.DAY_OF_MONTH) == dayOfMonth){
                return "Today";
            }
            day.setTimeInMillis(day.getTimeInMillis() + msInDay);
            if(day.get(GregorianCalendar.YEAR) == year && day.get(GregorianCalendar.MONTH) == month && day.get(GregorianCalendar.DAY_OF_MONTH) == dayOfMonth){
                return "Tomorrow";
            }
            day.setTimeInMillis(day.getTimeInMillis()-2*msInDay);
            if(day.get(GregorianCalendar.YEAR) == year && day.get(GregorianCalendar.MONTH) == month && day.get(GregorianCalendar.DAY_OF_MONTH) == dayOfMonth){
                return "Yesterday";
            }
        }

        String s = "";
        switch(setting_DateFormat){
            case mn_dn_yn:
                String y = ""+year;
                s = month+"/"+dayOfMonth+"/"+y.substring(2);
                break;
            case dw_mw_dn:
                String day = format.getWeekdays()[cal.get(GregorianCalendar.DAY_OF_WEEK)];
                s = day + ", " + format.getShortMonths()[month] +" "+ dayOfMonth;
                break;
            default:
                s = "ERROR";//TODO handle error
        }

        return s;
    }

    public void setUseManualTimes(boolean useManualTimes) {
        this.useManualTimes = useManualTimes;
    }

    public boolean isUsingManualTimes() {
        return useManualTimes;
    }

    public void setBreakfastStartManual(int startTime) {
        setSharedPreference(Constants.SETTINGS_BREAKFAST_START_MANUAL, startTime);
        breakfastStartManual = startTime;
    }

    public int getBreakfastStartManual() {
        return breakfastStartManual;
    }

    public void setBreakfastEndManual(int endTime) {
        setSharedPreference(Constants.SETTINGS_BREAKFAST_END_MANUAL, endTime);
        breakfastEndManual = endTime;
    }

    public int getBreakfastEndManual() {
        return breakfastEndManual;
    }

    public void setLunchStartManual(int startTime) {
        setSharedPreference(Constants.SETTINGS_LUNCH_START_MANUAL, startTime);
        lunchStartManual = startTime;
    }

    public int getLunchStartManual() {
        return lunchStartManual;
    }

    public void setLunchEndManual(int endTime) {
        setSharedPreference(Constants.SETTINGS_LUNCH_END_MANUAL, endTime);
        lunchEndManual = endTime;
    }

    public int getLunchEndManual() {
        return lunchEndManual;
    }

    public void setDinnerStartManual(int startTime) {
        setSharedPreference(Constants.SETTINGS_DINNER_START_MANUAL, startTime);
        dinnerStartManual = startTime;
    }

    public int getDinnerStartManual() {
        return dinnerStartManual;
    }

    public void setDinnerEndManual(int endTime) {
        setSharedPreference(Constants.SETTINGS_DINNER_END_MANUAL, endTime);
        dinnerEndManual = endTime;
    }

    public int getDinnerEndManual() {
        return dinnerEndManual;
    }

    public void setBreakfastStartAuto(int startTime) {
        setSharedPreference(Constants.SETTINGS_BREAKFAST_START_AUTO, startTime);
        breakfastStartAuto = startTime;
    }

    public int getBreakfastStartAuto() {
        return breakfastStartAuto;
    }

    public void setBreakfastEndAuto(int endTime) {
        setSharedPreference(Constants.SETTINGS_BREAKFAST_END_AUTO, endTime);
        breakfastEndAuto = endTime;
    }

    public int getBreakfastEndAuto() {
        return breakfastEndAuto;
    }

    public void setLunchStartAuto(int startTime) {
        setSharedPreference(Constants.SETTINGS_LUNCH_START_AUTO, startTime);
        lunchStartAuto = startTime;
    }

    public int getLunchStartAuto() {
        return lunchStartAuto;
    }

    public void setLunchEndAuto(int endTime) {
        setSharedPreference(Constants.SETTINGS_LUNCH_END_AUTO, endTime);
        lunchEndAuto = endTime;
    }

    public int getLunchEndAuto() {
        return lunchEndAuto;
    }

    public void setDinnerStartAuto(int startTime) {
        setSharedPreference(Constants.SETTINGS_DINNER_START_AUTO, startTime);
        dinnerStartAuto = startTime;
    }

    public int getDinnerStartAuto() {
        return dinnerStartAuto;
    }

    public void setDinnerEndAuto(int endTime) {
        setSharedPreference(Constants.SETTINGS_DINNER_END_AUTO, endTime);
        dinnerEndAuto = endTime;
    }

    public int getDinnerEndAuto() {
        return dinnerEndAuto;
    }

    private void setAllManualTimes(int breakfastStart, int breakfastEnd, int lunchStart, int lunchEnd, int dinnerStart, int dinnerEnd) {
        SharedPreferences.Editor edit = mContext.getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE).edit();

        edit.putInt(Constants.SETTINGS_BREAKFAST_START_MANUAL, breakfastStart);
        edit.putInt(Constants.SETTINGS_BREAKFAST_END_MANUAL, breakfastEnd);
        edit.putInt(Constants.SETTINGS_LUNCH_START_MANUAL, lunchStart);
        edit.putInt(Constants.SETTINGS_LUNCH_END_MANUAL, lunchEnd);
        edit.putInt(Constants.SETTINGS_DINNER_START_MANUAL, dinnerStart);
        edit.putInt(Constants.SETTINGS_DINNER_END_MANUAL, dinnerEnd);

        edit.apply();

        breakfastStartManual = breakfastStart;
        breakfastEndManual = breakfastEnd;
        lunchStartManual = lunchStart;
        lunchEndManual = lunchEnd;
        dinnerStartManual = dinnerStart;
        dinnerEndManual = dinnerEnd;
    }

    private void setSharedPreference(String key, int value) {
        mContext.getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE).edit().putInt(key, value).apply();
    }

    private void setSharedPreference(String key, String value) {
        mContext.getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE).edit().putString(key, value).apply();
    }
}
