package SeniorProject.foodscanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
/**
 * ColorBall class used to store points of the screen.
 *
 * @author Chintan Rathod (http://chintanrathod.com)
 */
public class ColorBall
{
    Bitmap bitmap;
    Context mContext;
    Point point;
    int id;
    static int count = 0;

    public ColorBall(Context context, int resourceId, Point point) {
        this.id = (count++)%5;
        bitmap = BitmapFactory.decodeResource(context.getResources(),
                resourceId);
        mContext = context;
        this.point = point;
    }

    public int getWidthOfBall()
    {
        return bitmap.getWidth();
    }

    public int getHeightOfBall() {
        return bitmap.getHeight();
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getX() {
        return point.x;
    }

    public int getY() {
        return point.y;
    }

    public int getID() {
        return id;
    }

    public void setX(int x) {
        point.x = x;
    }

    public void setY(int y) {
        point.y = y;
    }
}