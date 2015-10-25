package senior_project.foodscanner.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ViewFlipper;

import java.io.File;
import java.io.FileOutputStream;

import senior_project.foodscanner.R;
import senior_project.foodscanner.ui.components.ImageBrowser;
import senior_project.foodscanner.ui.components.foodscanner.CameraView;
import senior_project.foodscanner.ui.components.foodscanner.ErrorDialogFragment;

/**
 * Activity for taking an X number of pictures.
 * <p/>
 * * When starting this activity there are several extras you can put in the Intent to alter this activity's behavior:
 * -"pic_names" - String[] - specify the number of pictures to take and their names. (Defaults to taking one picture named "1")
 * <p/>
 * To get the pictures that were taken you need to start this activity with startActivityForResult().
 * The result data Intent has the extra named CameraActivity.EXTRA_IMAGE_DIR, which contains a File object to the directory where the image files are stored.
 * It is recommended to delete these files after you are done with them.
 * <p/>
 * <p/>
 * This class uses the old Camera api instead of Camera2 api, because Camera2 is for API level 21+.
 * <p/>
 * <p/>
 * Features:
 * -Back/Cancel
 * -Next
 * -Navigate between pictures
 * -Retake pictures
 * -Take picture
 * -Tap to focus
 * -Zoom fingers
 * -Toggle flash: On/Off/Auto
 * -Other:
 * -Auto focus
 * <p/>
 * User flow:
 * -Next button is disabled
 * -Take picture
 * -Picture is displayed, Retake button replaces take picture button
 * -Cycle through
 */
//TODO adjust for orientation and aspect ratio
//TODO check reloading
public class CameraActivity extends AppCompatActivity implements View.OnClickListener, Camera.ShutterCallback, Camera.PictureCallback, ImageBrowser.ActionButtonListener, ImageBrowser.FinishButtonListener {
    public static final String EXTRA_IMAGE_DIR = "image_directory";
    public static final String FILE_FORMAT_EXTENSION = ".jpg";

    private Camera camera;
    private int cameraId;
    private CameraView cameraView;
    private FrameLayout cameraContainer;
    private static final int requestCode_Camera = 0;
    private String[] picNames = {"1"};
    private ViewFlipper camFlipper;
    private ImageBrowser picBrowser;
    private static final String dirName = "CameraImageCache";
    private File cacheDir;
    private ProgressDialog picDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("CameraActivity", "ONCREATE");

        if(getIntent().hasExtra("pic_names")) {
            picNames = getIntent().getStringArrayExtra("pic_names");
        }

        setContentView(R.layout.activity_camera);
        camFlipper = ((ViewFlipper) findViewById(R.id.viewFlipper_camera));
        camFlipper.setInAnimation(this, android.R.anim.fade_in);
        camFlipper.setOutAnimation(this, android.R.anim.fade_out);
        Bitmap defbmp = BitmapFactory.decodeResource(getResources(), R.drawable.camera512);
        Drawable defPic = new BitmapDrawable(getResources(), defbmp);
        picBrowser = new ImageBrowser(this, picNames, defPic);
        picBrowser.setActionButtonListener(this);
        picBrowser.setFinishButtonListener(this);
        picBrowser.setActionButtonText("Take Picture");
        picBrowser.setFinishButtonEnabled(false);
        camFlipper.addView(picBrowser);
        camFlipper.showNext();
        cameraContainer = ((FrameLayout) findViewById(R.id.container_camera));
        cameraContainer.findViewById(R.id.imageButton_takePicture).setOnClickListener(this);
        cameraContainer.findViewById(R.id.imageButton_cancel).setOnClickListener(this);
        cameraContainer.findViewById(R.id.imageButton_flashMode).setOnClickListener(this);

