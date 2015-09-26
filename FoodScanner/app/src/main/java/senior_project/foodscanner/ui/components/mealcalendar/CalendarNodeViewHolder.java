package senior_project.foodscanner.ui.components.mealcalendar;

import android.content.Context;

import com.unnamed.b.atv.model.TreeNode;

/**
 * View for MealCalendar nodes.
 */
public abstract class CalendarNodeViewHolder<E extends Object> extends TreeNode.BaseNodeViewHolder<E> {


    public CalendarNodeViewHolder(Context context) {
        super(context);
    }

    /**
     * Generates a string consisting of spaces for the purpose of indenting text by level of the node.
     * @param node - node to indent for
     * @return
     */
    protected String indentText(TreeNode node){
        String ret = "";
        for(int i = 0; i < node.getLevel() - 1; i++){
            ret+="    ";
        }
        return ret;
    }
}
