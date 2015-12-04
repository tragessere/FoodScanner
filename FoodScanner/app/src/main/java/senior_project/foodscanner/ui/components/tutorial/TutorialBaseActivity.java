package senior_project.foodscanner.ui.components.tutorial;

import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import senior_project.foodscanner.R;

/**
 * Created by Evan on 12/1/2015.
 */
public abstract class TutorialBaseActivity extends AppCompatActivity {

	public TutorialSequence sequence;

	/**
	 * Create tutorial cards here and add them to the sequence.
	 */
	public abstract void setupTutorial();

	/**
	 * Standard handling for the method <code>onOptionsItemSelected</code>
	 *
	 * @return	<code>true</code> if the back button was handled, <code>false</code> otherwise.
	 */
	public boolean backButtonPressed() {
		return false;
	}

	/**
	 * Standard handling for the method <code>onOptionsItemSelected</code>
	 *
	 * @param item	Item in the toolbar that was selected
	 *
	 * @return	<code>true</code> if the action has been handled, <code>false</code> otherwise.
	 */
	public boolean optionsItemSelected(MenuItem item) {
		return false;
	}


	@Override
	protected void onResume() {
		super.onResume();
		if(sequence == null) {
			sequence = new TutorialSequence(this);
			setupTutorial();
		}
	}

	@Override
	public final boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.action_tutorial) {
			if(sequence.isActive())
				sequence.exitTutorial();
			else
				sequence.Start();
			return true;
		} else if(sequence.isActive()) {
			sequence.exitTutorial();
			if(optionsItemSelected(item))
				return true;
		} else {
			if(optionsItemSelected(item))
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public final void onBackPressed() {
		if(sequence.isActive())
			sequence.previousPosition();
		else if(!backButtonPressed())
			super.onBackPressed();
	}
}
