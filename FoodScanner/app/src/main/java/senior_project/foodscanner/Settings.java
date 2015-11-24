package senior_project.foodscanner;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Represents app settings.
 */
public class Settings {

    private static final String FORMAT_12_HOUR = "h:mm a";
    private static final String FORMAT_24_HOUR = "HH:mm";

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
    private Calendar formatHelper;

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

    private Settings(Context context) {
        mContext = context.getApplicationContext();
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        formatHelper = Calendar.getInstance();
        formatHelper.setTimeInMillis(0);

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

            breakfastStartAuto = prefs.getInt(Constants.SETTINGS_BREAKFAST_START_AUTO, Constants.DEFAULT_BREAKFAST_START);
            breakfastEndManual = prefs.getInt(Constants.SETTINGS_BREAKFAST_END_MANUAL, Constants.DEFAULT_BREAKFAST_END);
            breakfastEndAuto = prefs.getInt(Constants.SETTINGS_BREAKFAST_END_AUTO, Constants.DEFAULT_BREAKFAST_END);
            lunchStartManual = prefs.getInt(Constants.SETTINGS_LUNCH_START_MANUAL, Constants.DEFAULT_LUNCH_START);
            lunchStartAuto = prefs.getInt(Constants.SETTINGS_LUNCH_START_AUTO, Constants.DEFAULT_LUNCH_START);
            lunchEndManual = prefs.getInt(Constants.SETTINGS_LUNCH_END_MANUAL, Constants.DEFAULT_LUNCH_END);
            lunchEndAuto = prefs.getInt(Constants.SETTINGS_LUNCH_END_AUTO, Constants.DEFAULT_LUNCH_END);
            dinnerStartManual = prefs.getInt(Constants.SETTINGS_DINNER_START_MANUAL, Constants.DEFAULT_DINNER_START);
            dinnerStartAuto = prefs.getInt(Constants.SETTINGS_DINNER_START_AUTO, Constants.DEFAULT_DINNER_START);
            dinnerEndManual = prefs.getInt(Constants.SETTINGS_DINNER_END_MANUAL, Constants.DEFAULT_DINNER_END);
            dinnerEndAuto = prefs.getInt(Constants.SETTINGS_DINNER_END_AUTO, Constants.DEFAULT_DINNER_END);
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

    public void clearSettings() {
        SharedPreferences.Editor edit = mContext.getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE).edit();

        edit.remove(Constants.SETTINGS_TIME_FORMAT);
        edit.remove(Constants.SETTINGS_DATE_FORMAT);
        edit.remove(Constants.SETTINGS_DATE_TYT);
        edit.remove(Constants.SETTINGS_USE_MANUAL_TIMES);
        edit.remove(Constants.SETTINGS_BREAKFAST_START_MANUAL);
        edit.remove(Constants.SETTINGS_BREAKFAST_START_AUTO);
        edit.remove(Constants.SETTINGS_BREAKFAST_END_MANUAL);
        edit.remove(Constants.SETTINGS_BREAKFAST_END_AUTO);
        edit.remove(Constants.SETTINGS_LUNCH_START_MANUAL);
        edit.remove(Constants.SETTINGS_LUNCH_START_AUTO);
        edit.remove(Constants.SETTINGS_LUNCH_END_MANUAL);
        edit.remove(Constants.SETTINGS_LUNCH_END_AUTO);
        edit.remove(Constants.SETTINGS_DINNER_START_MANUAL);
        edit.remove(Constants.SETTINGS_DINNER_START_AUTO);
        edit.remove(Constants.SETTINGS_DINNER_END_MANUAL);
        edit.remove(Constants.SETTINGS_DINNER_END_AUTO);

        edit.apply();
    }


    public void setTimeFormat(TimeFormat tf){
        setting_TimeFormat = tf;
        setSharedPreference(Constants.SETTINGS_TIME_FORMAT, tf.toString());
    }

    public TimeFormat getTimeFormat() {
        return setting_TimeFormat;
    }

