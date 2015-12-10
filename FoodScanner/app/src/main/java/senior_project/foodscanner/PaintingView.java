package senior_project.foodscanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

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
    private Path                    drawPath;
    private Paint                   drawPaint;
    private int                     paintColor = Color.GREEN;
    private Canvas                  drawCanvas;
    private Bitmap                  canvasBitmap;
    private Bitmap                  background;
    private Bitmap                  previousBitmap;
    private DisplayMetrics          d;
    private ArrayList<Point>        endpoints1, endpoints2;
    private Point                   point1, point2, point3, point4, point5, point6;
    private ColorBall               touchArea;
    private String                  previousString;

    private File                    imageDirectory;

    private ArrayList<Path>         paths;
    private ArrayList<ArrayList<Point>>        pointHistory;
    private ArrayList<ArrayList<Point>>        pointHistory1;
    private float                   x1, y1;
    private Point                   p1, p2;
    private lineDrawnStatus         status;
    private boolean                 isReady = false;
    private boolean                 allLines = false;
    private boolean                 moving = false;
    private boolean                 whyDoINeedThis = false;
    private boolean                 perspectiveTwo;
    private double                  radius;
    private int                     currentPoint;
    private int                     temp;
    private int[]                   params = null;


    private int screenW, screenH;
    //endregion

    //region Constructor(s)
    public PaintingView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setupDrawing(context);
    }
    //endregion

    //region Helper Methods
    public void setupDrawing(Context context)
    {
        currentPoint = 0;
        perspectiveTwo = false;
        paths = new ArrayList<>();
        endpoints1 = new ArrayList<>();
        endpoints2 = new ArrayList<>();
        pointHistory = new ArrayList<ArrayList<Point>>();
        pointHistory1 = new ArrayList<ArrayList<Point>>();

        touchArea = new ColorBall(context, R.drawable.circle, new Point(0,0));
        radius = touchArea.getWidthOfBall()/2;

        d = new DisplayMetrics();
        WindowManager manager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(d);

        point1 = new Point(0,0);
        point2 = new Point(0,0);
        point3 = new Point(0,0);
        point4 = new Point(0,0);
        point5 = new Point(0,0);
        point6 = new Point(0,0);

        endpoints1.add(point1);
        endpoints1.add(point2);
        endpoints1.add(point3);
        endpoints1.add(point4);
        endpoints2.add(point5);
        endpoints2.add(point6);

        pointHistory.add(clone(endpoints1));
        pointHistory1.add(clone(endpoints2));
        imageDirectory = ((PaintingActivity)getContext()).getImageDirectory();
        screenW = d.widthPixels;
        screenH = d.heightPixels;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;

        background = decodeFile(new File(imageDirectory.getPath() + "/Top.png"));

        status = lineDrawnStatus.nolines;

        drawPath = new Path();
        drawPath.setFillType(Path.FillType.EVEN_ODD);
        drawPaint = new Paint();
        drawPaint.setColor(Color.parseColor("#ff8f4f"));
        drawPaint.setStyle(Paint.Style.FILL);
        drawCanvas = new Canvas();
        drawPaint.setAntiAlias(false);
        drawPaint.setStrokeWidth(20);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    /*undo's the user's last line drawn.  This is called when the undo button is pressed.*/

    public void undoLast()
    {
        if(getCurrentPointHistory().size() == 1)
            return;

        setCurrentEndpoints(clone(getCurrentPointHistory().get(getCurrentPointHistory().size() - 2)));
        getCurrentPointHistory().remove(getCurrentPointHistory().size() - 1);

        if(getCurrentEndpoints().equals(endpoints1)) {
            if(((PaintingActivity)getContext()).getActionBar() != null)
                ((PaintingActivity)getContext()).getActionBar().setTitle("Draw Lines A & B");
            if(((PaintingActivity)getContext()).getSupportActionBar() != null)
                ((PaintingActivity)getContext()).getSupportActionBar().setTitle("Draw Lines A & B");
        }

        for(Point p : getCurrentEndpoints())
            if(p.x == 0 || p.y == 0) {
                currentPoint = getCurrentEndpoints().indexOf(p);
                ((Button)((PaintingActivity)getContext()).findViewById(R.id.pixelButton)).setVisibility(INVISIBLE);
            }

        invalidate();
    }

    /*Goes to the next "perspective" for the user to draw more lines*/
    public void nextPerspective()
    {
        previousBitmap = background.copy(Bitmap.Config.ARGB_8888, true);
        background.recycle();
        currentPoint = 0;
        PaintingActivity p = (PaintingActivity)getContext();
        previousString = ((TextView)p.findViewById(R.id.pictureLabel)).getText().toString();
        p.setPictureLabel(p.getString(R.string.side_label));
        background = decodeFile(new File(imageDirectory.getPath() + "/Side.png"));
        int[] params = getPhotoParams(background);
        background = Bitmap.createScaledBitmap(background, params[0], params[1], false);
        isReady = true;
        drawCanvas.drawBitmap(background, new Rect(0, 0, background.getWidth(), background.getHeight()), new Rect(0, 0, background.getWidth(), background.getHeight()), new Paint());
        perspectiveTwo = true;
        invalidate();
    }

    public void previousPerspective()
    {
        currentPoint = 4;
        PaintingActivity p = (PaintingActivity)getContext();
        perspectiveTwo = false;
        p.setPictureLabel(previousString);
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

    private int[] getPhotoParams(Bitmap b) {
        int w = b.getWidth();
        int h = b.getHeight();

        // flip width and height depending on screen orientation (determined by shape rather than rotation)
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
        PaintingActivity p = (PaintingActivity)getContext();

        //Set the picture label to be the correct thing
        if(perspectiveTwo)
            p.setPictureLabel(p.getString(R.string.side_label));
        else
            p.setPictureLabel(p.getString(R.string.top_label));

        if(params == null) {
            params = getPhotoParams(background);
            background = Bitmap.createScaledBitmap(background, params[0], params[1], false);
        }

        canvas.drawBitmap(background, new Rect(0, 0, background.getWidth(), background.getHeight()), new Rect(0, 0, background.getWidth(), background.getHeight()), new Paint());

        //draw each of the lines
        for(int i = 0; i+1 < getCurrentEndpoints().size(); i += 2)
            if ((getCurrentEndpoints().get(i).x != 0 && getCurrentEndpoints().get(i).y != 0) || (getCurrentEndpoints().get(i + 1).x != 0 && getCurrentEndpoints().get(i + 1).y != 0))
                canvas.drawLine(getCurrentEndpoints().get(i).x, getCurrentEndpoints().get(i).y, getCurrentEndpoints().get(i+1).x, getCurrentEndpoints().get(i+1).y, drawPaint);

        //if we are in the 2nd view
        if((status == lineDrawnStatus.threeLines) && isReady)
        {
            canvas.drawBitmap(background, new Rect(0, 0, background.getWidth(), background.getHeight()), new Rect(0, 0, background.getWidth(), background.getHeight()), new Paint());
            //canvas.drawPath(paths.get(paths.size() - 1), drawPaint);
            return;
        }

        //canvas.drawPath(drawPath, drawPaint);

        if((status == lineDrawnStatus.twoLines) && isReady)
            return;

        /*for (Path p : paths)
            canvas.drawPath(p, drawPaint);*/
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float pointX = event.getX();
        float pointY = event.getY();

        int count = 0;

        // Checks for the event that occurs
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Starts a new line in the path
                drawPath.moveTo(pointX, pointY);

                //gets the first point of the line;
                x1 = pointX;
                y1 = pointY;

                temp = currentPoint;
                //if we are inside a line's "touch" zone
                for (Point p : getCurrentEndpoints()) {
                    if (Math.sqrt(Math.pow(p.x - x1, 2) + Math.pow(p.y - y1, 2)) < radius * 4) {
                        moving = true;
                        currentPoint = getCurrentEndpoints().indexOf(p);
                        break;
                    }
                }

                for(Point p : getCurrentEndpoints()) {
                    if(p.x != 0 && p.y != 0) {
                        count++;
                    }

                    if(count == 4)
                        return true;
                }

                Log.d("PaintingView", "currentPoint " + currentPoint);

                //if the current point we are on is too large,  dont do this stuff otherwise oob exception will be thrown
                if(currentPoint > getCurrentEndpoints().size() - 1)
                    break;

                getCurrentEndpoints().get(currentPoint).x = (int) x1;
                getCurrentEndpoints().get(currentPoint).y = (int) y1;
                return true;

            case MotionEvent.ACTION_MOVE:
                //check to see if all the lines have been drawn
                if(currentPoint > getCurrentEndpoints().size() - 1 || pointX <= 0 || pointY <= 0)
                    break;

                // Draws line between last point and this point
                drawPath.lineTo(pointX, pointY);

                if(moving) {
                    getCurrentEndpoints().get(currentPoint).x = (int) pointX;
                    getCurrentEndpoints().get(currentPoint).y = (int) pointY;
                    switch(currentPoint) {
                        case 0:
                            drawCanvas.drawLine(getCurrentEndpoints().get(currentPoint).x, getCurrentEndpoints().get(currentPoint).y, getCurrentEndpoints().get(currentPoint + 1).x, getCurrentEndpoints().get(currentPoint + 1).y, drawPaint);
                            break;
                        case 1:
                            drawCanvas.drawLine(getCurrentEndpoints().get(currentPoint - 1).x, getCurrentEndpoints().get(currentPoint - 1).y, getCurrentEndpoints().get(currentPoint).x, getCurrentEndpoints().get(currentPoint).y, drawPaint);
                            break;
                        case 2:
                            drawCanvas.drawLine(getCurrentEndpoints().get(currentPoint).x, getCurrentEndpoints().get(currentPoint).y, getCurrentEndpoints().get(currentPoint + 1).x, getCurrentEndpoints().get(currentPoint + 1).y, drawPaint);
                            break;
                        case 3:
                            drawCanvas.drawLine(getCurrentEndpoints().get(currentPoint - 1).x, getCurrentEndpoints().get(currentPoint-1).y, getCurrentEndpoints().get(currentPoint).x, getCurrentEndpoints().get(currentPoint).y, drawPaint);
                            break;
                        default:
                            break;
                    }

                    break;
                }

                if (currentPoint % 2 == 0) {
                    getCurrentEndpoints().get(currentPoint + 1).x = (int) pointX;
                    getCurrentEndpoints().get(currentPoint + 1).y = (int) pointY;
                    drawCanvas.drawLine(getCurrentEndpoints().get(currentPoint).x, getCurrentEndpoints().get(currentPoint).y, getCurrentEndpoints().get(currentPoint + 1).x, getCurrentEndpoints().get(currentPoint + 1).y, drawPaint);
                } else {
                    getCurrentEndpoints().get(currentPoint - 1).x = (int) pointX;
                    getCurrentEndpoints().get(currentPoint - 1).y = (int) pointY;
                    drawCanvas.drawLine(getCurrentEndpoints().get(currentPoint).x, getCurrentEndpoints().get(currentPoint).y, getCurrentEndpoints().get(currentPoint - 1).x, getCurrentEndpoints().get(currentPoint - 1).y, drawPaint);
                }

                break;

            case MotionEvent.ACTION_UP:
                //statement to decide weather to fill in line 1 or line 2
                //draws a line from where the user puts their finger down to where they lifted it up
                // switch(status)
                //{
                //case nolines:

                //if the current point we are on is too large,  dont do this stuff otherwise oob exception will be thrown
                //also check if the point is out of bounds
                if(currentPoint > getCurrentEndpoints().size() - 1|| pointX <= 0 || pointY <= 0)
                    break;

                if(moving) {
                    getCurrentEndpoints().get(currentPoint).x = (int) pointX;
                    getCurrentEndpoints().get(currentPoint).y = (int) pointY;
                    getCurrentPointHistory().add(clone(getCurrentEndpoints()));
                    switch(currentPoint) {
                        case 0:
                            drawCanvas.drawLine(getCurrentEndpoints().get(currentPoint).x, getCurrentEndpoints().get(currentPoint).y, getCurrentEndpoints().get(currentPoint + 1).x, getCurrentEndpoints().get(currentPoint + 1).y, drawPaint);
                            break;
                        case 1:
                            drawCanvas.drawLine(getCurrentEndpoints().get(currentPoint).x, getCurrentEndpoints().get(currentPoint).y, getCurrentEndpoints().get(currentPoint - 1).x, getCurrentEndpoints().get(currentPoint - 1).y, drawPaint);
                            break;
                        case 2:
                            drawCanvas.drawLine(getCurrentEndpoints().get(currentPoint).x, getCurrentEndpoints().get(currentPoint).y, getCurrentEndpoints().get(currentPoint + 1).x, getCurrentEndpoints().get(currentPoint + 1).y, drawPaint);
                            break;
                        case 3:
                            drawCanvas.drawLine(getCurrentEndpoints().get(currentPoint).x, getCurrentEndpoints().get(currentPoint).y, getCurrentEndpoints().get(currentPoint - 1).x, getCurrentEndpoints().get(currentPoint - 1).y, drawPaint);
                            break;
                        default:
                            break;
                    }
                    moving = false;
                    currentPoint = temp;
                    break;
                }

                if (currentPoint % 2 == 0) {
                    getCurrentEndpoints().get(currentPoint + 1).x = (int) pointX;
                    getCurrentEndpoints().get(currentPoint + 1).y = (int) pointY;
                    drawCanvas.drawLine(getCurrentEndpoints().get(currentPoint).x, getCurrentEndpoints().get(currentPoint).y, getCurrentEndpoints().get(currentPoint + 1).x, getCurrentEndpoints().get(currentPoint + 1).y, drawPaint);
                } else {
                    getCurrentEndpoints().get(currentPoint - 1).x = (int) pointX;
                    getCurrentEndpoints().get(currentPoint - 1).y = (int) pointY;
                    drawCanvas.drawLine(getCurrentEndpoints().get(currentPoint).x, getCurrentEndpoints().get(currentPoint).y, getCurrentEndpoints().get(currentPoint - 1).x, getCurrentEndpoints().get(currentPoint - 1).y, drawPaint);
                }

                //Log.d("PaintingView", "point1 " + x1 + "," + y1 + " point2 " + pointX + "," + pointY);

                paths.add(drawPath);
                status = lineDrawnStatus.oneLine;
                currentPoint = currentPoint == temp ? currentPoint+2 : temp;
                getCurrentPointHistory().add(clone(getCurrentEndpoints()));
                break;
                    /*case oneLine:
                        whyDoINeedThis = true;
                        line2 = new Line(x1, y1, pointX, pointY);
                        //Log.d("PaintingView", "point1 " + x1 + "," + y1 + " point2 " + pointX + "," + pointY);
                        endpoints1.get(2).x = (int)x1;
                        endpoints1.get(2).y = (int)x1;
                        endpoints1.get(3).x = (int)pointX;
                        endpoints1.get(3).y = (int)pointY;

                        drawCanvas.drawLine(endpoints1.get(2).x, endpoints1.get(2).y, endpoints1.get(3).x, endpoints1.get(3).y, drawPaint);
                        paths.add(drawPath);
                        status = lineDrawnStatus.twoLines;
                        PaintingActivity p = (PaintingActivity)this.getContext();

                        nextPerspective();
                        p.nextRectangle();
                        break;
                    case twoLines:
                        line3 = new Line(x1, y1, pointX, pointY);

                        endpoints2.get(0).x = (int)x1;
                        endpoints2.get(0).y = (int)x1;
                        endpoints2.get(1).x = (int)pointX;
                        endpoints2.get(1).y = (int)pointY;

                        drawCanvas.drawLine(endpoints2.get(0).x, endpoints2.get(0).y, endpoints2.get(1).x, endpoints2.get(1).y, drawPaint);

                        paths.add(drawPath);
                        status = lineDrawnStatus.threeLines;
                        allLines = true;

                    case threeLines:
                        break;
                    default:
                        break;
                }

                //drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.setFillType(Path.FillType.EVEN_ODD);
                drawPath = new Path();
            default:
                return true;*/
            //}
            //Check if we need to display the done button
            default:
            return true;
        }

        Boolean displayDoneButton = true;
        for(Point p : getCurrentEndpoints())
            if(p.x == 0 || p.y == 0)
                displayDoneButton = false;
        if(displayDoneButton)
            ((Button)(((PaintingActivity)getContext()).findViewById(R.id.pixelButton))).setVisibility(VISIBLE);

        invalidate();
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
        //fix here if inaccurate
        return new Line(endpoints1.get(0).x, endpoints1.get(0).y, endpoints1.get(1).x, endpoints1.get(1).y);
    }

    /*@return the second line drawn (b value)*/
    public Line getLine2()
    {
        return new Line(endpoints1.get(2).x, endpoints1.get(2).y, endpoints1.get(3).x, endpoints1.get(3).y);
    }

    /*@return the third line drawn (c value)*/
    public Line getLine3()
    {
        return new Line(endpoints2.get(0).x, endpoints2.get(0).y, endpoints2.get(1).x, endpoints2.get(1).y);
    }

    /*@return the background photo the user took */
    public Bitmap getGetPhoto()
    {
        return background;
    }
    //endregion

    /*Method that ACTUALLY does a deep copy of an Array list of points*/
    public static ArrayList<Point> clone(ArrayList<Point> list) {
        ArrayList<Point> ret = new ArrayList<>();
        for(Point p : list)
            ret.add(new Point(p.x, p.y));
        return ret;
    }

    private ArrayList<Point> getCurrentEndpoints() {
        return perspectiveTwo ? endpoints2 : endpoints1;
    }

    private void setCurrentEndpoints(ArrayList<Point> p) {
        if(perspectiveTwo)
            endpoints2 = new ArrayList<Point>(p);
        else
            endpoints1 = new ArrayList<Point>(p);
    }

    private ArrayList<ArrayList<Point>> getCurrentPointHistory() {
        return perspectiveTwo ? pointHistory1 : pointHistory;
    }

}
