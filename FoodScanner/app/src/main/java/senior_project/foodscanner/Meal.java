package senior_project.foodscanner;

import java.util.Date;

/**
 * This class represents a meal.
 */
public class Meal {

    public enum MealType{
        BREAKFAST, BRUNCH, LUNCH, DINNER, DESSERT, SNACK
    }

    // Meal Details
    private MealType type;
    private Date date;
    // TODO list of food items

    public Meal(Date date, MealType type){
        this.date = date;
        this.type = type;
    }

    public void setType(MealType type){
        this.type = type;
    }

    public MealType getType(){
        return type;
    }

    public Date getDate(){
        return date;
    }

    public void addFoodItem(FoodItem item){
        //TODO
    }

    public void removeFoodItem(){
        //TODO
    }
}
