package senior_project.foodscanner.ui.components;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;

import senior_project.foodscanner.R;

/**
 * Interactive view that allows browsing through a set number of images.
 */
public class ImageBrowser extends FrameLayout implements View.OnClickListener {
    private String[] imgNames;
    private ImageSwitcher imgSwitcher;
    private Context context;
    private FinishButtonListener fbl;
    private ActionButtonListener abl;
    private Drawable defaultImage;
    private Drawable[] images;
    private TextView textView_Name;
    private TextView textView_Index;
    private Button button_Action;
    private Button button_Finish;
    private ImageButton imgBt_Next;
    private ImageButton imgBt_Prev;

    private boolean isCyclic = false;
    private int currentIndex = 0;

    public ImageBrowser(Context context, String[] imgNames, Drawable defaultImage) {
        super(context);
        this.context = context;
        this.imgNames = imgNames;
        this.defaultImage = defaultImage;
        images = new Drawable[imgNames.length];
        inflate();
        setCurrentImage(currentIndex);
    }

    public ImageBrowser(Context context, String[] imgNames, Drawable defaultImage, Drawable[] images) {
        super(context);
        if(imgNames.length != images.length) {
            throw new IllegalArgumentException("Parameters 'imgNames' and 'images' are not the same length.");
        }
        this.context = context;
        this.imgNames = imgNames;
        this.defaultImage = defaultImage;
        this.images = images;
        inflate();
        setCurrentImage(currentIndex);
    }

    public int getNumImages() {
        return images.length;
    }

    public void setImage(int index, Drawable image) {
        images[index] = image;
        if(index == currentIndex){
            setCurrentImage(currentIndex);
        }
    }

    public Drawable getImage(int index) {
        return images[index];
    }

    public Drawable[] getImages() {
        return images;
    }

    public String[] getImageNames() {
        return imgNames;
    }

    public void setImageName(int index, String name) {
        imgNames[index] = name;
    }

    public String getImageName(int index) {
        return imgNames[index];
    }

    public void setIsCyclic(boolean b) {
        isCyclic = b;
        if(isCyclic) {
            imgBt_Next.setEnabled(true);
            imgBt_Prev.setEnabled(true);
        }
    }

    /**
     * Whether or not reaching the end of the browser loops around.
     *
     * @return
     */
    public boolean isCyclic() {
        return isCyclic;
    }


    private void inflate() {
        LayoutInflater lf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View browser = lf.inflate(R.layout.image_browser_layout, this, true);
        textView_Name = ((TextView) browser.findViewById(R.id.textView_title));
        textView_Name.setText(imgNames[currentIndex]);
        textView_Index = ((TextView) browser.findViewById(R.id.textView_count));
        textView_Index.setText("(" + (currentIndex + 1) + "/" + imgNames.length + ")");
        imgBt_Next = (ImageButton) browser.findViewById(R.id.imageButton_next);
        imgBt_Next.setOnClickListener(this);
        imgBt_Prev = (ImageButton) browser.findViewById(R.id.imageButton_prev);
        imgBt_Prev.setOnClickListener(this);
        button_Finish = (Button) browser.findViewById(R.id.button_finish);
        button_Finish.setOnClickListener(this);
        button_Action = (Button) browser.findViewById(R.id.button_action);
        button_Action.setOnClickListener(this);

        imgSwitcher = ((ImageSwitcher) browser.findViewById(R.id.imageSwitcher));
        imgSwitcher.addView(new ImageView(context));
        imgSwitcher.addView(new ImageView(context));
        //TODO animations
       // imgSwitcher.setInAnimation(context, android.R.anim.slide_in_left);
       // imgSwitcher.setOutAnimation(context, android.R.anim.slide_out_right);
    }

    public void setActionButtonText(String text) {
        button_Action.setText(text);
    }

    public void setCurrentImage(int index) {
        currentIndex = index;
        if(isCyclic) {
            if(currentIndex < 0) {
                currentIndex = getNumImages() + currentIndex % getNumImages();
            } else if(currentIndex > getNumImages()-1) {
                currentIndex = currentIndex % (getNumImages()-1);
            }
        }

        Drawable img = images[currentIndex];
        if(img == null){
            img = defaultImage;
        }
        if(img == null){
            Log.e("WTF","WTF");
        }
        if(imgSwitcher == null){
            Log.e("WTasfdfdsF","WTasdsaF");
        }
        imgSwitcher.setImageDrawable(img);
        textView_Name.setText(imgNames[currentIndex]);
        textView_Index.setText("(" + (currentIndex + 1) + "/" + imgNames.length + ")");

        if(!isCyclic) {
            if(currentIndex == 0) {
                imgBt_Prev.setEnabled(false);
            } else {
                imgBt_Prev.setEnabled(true);
            }
            if(currentIndex == getNumImages() - 1) {
                imgBt_Next.setEnabled(false);
            } else {
                imgBt_Next.setEnabled(true);
            }
        }
    }

    public Drawable getCurrentImage() {
        return images[currentIndex];
    }

    public String getCurrentImageName() {
        return imgNames[currentIndex];
    }

    public void setActionButtonEnabled(boolean b) {
        button_Action.setEnabled(b);
    }

    public void setFinishButtonEnabled(boolean b) {
        button_Finish.setEnabled(b);
    }

    public boolean containsNullImage(){
        for(Drawable image:images){
            if(image == null){
                return true;
            }
        }
        return false;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.imageButton_next:
                setCurrentImage(currentIndex + 1);
                break;
            case R.id.imageButton_prev:
                setCurrentImage(currentIndex - 1);
                break;
            case R.id.button_finish:
                if(fbl != null) {
                    fbl.onFinishButton();
                }
                break;
            case R.id.button_action:
                if(abl != null) {
                    abl.onActionButton();
                }
                break;
            default:
                break;
        }
    }

    public void setFinishButtonListener(FinishButtonListener listener) {
        fbl = listener;
    }

    public void setActionButtonListener(ActionButtonListener listener) {
        abl = listener;
    }

    public interface FinishButtonListener {
        void onFinishButton();
    }

    public interface ActionButtonListener {
        void onActionButton();
    }
}
