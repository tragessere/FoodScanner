package senior_project.foodscanner.ui.components.foodscanner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import senior_project.foodscanner.activities.FoodScannerActivity;

public class ErrorDialogFragment extends DialogFragment {

    public static ErrorDialogFragment newInstance(String message) {
        Bundle args = new Bundle();
        args.putString("message", message);
        ErrorDialogFragment fragment = new ErrorDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder = builder.setTitle("Error").setMessage(getArguments().getString("message")).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((FoodScannerActivity)getActivity()).errorDialogOk();
            }
        });
        return builder.create();

    }

}
