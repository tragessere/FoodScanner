package senior_project.foodscanner.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import senior_project.foodscanner.R;
import senior_project.foodscanner.ui.components.foodscanner.ErrorDialogFragment;
import senior_project.foodscanner.ui.components.foodscanner.CameraView;

/**
 * Activity for taking an X number of pictures.
 *
 * When starting this activity, specify the number of pictures to take with Intent.putExtra("num_pictures", n).
 * Defaults to taking only one picture.
 *
 * To get the pictures that were taken //TODO.
 *
 *
 * This class uses the old Camera api instead of Camera2 api, because Camera2 is for API level 21+.
 *
 *
 * Features:
 *  -Back/Cancel
 *  -Navigate between pictures
 *  -Retake pictures
 *  -Take picture
 *  -Tap to focus
 *  -Toggle flash: On/Off/Auto
 *  -Other:
 *      -Auto focus
 *
 */
public class FoodScannerActivity extends AppCompatActivity {
    private Camera camera;
    private CameraView cameraView;
    private FrameLayout cameraContainer;
    private static final int requestCode_Camera = 0;
    private int numPics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_scanner);
        cameraContainer = ((FrameLayout) findViewById(R.id.container_camera));
        numPics = getIntent().getIntExtra("num_pictures",1);
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

    @Override
    protected void onStart() {
        super.onStart();
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
        Log.d("FoodScanner", "ONSTART");
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
        Log.d("FoodScanner", "ONPAUSE");
    }

    private void releaseCamera() {
        if(camera != null) {
            camera.release();
            camera = null;
            cameraContainer.removeView(cameraView);
            cameraView = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_food_scanner, menu);
        return true;
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

    private void onCameraLoad(Camera camera, String reason) {
        this.camera = camera;
        if(camera != null) {
            cameraView = new CameraView(this, camera);
            cameraContainer.addView(cameraView);
        } else {
            showErrorDialog(reason);
        }
    }

    private void showErrorDialog(String message) {
        ErrorDialogFragment newFragment = ErrorDialogFragment.newInstance(message);
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void errorDialogOk() {
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

    private class CameraLoader extends AsyncTask<Void, Void, Camera> {
        private FoodScannerActivity act;
        private ProgressDialog d;
        private String reason;

        CameraLoader(FoodScannerActivity act) {
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
                c = Camera.open(); // attempt to get a Camera instance
                if(c == null) {
                    reason = "Could not access camera.";
                }
            } catch(Exception e) {
                reason = e.toString() + ":\n" + e.getMessage();
            }

            return c;
        }

        @Override
        protected void onPostExecute(Camera c) {
            if(d.isShowing()) {
                d.dismiss();
            }
            act.onCameraLoad(c, reason);
        }


    }

}
