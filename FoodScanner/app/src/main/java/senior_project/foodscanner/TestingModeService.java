package senior_project.foodscanner;

import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Enable and disable testing modes for this application. A warning will be logged when modes are enabled.
 */
public class TestingModeService {
    public static final boolean TESTMODE_CALENDAR_FAKE_SERVER = false;// Disable server queries in MealCalendarActivity. Simulate them with sleeping threads.

    public static TestingModeService service;

    public static void init(){
        service = new TestingModeService();
        Field[] modes = service.getClass().getDeclaredFields();
        try {
            for(Field mode : modes) {
                String name = mode.getName();
                if(!name.equals("service") && mode.getBoolean(service)) {
                    Log.w("TestingModeService", "Testing Mode Enabled: "+name);
                }
            }
        }
        catch(IllegalAccessException e){
            Log.e("TestingModeService","init()",e);
        }
    }

    public static List<String> getEnabledTestModes() {
        Field[] modes = service.getClass().getDeclaredFields();
        List<String> enabled = new ArrayList<>(modes.length);
        try {
            for(Field mode : modes) {
                String name = mode.getName();
                if(!name.equals("service") && mode.getBoolean(service)) {
                    enabled.add(name);
                }
            }
        }
        catch(IllegalAccessException e){
            Log.e("TestingModeService","getEnabledTestModes()",e);
            enabled.clear();
            enabled.add(e.getLocalizedMessage());
        }
        return enabled;
    }
}
