package senior_project.foodscanner;

import java.io.Serializable;
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
    private String servingSizeUnit;  //e.g. cup, gram, lb, taco

    public FoodItem() {
        //LinkedHashMap used to ensure insertion order is maintained, for iteration.
        fields = new LinkedHashMap<String, Double>(7);
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
        //remove '.' from end of unit, if necessary
        if(servingSizeUnit.charAt(servingSizeUnit.length() - 1) == '.'){
            this.servingSizeUnit = servingSizeUnit.substring(0,servingSizeUnit.length() - 1);
        }
        else{
            this.servingSizeUnit = servingSizeUnit;
        }
    }

    //endregion

    @Override
    public String toString() {
        return name + " (" + brand + ")";
    }

}
