package senior_project.foodscanner;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * This class represents a meal.
 */
public class Meal {

    private static final String[] am_pm = {"am", "pm"};

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
    }

    // Meal Details
    private MealType type;
    private GregorianCalendar date;
    // TODO list of food items

    public Meal(GregorianCalendar date, MealType type){
        this.date = date;
        this.type = type;
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
        //TODO
    }

    public void removeFoodItem(){
        //TODO
    }

    /**
     * String in format: hh:mm am/pm type
     * EX: 12:35pm Lunch
     * @return
     */
    public String toString(){
        int minute = date.get(GregorianCalendar.MINUTE);
        String time = date.get(GregorianCalendar.HOUR) + ":";
        if(minute < 10){
            time += '0';
        }
        time += minute + am_pm[date.get(GregorianCalendar.AM_PM)];

        return time+ " " + type.name;
    }
}
