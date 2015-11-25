package senior_project.foodscanner.ui.components.mealcalendar;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.CalendarView;

import java.util.Date;
import java.util.GregorianCalendar;

import senior_project.foodscanner.R;

/**
 * Shows a calendar dialog to choose a date.
 */
public class CalendarDialog {

    public static void show(Context context, final CalendarDialogListener listener){
        show(context, listener, new Date().getTime());
    }

    public static void show(Context context, final CalendarDialogListener listener, long date){
        final Dialog d = new Dialog(context);
        d.setContentView(R.layout.dialog_calendar);
        final CalendarView calendar = (CalendarView)d.findViewById(R.id.calendarView);
        calendar.setDate(date, false, true);
        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                listener.onCalendarDialogDateSelected(new GregorianCalendar(year, month, dayOfMonth));
                d.hide();
            }
        });
        d.findViewById(R.id.button_today).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCalendarDialogDateSelected(new GregorianCalendar());
                d.hide();
            }
        });
        d.findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.hide();
            }
        });
        d.show();
    }

    public interface CalendarDialogListener {
        void onCalendarDialogDateSelected(GregorianCalendar date);
    }
}
