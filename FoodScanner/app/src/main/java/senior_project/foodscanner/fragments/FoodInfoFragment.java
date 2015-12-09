package senior_project.foodscanner.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;

import java.text.DecimalFormat;
import java.text.NumberFormat;
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
    private boolean isSaved;  //if true, dialog is being viewed after food item has been added.

    public static FoodInfoFragment newInstance(FoodItem food, boolean isSaved) {
        FoodInfoFragment frag = new FoodInfoFragment();
        frag.food = food;
        frag.isSaved = isSaved;
        return frag;
    }

    public interface FoodInfoDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
        public void onDialogNeutralClick(DialogFragment dialog);
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

        if (isSaved && food.getNumServings() != 0.0) {
            info.append("<br><b>Servings:</b> ");
            NumberFormat formatter = new DecimalFormat("#0.00");
            info.append(formatter.format(food.getNumServings()));
        }

        info.append("<br><b>Serving Size:</b>  ");
        if (food.usesMass() || food.usesVolume()) {
            info.append(food.getServingSize());
        } else {
            // Round for manual serving sizes, e.g. "1 taco" instead of "1.0 taco"
            info.append(Math.round(food.getServingSize()));
        }
        info.append(" ");
        info.append(food.getServingSizeUnit());

        NumberFormat formatter = new DecimalFormat("#0.0");

        if (!isSaved || ((food.usesMass() || food.usesVolume()) && food.getVolume() == 0.0)) {
            // Display uncalculated nutrition info
            for (Map.Entry<String, Double> field : food.getSet()) {
                info.append("<br><b>");
                info.append(field.getKey());
                info.append(":</b>  ");
                info.append(formatter.format(field.getValue()));
                if (field.getKey().equals("Sodium")) {  //if additional fields added, add here if in mg
                    info.append(" mg");
                } else if (!field.getKey().equals("Calories")) {
                    info.append(" g");
                }
            }
        } else {
            // Display calculated nutrition info
            Map<String, Double> calculatedNutr = food.getNutrition();
            for (Map.Entry<String, Double> field : calculatedNutr.entrySet()) {
                info.append("<br><b>");
                info.append(field.getKey());
                info.append(":</b>  ");
                info.append(formatter.format(field.getValue()));
                if (field.getKey().equals("Sodium")) {  //if additional fields added, add here if in mg
                    info.append(" mg");
                } else if (!field.getKey().equals("Calories")) {
                    info.append(" g");
                }
            }
        }

        NumberFormat formatterTwo = new DecimalFormat("#0.00");

        // Display mass & volume
        if (food.usesVolume() && !food.usesMass() && food.getVolume() != 0.0) {
            info.append("<br><b>Volume:</b> ");
            info.append(formatterTwo.format(food.getVolume()) + " " + food.getActualServingSizeUnit());
        } else if ((food.usesMass()) && food.getMass() != 0.0) {
            info.append("<br><b>Volume:</b> ");
            info.append(formatterTwo.format(food.getVolume()) + " ml");
            info.append("<br><b>Mass:</b> ");
            info.append(formatterTwo.format(food.getMass()) + " " + food.getActualServingSizeUnit());
        } else if ((food.usesMass() || food.usesVolume()) && isSaved) {
            // Food has not yet been scanned, add message
            info.append("<br><b><i>This needs to be scanned</b></i>");
        }

        Spanned nutritionInfo = Html.fromHtml(info.toString());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (!isSaved) {
            // Dialog displayed before food item is added
            builder.setTitle(food.getName())
                    .setMessage(nutritionInfo)
                    .setPositiveButton("Add to Meal", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mListener.onDialogPositiveClick(FoodInfoFragment.this);
                        }
                    })
                    .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mListener.onDialogNeutralClick(FoodInfoFragment.this);
                        }
                    });
        } else {
            // Dialog displayed after food item is added
            // Two versions: (1) food uses mass or volume, (2) food has manual servings

            if (food.usesMass() || food.usesVolume()) {
                builder.setTitle(food.getName())
                        .setMessage(nutritionInfo)
                        .setPositiveButton("Scan", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mListener.onDialogPositiveClick(FoodInfoFragment.this);
                            }
                        })
                        .setNegativeButton("Replace", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mListener.onDialogNegativeClick(FoodInfoFragment.this);
                            }
                        })
                        .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mListener.onDialogNeutralClick(FoodInfoFragment.this);
                            }
                        });
            }
            else {
                builder.setTitle(food.getName())
                        .setMessage(nutritionInfo)
                        .setPositiveButton("Servings", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mListener.onDialogPositiveClick(FoodInfoFragment.this);
                            }
                        })
                        .setNegativeButton("Replace", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mListener.onDialogNegativeClick(FoodInfoFragment.this);
                            }
                        })
                        .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mListener.onDialogNeutralClick(FoodInfoFragment.this);
                            }
                        });
            }
        }

        // Create the AlertDialog object and return it
        return builder.create();
    }
}