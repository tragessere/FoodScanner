package senior_project.foodscanner;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import senior_project.foodscanner.DrawView;
import senior_project.foodscanner.Line;
import senior_project.foodscanner.ImageDirectoryManager;
import senior_project.foodscanner.R;
import senior_project.foodscanner.activities.PaintingActivity;

public class PaintingView extends View
{

    /*This enum helps keep track of how many lines are currently drawn
    * it is the current status of the canvas*/
    public enum lineDrawnStatus
    {
        nolines,
        oneLine,
        twoLines,
        threeLines;
    }

    //region Variable Declaration
    private Path            drawPath;
    private Paint           drawPaint, canvasPaint;
    private int             paintColor = Color.BLACK;
    private Canvas          drawCanvas;
    private Bitmap          canvasBitmap;
    private Bitmap          background;
    private Bitmap          previousBitmap;
    private DisplayMetrics  d;
    private DrawView rectangle;

    private File        imageDirectory;
    private Resources   res;

    private Line line1, line2, line3;
    private ArrayList<Path> paths;
    private float x1, y1;
    private lineDrawnStatus status;
    private boolean isReady = false;
    private boolean allLines = false;
    private boolean whyDoINeedThis = false;


    private int screenW, screenH;
    //endregion

    //region Constructor(s)
    public PaintingView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setupDrawing();
    }
    //endregion

    //region Helper Methods
    public void setupDrawing()
    {
        paths = new ArrayList<>();
        d = new DisplayMetrics();
        WindowManager manager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(d);

        imageDirectory = ((PaintingActivity)getContext()).getImageDirectory();
        screenW = d.widthPixels;
        screenH = d.heightPixels;

        res = getResources();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        background = decodeFile(new File(imageDirectory.getPath() + "/Side.png"));

        status = lineDrawnStatus.nolines;

        drawPath = new Path();
        drawPath.setFillType(Path.FillType.EVEN_ODD);
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setStyle(Paint.Style.FILL);
        drawCanvas = new Canvas();
        drawPaint.setAntiAlias(false);
        drawPaint.setStrokeWidth(20);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    /*undo's the user's last line drawn.  This is called when the undo button is pressed.*/
    public void undoLast()
    {
        PaintingActivity p = (PaintingActivity)this.getContext();
        switch(status)
        {
            case nolines:
                p.lastRectangle(false);
                invalidate();
                return;

            case oneLine:
                status = lineDrawnStatus.nolines;
                break;

            case twoLines:
                if(whyDoINeedThis && !p.inRectangleView()) {
                    background.recycle();
                    p.lastRectangle(true);
                    invalidate();
                    return;
                }

                //If we are in a rectangle view, go back to drawing
                if(p.inRectangleView()) {
                    whyDoINeedThis = false;
                    p.lastPaintingView();
                    return;
                }

                status = lineDrawnStatus.oneLine;
                previousPerspective();
                invalidate();
                break;

            case threeLines:
                if(allLines) {
                    status = lineDrawnStatus.twoLines;
                    allLines = false;
                }

                else
                {
                    whyDoINeedThis = true;
                    background.recycle();
                    p.lastRectangle(false);
                    invalidate();
                    return;
                }

                break;
        }

        if(paths.size() <= 0)
            return;

        paths.remove(paths.size() - 1);
        invalidate();
    }

    /*Goes to the next "perspective" for the user to draw more lines*/
    public void nextPerspective()
    {
        previousBitmap = background.copy(Bitmap.Config.ARGB_8888, true);
        background.recycle();

        background = decodeFile(new File(imageDirectory.getPath() + "/Top.png"));
        int[] params = getPhotoParams(background);
        background = Bitmap.createScaledBitmap(background, params[0], params[1], false);
        isReady = true;
        drawCanvas.drawBitmap(background, new Rect(0, 0, background.getWidth(), background.getHeight()), new Rect(0, 0, background.getWidth(), background.getHeight()), new Paint());
        invalidate();
    }

    public void previousPerspective()
    {
        isReady = false;
        background.recycle();
        background = previousBitmap;
        invalidate();
    }

    //FOUND HERE: http://stackoverflow.com/questions/18255572/android-bitmap-cache-takes-a-lot-of-memory/18255693#18255693
    /*Decodes a file with the correct options so it does not crash the application from an out of memory exeption*/
    public static Bitmap decodeFile(File f) {
        Bitmap b = null;
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            FileInputStream fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();
            int IMAGE_MAX_SIZE = 1000;
            int scale = 1;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(
                        2,
                        (int) Math.round(Math.log(IMAGE_MAX_SIZE
                                / (double) Math.max(o.outHeight, o.outWidth))
                                / Math.log(0.5)));
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return b;
    }

    //FOUND HERE http://stackoverflow.com/questions/14235287/suggestions-to-avoid-bitmap-out-of-memory-error
    /*Decodes a bitmap file without running out of memory.   Edited from the source some.*/
    public static BitmapFactory.Options getBitmapOptions(int WIDTH, int HEIGHT) {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            //The new size we want to scale to
            final int REQUIRED_WIDTH=WIDTH;
            final int REQUIRED_HIGHT=HEIGHT;
            //Find the correct scale value. It should be the power of 2.
            int scale=1;
            while(o.outWidth/scale/2>=REQUIRED_WIDTH && o.outHeight/scale/2>=REQUIRED_HIGHT)
                scale*=2;

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            return o2;
    }

    /*Goes back to the first photo "perspective" form the second*/


    /*People from the internet made scaling the bitmap ez :) */
    public Bitmap resizeImageForImageView(Bitmap bitmap) {
        Bitmap resizedBitmap = null;
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int newWidth = -1;
        int newHeight = -1;
        float multFactor = -1.0F;
        if(originalHeight > originalWidth) {
            newHeight = 1447;
            multFactor = (float) originalWidth/(float) originalHeight;
            newWidth = (int) (newHeight*multFactor);
        } else if(originalWidth > originalHeight) {
            newWidth = 985;
            multFactor = (float) originalHeight/ (float)originalWidth;
            newHeight = (int) (newWidth*multFactor);
        } else if(originalHeight == originalWidth) {
            newHeight = 1447;
            newWidth = 985;
        }
        resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
        return resizedBitmap;
    }

    private int[] getPhotoParams(Bitmap b) {
        int w = b.getWidth();
        int h = b.getHeight();

        // flip width and height depending on screen orientation (determined by shape rather than rotation)
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        if(this.getWidth() < this.getHeight() && w > h) {
            int temp = w;
            w = h;
            h = temp;
        }

        // fit to container
        double ratio = (double) (w) / h;
        if(this.getWidth() < this.getHeight()) {
            w = this.getWidth();//cameraContainer.getWidth();
            h = (int) (w / ratio);
        } else {
            h = this.getHeight();//cameraContainer.getHeight();
            w = (int) (h * ratio);
        }

        return new int[] {w, h};
    }
    //endregion

    //region Override Methods
    @Override
    protected void onDraw(Canvas canvas)
    {
        int[] params = getPhotoParams(background);
        background = Bitmap.createScaledBitmap(background, params[0], params[1], false);
        canvas.drawBitmap(background, new Rect(0,0,background.getWidth(), background.getHeight()), new Rect(0,0,background.getWidth(), background.getHeight()), new Paint());
        //if in the last perspective dont show the other lines
        if((status == lineDrawnStatus.threeLines) && isReady)
        {
            canvas.drawBitmap(background, new Rect(0,0,background.getWidth(), background.getHeight()), new Rect(0,0,background.getWidth(), background.getHeight()), new Paint());
            canvas.drawPath(paths.get(paths.size() - 1), drawPaint);
            return;
        }

        canvas.drawBitmap(background, new Rect(0,0,background.getWidth(), background.getHeight()), new Rect(0,0,background.getWidth(), background.getHeight()), new Paint());
        canvas.drawPath(drawPath, drawPaint);

        if((status == lineDrawnStatus.twoLines) && isReady)
            return;

        for (Path p : paths)
            canvas.drawPath(p, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        float pointX = event.getX();
        float pointY = event.getY();

        if(status == lineDrawnStatus.threeLines)
        {
            return true;
        }

        // Checks for the event that occurs
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:

                // Starts a new line in the path

                drawPath.moveTo(pointX, pointY);

                //gets the first point of the line;
                x1 = pointX;
                y1 = pointY;

                break;
            case MotionEvent.ACTION_MOVE:
                //Log.d("PaintingActivity", "PointX:" + rawX + " PointY:" + rawY);
                // Draws line between last point and this point

                drawPath.lineTo(pointX, pointY);
                break;
            case MotionEvent.ACTION_UP:

                //statement to decide weather to fill in line 1 or line 2
                //draws a line from where the user puts their finger down to where they lifted it up
                switch(status)
                {
                    case nolines:
                        line1 = new Line(x1, y1, pointX, pointY);
                        paths.add(drawPath);
                        status = lineDrawnStatus.oneLine;
                        break;
                    case oneLine:
                        whyDoINeedThis = true;
                        line2 = new Line(x1, y1, pointX, pointY);
                        paths.add(drawPath);
                        status = lineDrawnStatus.twoLines;
                        PaintingActivity p = (PaintingActivity)this.getContext();
                        nextPerspective();
                        p.nextRectangle();
                        break;
                    case twoLines:
                        line3 = new Line(x1, y1, pointX, pointY);
                        paths.add(drawPath);
                        status = lineDrawnStatus.threeLines;
                        allLines = true;

                    case threeLines:
                        break;
                    default:
                        break;
                }

                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.setFillType(Path.FillType.EVEN_ODD);
                drawPath = new Path();
            default:
                return true;
        }

        postInvalidate();
        return true;
    }

    @Override
    public void onSizeChanged (int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenW = w;
        screenH = h;
    }
    //endregion

    //region Accessor Methods

    /*returns the bitmap associated with the paintingView*/
    public Bitmap getBitmap()
    {
        return canvasBitmap;
    }

    /*function to set up the bitmap in the paintingView.  We need the screen metrics, but we can not get
     *them from this context.  We do this in PaintingActivity*/
    public void setBitmap(int width, int height)
    {
        canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        drawCanvas.setBitmap(canvasBitmap);
    }

    /*@return the first line drawn (a value)*/
    public Line getLine1()
    {
        return line1;
    }

    /*@return the second line drawn (b value)*/
    public Line getLine2()
    {
        return line2;
    }

    /*@return the third line drawn (c value)*/
    public Line getLine3()
    {
        return line3;
    }

    /*@return the background photo the user took */
    public Bitmap getGetPhoto()
    {
        return background;
    }
    //endregion
}