        // reset cache dir
        cacheDir = new File(getCacheDir(), dirName);
        if(!cacheDir.exists()) {
            cacheDir.mkdir();
        } else {
            for(File f : cacheDir.listFiles()) {
                if(!f.delete()) {
                    showErrorDialog("Reset cache dir: Could not delete file \"" + f.getName() + "\".");
                }
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d("CameraActivity", "ONSTART: " + picNames[0]);
        if(camera == null) {
            if(cameraExists()) {
                int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
                if(permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, requestCode_Camera);
                } else {
                    new CameraLoader(this).execute();
                }
            } else {
                showErrorDialog("Device does not have a camera.");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("CameraActivity", "ONPAUSE");
        releaseCamera();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera, menu);
        return true;
    }


    private void takePicture() {//TODO loading
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


    private void releaseCamera() {
        if(camera != null) {
            camera.release();
            camera = null;
            cameraContainer.removeView(cameraView);
            cameraView = null;
        }
    }


    private void onCameraLoad(int cameraId, Camera camera, String reason) {
        this.camera = camera;
        this.cameraId = cameraId;
        if(camera != null) {
            setCameraOrientation();
            cameraView = new CameraView(this, camera);
            cameraContainer.addView(cameraView, 0);
            camera.stopPreview();

            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPictureSize();
            Log.d("Cam load", "preview size = " + parameters.getPreviewSize().width + "x" + parameters.getPreviewSize().height);
            Log.d("Cam load", "picsize = " + parameters.getPictureSize().width + "x" + parameters.getPictureSize().height);
        } else {
            showErrorDialog(reason);
        }
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
     * adjust camera display and picture orientations to match screen orientation
     */
    private void setCameraOrientation() {
        int degrees = getScreenRotation();

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        Camera.Parameters params = camera.getParameters();
        int rDisp;
        int rPic;
        if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rDisp = (info.orientation + degrees) % 360;
            rDisp = (360 - rDisp) % 360;  // compensate the mirror
            rPic = (info.orientation - degrees + 360) % 360;
        } else {  // back-facing
            rDisp = (info.orientation - degrees + 360) % 360;
            rPic = (info.orientation + degrees) % 360;
        }
        camera.setDisplayOrientation(rDisp);
        Log.d("ROTATE", "" + rPic);
        rPic += 180;
        params.setRotation(rPic);
        camera.setParameters(params);
    }


    private void showErrorDialog(String message) {
        ErrorDialogFragment newFragment = ErrorDialogFragment.newInstance(message);
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void onErrorDialogOk() {
        finish();
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

    private void flipToCamera() {
        camera.startPreview();
        camFlipper.showNext();
    }

    private void flipToPicture() {
        camera.stopPreview();
        camFlipper.showNext();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.imageButton_takePicture:
                takePicture();
                break;
            case R.id.imageButton_cancel:
                flipToPicture();
                break;
            case R.id.imageButton_flashMode:
                //TODO flash modes
                showErrorDialog("Not Implemented");
                break;
            default:
                showErrorDialog("Unhandled click action!");
                break;
        }
    }

    @Override
    public void onActionButton() {
        flipToCamera();
    }

    @Override
    public void onFinishButton() {
        Intent intent = new Intent(this, MealDetailsActivity.class);
        intent.putExtra("image_directory", cacheDir);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onShutter() {
        picDialog = new ProgressDialog(this);
        picDialog.setTitle("Processing Picture...");
        picDialog.show();
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        try {
            if(data != null) {
                // create bitmap from data bytes
                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                bmp = orientBitmap(bmp);

                // save to cache directory
                File imgF = new File(cacheDir, picBrowser.getCurrentImageName()+FILE_FORMAT_EXTENSION);
                if(imgF.exists()) {
                    if(!imgF.delete()) {
                        showErrorDialog("Cache directory saving: Could not delete file \"" + imgF.getName() + "\"");
                    }
                    if(!imgF.createNewFile()) {
                        showErrorDialog("Cache directory saving: Could not create file \"" + imgF.getName() + "\"");
                    }
                }
                FileOutputStream fos = new FileOutputStream(imgF.getPath());
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();

                //update picBrowser
                picBrowser.setImage(picBrowser.getCurrentIndex(), new BitmapDrawable(getResources(), bmp));
                if(!picBrowser.containsNullImage()) {
                    picBrowser.setFinishButtonEnabled(true);
                }

                // return to picBrowser
                flipToPicture();

                if(picDialog.isShowing()) {
                    picDialog.dismiss();
                }

                Log.d("TAKEN JPEG", "size = " + bmp.getWidth() + "x" + bmp.getHeight());
            } else {
                showErrorDialog("FATAL: Unable to get picture data.");
            }
        } catch(Exception e) {
            Log.e("OnPictureTaken", "Exception", e);
            showErrorDialog("Exception:" + e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == requestCode_Camera) {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showErrorDialog("This application does not have permission to access the camera.");
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
