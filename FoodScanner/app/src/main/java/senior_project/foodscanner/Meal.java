package senior_project.foodscanner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * This class represents a meal.
 */
public class Meal implements Serializable {

    private static final long serialVersionUID = 418772005483570552L;

    public enum MealType{
        BREAKFAST ("Breakfast"),
        LUNCH ("Lunch"),
        DINNER ("Dinner"),
        DESSERT ("Dessert"),
        SNACK ("Snack");

        private final String name;

        MealType(String name){
            this.name = name;
        }

        public String getName(){
            return name;
        }
    }

    // Meal Details
    private MealType type;
    private GregorianCalendar date;
    public List<FoodItem> food;  //made public for now, for ease of use in ListView

    public Meal(GregorianCalendar date, MealType type){
        this.date = date;
        this.type = type;
        food = new ArrayList<FoodItem>();
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
        food.add(item);
    }

    public void removeFoodItem(FoodItem item){
        food.remove(item);
    }

}
