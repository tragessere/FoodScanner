package senior_project.foodscanner.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;

import java.util.Map;

import senior_project.foodscanner.FoodItem;
import senior_project.foodscanner.Meal;
import senior_project.foodscanner.R;
import senior_project.foodscanner.activities.FoodItemActivity;

/**
 * Created by Tyler on 10/12/2015.
 *
 * Dialog that appears when food item is selected.
 */
public class FoodInfoFragment extends DialogFragment {

    public FoodItem food;

    public static FoodInfoFragment newInstance(FoodItem newFood, Meal newMeal) {
        FoodInfoFragment frag = new FoodInfoFragment();
        frag.food = newFood;
        return frag;
    }

    public interface FoodInfoDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    FoodInfoDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (FoodInfoDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        StringBuilder info = new StringBuilder();

        info.append("<b>Brand:</b>  ");
        info.append(food.getBrand());
        info.append("<br><b>Serving Size:</b>  ");
        info.append(food.getServingSize());
        info.append(" ");
        info.append(food.getServingSizeUnit());

        for (Map.Entry<String, Double> field : food.getSet()) {
            info.append("<br><b>");
            info.append(field.getKey());
            info.append(":</b>  ");
            info.append(field.getValue());
            if (field.getKey() == "Sodium") {  //if additional fields added, add here if in mg
                info.append(" mg");
            } else {
                info.append(" g");
            }
        }

        Spanned nutritionInfo = Html.fromHtml(info.toString());

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(food.getName())
                .setMessage(nutritionInfo)
                .setPositiveButton("Add to Meal", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(FoodInfoFragment.this);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick(FoodInfoFragment.this);
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}