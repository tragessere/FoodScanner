package senior_project.foodscanner.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.MediaActionSound;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;

import senior_project.foodscanner.ImageDirectoryManager;
import senior_project.foodscanner.R;
import senior_project.foodscanner.fragments.ErrorDialogFragment;
import senior_project.foodscanner.ui.components.camera.CameraView;
import senior_project.foodscanner.ui.components.tutorial.TutorialCard;
import senior_project.foodscanner.ui.components.tutorial.TutorialSequence;

/**
 * Activity to take a single picture.
 * Must specify file name of the picture by putting a String extra in the Intent named EXTRA_FILENAME.
 * To get the resulting image file, get the extra named RESULT_IMAGE_FILE.
 */
public class CameraActivity extends AppCompatActivity implements ErrorDialogFragment.ErrorDialogListener, View.OnClickListener, Camera.ShutterCallback, Camera.PictureCallback, Camera.AutoFocusCallback {

    // Public fields
    public static final String EXTRA_FILENAME = "filename";
    public static final String EXTRA_IMAGE_NAME = "image_name"; //optional String
    public static final String EXTRA_IMAGE_DESCRIPTION = "image_desc"; //optional String
    public static final String EXTRA_HELP_ACTOR = "help_actor"; //optional CameraActivity_HelpActor
    public static final String RESULT_IMAGE_FILE = "image_file";

    public static final int TUTORIAL_SCAN = 0;

    // Image file format
    public static final String IMAGE_FORMAT_EXTENSION = ".png";
    public static final Bitmap.CompressFormat IMAGE_FORMAT_COMPRESSION = Bitmap.CompressFormat.PNG;

    // Activity params
    private String filename;
    private CameraActivity_HelpActor help_actor;

    // Private fields
    private static final int REQUEST_PERMISSION_CAMERA = 0;
    private Camera camera;
    private int cameraId;
    private FrameLayout cameraContainer;
    private CameraView cameraView;
    private TextView textView_Image_Title;
    private TextView textView_Image_Desc;
    private ImageButton button_takePicture;
    private ImageButton button_help;
    private int shutterOrientation;
    private MediaActionSound sound;

