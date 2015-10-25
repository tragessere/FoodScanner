package senior_project.foodscanner;

import android.content.Context;

import java.io.File;

/**
 * Images for this app are stored temporarily in a designated folder in the app's cache directory.
 * Use this class for storing camera images and drawn images.
 * Note that since they are stored in the cache directory, they are subject to deletion at any point in time.
 */
public class ImageDirectoryManager {
    public static final String IMAGE_DIR_NAME = "CameraImageCache";
    public static final String EXTRA_IMAGE_DIR = "image_directory";

    /**
     * Deletes all image files in the image directory.
     *
     * @param context
     */
    public static boolean clearImageDirectory(Context context) {
        boolean allDeleted = true;
        File cacheDir = getImageDirectory(context);
        for(File f : cacheDir.listFiles()) {
            if(!f.delete()) {
                allDeleted = false;
            }
        }
        return allDeleted;
    }

    /**
     * Returns a File object representing the image directory.
     * @param context
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
