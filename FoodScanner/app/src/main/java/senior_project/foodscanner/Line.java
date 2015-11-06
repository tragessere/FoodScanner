package senior_project.foodscanner;

/**
 * Created by Jacob on 10/4/2015.
 */
public class Line
{
    private float x1, x2, y1, y2;
    public Line(float X1, float Y1, float X2, float Y2)
    {
        x1 = X1;
        x2 = X2;
        y1 = Y1;
        y2 = Y2;
    }

    public double getLength()
    {
        return Math.sqrt(Math.pow((x1 - x2),2) + Math.pow((y1 - y2),2));
    }

    public boolean intersect()
    {
        return true;
    }


    public float getX1()
    {
        return x1;
    }

    public float getX2()
    {
        return x2;
    }

    public float getY1()
    {
        return y1;
    }

    public float getY2()
    {
        return y2;
    }
}
