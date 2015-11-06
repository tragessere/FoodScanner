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
import android.widget.TextView;

import senior_project.foodscanner.FoodItem;
import senior_project.foodscanner.R;

/**
 * Created by Tyler on 11/3/2015.
 *
 * Dialog that appears if only volume needs to be calculated.
 * This is temporary; only for the Sprint 2 demo.
 */
public class FoodVolumeFragment extends DialogFragment {

    public FoodItem food;
    private View view;

    public static FoodVolumeFragment newInstance(FoodItem food) {
        FoodVolumeFragment frag = new FoodVolumeFragment();
        frag.food = food;
        return frag;
    }

    public interface FoodVolumeDialogListener {
        public void onVolumeDialogPositiveClick(DialogFragment dialog);
        public void onVolumeDialogNeutralClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver Volume events
    FoodVolumeDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (FoodVolumeDialogListener) activity;
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
        view = inflater.inflate(R.layout.food_volume_dialog, null);

        builder.setView(view)
                // Add Volume buttons
                .setPositiveButton("Scan Food", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onVolumeDialogPositiveClick(FoodVolumeFragment.this);
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onVolumeDialogNeutralClick(FoodVolumeFragment.this);
                    }
                });

        TextView volumeTitle = (TextView) view.findViewById(R.id.volumeTitle);
        volumeTitle.setText(Html.fromHtml("<b>Volume Info</b>"));

        return builder.create();
    }


}
