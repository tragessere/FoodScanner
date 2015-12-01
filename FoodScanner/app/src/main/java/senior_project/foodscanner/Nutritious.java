package senior_project.foodscanner;

import android.text.Html;

import java.util.List;
import java.util.Map;

/**
 * Abstract class representing something that has nutrition.
 */
public abstract class Nutritious {

    public static Map<String, Double> calculateTotalNutrition(List<? extends Nutritious> items) {
        Map<String, Double> total = null;
        if(!items.isEmpty()) {
            for(Nutritious item : items) {
                if(item != null) {
                    Map<String, Double> nutr = item.getNutrition();
                    if(nutr != null) {
                        if(total == null) {
                            total = nutr;
                        } else {
                            for(String key : nutr.keySet()) {
                                if(total.containsKey(key)) {
                                    total.put(key, total.get(key) + nutr.get(key));
                                } else {
                                    total.put(key, nutr.get(key));
                                }
                            }
                        }
                    }
                }
            }
        }
        return total;
    }

    /**
     * Returns html formatted text of the nutrition that can be used in a view.
     *
     * @param nutr
     * @return
     */
    public static CharSequence nutritionText(Map<String, Double> nutr) {
        StringBuilder s = new StringBuilder();
        if(nutr != null && !nutr.isEmpty()) {
            for(String key : nutr.keySet()) {
                s.append("<b>" + key + ": </b>");
                s.append(nutr.get(key) + "<br>");
            }
            return Html.fromHtml(s.toString());
        } else {
            return "No nutrition info to show.";
        }
    }

    public abstract Map<String, Double> getNutrition();
}
