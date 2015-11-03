package senior_project.foodscanner;

import android.content.Context;

import java.io.File;

/**
 * Images for this app are stored temporarily in a designated folder private to this app.
 * Use this class for storing camera images and drawn images.
 * Files should be deleted from this directory as soon as they are no longer needed.
 * Note: The designated folder is in the app cache directory. Images stored here are subject to random deletion by the OS when free space is low.
 */
public class ImageDirectoryManager {
    public static final String IMAGE_DIR_NAME = "CameraImages";

    /**
     * Deletes all image files in the image directory.
     *
     * @param context - context
     */
    public static boolean clearImageDirectory(Context context) {
        boolean allDeleted = true;
        File dir = getImageDirectory(context);
        for(File f : dir.listFiles()) {
            if(!f.delete()) {
                allDeleted = false;
            }
        }
        return allDeleted;
    }

    /**
     * Returns a File object representing the image directory.
     * @param context - context
     * @return
     */
    public static File getImageDirectory(Context context) {
        File imageDir = new File(context.getCacheDir(), IMAGE_DIR_NAME);
        if(!imageDir.exists()) {
            imageDir.mkdir();
        }
        return imageDir;
    }
}
