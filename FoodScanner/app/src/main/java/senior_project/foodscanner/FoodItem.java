package senior_project.foodscanner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a single item of food.
 */
public class FoodItem extends Nutritious implements Serializable {

    private Map<String, Double> fields;  //holds all nutrition info

    public static final String KEY_CAL = "Calories";

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
    private Density density;
    private List<Portion> portions;  //list of individual items, e.g. 3 pieces of chicken that we
    //want to calculate individually will each have a portion.

    private static Map<String, Double> densities = null;

    public FoodItem() {
        //LinkedHashMap used to ensure insertion order is maintained, for iteration.
        fields = new LinkedHashMap<>(7);

        //Create portions list and first portion
        portions = new ArrayList<>();
        portions.add(new Portion());

        //NOTE: when checking if the serving size matches, call toLower() on it first, and remove
        //'s' at the end of the word, if necessary.

        //Create list of volume units, and find max length
        String[] vol_values = new String[]{"cup", "quart", "pt", "pint", "ml", "milliliter",
                "millilitre", "l", "liter", "litre", "tsp", "teaspoon", "tbsp", "tbl",
                "tablespoon"};
        maxVolLen = Integer.MIN_VALUE;
        for(String s : vol_values) {
            if(s.length() > maxVolLen) {
                maxVolLen = s.length();
            }
        }
        volUnits = new HashSet<>(Arrays.asList(vol_values));

        //Create list of mass units, and find max length
        String[] mass_values = new String[]{"oz", "ounce", "fl oz", "fl. oz", "fluid ounce", "g",
                "gram", "mg", "milligram"};
        maxMassLen = Integer.MIN_VALUE;
        for(String s : mass_values) {
            if(s.length() > maxMassLen) {
                maxMassLen = s.length();
            }
        }
        massUnits = new HashSet<>(Arrays.asList(mass_values));

        density = new Density();
        density.value = 0.0;  //for clarity
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
        if(formattedUnit.charAt(formattedUnit.length() - 1) == '.') {
            formattedUnit = formattedUnit.substring(0, formattedUnit.length() - 1);
        }

        this.servingSizeUnit = formattedUnit;

        //check if volume, mass, or neither is needed

        formattedUnit = formattedUnit.toLowerCase();
        if(formattedUnit.charAt(formattedUnit.length() - 1) == 's') {
            //remove last 's' for comparison
            formattedUnit = formattedUnit.substring(0, formattedUnit.length() - 1);
        }

        if(massUnits.contains(formattedUnit)) {
            setCalculateVol(true);
            setCalculateMass(true);
        } else if(volUnits.contains(formattedUnit)) {
            setCalculateVol(true);
            setCalculateMass(false);
        } else {
            //check that each substring is not in either, e.g. 'cups (chopped)'
            boolean stopCheck = false;

            //check that substring is not in massUnits
            for(int i = 1; i <= maxMassLen; i++) {
                if(i > formattedUnit.length()) {
                    break;
                }
                if(massUnits.contains(formattedUnit.substring(0, i))) {
                    stopCheck = true;
                    setCalculateVol(true);
                    setCalculateMass(true);
                    break;
                }
            }
            if(stopCheck) {
                return;
            }

            //check that substring is not in volUnits
            for(int i = 1; i <= maxVolLen; i++) {
                if(i > formattedUnit.length()) {
                    break;
                }
                if(volUnits.contains(formattedUnit.substring(0, i))) {
                    stopCheck = true;
                    setCalculateVol(true);
                    setCalculateMass(false);
                    break;
                }
            }
            if(stopCheck) {
                return;
            }

            //not necessary; here for clarity
            setCalculateVol(false);
            setCalculateMass(false);
        }
    }

    public boolean needCalculateVol() {
        if(!calculateVol) {
            return false;
        }

        // Check if all portions have been calculated
        for(Portion p : portions) {
            if(p.getVolume() == 0.0) {
                // Has not yet been calculated
                return true;
            }
        }

        // All portions have been calculated
        return false;
    }

    public void setCalculateVol(boolean calculateVol) {
        this.calculateVol = calculateVol;
    }

    public boolean needCalculateMass() {
        if(!calculateMass) {
            return false;
        }
        // Check if all portions have been calculated
        for(Portion p : portions) {
            if(p.getMass() == 0.0) {
                // Has not yet been calculated
                return true;
            }
        }

        // All portions have been calculated
        return false;
    }

    public void setCalculateMass(boolean calculateMass) {
        this.calculateMass = calculateMass;
    }

    public boolean needDisplayMass() {
        return calculateMass;
    }

    public boolean needDisplayVolume() {
        return calculateVol;
    }


    //endregion

    @Override
    public String toString() {
        return name + " (" + brand + ")";
    }

    @Override
    public boolean equals(Object o) {
        if(getClass() != o.getClass()) {
            return false;
        }
        FoodItem fi = (FoodItem) o;
        if(fi.getName().equals(this.getName()) &&
                fi.getBrand().equals(this.getBrand()) &&
                fi.getServingSizeUnit().equals(this.getServingSizeUnit())) {
            return true;
        } else {
            return false;
        }
    }

    private class Portion implements Serializable {
        private double volume = 0.0;
        private double mass = 0.0;

        //region getters and setters
        public double getVolume() {
            return volume;
        }

        public void setVolume(double volume) {
            this.volume = volume;
        }

        public double getMass() {
            return mass;
        }

        public void setMass(double mass) {
            this.mass = mass;
        }
        //endregion
    }

    //NOTE: not sure how these will be used exactly. subject to change.

    public List<Portion> getPortions() {
        return portions;
    }

    public Portion getPortion(int index) {
        return portions.get(index);
    }

    public int getNumPortions() {
        return portions.size();
    }

    public Portion addPortion() {
        Portion p = new Portion();
        portions.add(p);
        return p;
    }

    public void removePortion(int index) throws IndexOutOfBoundsException {
        portions.remove(index);
    }

    public void removePortion(Portion p) {
        portions.remove(p);
    }

    public void replacePortions(List<Portion> newPortions) {
        portions = newPortions;
    }

    @Override
    public Map<String, Double> getNutrition() {
        Map<String, Double> nutr = new LinkedHashMap<>(fields);
        //TODO: create & return total nutrition info, based on calculations
        return nutr;
    }

    public double getTotalMass() {
        double totalMass = 0.0;
        for(Portion p : portions) {
            totalMass += p.getMass();
        }
        return totalMass;
    }

    public double getTotalVolume() {
        double totalVolume = 0.0;
        for(Portion p : portions) {
            totalVolume += p.getVolume();
        }
        return totalVolume;
    }

    private class Density implements Serializable {
        private double value;
        private String name;
        private String id;
    }

    public double getDensity() {
        return density.value;
    }

    public void setDensity(double value) {
        this.density.value = value;
    }

    public String getDensityName() {
        return density.name;
    }

    public void setDensityName(String name) {
        density.name = name;
    }

    public String getDensityId() {
        return density.id;
    }

    public void setDensityId(String id) {
        density.id = id;
    }


    // Methods for saving & retrieving densities from database (may be moved)

    public static void addDensity(String name, Double value) {
        if (densities == null) {
            densities = new HashMap<>();
        }
        densities.put(name, value);
    }

    public static String[] getDensityKeys() {
        if (densities == null) {
            return null;
        } else {
            Set<String> densitySet = densities.keySet();
            String[] retArray = densitySet.toArray(new String[densitySet.size()]);
            return retArray;
        }
    }

    public static Double getDensityValue(String name) {
        if (densities == null) {
            return null;
        } else {
            return densities.get(name);
        }
    }

}
