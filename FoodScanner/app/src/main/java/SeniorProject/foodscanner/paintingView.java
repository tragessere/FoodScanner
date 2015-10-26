package SeniorProject.foodscanner;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;

import senior_project.foodscanner.R;

public class paintingView extends View
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

    private Resources res;

    private Line line1, line2, line3;
    private ArrayList<Path> paths;
    private float x1, y1;
    private lineDrawnStatus status;
    private boolean isReady = false;
    private boolean allLines = false;

    private int screenW, screenH;
    //endregion

    //region Constructor(s)
    public paintingView(Context context, AttributeSet attrs)
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

        screenW = d.widthPixels;
        screenH = d.heightPixels;

        res = getResources();
        background = BitmapFactory.decodeResource(res, R.drawable.side);
        background = resizeImageForImageView(background);

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
        if(paths.size() <= 0)
        {
            return;
        }
        switch(status)
        {
            case oneLine:
                status = lineDrawnStatus.nolines;
                break;
            case twoLines:
                status = lineDrawnStatus.oneLine;
                if(!allLines) {
                    previousPerspective();
                    invalidate();
                    return;
                }
                break;
            case threeLines:
                status = lineDrawnStatus.twoLines;
                if(allLines)
                    allLines = false;
                break;
        }
        paths.remove(paths.size() - 1);
        invalidate();
    }

    /*Goes to the next "perspective" for the user to draw more lines*/
    public void nextPerspective()
    {
        previousBitmap = background.copy(Bitmap.Config.ARGB_8888, true);
        background.recycle();
        background = BitmapFactory.decodeResource(res, R.drawable.top);
        background = resizeImageForImageView(background);
        isReady = true;
        drawCanvas.drawBitmap(background, new Rect(0, 0, background.getWidth(), background.getHeight()), new Rect(0, 0, 1080, 1920), new Paint());
        invalidate();
    }

    private void setPic(/*final String path*/) {
        int targetW = this.getWidth();
        int targetH = this.getHeight();
        String path ="Phone\\DCIM\\foodscanner\\side";

        final BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
        background = BitmapFactory.decodeFile(path, bmOptions);
    }

    /*Goes back to the first photo "perspective" form the second*/
    private void previousPerspective()
    {
        isReady = false;
        background.recycle();
        background = previousBitmap;
        invalidate();
    }

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

    //endregion

    //region Override Methods
    @Override
    protected void onDraw(Canvas canvas)
    {
        //if in the last perspective dont show the other lines
        if((status == lineDrawnStatus.threeLines) && isReady)
        {
            canvas.drawBitmap(background, new Rect(0, 0, background.getWidth(), background.getHeight()), new Rect(0, 0, 1080, 1920), new Paint());
            canvas.drawPath(paths.get(paths.size() - 1), drawPaint);
            return;
        }

        canvas.drawBitmap(background, new Rect(0, 0, background.getWidth(), background.getHeight()), new Rect(0, 0, 1080, 1920), new Paint());
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
                        line2 = new Line(x1, y1, pointX, pointY);
                        paths.add(drawPath);
                        status = lineDrawnStatus.twoLines;
                        nextPerspective();
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
