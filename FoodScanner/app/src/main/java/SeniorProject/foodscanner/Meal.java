/**
 * This class represents a meal.
 */
public class Meal {

    public enum MealType{
        BREAKFAST, BRUNCH, LUNCH, DINNER, DESSERT, SNACK
    }

    // Meal Details
    private MealType type;
    // TODO date/time
    // TODO food data

    //TODO add date/time to constructor
    public Meal(MealType type){
        this.type = type;
    }

    public void setType(MealType type){
        this.type = type;
    }

    public MealType getType(){
        return type;
    }

    //TODO date/time get

    //TODO addFoodItem()
}
