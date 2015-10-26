//CREDIT CARD HEIGHT 2.125"
//CREDIT CARD WIDTH 3.375"

package senior_project.foodscanner.activities;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import SeniorProject.foodscanner.DrawView;
import SeniorProject.foodscanner.paintingView;
import senior_project.foodscanner.R;

public class PaintingActivity extends AppCompatActivity
{
    //region Variable Declaration
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private Button getPixelsButton;
    private Button rectangleButton;
    private Button undoButton;
    private paintingView p;
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

        /*//Sets up the camera and saves an image to food_image1.jpg
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imagesFolder = new File(Environment.DIRECTORY_PICTURES);
        //TODO:  be able to create multiple images
        File image = new File(imagesFolder, "food_image1.jpg");
        fileUri = Uri.fromFile(image);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(cameraIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);*/


        getPixelsButton = (Button)findViewById(R.id.pixelButton);
        rectangleButton = (Button)findViewById(R.id.rectangleButton);

        p = (paintingView)findViewById(R.id.paintingView);
        metrics = getApplicationContext().getResources().getDisplayMetrics();
        p.setBitmap(metrics.widthPixels, metrics.heightPixels);
        setupPixelsButton();
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
                paintingView p = (paintingView)findViewById(R.id.paintingView);
                p.undoLast();
            }
        });
    }

    //TODO: make sure to not allow user to press this button before they place 2 lines
    private void setupPixelsButton()
    {
        getPixelsButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                paintingView p = (paintingView)findViewById(R.id.paintingView);

                a = p.getLine1().getLength()/pixelsPerInch;
                b = p.getLine2().getLength()/pixelsPerInch;
                c = p.getLine3().getLength()/pixelsPerInch;
                volume = 4/3 * Math.PI * a*b*c;

                Toast.makeText(PaintingActivity.this, "a: " + a + " b: " + b + " c: " + c + " Approximate Volume: " + volume, Toast.LENGTH_SHORT).show();
                p.nextPerspective();
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
}
