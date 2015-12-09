package senior_project.foodscanner;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import senior_project.foodscanner.ColorBall;
import senior_project.foodscanner.R;
import senior_project.foodscanner.activities.PaintingActivity;
/**
 * Created by Jacob on 10/3/2015.
 */
public class DrawView extends View
{
    public final double CREDIT_CARD_HEIGHT  = 2.125;
    public final double CREDIT_CARD_WIDTH   = 3.375;


    private boolean top;

    private Point point1, point3;
    private Point point2, point4;
    private Point point5; //center point
    private float radius;
    private int screenW, screenH;
    private Bitmap background;
    private BitmapFactory.Options options;
    private File imageDirectory;
    private static RectF currentRectangle;
    private boolean moving = false;
    private boolean secondPerspective;
    private ArrayList<ColorBall> colorballs = new ArrayList < ColorBall > ();

    int groupId = -1;
    private int balID = 0;

    Paint paint, rectPaint;
    Canvas canvas;

    Resources res;
    Bitmap previousBackground;

    public DrawView(Context context)
    {
        super(context);

        top = true;
        secondPerspective = false;

        DisplayMetrics d = new DisplayMetrics();
        WindowManager manager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(d);

        screenW = d.widthPixels;
        screenH = d.heightPixels;

        paint = new Paint();
        rectPaint = new Paint();

        res = getResources();
        imageDirectory = ((PaintingActivity)getContext()).getImageDirectory();
        background = PaintingView.decodeFile(new File(imageDirectory.getPath() + "/Top.png"));

        int[] params = getPhotoParams(background);

        background = Bitmap.createScaledBitmap(background, params[0], params[1], false);

        setFocusable(true); // necessary for getting the touch events
        canvas = new Canvas();

        // setting the start point for the balls
        setupRectangle(context);
    }

    public DrawView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        top = true;
        secondPerspective = false;
        options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        imageDirectory = ((PaintingActivity)getContext()).getImageDirectory();
        background = PaintingView.decodeFile(new File(imageDirectory.getPath() + "/Top.png"));

        DisplayMetrics d = new DisplayMetrics();
        WindowManager manager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(d);

        screenW = d.widthPixels;
        screenH = d.heightPixels;

