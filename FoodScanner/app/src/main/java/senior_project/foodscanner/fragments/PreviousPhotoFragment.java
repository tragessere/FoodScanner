package senior_project.foodscanner.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import senior_project.foodscanner.FoodItem;
import senior_project.foodscanner.ImageDirectoryManager;
import senior_project.foodscanner.R;

/**
 * Created by Tyler on 12/1/2015.
 *
 * Dialog that appears if photos have already been taken
 */
public class PreviousPhotoFragment extends DialogFragment {

    private View view;

    public static PreviousPhotoFragment newInstance() {
        PreviousPhotoFragment frag = new PreviousPhotoFragment();
        return frag;
    }

    public interface PreviousPhotoDialogListener {
        public void onPhotoDialogNeutralClick(DialogFragment dialog);
        public void onPhotoDialogPositiveClick(DialogFragment dialog);
        public void onPhotoDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver Serving events
    PreviousPhotoDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (PreviousPhotoDialogListener) activity;
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
        view = inflater.inflate(R.layout.prev_image_dialog, null);

        File topImage = new File(ImageDirectoryManager.
                getImageDirectory(getActivity()).getPath() + "/Top.png");
        File sideImage = new File(ImageDirectoryManager.
                getImageDirectory(getActivity()).getPath() + "/Side.png");

        Bitmap topBitmap = BitmapFactory.decodeFile(topImage.getAbsolutePath());
        ImageView topImageView = (ImageView) view.findViewById(R.id.imageView_top);
        topImageView.setImageBitmap(topBitmap);

        Bitmap sideBitmap = BitmapFactory.decodeFile(sideImage.getAbsolutePath());
        ImageView sideImageView = (ImageView) view.findViewById(R.id.imageView_side);
        sideImageView.setImageBitmap(sideBitmap);

        builder.setView(view)
            // Add buttons
            .setPositiveButton("New", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    mListener.onPhotoDialogPositiveClick(PreviousPhotoFragment.this);
                }
            })
            .setNegativeButton("Keep", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mListener.onPhotoDialogNegativeClick(PreviousPhotoFragment.this);
                }
            })
            .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mListener.onPhotoDialogNeutralClick(PreviousPhotoFragment.this);
                }
            });

        return builder.create();
    }


}
