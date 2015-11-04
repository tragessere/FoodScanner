package senior_project.foodscanner.ui.components.mealcalendar;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;

import java.util.Date;
import java.util.GregorianCalendar;

import senior_project.foodscanner.R;

public class TextDialog {

    public static void show(Context context, CharSequence s){
        final Dialog d = new Dialog(context);
        d.setContentView(R.layout.dialog_text);
        TextView tv = (TextView)d.findViewById(R.id.textView);

        tv.setText(s);

        d.findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.hide();
            }
        });
        d.show();
    }

}
