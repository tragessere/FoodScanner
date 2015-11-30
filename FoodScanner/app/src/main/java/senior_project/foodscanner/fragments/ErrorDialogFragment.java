package senior_project.foodscanner.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Easy error dialog pop up.
 * Implement interface ErrorDialogListener to listen for when the dialog is closed.
 */
public class ErrorDialogFragment extends DialogFragment {

    public static void showErrorDialog(Activity act, String message){
        ErrorDialogFragment newFragment = ErrorDialogFragment.newInstance(message);
        newFragment.show(act.getFragmentManager(), "dialog");
    }

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
                Activity act = getActivity();
                if(act instanceof  ErrorDialogListener){
                    ((ErrorDialogListener) act).onErrorDialogClose();
                }
            }
        });
        return builder.create();

    }

    public interface ErrorDialogListener {
        void onErrorDialogClose();
    }

}
