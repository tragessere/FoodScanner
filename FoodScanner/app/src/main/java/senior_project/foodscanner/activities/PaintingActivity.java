//CREDIT CARD HEIGHT 2.125"
//CREDIT CARD WIDTH 3.375"

package senior_project.foodscanner.activities;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import senior_project.foodscanner.DrawView;
import senior_project.foodscanner.PaintingView;
import senior_project.foodscanner.ImageDirectoryManager;
import senior_project.foodscanner.R;
import senior_project.foodscanner.ui.components.tutorial.TutorialBaseActivity;
import senior_project.foodscanner.ui.components.tutorial.TutorialCard;

public class PaintingActivity extends TutorialBaseActivity
{
    //region Variable Declaration
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static Button getPixelsButton;
    private static Button rectangleButton;
    private static Button undoButton;
    private PaintingView p;
    private DisplayMetrics metrics;
    private RectF rectangle;
    private double pixelsPerInch;
    private double a, b, c, volume;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_painting);

        getPixelsButton = (Button)findViewById(R.id.pixelButton);
        rectangleButton = (Button)findViewById(R.id.rectangleButton);

        p = (PaintingView)findViewById(R.id.paintingView);
        metrics = getApplicationContext().getResources().getDisplayMetrics();
        p.setBitmap(metrics.widthPixels, metrics.heightPixels);

        setupPixelsButton(false);
        setupRectangleButton();
        setupUndoButton();
    }

    @Override
    public void setupTutorial() {
        TutorialCard page1 = new TutorialCard(p, getString(R.string.tutorial_painting_title), getString(R.string.tutorial_painting_card)).setHighlightPadding(-20);
        TutorialCard page2 = new TutorialCard(p, getString(R.string.tutorial_painting_title), getString(R.string.tutorial_painting_food)).setHighlightPadding(-20);
        TutorialCard page3 = new TutorialCard(p, getString(R.string.tutorial_painting_title), getString(R.string.tutorial_painting_second_picture)).setHighlightPadding(-20);

        sequence.addCard(page1);
        sequence.addCard(page2);
        sequence.addCard(page3);
    }

    private void setupRectangleButton()
   {
       rectangleButton.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
               rectangleButton.setVisibility(View.INVISIBLE);
               getPixelsButton.setVisibility(View.VISIBLE);
               undoButton.setVisibility(View.VISIBLE);

               findViewById(R.id.rectangleView).setVisibility(View.INVISIBLE);
               findViewById(R.id.paintingView).setVisibility(View.VISIBLE);

               DrawView rectangleView = (DrawView) findViewById(R.id.rectangleView);
               rectangle = rectangleView.getRectangle();

               pixelsPerInch = Math.min(rectangle.height(), rectangle.width()) / 2.125;
               Toast.makeText(PaintingActivity.this, "Rectangle Height: " + rectangle.height() + " Rectangle Width: " + rectangle.width() + "\nPixels Per Inch: " + Math.min(rectangle.height(), rectangle.width()) / 2.125, Toast.LENGTH_LONG).show();
           }
       });
    }

    private void setupUndoButton()
    {
        undoButton = (Button)findViewById(R.id.undoButton);
        undoButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                PaintingView p = (PaintingView)findViewById(R.id.paintingView);
                p.undoLast();
            }
        });
    }

    @Override
    public boolean backButtonPressed()
    {
        PaintingView p = (PaintingView)findViewById(R.id.paintingView);
        p.undoLast();
        return true;
    }

    //TODO: make sure to not allow user to press this button before they place 2 lines
    private void setupPixelsButton(boolean first)
    {
        getPixelsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PaintingView p = (PaintingView) findViewById(R.id.paintingView);
                if (p.getLine1() == null || p.getLine2() == null || p.getLine3() == null)
                    return;
                a = p.getLine1().getLength() / pixelsPerInch;
                b = p.getLine2().getLength() / pixelsPerInch;
                c = p.getLine3().getLength() / pixelsPerInch;
                volume = 4 / 3 * Math.PI * a * b * c;

                Toast.makeText(PaintingActivity.this, "a: " + a + " b: " + b + " c: " + c + " Approximate Volume: " + volume, Toast.LENGTH_SHORT).show();
                Intent volumeReturn = new Intent();
                volumeReturn.putExtra(PhotoTakerActivity.RESULT_VOLUME, volume);
                setResult(RESULT_OK, volumeReturn);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tutorial, menu);
        return true;
    }

    public Point setupDrawing()
    {
        Point p = new Point(metrics.widthPixels, metrics.heightPixels);
        return p;
    }

    public void nextRectangle()
    {
        PaintingView painting = (PaintingView)findViewById(R.id.paintingView);
        DrawView rectangle = (DrawView)findViewById(R.id.rectangleView);

        painting.setVisibility(View.INVISIBLE);
        rectangle.setVisibility(View.VISIBLE);

        rectangle.nextPerspective();
        rectangleButton.setVisibility(View.VISIBLE);
        getPixelsButton.setVisibility(View.INVISIBLE);
        undoButton.setVisibility(View.INVISIBLE);
    }

    public boolean lastRectangle(Boolean inPainting)
    {
        PaintingView painting = (PaintingView)findViewById(R.id.paintingView);
        DrawView rectangle = (DrawView)findViewById(R.id.rectangleView);

        painting.setVisibility(View.INVISIBLE);
        rectangle.setVisibility(View.VISIBLE);

        rectangleButton.setVisibility(View.VISIBLE);
        getPixelsButton.setVisibility(View.INVISIBLE);
        undoButton.setVisibility(View.INVISIBLE);

        if(inPainting)
            return rectangle.currentPerspective();

        else
            return rectangle.lastPerspective();
    }

    public void lastPaintingView()
    {
        PaintingView painting = (PaintingView)findViewById(R.id.paintingView);
        DrawView rectangle = (DrawView)findViewById(R.id.rectangleView);

        painting.setVisibility(View.VISIBLE);
        rectangle.setVisibility(View.INVISIBLE);

        rectangleButton.setVisibility(View.INVISIBLE);
        getPixelsButton.setVisibility(View.VISIBLE);
        undoButton.setVisibility(View.VISIBLE);

        painting.previousPerspective();
    }

    public File getImageDirectory()
    {
        return ImageDirectoryManager.getImageDirectory(getApplicationContext());
    }

    public boolean inRectangleView() {
        return ((DrawView)findViewById(R.id.rectangleView)).getVisibility() == View.VISIBLE ? true : false;
    }

}
