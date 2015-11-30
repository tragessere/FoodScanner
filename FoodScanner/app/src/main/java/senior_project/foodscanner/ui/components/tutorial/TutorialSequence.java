package senior_project.foodscanner.ui.components.tutorial;

import android.graphics.Rect;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

import java.util.ArrayList;
import java.util.List;

import senior_project.foodscanner.R;

/**
 * Created by Evan on 11/18/2015.
 */
public class TutorialSequence implements SpringListener {
	public static final int SPRING_TENSION = 145;
	public static final int SPRING_FRICTION = 18;
	private Spring mSpring;

	private AppCompatActivity mActivity;

	private ViewGroup activityRoot;
	private HighlightView background;
	private CardView tutorialCard;
	private View cardContents;
	private TextView title;
	private TextView subTitle;
	private TextView message;
	private Button buttonBack;
	private Button buttonNext;

	private List<TutorialCard> allPages;
	private int currentPosition = -1;

	private float parentTop;
	private float margin;

	//Animation positions for the tutorial card
	private float startPosX;
	private float startPosY;
	private float endPosX;
	private float endPosY;

	private float startScaleX;
	private float startScaleY;
	private float endScaleX;
	private float endScaleY;

	private boolean exitWhenFinished = false;


	/**
	 * Creates a new guided tutorial. This tutorial will play through all the given pages which
	 * can each highlight a different view
	 *
	 * @param activity	Activity where this tutorial will be shown
	 */
	public TutorialSequence(AppCompatActivity activity) {
		mActivity = activity;
		allPages = new ArrayList<>();

		mSpring = SpringSystem.create().createSpring();
		mSpring.addListener(this);
		mSpring.setSpringConfig(new SpringConfig(SPRING_TENSION, SPRING_FRICTION));
	}


	/**
	 * Adds another page of information to the tutorial
	 *
	 * @param newPage	<code>TutorialCard</code> that details the function of a view
	 */
	public void addCard(TutorialCard newPage) {
		allPages.add(newPage);
	}


