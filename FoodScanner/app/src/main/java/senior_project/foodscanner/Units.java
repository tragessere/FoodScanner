package senior_project.foodscanner;

import java.util.List;

/**
 * Created by Tyler on 11/13/2015.
 *
 * Class for unit conversions.
 */
public class Units {

    //region Constants for units conversion
    static final double ML_IN_CUBIC_INCH = 16.3871;
    static final double LITER_IN_CUBIC_INCH = 0.0163871;
    static final double CUP_IN_CUBIC_INCH = 0.0692641;
    static final double QUART_IN_CUBIC_INCH = 0.017316;
    static final double PINT_IN_CUBIC_INCH = 0.034632;
    static final double TSP_IN_CUBIC_INCH = 3.32468;
    static final double TBL_IN_CUBIC_INCH = 1.10823;
    static final double FL_OZ_IN_CUBIC_INCH = 0.554113;

    static final double OZ_IN_GRAM = 0.035274;
    static final double GRAM_IN_GRAM = 1.0;
    static final double LB_IN_GRAM = 0.00220462;
    static final double MG_IN_GRAM = 1000.0;
    //endregion

    /*
    Converts volume from scanner (cubic inches) to ml for mass FoodItems,
    or to whatever volume measurement the FoodItem needs.
     */
    public static FoodItem convertVolume(FoodItem food) {
        if (!food.needConvertVol()) {
            // volume has already been converted
            return food;
        }

        if (food.usesMass()) {
            food.setConvertedVolume(food.getVolume() * ML_IN_CUBIC_INCH);
            food.setNeedConvertVol(false);
            return food;

        } else if (food.usesVolume()) {
            String unit = food.getActualServingSizeUnit();
            double conversion;

            if (unit.equals("cup")) {
                conversion = CUP_IN_CUBIC_INCH;
            } else if (unit.equals("quart")) {
                conversion = QUART_IN_CUBIC_INCH;
            } else if (unit.equals("pt") || unit.equals("pint")) {
                conversion = PINT_IN_CUBIC_INCH;
            } else if (unit.equals("ml") || unit.equals("milliliter") || unit.equals("millilitre")) {
                conversion = ML_IN_CUBIC_INCH;
            } else if (unit.equals("l") || unit.equals("liter") || unit.equals("litre")){
                conversion = LITER_IN_CUBIC_INCH;
            } else if (unit.equals("tsp") ||  unit.equals("teaspoon")) {
                conversion = TSP_IN_CUBIC_INCH;
            } else if (unit.equals("tbsp") || unit.equals("tbl") || unit.equals("tablespoon")) {
                conversion = TBL_IN_CUBIC_INCH;
            } else {  //unit is fl oz
                conversion = FL_OZ_IN_CUBIC_INCH;
            }

            food.setConvertedVolume(food.getVolume() * conversion);
            food.setNeedConvertVol(false);
            return food;
        }

        // return food without changes, if none to make
        return food;
    }

    /*
    Converts mass (in grams, from density entries being g/ml) to needed mass unit.
     */
    public static FoodItem convertMass(FoodItem food) {

        String unit = food.getActualServingSizeUnit();
        double conversion;

        if (unit.equals("oz") || unit.equals("ounce")) {
            conversion = OZ_IN_GRAM;
        } else if (unit.equals("g") || unit.equals("gram")) {
            conversion = GRAM_IN_GRAM;
        } else if (unit.equals("lb") || unit.equals("pound")) {
            conversion = LB_IN_GRAM;
        } else {  //unit is mg
            conversion = MG_IN_GRAM;
        }

        food.setMass(food.getMass() * conversion);

        return food;
    }

}