    private TutorialSequence sequence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= 16) {
            sound = new MediaActionSound();
            sound.load(MediaActionSound.FOCUS_COMPLETE);
        }

        if(getIntent().hasExtra(EXTRA_FILENAME)) {
            filename = getIntent().getStringExtra(EXTRA_FILENAME);
        } else {
            ErrorDialogFragment.showErrorDialog(this, "Intent extra \"filename\" not found.");
        }

        setContentView(R.layout.activity_camera);
        textView_Image_Title = (TextView)findViewById(R.id.textView_image_title);
        textView_Image_Desc = (TextView)findViewById(R.id.textView_image_count);
        cameraContainer = ((FrameLayout) findViewById(R.id.container_camera));
        button_help = ((ImageButton) findViewById(R.id.imageButton_help));
        button_takePicture = (ImageButton)cameraContainer.findViewById(R.id.imageButton_takePicture);
        button_takePicture.setOnClickListener(this);
        button_help.setOnClickListener(this);
        cameraContainer.setOnClickListener(this);

        sequence = new TutorialSequence(this);

        if(getIntent().hasExtra(EXTRA_IMAGE_NAME)) {
            textView_Image_Title.setText(getIntent().getCharSequenceExtra(EXTRA_IMAGE_NAME));
        }
        else {
            textView_Image_Title.setVisibility(View.GONE);
        }

        if(getIntent().hasExtra(EXTRA_IMAGE_DESCRIPTION)) {
            textView_Image_Desc.setText(getIntent().getCharSequenceExtra(EXTRA_IMAGE_DESCRIPTION));
        }
        else {
            textView_Image_Desc.setVisibility(View.GONE);
        }

        if(getIntent().hasExtra(EXTRA_HELP_ACTOR)) {
//            help_actor = (CameraActivity_HelpActor)getIntent().getSerializableExtra(EXTRA_HELP_ACTOR);
            int tutorialType = getIntent().getIntExtra(EXTRA_HELP_ACTOR, -1);

            if(tutorialType == TUTORIAL_SCAN) {
                sequence.addCard(new TutorialCard(cameraContainer, getString(R.string.tutorial_camera_title), getString(R.string.tutorial_camera_overview)).useHighlight(false));
                sequence.addCard(new TutorialCard(cameraContainer, getString(R.string.tutorial_camera_title), getString(R.string.tutorial_camera_card)).useHighlight(false));
                sequence.addCard(new TutorialCard(cameraContainer, getString(R.string.tutorial_camera_title), getString(R.string.tutorial_camera_closeup)).useHighlight(false));
            }

        }
        else {
            button_help.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(camera == null) {
            if(cameraExists()) {
                int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
                if(permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
                } else {
                    loadCamera();
                }
            } else {
                ErrorDialogFragment.showErrorDialog(this, "Device does not have a camera.");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    @Override
    public void onErrorDialogClose() {
        finish();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.imageButton_takePicture:
                takePicture();
                break;
            case R.id.imageButton_help:
//                help_actor.doHelpAction(this);
                sequence.Start();
                break;
            case R.id.container_camera:
                startFocus();
                break;
            default:
                ErrorDialogFragment.showErrorDialog(this, "Unhandled click action!");
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if(sequence.isActive())
            sequence.previousPosition();
        else
            super.onBackPressed();
    }

    private void takePicture() {
        if(camera != null) {
            button_takePicture.setEnabled(false);
            camera.takePicture(this, null, this);
        }
    }

    private void startFocus(){
        if(camera != null) {
            camera.autoFocus(this);
        }
    }

    @Override
    public void onShutter() {
        shutterOrientation = getScreenOrientation();
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if(Build.VERSION.SDK_INT >= 16) {
            sound.play(MediaActionSound.FOCUS_COMPLETE);
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        try {
            if(data != null) {

                // create bitmap from data bytes
                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                bmp = orientBitmap(bmp);

                // save to image directory
                File imgF = new File(ImageDirectoryManager.getImageDirectory(this), filename + IMAGE_FORMAT_EXTENSION);
                if(imgF.exists()) {
                    if(!imgF.delete()) {
                        ErrorDialogFragment.showErrorDialog(this, "Image directory saving: Could not delete file \"" + imgF.getName() + "\"");
                        return;
                    }
                    if(!imgF.createNewFile()) {
                        ErrorDialogFragment.showErrorDialog(this, "Image directory saving: Could not create file \"" + imgF.getName() + "\"");
                        return;
                    }
                }
                FileOutputStream fos = new FileOutputStream(imgF.getPath());
                if(!bmp.compress(IMAGE_FORMAT_COMPRESSION, 100, fos)) {
                    ErrorDialogFragment.showErrorDialog(this, "Failed to compress bitmap.");
                    return;
                }
                fos.close();

                Intent intent = new Intent();
                intent.putExtra("image_file", imgF);
                setResult(RESULT_OK, intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);// fade animation
            } else {
                ErrorDialogFragment.showErrorDialog(this, "Unable to get picture data.");
            }
        } catch(Exception e) {
            Log.e("OnPictureTaken", "Exception", e);
            ErrorDialogFragment.showErrorDialog(this, "Exception:" + e.getMessage());
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_PERMISSION_CAMERA) {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialogFragment.showErrorDialog(this, "This application does not have permission to access the camera.");
            } else {
                loadCamera();
            }
        }
    }

    private void loadCamera() {
        String reason = "";
        camera = null;
        try {
            // choose first back-facing camera
            int n = Camera.getNumberOfCameras();
            for(int i = 0; i < n; i++) {
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(i, info);
                if(info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    cameraId = i;
                    break;
                }
            }

            // open camera
            if(cameraId != -1) {
                camera = Camera.open(cameraId);
                if(camera == null) {
                    reason = "Could not access camera.";
                }
            } else {
                reason = "Device does not have a back-facing camera.";
            }
        } catch(Exception e) {
            Log.e("OnPictureTaken", "Exception", e);
            reason = e.toString() + ":\n" + e.getMessage();
        }

        if(camera != null) {
            setCameraOrientation();
            setPictureSizeToMin();
            setFocusParams();

            // create camera view with optimal size
            if(cameraView != null) {
                cameraContainer.removeView(cameraView);
            }
            cameraView = new CameraView(this, camera);
            cameraContainer.addView(cameraView, 0, getOptimalLayoutParams());
        } else {
            ErrorDialogFragment.showErrorDialog(this, reason);
        }
    }

    private FrameLayout.LayoutParams getOptimalLayoutParams() {
        Camera.Size pSize = camera.getParameters().getPreviewSize();
        int w = pSize.width;
        int h = pSize.height;

        // flip width and height depending on screen orientation (determined by shape rather than rotation)
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        if(metrics.widthPixels < metrics.heightPixels && w > h) {
            int temp = w;
            w = h;
            h = temp;
        }

        // fit to container
        double ratio = (double) (w) / h;
        if(metrics.widthPixels < metrics.heightPixels) {
            w = metrics.widthPixels;//cameraContainer.getWidth();
            h = (int) (w / ratio);
        } else {
            h = metrics.heightPixels;//cameraContainer.getHeight();
            w = (int) (h * ratio);
        }

        return new FrameLayout.LayoutParams(w, h, Gravity.CENTER);//new FrameLayout.LayoutParams(w, h, Gravity.CENTER);
    }

    private void setPictureSizeToMin() {
        Camera.Parameters param = camera.getParameters();
        Camera.Size size = param.getPictureSize();
        for(Camera.Size s : param.getSupportedPictureSizes()) {
            if(s.width * s.height < size.width * size.height) {
                size = s;
            }
        }
        param.setPictureSize(size.width, size.height);
        camera.setParameters(param);
    }

    private void setFocusParams(){
        Camera.Parameters param = camera.getParameters();
        param.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        camera.setParameters(param);
    }


    /**
     * Rotates bitmap to align with screen orientation.
     *
     * @param bmp - bitmap
     * @return
     */
    private Bitmap orientBitmap(Bitmap bmp) {

        // orientation of bitmap
        int orientBMP = 0;//portrait
        if(bmp.getWidth() > bmp.getHeight()) {
            orientBMP = 90;//landscape
        }

        int rot = orientBMP - shutterOrientation;
        if(rot != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rot);
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        }

        return bmp;
    }


    /**
     * Gets screen rotation in degrees from it's default rotation.
     */
    private int getScreenRotation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch(rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        return degrees;
    }

    /**
     * Gets the natural(default) screen orientation of the device when it's rotation is zero.
     *
     * @return 0 for portrait, 90 for landscape.
     */
    private int getNaturalScreenOrientation() {

        // width and height depending on screen orientation
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int rot = getScreenRotation(); // degrees screen is rotated from it's natural orientation

        int nOrient = 0;// start at portrait

        // wider than taller indicates landscape
        if(metrics.widthPixels > metrics.heightPixels) {
            nOrient = 90;
        }

        // but if screen is rotated 90 degrees in either direction, then natural orientation is the opposite
        if(rot % 180 != 0) {
            nOrient = (nOrient + 90) % 180;
        }

        return nOrient;
    }

    /**
     * Gets the current screen orientation.
     *
     * @return 0 for portrait, 90 for landscape, 180 for flipped portrait, 270 for flipped landscape
     */
    private int getScreenOrientation() {
        return (getNaturalScreenOrientation() + getScreenRotation()) % 360;
    }

    private void releaseCamera() {
        if(camera != null) {
            camera.release();
            camera = null;
        }
    }

    /**
     * Check if this device has a camera
     */
    private boolean cameraExists() {
        if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adjust camera display and picture orientations to always point up.
     */
    private void setCameraOrientation() {
        int deviceO = getScreenRotation();

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int camO = info.orientation;

        Camera.Parameters params = camera.getParameters();
        int rCamDisplay;
        int rPic;
        if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rCamDisplay = (camO + deviceO) % 360;
            rCamDisplay = (360 - rCamDisplay) % 360;  // compensate the mirror
            rPic = (camO - deviceO + 360) % 360;
        } else {  // back-facing
            rCamDisplay = (camO - deviceO + 360) % 360;
            rPic = (camO + deviceO) % 360;
        }

        camera.setDisplayOrientation(rCamDisplay);

        params.setRotation(rPic);
        camera.setParameters(params);
    }

    public interface CameraActivity_HelpActor extends Serializable{
        void doHelpAction(Activity activity);
    }

}