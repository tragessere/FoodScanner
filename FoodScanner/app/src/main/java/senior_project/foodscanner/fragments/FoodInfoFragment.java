package senior_project.foodscanner.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;

import java.util.Map;

import senior_project.foodscanner.FoodItem;
import senior_project.foodscanner.R;

/**
 * Created by Tyler on 10/12/2015.
 *
 * Dialog that appears when food item is selected.
 */
public class FoodInfoFragment extends DialogFragment {

    FoodItem food;

    public static FoodInfoFragment newInstance(FoodItem newFood) {
        FoodInfoFragment frag = new FoodInfoFragment();
        frag.food = newFood;
        return frag;
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
                        //TODO: Add food item to meal
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog, do nothing
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}