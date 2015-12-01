package senior_project.foodscanner.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;

import android.os.Bundle;


/**
 * Generic message dialog.
 */
public class MessageDialogFragment extends DialogFragment {

    private CharSequence message;
    private CharSequence title;
    private int iconId;

    public static MessageDialogFragment newInstance(CharSequence message, CharSequence title, int iconId){
        MessageDialogFragment frag = new MessageDialogFragment();
        frag.message = message;
        frag.title = title;
        frag.iconId = iconId;
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder d = new AlertDialog.Builder(getActivity());
        d.setMessage(message);
        d.setTitle(title);
        d.setIcon(iconId);
        return d.create();
    }
}