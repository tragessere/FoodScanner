package senior_project.foodscanner.ui.components.tutorial;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
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
	View view;
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

	//Animation translation positions
	private float startPosX;
	private float startPosY;
	private float endPosX;
	private float endPosY;

	//Animation scale values
	private float previousHeight = 0;
	private float previousWidth = 0;
	private float nextHeight;
	private float nextWidth;

	private float startScaleX;
	private float startScaleY;
	private float endScaleX;
	private float endScaleY;


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

		view = View.inflate(mActivity, R.layout.tutorial_page, activityRoot);

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
		if(currentPosition == allPages.size()) {
			animateEnd();
		} else {
			if(currentPosition != 0) {
				previousHeight = tutorialCard.getHeight();
				previousWidth = tutorialCard.getWidth();
			}
			
			TutorialCard currentPage = allPages.get(currentPosition);

			title.setText(currentPage.getTitle());
			subTitle.setText(currentPage.getSubTitle());
			message.setText(currentPage.getMessage());

			if(currentPosition == 0)
				animateStart(currentPage);
			else
				animateBetween();
		}

	}

	private void previousPosition() {

	}

	private void animateStart(TutorialCard currentPage) {

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

		boolean positionTop;
		//Use the overridden screen location if set
		if(currentPage.getPosition() != TutorialCard.POSITION_DEFAULT)
			positionTop = currentPage.getPosition() == TutorialCard.POSITION_TOP;
		else	//Check where the highlighted view is and choose to put the tutorial card on the opposite side
			positionTop = startPosY > screenHeight / 2;

		endPosX = (screenWidth / 2) - (endSize[0] / 2) + margin;
		if(positionTop)
			endPosY = margin;
		else
			endPosY = screenHeight - endSize[1] - margin;

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

	private void animateEnd() {

	}

	private void animateBetween() {

	}

	private int[] setCardViews(TutorialCard currentPage) {
		title.setText(currentPage.getTitle());
		subTitle.setText(currentPage.getSubTitle());
		message.setText(currentPage.getMessage());

		cardContents.measure(View.MeasureSpec.makeMeasureSpec(activityRoot.getWidth(), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(activityRoot.getHeight(), View.MeasureSpec.AT_MOST));
		return new int[]{cardContents.getMeasuredWidth(), cardContents.getMeasuredHeight()};
	}


	@Override
	public void onSpringUpdate(Spring spring) {
		float value = (float) spring.getCurrentValue();
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