    public boolean isUsing24HourFormat() {
        return setting_TimeFormat == TimeFormat._24;
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
        GregorianCalendar date = new GregorianCalendar();
        date.setTimeInMillis(meal.getDate());

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

    public String formatDate(long date){
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(date);
        return formatDate(cal);
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
            day.add(Calendar.DATE, 1);
            if(day.get(GregorianCalendar.YEAR) == year && day.get(GregorianCalendar.MONTH) == month && day.get(GregorianCalendar.DAY_OF_MONTH) == dayOfMonth){
                return "Tomorrow";
            }
            day.add(Calendar.DATE, -2);
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
                if(year != new GregorianCalendar().get(Calendar.YEAR)){
                    s += ", " + year;
                }
                break;
            default:
                s = "ERROR";
        }

        return s;
    }

    public String formatHour(int millis) {
        formatHelper.set(Calendar.HOUR_OF_DAY, millisToHours(millis));
        formatHelper.set(Calendar.MINUTE, millisToMins(millis));
        return new SimpleDateFormat(isUsing24HourFormat() ? FORMAT_24_HOUR : FORMAT_12_HOUR, Locale.getDefault())
                .format(formatHelper.getTimeInMillis());
    }

    public String formatHour(int hour, int minute) {
        formatHelper.set(Calendar.HOUR_OF_DAY, hour);
        formatHelper.set(Calendar.MINUTE, minute);
        return new SimpleDateFormat(isUsing24HourFormat() ? FORMAT_24_HOUR : FORMAT_12_HOUR, Locale.getDefault())
                .format(formatHelper.getTimeInMillis());
    }


    /**
     * Get the meal that would be eaten during the given time based on the current meal time settings.
     * This method does not auto-suggest dessert since it happens immediately after a meal (thus during another given meal time)
     *
     * @param millis    milliseconds since epoch
     * @return          best guess at current meal
     */
    public Meal.MealType getMealAtTime(long millis) {
        int currentDayTime;

        formatHelper.setTimeInMillis(millis);
        currentDayTime = formatHelper.get(Calendar.HOUR_OF_DAY) * Constants.MILLIS_IN_HOUR + formatHelper.get(Calendar.MINUTE) * Constants.MILLIS_IN_MIN;



        int breakfastStart;
        int breakfastEnd;
        int lunchStart;
        int lunchEnd;
        int dinnerStart;
        int dinnerEnd;

        if(useManualTimes) {
            breakfastStart = breakfastStartManual;
            breakfastEnd = breakfastEndManual;
            lunchStart = lunchStartManual;
            lunchEnd = lunchEndManual;
            dinnerStart = dinnerStartManual;
            dinnerEnd = dinnerEndManual;
        } else {
            breakfastStart = breakfastStartAuto;
            breakfastEnd = breakfastEndAuto;
            lunchStart = lunchStartAuto;
            lunchEnd = lunchEndAuto;
            dinnerStart = dinnerStartAuto;
            dinnerEnd = dinnerEndAuto;
        }


        if(breakfastEnd < breakfastStart) {
            if(currentDayTime >= breakfastStart || currentDayTime < breakfastEnd)
                return Meal.MealType.BREAKFAST;
        } else {
            if(currentDayTime >= breakfastStart && currentDayTime < breakfastEnd)
                return Meal.MealType.BREAKFAST;
        }
        if(lunchEnd < lunchStart) {
            if(currentDayTime >= lunchStart || currentDayTime < lunchEnd)
                return Meal.MealType.LUNCH;
        } else {
            if(currentDayTime >= lunchStart && currentDayTime < lunchEnd)
                return Meal.MealType.LUNCH;
        }

        if(dinnerEnd < dinnerStart) {
            if(currentDayTime >= dinnerStart || currentDayTime < dinnerEnd)
                return Meal.MealType.DINNER;
        } else {
            if(currentDayTime >= dinnerStart && currentDayTime < dinnerEnd)
                return Meal.MealType.DINNER;
        }

        if(dinnerEnd < breakfastStart) {
            if(currentDayTime >= dinnerEnd && currentDayTime < breakfastStart)
                return Meal.MealType.DESSERT;
        } else {
            if(currentDayTime >= dinnerEnd || currentDayTime < breakfastStart)
                return Meal.MealType.DESSERT;
        }

        if(lunchStart < breakfastEnd) {
            if(currentDayTime >= breakfastEnd || currentDayTime < lunchStart)
                return Meal.MealType.BRUNCH;
        } else {
            if(currentDayTime >= breakfastEnd && currentDayTime < lunchStart)
                return Meal.MealType.BRUNCH;
        }



        return Meal.MealType.SNACK;
    }

