package senior_project.foodscanner;

import android.content.ContentValues;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import senior_project.foodscanner.database.SQLHelper;
import senior_project.foodscanner.database.SQLQueryHelper;

/**
 * This class represents a meal.
 */
public class Meal implements Serializable {

    private static final long serialVersionUID = 418772005483570552L;

    public enum MealType{
        BREAKFAST ("Breakfast"),
        BRUNCH ("Brunch"),
        LUNCH ("Lunch"),
        SNACK ("Snack"),
        DINNER ("Dinner"),
        DESSERT ("Dessert");


        private final String name;

        MealType(String name){
            this.name = name;
        }

        public String getName(){
            return name;
        }
    }

    //Database ID
    private long id;

    // Meal Details
    private MealType type;
    private GregorianCalendar date;
    private List<FoodItem> food;
    private boolean isNew;

    public Meal(GregorianCalendar date, MealType type){
        id = -1;
        this.date = date;
        this.type = type;
        food = new ArrayList<>();
        setIsNew(true);
    }

    public Meal(long id, long date, MealType type, ArrayList<FoodItem> foodItems) {
        this.id = id;
        this.date = (GregorianCalendar) GregorianCalendar.getInstance();
        this.date.setTimeInMillis(date);
        this.type = type;
        food = foodItems;
        setIsNew(false);
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setType(MealType type){
        this.type = type;
    }

    public MealType getType(){
        return type;
    }

    public GregorianCalendar getDate(){
        return date;
    }

    public void addFoodItem(FoodItem item){
        // Check if food item has already been added
        for (FoodItem fi : food) {
            if (fi.equals(item)) {
                // Add one more portion of food item
                fi.addPortion();
                return;
            }
        }

        // Food item hasn't already been added
        food.add(item);
    }

    public void removeFoodItem(FoodItem item) {
        food.remove(item);
    }

    public FoodItem getFoodItem(int index) {
        return food.get(index);
    }

    public List<FoodItem> getFood() {
        return food;
    }

    public void replaceFoodItem(FoodItem oldFood, FoodItem newFood) {
        newFood.replacePortions(oldFood.getPortions());
        this.addFoodItem(newFood);
        this.removeFoodItem(oldFood);
    }

    public boolean isNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    //returns a set, which can be iterated through
    public Set<Map.Entry<String, Double>> getTotalNutrition() {
        //TODO: create & return total nutrition info, based on calculations of all FoodItems
        //For now, simply return null
        return null;
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(SQLHelper.COLUMN_MEAL_TYPE, type.toString());
        cv.put(SQLHelper.COLUMN_TIME, date.getTimeInMillis());
        cv.put(SQLHelper.COLUMN_FOOD_LIST, SQLQueryHelper.foodListToBytes(food));
        return cv;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(": id=");
        sb.append(id);
        sb.append(", type=");
        sb.append(type);
        sb.append(", time=");
        sb.append(date.toString());
        sb.append(", food item count=");
        sb.append(food.size());
        return sb.toString();
    }
}