        paint = new Paint();
        rectPaint = new Paint();
        setFocusable(true); // necessary for getting the touch events
        canvas = new Canvas();
        // setting the start point for the balls
        setupRectangle(context);
    }

    public DrawView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        int[] params = getPhotoParams(background);
        background = Bitmap.createScaledBitmap(background, params[0], params[1], false);
        canvas.drawBitmap(background, new Rect(0, 0, background.getWidth(), background.getHeight()), new Rect(0, 0, background.getWidth(), background.getHeight()), new Paint());

        if(secondPerspective)
            ((TextView)((PaintingActivity)getContext()).findViewById(R.id.pictureLabel)).setText(((PaintingActivity)getContext()).getString(R.string.side_label));
        else
            ((TextView)((PaintingActivity)getContext()).findViewById(R.id.pictureLabel)).setText(((PaintingActivity) getContext()).getString(R.string.top_label));

        rectPaint.setAntiAlias(true);
        rectPaint.setDither(true);
        rectPaint.setColor(Color.GREEN);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeJoin(Paint.Join.ROUND);
        rectPaint.setStrokeWidth(5);

        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.parseColor("#55000000"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(5);

        canvas.drawPaint(paint);
        paint.setColor(Color.parseColor("#55FFFFFF"));


        //draw the rectangle before the colored balls
        currentRectangle = new RectF(Math.min(colorballs.get(0).getX(), colorballs.get(1).getX()) + radius,
                Math.min(colorballs.get(0).getY(), colorballs.get(3).getY()) + radius,
                Math.max(colorballs.get(0).getX(), colorballs.get(1).getX()) + radius,
                Math.max(colorballs.get(0).getY(), colorballs.get(3).getY()) + radius);

        canvas.drawRect(currentRectangle, rectPaint);

        // draw the balls on the canvas
        for (ColorBall ball: colorballs)
            canvas.drawBitmap(ball.getBitmap(), ball.getX(), ball.getY(), new Paint(Color.GREEN));

        radius = (float)colorballs.get(0).getWidthOfBall()/2;
    }

    // events when touching the screen
    public boolean onTouchEvent(MotionEvent event)
    {
        int eventaction = event.getAction();

        int X = (int) event.getX();
        int Y = (int) event.getY();
/*1 0
* 2 3*/
        switch (eventaction)
        {
            case MotionEvent.ACTION_DOWN:
                // touch down so check if the finger is on
                // a ball
                balID = -1;
                groupId = -1;

                for (ColorBall ball: colorballs)
                {
                    // check if inside the bounds of the ball (circle)
                    // get the center for the ball
                    int centerX = ball.getX() + ball.getWidthOfBall();
                    int centerY = ball.getY() + ball.getHeightOfBall();

                    // calculate the radius from the touch to the center of the ball
                    double radCircle = Math.sqrt((double)(((centerX - X) * (centerX - X)) + (centerY - Y) * (centerY - Y)));

                    if (radCircle < ball.getWidthOfBall()*2)
                    {
                        balID = ball.getID();
                        if (balID == 1 || balID == 3)
                        {
                            groupId = 2;
                            canvas.drawRect(point1.x, point3.y, point3.x, point1.y,
                                    paint);
                        }

                        //we are moving the 4 balls surrounding the center ball
                        else if(balID == 4)
                            moving = true;

                        else
                        {
                            groupId = 1;
                            canvas.drawRect(point2.x, point4.y, point4.x, point2.y,
                                    paint);
                        }

                        invalidate();
                        break;
                    }
                    invalidate();
                }

                break;

            case MotionEvent.ACTION_MOVE:

                //if we have a proper balid and we are not moving the center ball
                //for(ColorBall c : colorballs)
                    //Log.d("test", "ID: " + c.getID() + " X: " + c.getX() + " Y: " + c.getY());
                if (balID > -1 && !moving)
                {
                    //checks to see if you are trying to drag the balls off the window
                    if(X < 0)
                        X = 0 + colorballs.get(0).getWidthOfBall();

                    if(Y < getStatusBarHeight())
                        Y = getStatusBarHeight() + colorballs.get(0).getWidthOfBall();

                    if(X > getW())
                        X = getW() - colorballs.get(0).getWidthOfBall();

                    if(Y > getH())
                        Y = getH() - colorballs.get(0).getWidthOfBall();

                    paint.setColor(Color.CYAN);

                    setupBalls(X, Y);
                    colorballs.get(4).setX(Math.max(colorballs.get(0).getX(), colorballs.get(1).getX()) - Math.abs(colorballs.get(0).getX() - colorballs.get(1).getX()) / 2);
                    colorballs.get(4).setY(Math.max(colorballs.get(0).getY(), colorballs.get(3).getY()) - Math.abs(colorballs.get(0).getY() - colorballs.get(3).getY()) / 2);

                    //canvas.save();
                    //canvas.rotate(45);

                    canvas.drawRect(point1.x, point2.y, point2.x, point3.y,
                            paint);

                    //canvas.restore();
                    invalidate();
                }

                /*
                 *  0   1
                 *    4
                 *  2   3
                */
                //TODO: fix moving the box fast and moving box into white zone,  keep black dots from moving outside the borders

                //This is when we are moving the entire set of balls using ball 4
                else if (balID > -1 && moving)
                {
                    int horzGap = colorballs.get(1).getX() - colorballs.get(0).getX();
                    int vertGap = colorballs.get(3).getY() - colorballs.get(0).getY();
                    int diameter = colorballs.get(0).getWidthOfBall();

                    //off the top left of the screen
                    if(((X - (Math.abs(horzGap) / 2)) < 0) && ((Y - (Math.abs(vertGap) / 2)) < 0))
                        break;

                    //off the bottom left of the screen
                    else if(((Y + (Math.abs(vertGap) / 2)) > getHeight()- getStatusBarHeight()) && (((X - (Math.abs(horzGap) / 2)) < 0)))
                        break;

                    //off the bottom right hand side of the screen
                    else if((X + (Math.abs(horzGap) / 2) > getWidth()) && ((Y + (Math.abs(vertGap) / 2)) > getHeight()- getStatusBarHeight()))
                        break;

                    //off the top right of the screen
                    else if(((Y - (Math.abs(vertGap) / 2)) < 0) && ((X + (Math.abs(horzGap) / 2) > getWidth())))
                        break;

                    //off the left of the screen
                    else if((X - (Math.abs(horzGap) / 2)) < 0) {
                        horzGap = Math.max(colorballs.get(1).getX(), colorballs.get(0).getX()) + diameter;
                        colorballs.get(4).setX(horzGap / 2);
                        colorballs.get(4).setY(Y);
                        colorballs.get(0).setY(colorballs.get(4).getY() + vertGap / 2);
                        colorballs.get(1).setY(colorballs.get(4).getY() + vertGap / 2);
                        colorballs.get(2).setY(colorballs.get(4).getY() - vertGap / 2);
                        colorballs.get(3).setY(colorballs.get(4).getY() - vertGap / 2);
                        break;
                    }

                    //off the bottom of the screen
                    else if((Y + (Math.abs(vertGap) / 2)) > getHeight() - getStatusBarHeight()) {
                        vertGap = getHeight() - Math.min(colorballs.get(3).getY(), colorballs.get(0).getY()) - diameter - getStatusBarHeight();
                        colorballs.get(4).setX(X);
                        colorballs.get(4).setY(getHeight() - vertGap / 2 - getStatusBarHeight());
                        colorballs.get(0).setX(colorballs.get(4).getX() - horzGap / 2);
                        colorballs.get(1).setX(colorballs.get(4).getX() + horzGap / 2);
                        colorballs.get(2).setX(colorballs.get(4).getX() + horzGap / 2);
                        colorballs.get(3).setX(colorballs.get(4).getX() - horzGap / 2);
                        break;
                    }

                    //off the right hand side of the screen
                    else if((X + (Math.abs(horzGap) / 2) > getWidth())) {
                        horzGap = getWidth() - Math.min(colorballs.get(1).getX(), colorballs.get(0).getX()) - diameter;
                        colorballs.get(4).setX(getWidth() - horzGap/2);
                        colorballs.get(4).setY(Y);
                        colorballs.get(0).setY(colorballs.get(4).getY() + vertGap / 2);
                        colorballs.get(1).setY(colorballs.get(4).getY() + vertGap / 2);
                        colorballs.get(2).setY(colorballs.get(4).getY() - vertGap / 2);
                        colorballs.get(3).setY(colorballs.get(4).getY() - vertGap / 2);
                        break;
                    }

                    //off the top of the screen
                    else if((Y - (Math.abs(vertGap) / 2)) < 0) {
                        vertGap = Math.max(colorballs.get(3).getY(), colorballs.get(0).getY()) - diameter;
                        colorballs.get(4).setX(X);
                        colorballs.get(4).setY(vertGap/2);
                        colorballs.get(0).setX(colorballs.get(4).getX() - horzGap / 2);
                        colorballs.get(1).setX(colorballs.get(4).getX() + horzGap / 2);
                        colorballs.get(2).setX(colorballs.get(4).getX() + horzGap / 2);
                        colorballs.get(3).setX(colorballs.get(4).getX() - horzGap / 2);
                        break;
                    }

                    colorballs.get(balID).setX(X);
                    colorballs.get(balID).setY(Y);
                    colorballs.get(0).setX(colorballs.get(4).getX() - horzGap / 2);
                    colorballs.get(0).setY(colorballs.get(4).getY() + vertGap / 2);
                    colorballs.get(1).setX(colorballs.get(4).getX() + horzGap / 2);
                    colorballs.get(1).setY(colorballs.get(4).getY() + vertGap / 2);
                    colorballs.get(2).setX(colorballs.get(4).getX() + horzGap / 2);
                    colorballs.get(2).setY(colorballs.get(4).getY() - vertGap / 2);
                    colorballs.get(3).setX(colorballs.get(4).getX() - horzGap / 2);
                    colorballs.get(3).setY(colorballs.get(4).getY() - vertGap / 2);
                }

                break;

            case MotionEvent.ACTION_UP:
                moving = false;
                break;
        }
        // redraw the canvas
        invalidate();
        return true;

    }

    private void sortBalls() {
        int maxX, maxY, minX, minY;
        maxX = minX = 0;
        maxY = minY = 0;
        Point p1, p2, p3, p4, p5;
        p1 = new Point();
        p2 = new Point();
        p3 = new Point();
        p4 = new Point();
        p5 = new Point();

        for(ColorBall c : colorballs) {
            if(c.getX() > maxX)
                maxX = c.getX();
            if(c.getY() > maxY)
                maxY = c.getY();
            if(c.getX() < minX)
                minX = c.getX();
            if(c.getY() < minY)
                minY = c.getY();
        }

        for(ColorBall c : colorballs) {
            if(c.getX() == maxX) {
                if(c.getY() == minY)
                    p2 = c.point;

                if(c.getY() == maxY)
                    p4 = c.point;
            }
            else if(c.getY() == maxY) {
                if(c.getX() == minX)
                    p3 = c.point;
            }
            else if(c.getX() == minX) {
                if (c.getY() == minY)
                    p1 = c.point;
            }

            else
                p5 = c.point;
        }

        colorballs.get(0).point = p1;
        colorballs.get(1).point = p2;
        colorballs.get(2).point = p3;
        colorballs.get(3).point = p4;
        colorballs.get(4).point = p5;

        colorballs.get(0).id = 1;
        colorballs.get(1).id = 2;
        colorballs.get(2).id = 3;
        colorballs.get(3).id = 4;
        colorballs.get(4).id = 0;
    }

    /*
     *  0   1
     *    4
     *  2   3
    */
    //TODO: THIS is what is causing issues
    private void setupBalls(int x, int y) {
        //Log.d("drawing", "Ball ID: " + balID);

        Log.d("drawing", "BalID: " + balID);
        for(ColorBall c : colorballs)
            Log.d("drawing", "setup START : balID: " + c.getID() + " X: " + c.getX() + " Y: " + c.getY() + "\n");

        colorballs.get(balID).setX(x);

        if (balID == 0) {
            colorballs.get(balID).setY(colorballs.get(4).getY() - ((colorballs.get(4).getX() - x) * (-CREDIT_CARD_HEIGHT / CREDIT_CARD_WIDTH)));
            colorballs.get(1).setX(colorballs.get(4).getX() + colorballs.get(4).getX() - colorballs.get(0).getX());
            colorballs.get(1).setY(colorballs.get(0).getY());
            colorballs.get(2).setX(colorballs.get(0).getX());
            colorballs.get(2).setY(colorballs.get(4).getY() + (colorballs.get(4).getY() - colorballs.get(0).getY()));
            colorballs.get(3).setX(colorballs.get(1).getX());
            colorballs.get(3).setY(colorballs.get(2).getY());
        }

        else if(balID == 3) {
            colorballs.get(balID).setY(colorballs.get(4).getY() + ((x - colorballs.get(4).getX()) * (-CREDIT_CARD_HEIGHT / CREDIT_CARD_WIDTH)));
            colorballs.get(1).setX(colorballs.get(3).getX());
            colorballs.get(1).setY(colorballs.get(4).getY() - (colorballs.get(3).getY() - colorballs.get(4).getY()));
            colorballs.get(2).setX(colorballs.get(4).getX() - (colorballs.get(3).getX() - colorballs.get(4).getX()));
            colorballs.get(2).setY(colorballs.get(3).getY());
            colorballs.get(0).setX(colorballs.get(2).getX());
            colorballs.get(0).setY(colorballs.get(1).getY());
        }

        else if(balID == 1) {
            colorballs.get(balID).setY(colorballs.get(4).getY() - ((x - colorballs.get(4).getX()) * (CREDIT_CARD_HEIGHT / CREDIT_CARD_WIDTH)));
            colorballs.get(0).setX(colorballs.get(4).getX() - (colorballs.get(1).getX() - colorballs.get(4).getX()));
            colorballs.get(0).setY(colorballs.get(1).getY());
            colorballs.get(2).setX(colorballs.get(0).getX());
            colorballs.get(2).setY(colorballs.get(4).getY() + (colorballs.get(4).getY() - colorballs.get(0).getY()));
            colorballs.get(3).setX(colorballs.get(1).getX());
            colorballs.get(3).setY(colorballs.get(2).getY());
        }

        else if(balID == 2) {
            colorballs.get(balID).setY(colorballs.get(4).getY() +  ((colorballs.get(4).getX() - x) * CREDIT_CARD_HEIGHT / CREDIT_CARD_WIDTH));
            colorballs.get(1).setX(colorballs.get(4).getX() + colorballs.get(4).getX() - colorballs.get(2).getX());
            colorballs.get(1).setY(colorballs.get(4).getY() - (colorballs.get(2).getY() - colorballs.get(4).getY()));
            colorballs.get(3).setX(colorballs.get(1).getX());
            colorballs.get(3).setY(colorballs.get(2).getY());
            colorballs.get(0).setX(colorballs.get(2).getX());
            colorballs.get(0).setY(colorballs.get(1).getY());
        }

        for(ColorBall c : colorballs)
            Log.d("drawing", "setup FINISH balID2: " + c.getID() + " X2: " + c.getX() + " Y2: " + c.getY() + "\n");
    }

    public void nextPerspective()
    {
        previousBackground = background.copy(Bitmap.Config.ARGB_8888, true);
        secondPerspective = true;
        background.recycle();
        background = PaintingView.decodeFile(new File(imageDirectory.getPath() + "/Side.png"));
        resetRectangle(getContext());
    }

    public boolean lastPerspective()
    {
        secondPerspective = false;
        if(previousBackground != null) {
            background.recycle();
            background = previousBackground.copy(Bitmap.Config.ARGB_8888, true);
            resetRectangle(getContext());
            return true;
        }

        else
            return false;
    }

    public boolean currentPerspective()
    {
        if(background != null) {
            resetRectangle(getContext());
            return true;
        }

        else
            return false;
    }

    /*
     *  0   1
     *    4
     *  2   3
    */
    private void setupRectangle(Context context)
    {
        //TODO: in the future maybe include white space
        //(h-s) is the size of the view minus the status bar.  I am currently not including the whitespace in this calculation
        int w = getW()/2;
        int h = getH()/2;
        int s = getStatusBarHeight();

        point1 = new Point();
        point1.x = (int)(w - 75);
        point1.y = (int)(h/2 + s + ((75) * (CREDIT_CARD_HEIGHT/CREDIT_CARD_WIDTH)));

        point2 = new Point();
        point2.x = (int)(w + 75);
        point2.y = (int)(h/2 + s + ((75) * CREDIT_CARD_HEIGHT/CREDIT_CARD_WIDTH));

        point3 = new Point();
        point3.x = (int)(w - 75);
        point3.y = (int)(h/2 + s - ((75) * CREDIT_CARD_HEIGHT/CREDIT_CARD_WIDTH));

        point4 = new Point();
        point4.x = (int)(w + 75);
        point4.y = (int)(h/2 + s - ((75) * (CREDIT_CARD_HEIGHT/CREDIT_CARD_WIDTH)));

        point5 = new Point();
        point5.x = (w);
        point5.y = (h/2 + s);

        colorballs.add(new ColorBall(context, R.drawable.circle, point1));
        colorballs.add(new ColorBall(context, R.drawable.circle, point2));
        colorballs.add(new ColorBall(context, R.drawable.circle, point3));
        colorballs.add(new ColorBall(context, R.drawable.circle, point4));
        colorballs.add(new ColorBall(context, R.drawable.circle, point5));

        colorballs.get(0).id = 0;
        colorballs.get(1).id = 1;
        colorballs.get(2).id = 2;
        colorballs.get(3).id = 3;
        colorballs.get(4).id = 4;

        for(ColorBall c : colorballs)
            Log.d("drawing", "rectangle START : balID: " + c.getID() + " X: " + c.getX() + " Y: " + c.getY() + "\n");
    }

    private void resetRectangle(Context context)
    {
        colorballs.clear();
        setupRectangle(context);
        invalidate();
    }

    //Thanks Dan!
    /*Takes in a bitmap and gets the correct metrics to fit it best to the view*/
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
            w = this.getWidth();
            h = (int) (w / ratio);
        } else {
            h = this.getHeight();
            w = (int) (h * ratio);
        }

        return new int[] {w, h};
    }
    public RectF getRectangle()
    {
        return new RectF(Math.min(Math.min(colorballs.get(0).getX(), colorballs.get(1).getX()), colorballs.get(2).getX()),
                Math.min(Math.min(colorballs.get(0).getY(), colorballs.get(1).getY()), colorballs.get(2).getY()),
                         Math.max(Math.max(colorballs.get(0).getX(), colorballs.get(1).getX()), colorballs.get(2).getX()),
                         Math.max(Math.max(colorballs.get(0).getY(), colorballs.get(1).getY()), colorballs.get(2).getY()));
    }

    public int getW()
    {
        return screenW;
    }

    public int getH()
    {
        return screenH;
    }

    public boolean onTop() {
        return top;
    }

    public void setOnTop(Boolean b) {
        top = b;
    }

    //http://stackoverflow.com/questions/3355367/height-of-statusbar/3356263#3356263
    /*Gets the height in pixels of the status bar*/
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

}
