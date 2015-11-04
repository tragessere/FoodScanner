package senior_project.foodscanner.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import senior_project.foodscanner.FoodItem;
import senior_project.foodscanner.R;

/**
 * Created by Tyler on 11/3/2015.
 *
 * Dialog that appears if mass needs to be calculated.
 */
public class FoodActionFragment extends DialogFragment {

    public FoodItem food;
    private View view;

    public static FoodActionFragment newInstance(FoodItem food) {
        FoodActionFragment frag = new FoodActionFragment();
        frag.food = food;
        return frag;
    }

    public interface FoodActionDialogListener {
        public void onActionDialogPositiveClick(DialogFragment dialog);
        public void onActionDialogNeutralClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    FoodActionDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (FoodActionDialogListener) activity;
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
        view = inflater.inflate(R.layout.food_action_dialog, null);

        builder.setView(view)
                //.setTitle(food.toString())
                // Add action buttons
                .setPositiveButton("Scan Food", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onActionDialogPositiveClick(FoodActionFragment.this);
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onActionDialogNeutralClick(FoodActionFragment.this);
                    }
                });

        TextView densityTitle = (TextView) view.findViewById(R.id.densityTitle);
        densityTitle.setText(Html.fromHtml("<b>Density Info</b>"));

        // If density not yet found, display default text for density
        TextView densityValue = (TextView) view.findViewById(R.id.densityValue);
        TextView densityEntry = (TextView) view.findViewById(R.id.densityEntry);
        if (food.getDensity() == 0.0) {
            densityValue.setText(Html.fromHtml("<b>Value:</b> Please search for entry."));
            densityEntry.setText(Html.fromHtml("<b>Entry:</b> Please search for entry."));
        } else {
            densityValue.setText(Html.fromHtml("<b>Value:</b> " + food.getDensity()));
            densityEntry.setText(Html.fromHtml("<b>Entry:</b> " + food.getDensityName()));
        }

        return builder.create();
    }

}
