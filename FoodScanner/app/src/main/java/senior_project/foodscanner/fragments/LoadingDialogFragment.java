package senior_project.foodscanner.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ProgressBar;

/**
 * Created by Penguinator on 12/8/2015.
 */
public class LoadingDialogFragment extends AlertDialogFragment {
    @Override
    protected AlertDialog.Builder onBuildDialog(Bundle savedInstanceState){
        AlertDialog.Builder d = super.onBuildDialog(savedInstanceState);
        d.setView(new ProgressBar(getActivity()));
        return d;
    }
}
