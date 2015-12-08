package senior_project.foodscanner.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * General Alert Dialog Fragment
 */
public class AlertDialogFragment extends DialogFragment {
    private int iconId;
    private CharSequence title;
    private CharSequence message;
    private CharSequence positiveText;
    private CharSequence negativeText;
    private CharSequence neutralText;
    private AlertDialogFragment.OnClickListener posListener;
    private AlertDialogFragment.OnClickListener negListener;
    private AlertDialogFragment.OnClickListener neuListener;
    private DialogInterface.OnCancelListener cancelListener;

    public void setIcon(int id){
        iconId = id;
    }

    public void setTitle(CharSequence text){
        title = text;
    }

    public void setMessage(CharSequence text){
        message = text;
    }

    public void setPositiveButton(CharSequence text, AlertDialogFragment.OnClickListener listener){
        positiveText = text;
        posListener = listener;
    }

    public void setNegativeButton(CharSequence text, AlertDialogFragment.OnClickListener listener){
        negativeText = text;
        negListener = listener;
    }

    public void setNeutralButton(CharSequence text, AlertDialogFragment.OnClickListener listener){
        neutralText = text;
        neuListener = listener;
    }

    @Override
    public void setCancelable(boolean isCancelable){
        super.setCancelable(isCancelable);
    }

    public void setOnCancelListener(DialogInterface.OnCancelListener cancelListener){
        this.cancelListener = cancelListener;
    }

    @Override
    public void onCancel(DialogInterface dialog){
        if(cancelListener != null){
            cancelListener.onCancel(dialog);
        }
    }

    /**
     * Subclasses can override this method and call super.onBuildDialog() to add more customization options.
     * @param savedInstanceState
     * @return
     */
    protected AlertDialog.Builder onBuildDialog(Bundle savedInstanceState){
        AlertDialog.Builder d = new AlertDialog.Builder(getActivity());
        d.setIcon(iconId);
        if(title!=null){
            d.setTitle(title);
        }
        if(message!=null){
            d.setMessage(message);
        }
        if(positiveText != null) {
            d.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(posListener != null) {
                        posListener.onClick(AlertDialogFragment.this, which);
                    }
                }
            });
        }
        if(negativeText != null) {
            d.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(negListener != null) {
                        negListener.onClick(AlertDialogFragment.this, which);
                    }
                }
            });
        }
        if(neutralText != null) {
            d.setNeutralButton(neutralText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(neuListener != null) {
                        neuListener.onClick(AlertDialogFragment.this, which);
                    }
                }
            });
        }

        return d;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return onBuildDialog(savedInstanceState).create();
    }

    public interface OnClickListener{
        /**
         *
         * @param which - AlertDialog.BUTTON_NEGATIVE, BUTTON_POSITIVE, BUTTON_NEUTRAL
         */
        public void onClick(AlertDialogFragment dialog, int which);
    }
}
