package senior_project.foodscanner.ui.components.mealcalendar;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import senior_project.foodscanner.FoodItem;
import senior_project.foodscanner.Meal;
import senior_project.foodscanner.R;
import senior_project.foodscanner.Settings;

public class MealArrayAdapter extends ArrayAdapter<Meal> {
    private static final int layoutId = R.layout.list_layout_meal;
    private static final int textViewId = R.id.meal_text;
    private static final int textViewNutrId = R.id.textView_nutr;
    private static final int deleteButtonId = R.id.button_delete;
    private static final int warnButtonId = R.id.imageButton_warning;
    private MealArrayAdapterListener listener = null;

    public MealArrayAdapter(Context context) {
        super(context, layoutId);
        addAddItem();
    }

    public MealArrayAdapter(Context context, List<Meal> list) {
        super(context, layoutId, list);
        addAddItem();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Meal meal = getItem(position);

        // view inflation
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layoutId, parent, false);
        }

        // text
        final TextView text = (TextView) convertView.findViewById(textViewId);
        final TextView textNutr = (TextView) convertView.findViewById(textViewNutrId);
        if(meal != null) {
            text.setGravity(Gravity.CENTER_VERTICAL);
            text.setText(mealString(meal));
            textNutr.setText(nutrString(meal));
            textNutr.setVisibility(View.VISIBLE);
        } else {
            text.setGravity(Gravity.CENTER);
            text.setText("+");
            textNutr.setVisibility(View.GONE);
        }

        // button handling
        final ImageButton buttonDel = (ImageButton) convertView.findViewById(deleteButtonId);
        buttonDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(meal != null && listener != null) {
                    listener.onDelete(MealArrayAdapter.this, position);
                }
            }
        });
        final ImageButton buttonWarn = (ImageButton) convertView.findViewById(warnButtonId);
        buttonWarn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(meal != null && listener != null) {
                    listener.onWarning(MealArrayAdapter.this, position);
                }
            }
        });
        if(meal == null) {
            buttonDel.setVisibility(View.GONE);
            buttonWarn.setVisibility(View.GONE);
        } else {
            buttonDel.setVisibility(View.VISIBLE);
            if(meal.isChanged()){
                buttonWarn.setVisibility(View.VISIBLE);
            }
            else{
                buttonWarn.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    private String nutrString(Meal meal) {
        double cal = 0;
        Map<String, Double> nutr = meal.getNutrition();
        if(nutr != null && nutr.containsKey(FoodItem.KEY_CAL)) {
            cal = nutr.get(FoodItem.KEY_CAL);
        }
        return (int) cal + " Cal";
    }

    private String mealString(Meal meal) {
        return Settings.getInstance().formatTime(meal) + " - " + meal.getType().getName();
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

    public void setOnDeleteListener(MealArrayAdapterListener l) {
        listener = l;
    }

    private void addAddItem() {
        super.add(null);
    }

    private void removeAddItem() {
        super.remove(null);
    }

    public interface MealArrayAdapterListener {
        void onDelete(MealArrayAdapter adapter, int position);

        void onWarning(MealArrayAdapter adapter, int position);
    }

}
