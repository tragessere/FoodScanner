package senior_project.foodscanner;

import java.text.DateFormatSymbols;
import java.util.GregorianCalendar;

/**
 * Represents app settings.
 */
public class Settings {

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
    private TimeFormat setting_TimeFormat = TimeFormat._12;
    private DateFormat setting_DateFormat = DateFormat.dw_mw_dn;
    private boolean setting_DateFormat_TYT = true;// whether or not to display today's, yesterday's, and tomorrow's dates as "Today", "Yesterday", "Tomorrow" respectively.

    private static DateFormatSymbols format = new DateFormatSymbols();
    private static final String[] am_pm = {"am", "pm"};
    private static final long msInDay = 24*60*60*1000;// TODO come up with more reliable method for prev/next day navigation

    public void setTimeFormat(TimeFormat tf){
        setting_TimeFormat = tf;
    }

    public void setDateFormat(DateFormat df){
        setting_DateFormat = df;
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

}
