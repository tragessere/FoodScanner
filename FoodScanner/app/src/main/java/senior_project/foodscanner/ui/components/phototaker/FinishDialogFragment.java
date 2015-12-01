package senior_project.foodscanner.ui.components.phototaker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class FinishDialogFragment extends DialogFragment {

    public interface FinishDialogListener {
        void onFinishDialog_Review();
        void onFinishDialog_Finish();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("All Photos Taken").setMessage("Review Photos or Finish?")
                    .setPositiveButton("Finish", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ((FinishDialogListener)getActivity()).onFinishDialog_Finish();
                        }
                    })
                    .setNeutralButton("Review Photos", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ((FinishDialogListener)getActivity()).onFinishDialog_Review();
                        }
                    });

        return builder.create();
    }
}