package senior_project.foodscanner.ui.components.mealcalendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;

import java.util.GregorianCalendar;

import senior_project.foodscanner.Meal;
import senior_project.foodscanner.R;

/**
 * View for MealCalendar Meal node.
 */
public class MealNodeViewHolder extends CalendarNodeViewHolder<Meal> {
    private TextView tvValue;
    private static final String[] am_pm = {"am", "pm"};

    public MealNodeViewHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(TreeNode treeNode, Meal meal) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.nodeviewholder_meal, null, false);

        tvValue = (TextView) view.findViewById(R.id.node_value);
        tvValue.setText(indentText(treeNode) + mealDisplay(meal));

        return view;
    }

    /**
     * Converts meal to a user friendly text.
     *
     * @param meal - meal to interpret
     * @return String in format: hh:mm am/pm type. EX: 12:35pm Lunch
     */
    private String mealDisplay(Meal meal){
        GregorianCalendar date = meal.getDate();
        int minute = date.get(GregorianCalendar.MINUTE);
        String time = date.get(GregorianCalendar.HOUR) + ":";
        if(minute < 10){
            time += '0';
        }
        time += minute + am_pm[date.get(GregorianCalendar.AM_PM)];

        return time+ " " + meal.getType().getName();
    }
}
