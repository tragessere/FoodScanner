package SeniorProject.foodscanner;

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
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;

import senior_project.foodscanner.R;
import senior_project.foodscanner.activities.PaintingActivity;

/**
 * Created by Jacob on 10/3/2015.
 */
public class DrawView extends View
{
    private Point point1, point3;
    private Point point2, point4;
    private Point point5; //center point
    private float radius;
    private int screenW, screenH;

    private Bitmap background;

    private static RectF currentRectangle;

    private boolean moving = false;

    private ArrayList< ColorBall > colorballs = new ArrayList < ColorBall > ();

    int groupId = -1;
    private int balID = 0;
    Paint paint, rectPaint;
    Canvas canvas;

    public DrawView(Context context)
    {
        super(context);

        paintingView p = (paintingView)findViewById(R.id.paintingView);

        DisplayMetrics d = new DisplayMetrics();
        WindowManager manager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(d);

        screenW = d.widthPixels;
        screenH = d.heightPixels;

        paint = new Paint();
        rectPaint = new Paint();

        Resources res = getResources();
        background = BitmapFactory.decodeResource(res, R.drawable.side);

        setFocusable(true); // necessary for getting the touch events
        canvas = new Canvas();
        // setting the start point for the balls
        point1 = new Point();
        point1.x = 50;
        point1.y = 30;

        point2 = new Point();
        point2.x = 200;
        point2.y = 30;

        point3 = new Point();
        point3.x = 200;
        point3.y = 180;

        point4 = new Point();
        point4.x = 50;
        point4.y = 180;

        point5 = new Point();
        point5.x = 125;
        point5.y = 105;

        colorballs.add(new ColorBall(context, R.drawable.circle, point1));
        colorballs.add(new ColorBall(context, R.drawable.circle, point2));
        colorballs.add(new ColorBall(context, R.drawable.circle, point3));
        colorballs.add(new ColorBall(context, R.drawable.circle, point4));
        colorballs.add(new ColorBall(context, R.drawable.circle, point5));
    }

    public DrawView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        Resources res = getResources();
        background = BitmapFactory.decodeResource(res, R.drawable.side);

        paintingView p = (paintingView)findViewById(R.id.paintingView);

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
        point1 = new Point();
        point1.x = 50;
        point1.y = 30;

        point2 = new Point();
        point2.x = 200;
        point2.y = 30;

        point3 = new Point();
        point3.x = 200;
        point3.y = 180;

        point4 = new Point();
        point4.x = 50;
        point4.y = 180;

        point5 = new Point();
        point5.x = 125;
        point5.y = 105;

        colorballs.add(new ColorBall(context, R.drawable.circle, point1));
        colorballs.add(new ColorBall(context, R.drawable.circle, point2));
        colorballs.add(new ColorBall(context, R.drawable.circle, point3));
        colorballs.add(new ColorBall(context, R.drawable.circle, point4));
        colorballs.add(new ColorBall(context, R.drawable.circle, point5));
    }

    public DrawView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        //canvas.drawColor(Color.TRANSPARENT);


        canvas.drawBitmap(background, new Rect(0,0,background.getWidth(), background.getHeight()), new Rect(0,0,1080, 1920), new Paint());

        rectPaint.setAntiAlias(true);
        rectPaint.setDither(true);
        rectPaint.setColor(Color.BLACK);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeJoin(Paint.Join.ROUND);
        rectPaint.setStrokeWidth(5);

        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.parseColor("#55000000"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        // mPaint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(5);

        canvas.drawPaint(paint);
        paint.setColor(Color.parseColor("#55FFFFFF"));

        BitmapDrawable mBitmap;
        mBitmap = new BitmapDrawable();

        // draw the balls on the canvas
        for (ColorBall ball: colorballs)
        {
            canvas.drawBitmap(ball.getBitmap(), ball.getX(), ball.getY(), new Paint());
        }

        radius = (float)colorballs.get(0).getWidthOfBall()/2;
        //left top right bottom

        currentRectangle = new RectF(Math.min(colorballs.get(0).getX(), colorballs.get(1).getX()) + radius,
                Math.min(colorballs.get(0).getY(), colorballs.get(3).getY()) + radius,
                Math.max(colorballs.get(0).getX(), colorballs.get(1).getX()) + radius,
                Math.max(colorballs.get(0).getY(), colorballs.get(3).getY()) + radius);

        canvas.drawRect(currentRectangle, rectPaint);
    }

    // events when touching the screen
    public boolean onTouchEvent(MotionEvent event)
    {
        int eventaction = event.getAction();

        int X = (int) event.getX();
        int Y = (int) event.getY();

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
                    paint.setColor(Color.CYAN);
                    // calculate the radius from the touch to the center of the ball
                    double radCircle = Math.sqrt((double)(((centerX - X) * (centerX - X)) + (centerY - Y) * (centerY - Y)));

                    if (radCircle < ball.getWidthOfBall())
                    {

                        balID = ball.getID();

                        if (balID == 1 || balID == 3)
                        {
                            groupId = 2;
                            canvas.drawRect(point1.x, point3.y, point3.x, point1.y,
                                    paint);
                        }

                        else if(balID == 4)
                        {
                            moving = true;
                        }

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
                // touch drag with the ball
                // move the balls the same as the finger
                if (balID > -1 && !moving)
                {
                    colorballs.get(balID).setX(X);
                    colorballs.get(balID).setY(Y);

                    paint.setColor(Color.CYAN);

                    if (groupId == 1) {
                        colorballs.get(1).setX(colorballs.get(2).getX());
                        colorballs.get(1).setY(colorballs.get(0).getY());
                        colorballs.get(3).setX(colorballs.get(0).getX());
                        colorballs.get(3).setY(colorballs.get(2).getY());
                        colorballs.get(4).setX(Math.max(colorballs.get(0).getX(), colorballs.get(1).getX()) - Math.abs(colorballs.get(0).getX() - colorballs.get(1).getX())/2);
                        colorballs.get(4).setY(Math.max(colorballs.get(0).getY(), colorballs.get(3).getY()) - Math.abs(colorballs.get(0).getY() - colorballs.get(3).getY())/2);
                        canvas.drawRect(point1.x, point3.y, point3.x, point1.y,
                                paint);
                    }

                    else {
                        colorballs.get(0).setX(colorballs.get(3).getX());
                        colorballs.get(0).setY(colorballs.get(1).getY());
                        colorballs.get(2).setX(colorballs.get(1).getX());
                        colorballs.get(2).setY(colorballs.get(3).getY());
                        colorballs.get(4).setX(Math.max(colorballs.get(0).getX(), colorballs.get(1).getX()) - Math.abs(colorballs.get(0).getX() - colorballs.get(1).getX())/2);
                        colorballs.get(4).setY(Math.max(colorballs.get(0).getY(), colorballs.get(3).getY()) - Math.abs(colorballs.get(0).getY() - colorballs.get(3).getY())/2);
                        canvas.drawRect(point2.x, point4.y, point4.x, point2.y,
                                paint);
                    }

                    invalidate();
                }

                /*
                 *  0   1
                 *    4
                 *  2   3
                */
                //TODO: make sure this does not go off the screen...
                else if (balID > -1 && moving)
                {
                    colorballs.get(4).setX(X);
                    colorballs.get(4).setY(Y);

                    int horzGap = colorballs.get(1).getX() - colorballs.get(0).getX();
                    int vertGap = colorballs.get(3).getY() - colorballs.get(0).getY();

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

    public static RectF getRectangle()
    {
        return currentRectangle;
    }

    public int getW()
    {
        return screenW;
    }

    public int getH()
    {
        return screenH;
    }

}
