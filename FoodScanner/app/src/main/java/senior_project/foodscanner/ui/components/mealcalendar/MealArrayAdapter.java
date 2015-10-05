package senior_project.foodscanner.ui.components.mealcalendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import senior_project.foodscanner.Meal;
import senior_project.foodscanner.R;
import senior_project.foodscanner.Settings;

public class MealArrayAdapter extends ArrayAdapter<Meal> {
    private static final int layoutId = R.layout.list_layout_meal;
    private static final int textViewId = R.id.meal_text;
    private static final int deleteButtonId = R.id.button_delete;
    private OnDeleteListener odl = null;

    public MealArrayAdapter(Context context) {
        super(context, layoutId);
        addAddItem();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Meal meal = getItem(position);

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layoutId, parent, false);
        }

        TextView text = (TextView) convertView.findViewById(textViewId);
        if(meal != null) {
            text.setText(mealString(meal));
            //convertView.setOnTouchListener(this);
            //convertView.setOnGenericMotionListener(this);
        } else {
            text.setText("     (Add Meal)");
        }
            Button button = (Button) convertView.findViewById(deleteButtonId);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(meal != null && odl != null) {
                        odl.onDelete(MealArrayAdapter.this, position);
                    }
                }
            });

        return convertView;
    }

    private String mealString(Meal meal) {
        return new Settings().formatTime(meal) + " - " + meal.getType().getName();//TODO reference global Settings object
    }

    @Override
    public void add(Meal meal) {
        removeAddItem();
        super.add(meal);
        addAddItem();
    }

    @Override
    public void clear() {
        super.clear();
        addAddItem();
    }

    public void setOnDeleteListener(OnDeleteListener l){
        odl = l;
    }

    private void addAddItem() {
        super.add(null);
    }

    private void removeAddItem() {
        super.remove(null);
    }

    public interface OnDeleteListener{
        void onDelete(MealArrayAdapter adapter, int position);
    }

}