	/**
	 * Begin the tutorial process
	 */
	public void Start() {
		activityRoot = (ViewGroup) ((ViewGroup) mActivity.findViewById(android.R.id.content)).getChildAt(0);
		int[] position = new int[2];
		activityRoot.getLocationOnScreen(position);
		parentTop = position[1];

		View view = View.inflate(mActivity, R.layout.tutorial_page, activityRoot);

		background = (HighlightView) view.findViewById(R.id.background);
		tutorialCard = (CardView) view.findViewById(R.id.tutorial_card);
		cardContents = view.findViewById(R.id.card_contents);
		title = (TextView) view.findViewById(R.id.title);
		subTitle = (TextView) view.findViewById(R.id.subtitle);
		message = (TextView) view.findViewById(R.id.message);
		buttonBack = (Button) view.findViewById(R.id.button_back);
		buttonNext = (Button) view.findViewById(R.id.button_next);

		margin = ((FrameLayout.LayoutParams) tutorialCard.getLayoutParams()).leftMargin;

		buttonBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				previousPosition();
			}
		});

		buttonNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				nextPosition();
			}
		});

		background.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Consume touch events
			}
		});

		nextPosition();
	}

	private void nextPosition() {
		currentPosition++;

		if(currentPosition >= allPages.size()) {
			animateEnd(allPages.get(allPages.size() - 1));
		} else {
			TutorialCard currentPage = allPages.get(currentPosition);

			if(currentPosition == 0)
				animateStart(currentPage);
			else
				animateBetween(currentPage);
		}

	}

	private void previousPosition() {
		currentPosition--;

		if(currentPosition <= -1)
			animateEnd(allPages.get(0));
		else
			animateBetween(allPages.get(currentPosition));
	}

	private void animateStart(TutorialCard currentPage) {
		Rect statusBarBorder = new Rect();
		mActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(statusBarBorder);
		float statusBarHeight;
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
			statusBarHeight = statusBarBorder.top;
		else
			statusBarHeight = 0;

		tutorialCard.setScaleY(0);
		tutorialCard.setScaleX(0);

		int[] endSize = setCardViews(currentPage);

		float screenHeight = activityRoot.getHeight();
		float screenWidth = activityRoot.getWidth();
		float viewHalfHeight = currentPage.getHighlightView().getHeight() / 2;
		float viewHalfWidth = currentPage.getHighlightView().getWidth() / 2;

		int[] viewCenter = new int[2];
		currentPage.getHighlightView().getLocationOnScreen(viewCenter);
		viewCenter[0] += viewHalfWidth;
		viewCenter[1] += viewHalfHeight;

		//Set center of the dialog box to be the center of the view being highlighted
		startPosX = viewCenter[0] - endSize[0] / 2;
		startPosY = viewCenter[1] - endSize[1] / 2;

		tutorialCard.setX(startPosX);
		tutorialCard.setY(startPosY);

		boolean positionTop = showTutorialInTopHalf(viewCenter[1], screenHeight, currentPage.getPosition());

		if(endSize[0] >= screenWidth)
			endPosX = margin;
		else
			endPosX = (screenWidth / 2) - (endSize[0] / 2);

		if(positionTop)
			endPosY = margin;
		else
			endPosY = screenHeight - endSize[1] - margin - statusBarHeight;

		startScaleX = 0;
		startScaleY = 0;
		endScaleX = 1;
		endScaleY = 1;

		AlphaAnimation cardAlpha = new AlphaAnimation(0, 1);
		cardAlpha.setDuration(100);
		cardAlpha.setFillAfter(true);
		tutorialCard.startAnimation(cardAlpha);

		float radius = (float) Math.sqrt(
				viewHalfWidth * viewHalfWidth +
				viewHalfHeight * viewHalfHeight);

		viewCenter[1] -= parentTop;
		background.enter(viewCenter, radius);

		mSpring.setCurrentValue(0);
		mSpring.setEndValue(1);
	}

	private void animateEnd(TutorialCard currentPage) {
		float screenHeight = activityRoot.getHeight();
		float screenWidth = activityRoot.getWidth();

		float cardHeight = tutorialCard.getHeight();

		startPosX = endPosX;
		startPosY = endPosY;
		endPosX = startPosX;

		if(startPosY < screenHeight / 2)
			endPosY = -cardHeight * 1.2f;
		else
			endPosY = screenHeight;


		startScaleX = 1;
		startScaleY = 1;
		endScaleX = 1;
		endScaleY = 1;


		int[] viewCenter = new int[2];
		currentPage.getHighlightView().getLocationOnScreen(viewCenter);
		viewCenter[0] += currentPage.getHighlightView().getWidth() / 2;
		viewCenter[1] += currentPage.getHighlightView().getHeight() / 2;

		float radius;
		if(currentPosition == -1)
			radius = 0;
		else
			radius = (float) Math.sqrt(
				screenWidth * screenWidth +
				screenHeight * screenHeight);

		viewCenter[1] -= parentTop;
		background.exit(radius);

		exitWhenFinished = true;
		mSpring.setCurrentValue(0, false);
	}

	private void animateBetween(final TutorialCard currentPage) {
		AlphaAnimation cardTextAlpha = new AlphaAnimation(1, 0);
		cardTextAlpha.setDuration(100);
		cardTextAlpha.setFillAfter(true);
		cardTextAlpha.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				Rect statusBarBorder = new Rect();
				mActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(statusBarBorder);
				float statusBarHeight;
				if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
					statusBarHeight = statusBarBorder.top;
				else
					statusBarHeight = 0;

				float screenHeight = activityRoot.getHeight();
				float screenWidth = activityRoot.getWidth();

				int[] viewCenter = new int[2];
				currentPage.getHighlightView().getLocationOnScreen(viewCenter);

				float viewHalfHeight = currentPage.getHighlightView().getHeight() / 2;
				float viewHalfWidth = currentPage.getHighlightView().getWidth() / 2;
				viewCenter[0] += viewHalfWidth;
				viewCenter[1] += viewHalfHeight;

				float startWidth = tutorialCard.getWidth();
				float startHeight = tutorialCard.getHeight();

				int[] endSize = setCardViews(currentPage);

				startScaleX = startWidth / endSize[0];
				startScaleY = startHeight / endSize[1];

				endScaleX = 1;
				endScaleY = 1;

				startPosX = endPosX;
				startPosY = endPosY;

				boolean positionTop = showTutorialInTopHalf(viewCenter[1], screenHeight, currentPage.getPosition());

				if(endSize[0] >= screenWidth)
					endPosX = margin;
				else
					endPosX = (screenWidth / 2) - (endSize[0] / 2);

				if (positionTop)
					endPosY = margin;
				else
					endPosY = screenHeight - endSize[1] - margin - statusBarHeight;

				AlphaAnimation cardAlpha = new AlphaAnimation(0, 1);
				cardAlpha.setDuration(150);
				cardAlpha.setFillAfter(true);
				cardContents.startAnimation(cardAlpha);

				float radius = (float) Math.sqrt(
						viewHalfWidth * viewHalfWidth +
								viewHalfHeight * viewHalfHeight);

				viewCenter[1] -= parentTop;
				background.move(viewCenter, radius);

				mSpring.setCurrentValue(0, false);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
		cardContents.startAnimation(cardTextAlpha);
	}

	private int[] setCardViews(TutorialCard currentPage) {
		title.setText(currentPage.getTitle());
		subTitle.setText(currentPage.getSubTitle());
		message.setText(currentPage.getMessage());

		cardContents.measure(View.MeasureSpec.makeMeasureSpec(activityRoot.getWidth(), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(activityRoot.getHeight(), View.MeasureSpec.AT_MOST));
		return new int[]{cardContents.getMeasuredWidth(), cardContents.getMeasuredHeight()};
	}

	private boolean showTutorialInTopHalf(float viewY, float screenHeight, int positionValue) {
		boolean positionTop;
		//Use the overridden screen location if set
		if(positionValue != TutorialCard.POSITION_DEFAULT)
			positionTop = positionValue == TutorialCard.POSITION_TOP;
		else	//Check where the highlighted view is and choose to put the tutorial card on the opposite side
			positionTop = viewY > screenHeight / 2;

		return positionTop;
	}


	@Override
	public void onSpringUpdate(Spring spring) {
		float value = (float) spring.getCurrentValue();

		if(exitWhenFinished && value >= 1) {
			spring.setAtRest();
			activityRoot.removeView(background);
		}

		tutorialCard.setScaleY((endScaleY - startScaleY) * value + startScaleY);
		tutorialCard.setScaleX((endScaleX - startScaleX) * value + startScaleX);

		float setX = (endPosX - startPosX) * value + startPosX;
		float setY = (endPosY - startPosY) * value + startPosY;
		tutorialCard.setX(setX);
		tutorialCard.setY(setY);
	}

	@Override
	public void onSpringAtRest(Spring spring) { }

	@Override
	public void onSpringActivate(Spring spring) { }

	@Override
	public void onSpringEndStateChange(Spring spring) { }
}
