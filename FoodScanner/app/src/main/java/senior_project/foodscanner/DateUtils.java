package senior_project.foodscanner;

import java.util.Calendar;

public class DateUtils {
    public static void toStartOfDay(Calendar day){
        day.set(Calendar.HOUR_OF_DAY, 0);
        day.set(Calendar.MINUTE, 0);
        day.set(Calendar.SECOND, 0);
        day.set(Calendar.MILLISECOND, 0);
    }
    public static void toEndOfDay(Calendar day){
        day.add(Calendar.DATE, 1);
        day.set(Calendar.HOUR_OF_DAY, 0);
        day.set(Calendar.MINUTE, 0);
        day.set(Calendar.SECOND, 0);
        day.set(Calendar.MILLISECOND, 0);
        day.add(Calendar.MILLISECOND, -1);
    }
}
