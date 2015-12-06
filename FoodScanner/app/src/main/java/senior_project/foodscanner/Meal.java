package senior_project.foodscanner;

import android.content.ContentValues;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import senior_project.foodscanner.database.SQLHelper;
import senior_project.foodscanner.database.SQLQueryHelper;

/**
 * This class represents a meal.
 */
public class Meal extends Nutritious implements Serializable, Comparable<Meal> {

    private static final long serialVersionUID = 418772005483570552L;

    public enum MealType {
        BREAKFAST("Breakfast", 0),
        BRUNCH("Brunch", 1),
        LUNCH("Lunch", 2),
        SNACK("Snack", 3),
        DINNER("Dinner", 4),
        DESSERT("Dessert", 5);


        private final String name;
        private final int priority;

        MealType(String name, int priority) {
            this.name = name;
            this.priority = priority;
        }

        public String getName() {
            return name;
        }
        public int getPriority() {
            return priority;
        }
    }


    // Data Management
    private long id;    //Database ID
    private int isChanged = 0;// Flag that is incremented whenever meal details are changed. Must be manually set to 0 when meal is uploaded to backend.
    private boolean isDeleted = false;// Flag that is set to true when the Meal should be deleted from the backend
    private boolean isNew; // Flag determining whether or not the Meal was just created.

    // Meal Details
    private MealType type;
    private long date;
    private List<FoodItem> food;

    public Meal(long date, MealType type) {
        id = -1;
        this.date = date;
        this.type = type;
        food = new ArrayList<>();
        setIsNew(true);
    }

    public Meal(long id, long date, String type, ArrayList<FoodItem> foodItems,  boolean isNew, int isChanged, boolean isDeleted) {
        this.id = id;
        this.date = date;
        this.type = MealType.valueOf(type);
        food = foodItems;
        this.isChanged = isChanged;
        this.isDeleted = isDeleted;
        this.isNew = isNew;
        setIsNew(false);
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
    
    public void setType(MealType type) {
        if(!this.type.equals(type)) {
            this.type = type;
            setUnchanged();
        }
    }

    public MealType getType() {
        return type;
    }

    public long getDate() {
        return date;
    }

    /**
     * @param item - food item to be added
     * @return true is success, false is failure
     */
    public boolean addFoodItem(FoodItem item) {
        // Check if food item has already been added
        for(FoodItem fi : food) {
            if(fi.equals(item)) {
                return false;  // Can only add food items once, for this demo
            }
        }

        // Food item hasn't already been added
        item.calculateNutrition();
        food.add(item);
        setUnchanged();
        return true;
    }

    public void removeFoodItem(FoodItem item) {
        food.remove(item);
        setUnchanged();
    }

    public FoodItem getFoodItem(int index) {
        return food.get(index);
    }

    public List<FoodItem> getFood() {
        return food;
    }

    public void replaceFoodItem(FoodItem oldFood, FoodItem newFood) {
        newFood.setVolume(oldFood.getCubicVolume());
        newFood.calculateNutrition();
        this.removeFoodItem(oldFood);
        this.addFoodItem(newFood);
        setUnchanged();
    }

    public boolean isNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    public boolean isChanged() {
        return isChanged > 0;
    }

    public void setUnchanged() {
        isChanged = 0;
        if(isDeleted) {
            isChanged = 1;
        }
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setIsDeleted() {
        isChanged++;
        isDeleted = true;
    }

    @Override
    public Map<String, Double> getNutrition() {
        return calculateTotalNutrition(food);
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(SQLHelper.COLUMN_MEAL_TYPE, type.toString());
        cv.put(SQLHelper.COLUMN_TIME, date);
        cv.put(SQLHelper.COLUMN_FOOD_LIST, SQLQueryHelper.foodListToBytes(food));
        cv.put(SQLHelper.COLUMN_NEW, isNew);
        cv.put(SQLHelper.COLUMN_CHANGED, isChanged);
        cv.put(SQLHelper.COLUMN_DELETED, isDeleted);
        return cv;
    }

    public boolean isMoreRecentlyChangedThan(Meal meal2){
        if(meal2.isDeleted && !isDeleted){
            return false;
        }
        return isChanged >= meal2.isChanged;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": id=" + id + ", type=" + type + ", time=" + date + ", food item count=" + food.size();
    }

    @Override
    public int compareTo(Meal another) {
        int p1 = type.getPriority();
        int p2 = another.getType().getPriority();
        if(p1 > p2){
            return 1;
        }
        else if(p1 < p2){
            return -1;
        }
        return 0;
    }
}
