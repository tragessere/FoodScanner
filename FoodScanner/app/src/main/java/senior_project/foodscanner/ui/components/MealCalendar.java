package senior_project.foodscanner.ui.components;

import android.content.Context;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import senior_project.foodscanner.Meal;

/**
 * Tree view for Meals.
 */
public class MealCalendar extends AndroidTreeView{
    private ArrayList<Meal> meals = new ArrayList<Meal>();
    private TreeNode root;

    private static DateFormatSymbols dateFormat = new DateFormatSymbols();

    public MealCalendar(Context c, TreeNode root){
        super(c, root);
        this.root = root;
        setSelectionModeEnabled(true);

       /* parent = new TreeNode("MyParentNode");
        root.addChild(parent);

        TreeNode child0 = new TreeNode("ChildNode0");
        child1 = new TreeNode("ChildNode1");
        parent.addChildren(child0, child1);*/
    }

    public void addMeal(Meal meal){
        meals.add(meal);
        addFromYear(meal);
    }

    public void deleteMeal(){
        //TODO
    }


    protected void addFromYear(Meal meal){
        int year = meal.getDate().get(GregorianCalendar.YEAR);

        TreeNode child = null;
        for(TreeNode node: root.getChildren()){
            if(node.getValue().equals(year)){
                child = node;
                break;
            }
        }

        if(child == null){
            child = new TreeNode(year);
            addNode(root, child);
        }

        addFromMonth(child, meal);
        expandNode(child);
    }

    protected void addFromMonth(TreeNode parent, Meal meal){
        String month = dateFormat.getMonths()[meal.getDate().get(GregorianCalendar.MONTH)];

        TreeNode child = null;
        for(TreeNode node: parent.getChildren()){
            if(node.getValue().equals(month)){
                child = node;
                break;
            }
        }

        if(child == null){
            child = new TreeNode(month);
            addNode(parent, child);
        }

        addFromWeek(child, meal);
        expandNode(child);
    }

    protected void addFromWeek(TreeNode parent, Meal meal){
        String week = "Week "+meal.getDate().get(GregorianCalendar.WEEK_OF_MONTH);

        TreeNode child = null;
        for(TreeNode node: parent.getChildren()){
            if(node.getValue().equals(week)){
                child = node;
                break;
            }
        }

        if(child == null){
            child = new TreeNode(week);
            addNode(parent, child);
        }

        addFromDay(child, meal);
        expandNode(child);
    }

    protected void addFromDay(TreeNode parent, Meal meal){
        String day = dateFormat.getWeekdays()[meal.getDate().get(GregorianCalendar.DAY_OF_WEEK)];
        day += " "+meal.getDate().get(GregorianCalendar.MONTH)+"/"+meal.getDate().get(GregorianCalendar.DAY_OF_MONTH);
        TreeNode child = null;
        for(TreeNode node: parent.getChildren()){
            if(node.getValue().equals(day)){
                child = node;
                break;
            }
        }

        if(child == null){
            child = new TreeNode(day);
            addNode(parent, child);
        }

        addMealNode(child, meal);
        expandNode(child);
    }

    protected void addMealNode(TreeNode parent, Meal meal){

        TreeNode child = null;
        for(TreeNode node: parent.getChildren()){
            if(node.getValue().equals(meal)){
                child = node;
                break;
            }
        }

        if(child == null){
            child = new TreeNode(meal);
            addNode(parent, child);
        }

    }
}
