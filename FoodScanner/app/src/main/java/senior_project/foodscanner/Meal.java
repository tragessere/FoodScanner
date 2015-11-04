package senior_project.foodscanner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class represents a meal.
 */
public class Meal extends Nutritious implements Serializable {

    private static final long serialVersionUID = 418772005483570552L;

    public enum MealType {
        BREAKFAST("Breakfast"),
        BRUNCH("Brunch"),
        LUNCH("Lunch"),
        SNACK("Snack"),
        DINNER("Dinner"),
        DESSERT("Dessert");


        private final String name;

        MealType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    // Data Management
    private boolean isChanged = true;// Flag that is set to false whenever meal details are changed. Must be manually set to false when meal is uploaded to backend.
    private boolean isNew; // Flag determining whether or not the Meal was just created.

    // Meal Details
    private MealType type;
    private long date;
    private List<FoodItem> food;

    public Meal(long date, MealType type) {
        this.date = date;
        this.type = type;
        food = new ArrayList<>();
        setIsNew(true);
    }

    public void setType(MealType type) {
        if(!this.type.equals(type)) {
            this.type = type;
            setIsChanged(true);
        }
    }

    public MealType getType() {
        return type;
    }

    public long getDate() {
        return date;
    }

    public void addFoodItem(FoodItem item) {
        // Check if food item has already been added
        for(FoodItem fi : food) {
            if(fi.equals(item)) {
                // Add one more portion of food item
                fi.addPortion();
                return;
            }
        }

        // Food item hasn't already been added
        food.add(item);
        setIsChanged(true);
    }

    public void removeFoodItem(FoodItem item) {
        food.remove(item);
        setIsChanged(true);
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
        setIsChanged(true);
    }

    public boolean isNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    public boolean isChanged() {
        return isChanged;
    }

    public void setIsChanged(boolean isChanged) {
        this.isChanged = isChanged;
    }

    @Override
    public Map<String, Double> getNutrition() {
        return calculateTotalNutrition(food);
    }
}
