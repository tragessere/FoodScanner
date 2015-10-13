package senior_project.foodscanner.activities;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import java.io.IOException;

import senior_project.foodscanner.R;
import senior_project.foodscanner.ui.components.foodscanner.CameraView;

/**
 * Uses old Camera api instead of Camera2 api, because Camera2 is for API level 21+.
 */
public class FoodScannerActivity extends AppCompatActivity {
    private Camera camera;
    private CameraView cameraView;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_scanner);

        //TODO loading indicator for camera loading

        //TODO prompt for camera permission



        //TODO do on new thread
        camera = getCamera();
        cameraView = new CameraView(this, camera);
        if(camera != null) {
            ((FrameLayout) findViewById(R.id.container_camera)).addView(cameraView);
        }
        else{
            //TODO dialog
            finish();
            return;
        }

        // getParameters
        // getCamerainfo
    }

    @Override
    protected void onStart(){
        super.onStart();
        if(camera != null){
            try {
                camera.reconnect();
            } catch(IOException e) {
                // TODO dialog
            }
        }
        Log.d("FoodScanner","ONSTART");
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(camera != null){
            camera.release();
        }
        Log.d("FoodScanner", "ONPAUSE");
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

    /**
     * Check if this device has a camera
     */
    private boolean cameraExists() {
        if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /**
     * Initialize camera object.
     * @return camera object
     */
    private Camera getCamera(){
        Camera camera = null;
        if(cameraExists()){
            try {
                camera = Camera.open(); // attempt to get a Camera instance
            } catch(Exception e) {
                Log.e("FoodScanner", "Cam error", e);
                // Camera is not available (in use or does not exist)
            }
        }
        return camera;
    }

}