    /**
     * Get the most likely meal being eaten at the time this method was called.
     * This method does not auto-suggest dessert since it happens immediately after a meal (thus during another given meal time)
     *
     * @return
     */
    public Meal.MealType getCurrentMeal() {
        return getMealAtTime(System.currentTimeMillis());
    }



    public void setUseManualTimes(boolean useManualTimes) {
        this.useManualTimes = useManualTimes;
        setSharedPreference(Constants.SETTINGS_USE_MANUAL_TIMES, useManualTimes);
    }

    public boolean isUsingManualTimes() {
        return useManualTimes;
    }



    //Lots of time setters/getters below

    public void setBreakfastStartManual(int startTime) {
        setSharedPreference(Constants.SETTINGS_BREAKFAST_START_MANUAL, startTime);
        breakfastStartManual = startTime;
    }

    public void setBreakfastStartManual(int hour, int minute) {
        setBreakfastStartManual(timeToMillis(hour, minute));
    }

    public int getBreakfastStartManual() {
        return breakfastStartManual;
    }

    public int getBreakfastStartManualHour() {
        return millisToHours(breakfastStartManual);
    }

    public int getBreakfastStartManualMinute() {
        return millisToMins(breakfastStartManual);
    }

    public void setBreakfastEndManual(int endTime) {
        setSharedPreference(Constants.SETTINGS_BREAKFAST_END_MANUAL, endTime);
        breakfastEndManual = endTime;
    }

    public void setBreakfastEndManual(int hour, int minute) {
        setBreakfastEndManual(timeToMillis(hour, minute));
    }

    public int getBreakfastEndManual() {
        return breakfastEndManual;
    }

    public int getBreakfastEndManualHour() {
        return millisToHours(breakfastEndManual);
    }

    public int getBreakfastEndManualMinute() {
        return millisToMins(breakfastEndManual);
    }

    public void setLunchStartManual(int startTime) {
        setSharedPreference(Constants.SETTINGS_LUNCH_START_MANUAL, startTime);
        lunchStartManual = startTime;
    }

    public void setLunchStartManual(int hour, int minute) {
        setLunchStartManual(timeToMillis(hour, minute));
    }

    public int getLunchStartManual() {
        return lunchStartManual;
    }

    public int getLunchStartManualHour() {
        return millisToHours(lunchStartManual);
    }

    public int getLunchStartManualMinute() {
        return millisToMins(lunchStartManual);
    }

    public void setLunchEndManual(int endTime) {
        setSharedPreference(Constants.SETTINGS_LUNCH_END_MANUAL, endTime);
        lunchEndManual = endTime;
    }

    public void setLunchEndManual(int hour, int minute) {
        setLunchEndManual(timeToMillis(hour, minute));
    }

    public int getLunchEndManual() {
        return lunchEndManual;
    }

    public int getLunchEndManualHour() {
        return millisToHours(lunchEndManual);
    }

    public int getLunchEndManualMinute() {
        return millisToMins(lunchEndManual);
    }

    public void setDinnerStartManual(int startTime) {
        setSharedPreference(Constants.SETTINGS_DINNER_START_MANUAL, startTime);
        dinnerStartManual = startTime;
    }

    public void setDinnerStartManual(int hour, int minute) {
        setDinnerStartManual(timeToMillis(hour, minute));
    }

    public int getDinnerStartManual() {
        return dinnerStartManual;
    }

    public int getDinnerStartManualHour() {
        return millisToHours(dinnerStartManual);
    }

    public int getDinnerStartManualMinute() {
        return millisToMins(dinnerStartManual);
    }

    public void setDinnerEndManual(int endTime) {
        setSharedPreference(Constants.SETTINGS_DINNER_END_MANUAL, endTime);
        dinnerEndManual = endTime;
    }

    public void setDinnerEndManual(int hour, int minute) {
        setDinnerEndManual(timeToMillis(hour, minute));
    }

    public int getDinnerEndManual() {
        return dinnerEndManual;
    }

    public int getDinnerEndManualHour() {
        return millisToHours(dinnerEndManual);
    }

    public int getDinnerEndManualMinute() {
        return millisToMins(dinnerEndManual);
    }

