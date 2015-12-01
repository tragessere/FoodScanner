package senior_project.foodscanner.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Shows a calendar dialog to choose a date.
 */
public class CalendarDialogFragment extends DialogFragment {
    private long date = 0;

    public static CalendarDialogFragment newInstance(long date){
        CalendarDialogFragment d = new CalendarDialogFragment();
        d.date = date;
        return d;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        GregorianCalendar date = new GregorianCalendar();
        date.setTimeInMillis(this.date);
        DatePickerDialog d = new DatePickerDialog(getActivity(), null, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
        final DatePicker picker = d.getDatePicker();
        d.setCancelable(true);
        d.setCanceledOnTouchOutside(true);
        picker.setSpinnersShown(false);
        picker.setCalendarViewShown(true);
        d.setButton(Dialog.BUTTON_NEUTRAL, "Cancel", (DialogInterface.OnClickListener) null);
        d.setButton(Dialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((CalendarDialogListener) getActivity()).onCalendarDialogDateSelected(new GregorianCalendar(picker.getYear(), picker.getMonth(), picker.getDayOfMonth()));
            }
        });//Default
        d.setButton(Dialog.BUTTON_NEGATIVE, "Today", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((CalendarDialogListener)getActivity()).onCalendarDialogDateSelected(new GregorianCalendar());
            }
        });
        return d;
    }


    public interface CalendarDialogListener {
        void onCalendarDialogDateSelected(GregorianCalendar date);
    }
}
