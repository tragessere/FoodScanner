package senior_project.foodscanner;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a single item of food.
 */
public class FoodItem implements Serializable {

    private Map<String, Double> fields;  //holds all nutrition info

    private String name;
    private String brand;
    private double servingSize;
    private String servingSizeUnit;  //e.g. cup, grams, taco
    private boolean calculateVol;
    private boolean calculateMass;
    private Set<String> volUnits;
    private Set<String> massUnits;
    private int maxMassLen;
    private int maxVolLen;

    public FoodItem() {
        //LinkedHashMap used to ensure insertion order is maintained, for iteration.
        fields = new LinkedHashMap<>(7);

        //NOTE: when checking if the serving size matches, call toLower() on it first, and remove
                // 's' at the end of the word, if necessary.

        //Create list of volume units, and find max length
        String[] vol_values = new String[] { "cup", "quart", "pt", "pint", "ml", "milliliter",
                "millilitre", "l", "liter", "litre", "tsp", "teaspoon", "tbsp", "tbl",
                "tablespoon" };
        maxVolLen = Integer.MIN_VALUE;
        for (String s : vol_values) {
            if (s.length() > maxVolLen) {
                maxVolLen = s.length();
            }
        }
        volUnits = new HashSet<>(Arrays.asList(vol_values));

        //Create list of mass units, and find max length
        String[] mass_values = new String[] { "oz", "ounce", "fl oz", "fl. oz", "fluid ounce", "g",
                "gram", "mg", "milligram" };
        maxMassLen = Integer.MIN_VALUE;
        for (String s : mass_values) {
            if (s.length() > maxMassLen) {
                maxMassLen = s.length();
            }
        }
        massUnits = new HashSet<>(Arrays.asList(mass_values));
    }

    public void setField(String field, Double value) {
        fields.put(field, value);
    }

    public Double getField(String field) {
        return fields.get(field);
    }

    /**
     * @return set to be iterated through in an enhanced for loop
     */
    public Set<Map.Entry<String, Double>> getSet() {
        return fields.entrySet();
    }

    //region getters and setters for hard-coded fields

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public double getServingSize() {
        return servingSize;
    }

    public void setServingSize(double servingSize) {
        this.servingSize = servingSize;
    }

    public String getServingSizeUnit() {
        return servingSizeUnit;
    }

    public void setServingSizeUnit(String servingSizeUnit) {
        //remove last '.' from unit, if necessary
        String formattedUnit = servingSizeUnit;
        if (formattedUnit.charAt(formattedUnit.length()-1) == '.') {
            formattedUnit = formattedUnit.substring(0, formattedUnit.length()-1);
        }

        this.servingSizeUnit = formattedUnit;

        //check if volume, mass, or neither is needed

        if (formattedUnit.charAt(formattedUnit.length()-1) == 's') {
            //remove last 's' for comparison
            formattedUnit = formattedUnit.substring(0, formattedUnit.length()-1);
        }
        formattedUnit = formattedUnit.toLowerCase();

        if (massUnits.contains(formattedUnit)) {
            setCalculateVol(true);
            setCalculateMass(true);
        } else if (volUnits.contains(formattedUnit)) {
            setCalculateVol(true);
            setCalculateMass(false);
        } else {
            //check that each substring is not in either, e.g. 'cups (chopped)'
            boolean stopCheck = false;

            //check that substring is not in massUnits
            for (int i = 1; i <= maxMassLen; i++) {
                if (i > formattedUnit.length()) {
                    break;
                }
                if (massUnits.contains(formattedUnit.substring(0, i))) {
                    stopCheck = true;
                    setCalculateVol(true);
                    setCalculateMass(true);
                    break;
                }
            }
            if (stopCheck) {
                return;
            }

            //check that substring is not in volUnits
            for (int i = 1; i <= maxVolLen; i++) {
                if (i > formattedUnit.length()) {
                    break;
                }
                if (volUnits.contains(formattedUnit.substring(0, i))) {
                    stopCheck = true;
                    setCalculateVol(true);
                    setCalculateMass(false);
                    break;
                }
            }
            if (stopCheck) {
                return;
            }

            //not necessary; here for clarity
            setCalculateVol(false);
            setCalculateMass(false);
        }
    }

    public boolean needCalculateVol() {
        return calculateVol;
    }

    public void setCalculateVol(boolean calculateVol) {
        this.calculateVol = calculateVol;
    }

    public boolean needCalculateMass() {
        return calculateMass;
    }

    public void setCalculateMass(boolean calculateMass) {
        this.calculateMass = calculateMass;
    }

    //endregion

    @Override
    public String toString() {
        return name + " (" + brand + ")";
    }

}
