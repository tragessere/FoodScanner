package senior_project.foodscanner.ui.components.mealcalendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;

import senior_project.foodscanner.R;

/**
 * View for MealCalendar Date node.
 */
public class DateNodeViewHolder extends CalendarNodeViewHolder {
    private TextView tvValue;

    public DateNodeViewHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(TreeNode treeNode, Object value) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.nodeviewholder_date, null, false);

        tvValue = (TextView) view.findViewById(R.id.node_value);

        tvValue.setText(indentText(treeNode) + value.toString());

        return view;
    }

}
