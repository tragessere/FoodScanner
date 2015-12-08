package senior_project.foodscanner.ui.components.fooditem;

import android.content.Context;
import android.util.TypedValue;
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
import senior_project.foodscanner.R;
import senior_project.foodscanner.Settings;


/**
 * Contains a list of food items plus a null food item to represent the add button.
 * As a result, iEmpty() will never return true and getCount() will always be > 0.
 *
 * This was copied from Daniel's code and reused by Tyler.
 */
public class FoodArrayAdapter extends ArrayAdapter<FoodItem> {
    private static final int layoutId = R.layout.list_layout_food_new;
    private static final int textViewId = R.id.food_text;
    private static final int deleteButtonId = R.id.button_delete;
    private static final int warnButtonId = R.id.imageButton_warning;
    private FoodArrayAdapterListener listener = null;

    public FoodArrayAdapter(Context context) {
        super(context, layoutId);
        addAddItem();
    }

    public FoodArrayAdapter(Context context, List<FoodItem> list) {
        super(context, layoutId, list);
        addAddItem();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final FoodItem food = getItem(position);

        // view inflation
        if(convertView == null || ((TextView)convertView.findViewById(textViewId)).getText().equals("+")) { // create new view if convertView is null or is the "add button"
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layoutId, parent, false);
        }

        // text
        final TextView text = (TextView) convertView.findViewById(textViewId);
        if(food != null) {
            text.setText(foodString(food));
        } else {
            text.setGravity(Gravity.CENTER);
            text.setText("+");
            text.setTextColor(getContext().getResources().getColor(R.color.Accent));
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        }

        // button handling
        final ImageButton buttonDel = (ImageButton) convertView.findViewById(deleteButtonId);
        buttonDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(food != null && listener != null) {
                    listener.onDelete(FoodArrayAdapter.this, position);
                }
            }
        });
        final ImageButton buttonWarn = (ImageButton) convertView.findViewById(warnButtonId);
        buttonWarn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(food != null && listener != null) {
                    listener.onWarning(FoodArrayAdapter.this, position);
                }
            }
        });
        if(food == null) {
            buttonDel.setVisibility(View.GONE);
            buttonWarn.setVisibility(View.GONE);
        } else {
            if((food.usesMass() || food.usesVolume()) && food.getVolume() == 0.0){
                buttonWarn.setVisibility(View.VISIBLE);
            }
            else{
                buttonWarn.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    private String foodString(FoodItem food) {
        return food.getName() + " (" + food.getBrand() + ")";
    }

    @Override
    public void add(FoodItem food) {
        removeAddItem();
        super.add(food);
        addAddItem();
    }

    @Override
    public void clear() {
        super.clear();
        addAddItem();
    }

    @Override @Deprecated
    public boolean isEmpty(){
        return super.isEmpty();
    }

    public void setOnDeleteListener(FoodArrayAdapterListener l) {
        listener = l;
    }

    private void addAddItem() {
        super.add(null);
    }

    private void removeAddItem() {
        super.remove(null);
    }

    public interface FoodArrayAdapterListener {
        void onDelete(FoodArrayAdapter adapter, int position);

        void onWarning(FoodArrayAdapter adapter, int position);
    }

}

