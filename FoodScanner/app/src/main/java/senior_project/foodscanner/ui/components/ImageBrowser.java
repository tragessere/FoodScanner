package senior_project.foodscanner.ui.components;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import senior_project.foodscanner.R;

/**
 * Interactive view that allows browsing through a set number of images.
 */
public class ImageBrowser extends FrameLayout implements View.OnClickListener {

    public static int DIRECTION_PREVIOUS = 0;
    public static int DIRECTION_NEXT = 1;

    private String[] imgNames;
    private ImageSwitcher imgSwitcher;
    private Context context;
    private ImageBrowserListener listener;
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
        setCurrentIndex(currentIndex);
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
        setCurrentIndex(currentIndex);
    }

    public int getNumImages() {
        return images.length;
    }

    public void setImage(int index, Drawable image) {
        images[index] = image;
        if(index == currentIndex){
            setCurrentIndex(currentIndex);
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
            setEnabledButton_Next(true);
            setEnabledButton_Previous(true);
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
        textView_Name = ((TextView) browser.findViewById(R.id.textView_image_title));
        textView_Name.setText(imgNames[currentIndex]);
        textView_Index = ((TextView) browser.findViewById(R.id.textView_image_count));
        textView_Index.setText((currentIndex + 1) + "/" + imgNames.length);
        imgBt_Next = (ImageButton) browser.findViewById(R.id.imageButton_next);
        imgBt_Next.setOnClickListener(this);
        imgBt_Prev = (ImageButton) browser.findViewById(R.id.imageButton_prev);
        imgBt_Prev.setOnClickListener(this);
        button_Finish = (Button) browser.findViewById(R.id.button_finish);
        button_Finish.setOnClickListener(this);
        button_Action = (Button) browser.findViewById(R.id.button_action);
        button_Action.setOnClickListener(this);

        imgSwitcher = ((ImageSwitcher) browser.findViewById(R.id.imageSwitcher));
        imgSwitcher.addView(new ImageView(context), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imgSwitcher.addView(new ImageView(context), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void setEnabledButton_Next(boolean enabled){
        imgBt_Next.setEnabled(enabled);
        if(enabled) {
            imgBt_Next.setVisibility(View.VISIBLE);
        }
        else{
            imgBt_Next.setVisibility(View.INVISIBLE);
        }
    }

    private void setEnabledButton_Previous(boolean enabled){
        imgBt_Prev.setEnabled(enabled);
        if(enabled) {
            imgBt_Prev.setVisibility(View.VISIBLE);
        }
        else{
            imgBt_Prev.setVisibility(View.INVISIBLE);
        }
    }

    public void setActionButtonText(String text) {
        button_Action.setText(text);
    }

    public void nextIndex(){
        int newIndex = currentIndex + 1;
        if(newIndex < getNumImages() || isCyclic) {
            setCurrentIndex(currentIndex + 1);
        }
    }

    public void previousIndex(){
        int newIndex = currentIndex - 1;
        if(newIndex >= 0 || isCyclic) {
            setCurrentIndex(currentIndex - 1);
        }
    }

    public int getNextIndex(){
        return getCycledIndex(currentIndex+1);
    }

    public int getPreviousIndex(){
        return getCycledIndex(currentIndex-1);
    }

    private int getCycledIndex(int index){
        if(isCyclic) {
            if(currentIndex < 0) {
                currentIndex = getNumImages() + currentIndex % getNumImages();
            } else if(currentIndex > getNumImages()-1) {
                currentIndex = currentIndex % (getNumImages()-1);
            }
        }
        return index;
    }

    /**
     * Sets current index of image browser. If cyclic, then indexes out of bounds are cycled over.
     * @param index
     */
    public void setCurrentIndex(int index) {
        int oldIndex = currentIndex;
        currentIndex = getCycledIndex(index);

        Drawable img = images[currentIndex];
        if(img == null){
            img = defaultImage;
        }
        imgSwitcher.setImageDrawable(img);
        textView_Name.setText(imgNames[currentIndex]);
        textView_Index.setText((currentIndex + 1) + "/" + imgNames.length);

        if(!isCyclic) {
            if(currentIndex == 0) {
                setEnabledButton_Previous(false);
            } else {
                setEnabledButton_Previous(true);
            }
            if(currentIndex == getNumImages() - 1) {
                setEnabledButton_Next(false);
            } else {
                setEnabledButton_Next(true);
            }
        }

        if(listener != null) {
            listener.onImageBrowserIndexChanged(oldIndex, currentIndex);
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
                nextIndex();
                break;
            case R.id.imageButton_prev:
                previousIndex();
                break;
            case R.id.button_finish:
                if(listener != null) {
                    listener.onImageBrowserFinish();
                }
                break;
            case R.id.button_action:
                if(listener != null) {
                    listener.onImageBrowserAction();
                }
                break;
            default:
                break;
        }
    }

    public void setImageBrowserListener(ImageBrowserListener listener) {
        this.listener = listener;
    }

    public interface ImageBrowserListener {
        void onImageBrowserFinish();
        void onImageBrowserAction();
        void onImageBrowserIndexChanged(int oldIndex, int newIndex);
    }

}
