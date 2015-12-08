package senior_project.foodscanner.ui.components.tutorial;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * Created by Evan on 11/18/2015.
 */
public class TutorialCard {
	public static final int DEFAULT_CARD_MARGIN_DP = 0;
	public static final int DEFAULT_HIGHLIGHT_PADDING_DP = 10;

	public static final int POSITION_DEFAULT = 0;
	public static final int POSITION_TOP  = 1;
	public static final int POSITION_BOTTOM = 2;
	private int cardPosition = POSITION_DEFAULT;
	private int cardMargin = DEFAULT_CARD_MARGIN_DP;

	public static final int HIGHLIGHT_CIRCLE = 0;
	public static final int HIGHLIGHT_RECTANGLE = 1;
	private int highlightShape = HIGHLIGHT_CIRCLE;
	private int highlightPadding = DEFAULT_HIGHLIGHT_PADDING_DP;


	private View highlightView;

	private String title;
	private String subTitle;
	private String message;
	private int imageResource = -1;

	public TutorialCard(View highlightView) {
		this.highlightView = highlightView;
	}

	public TutorialCard(View highlight, String title, String subTitle, String message) {
		highlightView = highlight;
		this.title = title;
		this.subTitle = subTitle;
		this.message = message;
	}

	public TutorialCard(View highlight, String title, String message) {
		highlightView = highlight;
		this.title = title;
		this.message = message;
	}

	public View getHighlightView() {
		return highlightView;
	}

	public TutorialCard setTitle(String title) {
		this.title = title;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public TutorialCard setSubTitle(String subTitle) {
		this.subTitle = subTitle;
		return this;
	}

	public String getSubTitle() {
		return subTitle;
	}

	public TutorialCard setMessage(String message) {
		this.message = message;
		return this;
	}

	public String getMessage() {
		return message;
	}

	public TutorialCard setImageResource(int resourceId) {
		imageResource = resourceId;
		return this;
	}

	public int getImageResource() {
		return imageResource;
	}

	public TutorialCard setHightlightShape(int shape) {
		if(!(shape >= HIGHLIGHT_CIRCLE && shape <= HIGHLIGHT_RECTANGLE))
			highlightShape = HIGHLIGHT_CIRCLE;
		else
			highlightShape = shape;

		return this;
	}

	public int getHighlightShape() {
		return highlightShape;
	}

	public TutorialCard setHighlightPadding(int paddingDP) {
		highlightPadding = paddingDP;
		return this;
	}

	public int getHighlightPadding() {
		return highlightPadding;
	}

	public int getHighlightPaddingPx() {
		DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
		return (int) (highlightPadding * (metrics.densityDpi / 160f));
	}

	public TutorialCard setPosition(int position) {
		if(!(position >= POSITION_DEFAULT && position <= POSITION_BOTTOM))
			cardPosition = POSITION_DEFAULT;
		else
			cardPosition = position;

		return this;
	}

	public int getPosition() {
		return cardPosition;
	}

	public TutorialCard setMargin(int marginDP) {
		cardMargin = marginDP;
		return this;
	}

	public int getCardMargin() {
		return cardMargin;
	}

	public int getCardMarginPx() {
		DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
		return (int) (cardMargin * (metrics.densityDpi / 160f));
	}

}
