package senior_project.foodscanner.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import senior_project.foodscanner.FoodItem;
import senior_project.foodscanner.R;

/**
 * Created by Tyler on 11/3/2015.
 *
 * Dialog that appears if mass needs to be calculated.
 */
public class FoodDensityFragment extends DialogFragment {

    public FoodItem food;
    private View view;
    private Map<String, Double> matches;
    private List<String> stringList;

    public static FoodDensityFragment newInstance(FoodItem food, Map<String, Double> matches) {
        FoodDensityFragment frag = new FoodDensityFragment();
        frag.food = food;
        frag.matches = matches;
        return frag;
    }

    public interface FoodDensityDialogListener {
        public void onDensityDialogPositiveClick(DialogFragment dialog, String name, Double value);
        public void onDensityDialogNeutralClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver Density events
    FoodDensityDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (FoodDensityDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        view = inflater.inflate(R.layout.food_density_dialog, null);

        builder.setView(view)
                // Add Density buttons
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDensityDialogNeutralClick(FoodDensityFragment.this);
                    }
                });


        // Set up listview of density results
        stringList = new ArrayList<>(matches.keySet());
        ListView lv = (ListView) view.findViewById(R.id.listView_densities);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getActivity().getApplicationContext(),
                R.layout.list_layout,
                R.id.foodListText,
                stringList);
        lv.setAdapter(arrayAdapter);

        // Set up happens when you click a list item
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Create FoodItem with the selected density
                String name = stringList.get(position);
                mListener.onDensityDialogPositiveClick(FoodDensityFragment.this, name, matches.get(name));
            }
        });

        // Set up name of food item
        TextView foodName = (TextView) view.findViewById(R.id.food_name);
        String foodString = "<b>" + food.getName() + "</b>";
        foodName.setText(Html.fromHtml(foodString));

        return builder.create();
    }


}