    public void setBreakfastStartAuto(int startTime) {
        setSharedPreference(Constants.SETTINGS_BREAKFAST_START_AUTO, startTime);
        breakfastStartAuto = startTime;
    }

    public int getBreakfastStartAuto() {
        return breakfastStartAuto;
    }

    public int getBreakfastStartAutoHour() {
        return millisToHours(breakfastStartAuto);
    }

    public int getBreakfastStartAutoMinute() {
        return millisToMins(breakfastStartAuto);
    }

    public void setBreakfastEndAuto(int endTime) {
        setSharedPreference(Constants.SETTINGS_BREAKFAST_END_AUTO, endTime);
        breakfastEndAuto = endTime;
    }

    public void setBreakfastEndAuto(int hour, int minute) {
        setBreakfastEndAuto(timeToMillis(hour, minute));
    }

    public int getBreakfastEndAuto() {
        return breakfastEndAuto;
    }

    public int getBreakfastEndAutoHour() {
        return millisToHours(breakfastEndAuto);
    }

    public int getBreakfastEndAutoMinute() {
        return millisToMins(breakfastEndAuto);
    }

    public void setLunchStartAuto(int startTime) {
        setSharedPreference(Constants.SETTINGS_LUNCH_START_AUTO, startTime);
        lunchStartAuto = startTime;
    }

    public void setLunchStartAuto(int hour, int minute) {
        setLunchStartAuto(timeToMillis(hour, minute));
    }

    public int getLunchStartAuto() {
        return lunchStartAuto;
    }

    public int getLunchStartAutoHour() {
        return millisToHours(lunchStartAuto);
    }

    public int getLunchStartAutoMinute() {
        return millisToMins(lunchStartAuto);
    }

    public void setLunchEndAuto(int endTime) {
        setSharedPreference(Constants.SETTINGS_LUNCH_END_AUTO, endTime);
        lunchEndAuto = endTime;
    }

    public void setLunchEndAuto(int hour, int minute) {
        setLunchEndAuto(timeToMillis(hour, minute));
    }

    public int getLunchEndAuto() {
        return lunchEndAuto;
    }

    public int getLunchEndAutoHour() {
        return millisToHours(lunchEndAuto);
    }

    public int getLunchEndAutoMinute() {
        return millisToMins(lunchEndAuto);
    }

    public void setDinnerStartAuto(int startTime) {
        setSharedPreference(Constants.SETTINGS_DINNER_START_AUTO, startTime);
        dinnerStartAuto = startTime;
    }

    public void setDinnerStartAuto(int hour, int minute) {
        setDinnerStartAuto(timeToMillis(hour, minute));
    }

    public int getDinnerStartAuto() {
        return dinnerStartAuto;
    }

    public int getDinnerStartAutoHour() {
        return millisToHours(dinnerStartAuto);
    }

    public int getDinnerStartAutoMinute() {
        return millisToMins(dinnerStartAuto);
    }

    public void setDinnerEndAuto(int endTime) {
        setSharedPreference(Constants.SETTINGS_DINNER_END_AUTO, endTime);
        dinnerEndAuto = endTime;
    }

    public void setDinnerEndAuto(int hour, int minute) {
        setDinnerEndAuto(timeToMillis(hour, minute));
    }

    public int getDinnerEndAuto() {
        return dinnerEndAuto;
    }

    public int getDinnerEndAutoHour() {
        return millisToHours(dinnerEndAuto);
    }

    public int getDinnerEndAutoMinute() {
        return millisToMins(dinnerEndAuto);
    }

    public void setAllManualTimes(int breakfastStart, int breakfastEnd, int lunchStart, int lunchEnd, int dinnerStart, int dinnerEnd) {
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

    private void setSharedPreference(String key, boolean value) {
        mContext.getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE).edit().putBoolean(key, value).apply();
    }

    public int timeToMillis(int hour, int minute) {
        return hour * Constants.MILLIS_IN_HOUR + minute * Constants.MILLIS_IN_MIN;
    }

    public int millisToHours(int millis) {
        return millis / Constants.MILLIS_IN_HOUR;
    }

    public int millisToMins(int millis) {
        return (millis % Constants.MILLIS_IN_HOUR) / Constants.MILLIS_IN_MIN;
    }
}
