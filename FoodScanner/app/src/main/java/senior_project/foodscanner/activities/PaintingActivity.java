//CREDIT CARD HEIGHT 2.125"
//CREDIT CARD WIDTH 3.375"

package senior_project.foodscanner.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.RectF;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.Serializable;

import SeniorProject.foodscanner.DrawView;
import SeniorProject.foodscanner.PaintingView;
import senior_project.foodscanner.ImageDirectoryManager;
import senior_project.foodscanner.R;
import senior_project.foodscanner.ui.components.ImageBrowser;

public class PaintingActivity extends AppCompatActivity
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
    public void onBackPressed()
    {
        PaintingView p = (PaintingView)findViewById(R.id.paintingView);
        p.undoLast();
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
        getMenuInflater().inflate(R.menu.menu_painting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
