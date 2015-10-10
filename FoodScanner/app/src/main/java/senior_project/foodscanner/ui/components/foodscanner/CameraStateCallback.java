package senior_project.foodscanner.ui.components.foodscanner;

import android.hardware.camera2.CameraDevice;
import android.util.Log;

import senior_project.foodscanner.activities.FoodScannerActivity;

/**
 * Created by Penguinator on 10/9/2015.
 */
public class CameraStateCallback extends CameraDevice.StateCallback {

    private FoodScannerActivity act;

    public CameraStateCallback(FoodScannerActivity act){
        this.act = act;
    }

    @Override
    public void onOpened(CameraDevice camera) {
        Log.d("Callback", "opened");
    }

    @Override
    public void onDisconnected(CameraDevice camera) {
        Log.d("Callback", "disconnect");
    }

    @Override
    public void onError(CameraDevice camera, int error) {
        Log.d("Callback", "error");
    }
}
