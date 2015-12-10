package senior_project.foodscanner;

import android.content.Context;

import java.io.File;

/**
 * Images for this app are stored temporarily in a designated folder private to this app.
 * Use this class for storing camera images and drawn images.
 * Note: The designated folder is in the app cache directory. Images stored here are subject to random deletion by the OS when free space is low.
 *
 * Update 12/8 from Tyler: I am also using this class for storing density entries after pulling.
 * I am leaving the name as ImageDirectoryManager, but simply "DirectoryManager" would be better.
 */
public class ImageDirectoryManager {
    public static final String IMAGE_DIR_NAME = "CameraImages";
    public static final String DENSITY_DIR_NAME = "DensityEntries";
    public static final String PPI_DIR_NAME = "PixelsPerInch";

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

    /**
     * Returns a File object representing the density directory.
     * @param context - context
     * @return
     */
    public static File getDensityDirectory(Context context) {
        File densityDir = new File(context.getCacheDir(), DENSITY_DIR_NAME);
        if(!densityDir.exists()) {
            densityDir.mkdir();
        }
        return densityDir;
    }

    /**
     * Returns a File object representing the ppi directory.
     * @param context - context
     * @return
     */
    public static File getPixelsDirectory(Context context) {
        File ppiDir = new File(context.getCacheDir(), PPI_DIR_NAME);
        if(!ppiDir.exists()) {
            ppiDir.mkdir();
        }
        return ppiDir;
    }
}
