package senior_project.foodscanner.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileOutputStream;

import senior_project.foodscanner.ImageDirectoryManager;
import senior_project.foodscanner.R;
import senior_project.foodscanner.ui.components.ErrorDialogFragment;
import senior_project.foodscanner.ui.components.camera.CameraView;

/**
 * Activity to take a single picture.
 * Must specify file name of the picture by putting a String extra in the Intent named EXTRA_FILENAME.
 * To get the resulting image file, get the extra named RESULT_IMAGE_FILE.
 * <p/>
 * Possible Features:
 * -Toggle flash: On/Off/Auto
 * -Zoom fingers
 * -Focus and exposure
 */
//TODO adjust for orientation and aspect ratio
public class CameraActivity extends AppCompatActivity implements ErrorDialogFragment.ErrorDialogListener, View.OnClickListener, Camera.ShutterCallback, Camera.PictureCallback {

    // Public fields
    public static final String EXTRA_FILENAME = "filename";
    public static final String RESULT_IMAGE_FILE = "image_file";
    public static final String IMAGE_FORMAT_EXTENSION = ".png";
    public static final Bitmap.CompressFormat IMAGE_FORMAT_COMPRESSION = Bitmap.CompressFormat.PNG;

    // Activity params
    private String filename;

    // Private fields
    private static final String SAVEINST_FLASH = "flash";
    private static final int REQUEST_PERMISSION_CAMERA = 0;
    private Camera camera;
    private int cameraId;
    private FrameLayout cameraContainer;
    private CameraView cameraView;
    private ProgressDialog pDialog;


    @Override
    public void onErrorDialogClose() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("CameraActivity", "ONCREATE");

        if(getIntent().hasExtra(EXTRA_FILENAME)) {
            filename = getIntent().getStringExtra(EXTRA_FILENAME);
        } else {
            ErrorDialogFragment.showErrorDialog(this, "Intent extra \"filename\" not found.");
        }

        setContentView(R.layout.activity_camera);
        cameraContainer = ((FrameLayout) findViewById(R.id.container_camera));
        cameraContainer.findViewById(R.id.imageButton_takePicture).setOnClickListener(this);
        cameraContainer.findViewById(R.id.imageButton_cancel).setOnClickListener(this);
        cameraContainer.findViewById(R.id.imageButton_flashMode).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("CameraActivity", "ONSTART");
        if(camera == null) {
            if(cameraExists()) {
                int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
                if(permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
                } else {
                    new CameraLoader(this).execute();
                }
            } else {
                ErrorDialogFragment.showErrorDialog(this, "Device does not have a camera.");
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        //savedInstanceState.putSerializable(SAVEINST_FLASH, picFiles);
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d("CameraActivity", "ONPAUSE");
        releaseCamera();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.imageButton_takePicture:
                takePicture();
                break;
            case R.id.imageButton_cancel:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.imageButton_flashMode:
                //TODO flash modes
                ErrorDialogFragment.showErrorDialog(this, "Not Implemented");
                break;
            default:
                ErrorDialogFragment.showErrorDialog(this, "Unhandled click action!");
                break;
        }
    }

    @Override
    public void onShutter() {
        pDialog = new ProgressDialog(this);
        pDialog.setTitle("Processing Picture...");
        pDialog.show();
    }


    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        try {
            if(data != null) {
                // create bitmap from data bytes
                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                //bmp = orientBitmap(bmp);TODO
                Log.d("TAKEN JPEG", "size = " + bmp.getWidth() + "x" + bmp.getHeight());

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

                if(pDialog.isShowing()) {
                    pDialog.dismiss();
                }

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

    private FrameLayout.LayoutParams getOptimalLayoutParams() {
        //TODO rotation
        //TODO over stretching
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        // fit to container
        Camera.Size pSize = camera.getParameters().getPreviewSize();
        int w = pSize.width;
        int h = pSize.height;

        if(getScreenRotation() == 0){//TODO make dynamic
            int temp = w;
            w = h;
            h = temp;
        }
      /*  double ratio = (double) (w) / h;
        if(w >= h) {
            w = cameraContainer.getWidth();
            h = (int) (w / ratio);
        } else {
            h = cameraContainer.getHeight();
            w = (int) (h * ratio);
        }
        */
        return new FrameLayout.LayoutParams(w, h, Gravity.CENTER);
    }


    private void onCameraLoad(int cameraId, Camera camera, String reason) {
        this.camera = camera;
        this.cameraId = cameraId;
        if(camera != null) {
            setCameraOrientation();

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

    private void takePicture() {
        camera.takePicture(this, null, this);
    }

    /**
     * Rotates bitmap to align with screen orientation. //TODO may not work correctly on all devices, because it assumes 0 screen rotation is vertical.
     *
     * @param bmp - bitmap
     * @return
     */
    private Bitmap orientBitmap(Bitmap bmp) {
        int angleS = getScreenRotation();
        int angleB = 0;
        if(bmp.getWidth() > bmp.getHeight()) {
            angleB = -90;//bmp is rotated so that top side is to the left
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(angleS - angleB);
        return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
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
     * Adjust camera display and picture orientations to always point up no matter the screen orientation.
     */
    private void setCameraOrientation() {//TODO
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
            rCamDisplay = camO;
            rPic = (camO + deviceO) % 360;
        }
        camera.setDisplayOrientation(rCamDisplay);

        //params.setRotation(rPic);
        //camera.setParameters(params);

        Log.d("CameraActivity", "info.orientation = " + info.orientation);
        Log.d("CameraActivity", "deviceO = " + deviceO);
        Log.d("CameraActivity", "rCamDisplay = " + rCamDisplay);
        Log.d("CameraActivity", "rPic = " + rPic);


    }

    /**
     * Adjust camera display and picture orientations to always be portrait.
     */
    private void setCameraOrientationPortrait() {//TODO
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

        //params.setRotation(rPic);
        //camera.setParameters(params);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_PERMISSION_CAMERA) {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialogFragment.showErrorDialog(this, "This application does not have permission to access the camera.");
            } else {
                new CameraLoader(this).execute();
            }
        }
    }

    private class CameraLoader extends AsyncTask<Void, Void, Camera> {
        private CameraActivity act;
        private ProgressDialog d;
        private String reason;
        private int cid = -1;

        CameraLoader(CameraActivity act) {
            this.act = act;
        }

        @Override
        protected void onPreExecute() {
            d = new ProgressDialog(act);
            d.setTitle("Loading Camera...");
            d.show();
        }

        @Override
        protected Camera doInBackground(Void... params) {
            Camera c = null;
            try {
                // choose first back-facing camera
                int n = Camera.getNumberOfCameras();
                for(int i = 0; i < n; i++) {
                    Camera.CameraInfo info = new Camera.CameraInfo();
                    Camera.getCameraInfo(i, info);
                    if(info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        cid = i;
                        break;
                    }
                }

                // open camera
                if(cid != -1) {
                    c = Camera.open(cid);
                    if(c == null) {
                        reason = "Could not access camera.";
                    }
                } else {
                    reason = "Device does not have a back-facing camera.";
                }
            } catch(Exception e) {
                Log.e("OnPictureTaken", "Exception", e);
                reason = e.toString() + ":\n" + e.getMessage();
            }
            return c;
        }

        @Override
        protected void onPostExecute(Camera c) {
            if(d.isShowing()) {
                d.dismiss();
            }
            act.onCameraLoad(cid, c, reason);
        }
    }

}
