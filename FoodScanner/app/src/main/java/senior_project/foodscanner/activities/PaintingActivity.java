
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
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.Serializable;

import senior_project.foodscanner.DrawView;
import senior_project.foodscanner.PaintingView;
import senior_project.foodscanner.ImageDirectoryManager;
import senior_project.foodscanner.R;
import senior_project.foodscanner.ui.components.ImageBrowser;

public class PaintingActivity extends AppCompatActivity
{
    public final double CREDIT_CARD_HEIGHT  = 2.125;
    public final double CREDIT_CARD_WIDTH   = 3.370;

    //region Variable Declaration
    private static Button getPixelsButton;
    private static Button rectangleButton;
    private static Button undoButton;
    private PaintingView p;
    private DisplayMetrics metrics;
    private RectF rectangle;
    private double pixelsPerInch1, pixelsPerInch2;
    private double a, b, c, volume;
    private TextView pictureLabel;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_painting);

        getPixelsButton = (Button)findViewById(R.id.pixelButton);
        rectangleButton = (Button)findViewById(R.id.rectangleButton);

        pictureLabel = (TextView)findViewById(R.id.pictureLabel);

        //getActionBar().setTitle("Outline Card");
        getSupportActionBar().setTitle("Outline Card");

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
               DrawView rectangleView = (DrawView) findViewById(R.id.rectangleView);
               rectangle = rectangleView.getRectangle();


               if(rectangleView.onTop()) {
                   pixelsPerInch1 = rectangle.height() / CREDIT_CARD_HEIGHT;
                   rectangleView.nextPerspective();
                   rectangleView.setOnTop(false);
                   //Toast.makeText(PaintingActivity.this, "Rectangle Height: " + rectangle.height() + " Rectangle Width: " + rectangle.width() + "\nPixels Per Inch: " + Math.min(rectangle.height(), rectangle.width()) / 2.125, Toast.LENGTH_LONG).show();
               }

               else {
                   rectangleButton.setVisibility(View.INVISIBLE);
                   undoButton.setVisibility(View.VISIBLE);
                   findViewById(R.id.rectangleView).setVisibility(View.INVISIBLE);
                   findViewById(R.id.paintingView).setVisibility(View.VISIBLE);

                   //getActionBar().setTitle("Draw Lines A & B");
                   getSupportActionBar().setTitle("Draw Lines A & B");

                   pixelsPerInch2 = rectangle.height() / CREDIT_CARD_HEIGHT;
                   //Toast.makeText(PaintingActivity.this, "Rectangle Height: " + rectangle.height() + " Rectangle Width: " + rectangle.width() + "\nPixels Per Inch: " + Math.min(rectangle.height(), rectangle.width()) / 2.125, Toast.LENGTH_LONG).show();
               }
           }
       });
    }

    private void setupUndoButton()
    {
        undoButton = (Button)findViewById(R.id.undoButton);
        undoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PaintingView p = (PaintingView) findViewById(R.id.paintingView);
                p.undoLast();
            }
        });
    }

    private void setupPixelsButton(boolean first)
    {
        getPixelsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PaintingView p = (PaintingView) findViewById(R.id.paintingView);

                //have not finished drawing lines,  go to the next view
                if (p.getLine3().getLength() == 0) {
                    p.nextPerspective();
                    //getActionBar().setTitle("Draw Line C");
                    getPixelsButton.setVisibility(View.INVISIBLE);
                    getSupportActionBar().setTitle("Draw Line C");
                    return;
                }

                //otherwise compute the volume
                else {
                    a = p.getLine1().getLength() / pixelsPerInch1;
                    b = p.getLine2().getLength() / pixelsPerInch1;
                    c = p.getLine3().getLength() / pixelsPerInch2;

                    volume = 4.0 / 3.0 * Math.PI * a / 2.0 * b / 2.0 * c / 2.0;

                    //Toast.makeText(PaintingActivity.this, "a: " + a + " b: " + b + " c: " + c + " Approximate Volume: " + volume, Toast.LENGTH_SHORT).show();
                    Intent volumeReturn = new Intent();
                    volumeReturn.putExtra(PhotoTakerActivity.RESULT_VOLUME, volume);
                    setResult(RESULT_OK, volumeReturn);
                    finish();
                }
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

    //Should not need this anymore
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

    public void setPictureLabel(String s) {
        TextView v = (TextView)findViewById(R.id.pictureLabel);
        v.setText(s);
    }

}
