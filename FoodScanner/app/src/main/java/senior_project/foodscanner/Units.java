package senior_project.foodscanner;

import java.util.List;

/**
 * Created by Tyler on 11/13/2015.
 *
 * Class for unit conversions.
 */
public class Units {

    static final double ML_IN_CUBIC_INCH = 16.3871;
    static final double LITER_IN_CUBIC_INCH = 0.0163871;
    static final double CUP_IN_CUBIC_INCH = 0.0692641;
    static final double QUART_IN_CUBIC_INCH = 0.017316;
    static final double PINT_IN_CUBIC_INCH = 0.034632;
    static final double TSP_IN_CUBIC_INCH = 3.32468;
    static final double TBL_IN_CUBIC_INCH = 1.10823;

    /*
    Converts volume from scanner (cubic inches) to ml for mass FoodItems,
    or to whatever volume measurement the FoodItem needs.
     */
    public static FoodItem convertVolume(FoodItem food) {
        if (!food.needConvertVol()) {
            // volume has already been converted
            return food;
        }

        int numPortions = food.getNumPortions();
        List<FoodItem.Portion> portions = food.getPortions();

        if (food.usesMass()) {
            for (int i = 0; i < numPortions; i++) {
                FoodItem.Portion tempPort = food.getPortion(i);
                tempPort.setVolume(tempPort.getVolume() * ML_IN_CUBIC_INCH);
                portions.set(i, tempPort);
            }

            food.replacePortions(portions);
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
            } else {  //unit is tablespoon
                conversion = TBL_IN_CUBIC_INCH;
            }

            for (int i = 0; i < numPortions; i++) {
                FoodItem.Portion tempPort = food.getPortion(i);
                tempPort.setVolume(tempPort.getVolume() * conversion);
                portions.set(i, tempPort);
            }
            food.replacePortions(portions);
            food.setNeedConvertVol(false);
            return food;
        }

        // return food without changes, if none to make
        return food;
    }

}
