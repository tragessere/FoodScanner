package senior_project.foodscanner.ui.components.tutorial;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

/**
 * Created by Evan on 11/19/2015.
 */
public class HighlightView extends FrameLayout implements SpringListener {
	public static final int DRAW_MODE_ENTER = 0;
	public static final int DRAW_MODE_MOVE = 1;
	public static final int DRAW_MODE_EXIT = 2;
	private int drawMode;

	private static final int BACKGROUND_ALPHA_FINAL = 160;

	public static final int SPRING_TENSION_START = 225;
	public static final int SPRING_FRICTION_START = 20;
	private static final int SPRING_TENSION_MOVING = 110;
	private static final int SPRING_FRICTION_MOVING = 15;
	private boolean hasBounced;
	private Spring mSpring;

	private Bitmap mBitmap;
	private Canvas mCanvas;
	private PorterDuffXfermode cutout;
	private Paint mPaint;
	private Paint mEraser;

	private int[] prevCenter;
	private float prevRadius;

	private int[] destCenter;
	private float destRadius;

	private int[] centerStep;
	private float radiusStep;

	public HighlightView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setWillNotDraw(false);

		cutout = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.BLACK);

		mEraser = new Paint();
		mEraser.setColor(Color.BLACK);
		mEraser.setStyle(Paint.Style.FILL);
		mEraser.setXfermode(cutout);

		mSpring = SpringSystem.create().createSpring();
		mSpring.addListener(this);
		mSpring.setSpringConfig(new SpringConfig(SPRING_TENSION_START, SPRING_FRICTION_START));
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
		mCanvas = new Canvas(mBitmap);
	}

	public void enter(int[] center, float radius) {
		drawMode = DRAW_MODE_ENTER;
		destCenter = center;
		destRadius = radius;
		centerStep = center;
		hasBounced = false;

		mSpring.setCurrentValue(0);
		mSpring.setEndValue(1);
	}

	public void exit(float radius) {
		drawMode = DRAW_MODE_EXIT;
		hasBounced = false;
		prevRadius = destRadius;
		destRadius = radius;

		mSpring.setCurrentValue(0, false);
	}

	public void move(int[] newCenter, float newRadius) {
		drawMode = DRAW_MODE_MOVE;
		prevCenter = destCenter;
		prevRadius = destRadius;
		destCenter = newCenter;
		destRadius = newRadius;

		mSpring.setSpringConfig(new SpringConfig(SPRING_TENSION_MOVING, SPRING_FRICTION_MOVING));

		mSpring.setCurrentValue(0, false);
	}


	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mCanvas.drawPaint(mEraser);

		mPaint.setStyle(Paint.Style.FILL);
		mCanvas.drawPaint(mPaint);

		if(centerStep == null) {
			canvas.drawBitmap(mBitmap, 0, 0, mPaint);
		} else {
			mPaint.setXfermode(cutout);
			mCanvas.drawCircle(centerStep[0], centerStep[1], radiusStep, mPaint);
			mPaint.setXfermode(null);

			canvas.drawBitmap(mBitmap, 0, 0, mPaint);
		}
	}



	@Override
	public void onSpringUpdate(Spring spring) {
		float value = (float) spring.getCurrentValue();

		switch (drawMode) {
			case DRAW_MODE_ENTER:
				if(value >= 1)
					hasBounced = true;

				int currentAlpha;
				if(!hasBounced)
					currentAlpha = (int) (BACKGROUND_ALPHA_FINAL * value);
				else
					currentAlpha = BACKGROUND_ALPHA_FINAL;

				mPaint.setAlpha(currentAlpha);

				radiusStep = destRadius * value;

				break;
			case DRAW_MODE_MOVE:
				radiusStep = (destRadius - prevRadius) * value + prevRadius;

				centerStep[0] = (int) ((destCenter[0] - prevCenter[0]) * value + prevCenter[0]);
				centerStep[1] = (int) ((destCenter[1] - prevCenter[1]) * value + prevCenter[1]);

				break;
			case DRAW_MODE_EXIT:
				if(value >= 1)
					hasBounced = true;

				if(destRadius == 0) {
					if (hasBounced)
						currentAlpha = 0;
					else
						currentAlpha = (int) ((1 - value) * BACKGROUND_ALPHA_FINAL);

					mPaint.setAlpha(currentAlpha);
				}

				radiusStep = (destRadius - prevRadius) * value + prevRadius;

				break;
		}

		invalidate();
	}

	@Override
	public void onSpringAtRest(Spring spring) {

	}

	@Override
	public void onSpringActivate(Spring spring) {

	}

	@Override
	public void onSpringEndStateChange(Spring spring) {

	}
}
