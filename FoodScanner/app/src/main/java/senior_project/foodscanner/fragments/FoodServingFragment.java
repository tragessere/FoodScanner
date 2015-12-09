package senior_project.foodscanner.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import senior_project.foodscanner.FoodItem;
import senior_project.foodscanner.R;

/**
 * Created by Tyler on 11/3/2015.
 *
 * Dialog that appears if neither volume nor mass needs to be calculated
 */
public class FoodServingFragment extends DialogFragment {

    public FoodItem food;
    private View view;
    private boolean isSaved;  // if the food has been saved yet

    public static FoodServingFragment newInstance(FoodItem food, boolean isSaved) {
        FoodServingFragment frag = new FoodServingFragment();
        frag.food = food;
        frag.isSaved = isSaved;
        return frag;
    }

    public interface FoodServingDialogListener {
        public void onServingDialogNeutralClick(DialogFragment dialog);
        public void onServingDialogPositiveClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver Serving events
    FoodServingDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (FoodServingDialogListener) activity;
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
        view = inflater.inflate(R.layout.food_serving_dialog, null);

        // Set up servings edit text
        final TextView servingEntry = (TextView) view.findViewById(R.id.servingEntry);
        if (food.getNumServings() == 0.0) {
            servingEntry.setHint("N/A");
        } else {
            servingEntry.setText("" + food.getNumServings());
        }

        // Set up buttons
        builder.setView(view)
                // Add Serving buttons
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Update FoodItem's serving count
                        Double numServings;
                        try {
                            numServings = Double.parseDouble(servingEntry.getText().toString());
                        } catch (Exception e) {
                            // This happens if the user enters a blank string
                            Toast butteredToast = Toast.makeText(getActivity(),
                                    "Error: Invalid servings.", Toast.LENGTH_LONG);
                            butteredToast.show();
                            return;
                        }

                        food.setNumServings(numServings);
                        mListener.onServingDialogPositiveClick(FoodServingFragment.this);
//                        Toast butteredToast = Toast.makeText(getActivity(),
//                                "Saved servings.", Toast.LENGTH_SHORT);
//                        butteredToast.show();
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onServingDialogNeutralClick(FoodServingFragment.this);
                    }
                });

        // Set up title
        TextView servingTitle = (TextView) view.findViewById(R.id.servingTitle);
        if (isSaved) {
            servingTitle.setText(Html.fromHtml("<b>Edit Servings</b>"));
        } else {
            servingTitle.setText(Html.fromHtml("<b>Enter Servings</b>"));
        }

        // Set up subtitle
        TextView subtitle = (TextView) view.findViewById(R.id.servingDescription);
        subtitle.setText(Html.fromHtml("<i>Number of Servings:</i>"));

        // Set up serving size reminder
        TextView servingSize = (TextView) view.findViewById(R.id.food_serving);
        servingSize.setText(Html.fromHtml("<b>Size:</b> " + Math.round(food.getServingSize())
                + " " + food.getServingSizeUnit()));

        // Open keyboard automatically
        EditText editServings = (EditText) view.findViewById(R.id.servingEntry);
        editServings.requestFocus();
        Dialog retDialog = builder.create();
        retDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return retDialog;
    }

}
