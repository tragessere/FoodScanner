package senior_project.foodscanner.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import senior_project.foodscanner.Constants;
import senior_project.foodscanner.FoodItem;
import senior_project.foodscanner.Meal;
import senior_project.foodscanner.Nutritious;
import senior_project.foodscanner.R;
import senior_project.foodscanner.Settings;
import senior_project.foodscanner.backend_helpers.EndpointsHelper;
import senior_project.foodscanner.database.SQLQueryHelper;
import senior_project.foodscanner.fragments.AlertDialogFragment;
import senior_project.foodscanner.fragments.MessageDialogFragment;
import senior_project.foodscanner.fragments.ErrorDialogFragment;
import senior_project.foodscanner.fragments.CalendarDialogFragment;
import senior_project.foodscanner.ui.components.mealcalendar.MealArrayAdapter;

/**
 * Displays list or calendar of meals.
 * <p/>
 * Actions:
 * Add Meal - In Action Bar
 * View Meal - Click on an item in the list
 * Delete Meal - Swipe left on an item in the list
 * Log In - In Action Bar
 * - Takes user to Login/Register Activity
 * - When user is logged in:
 * - Button displays username
 * - Clicking this will log out the user
 * - Maybe a drop down menu for account settings
 */
public class MealCalendarActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, CalendarDialogFragment.CalendarDialogListener, AdapterView.OnItemClickListener, MealArrayAdapter.MealArrayAdapterListener, ErrorDialogFragment.ErrorDialogListener {
    private static final String SAVE_DATE = "currentDate";
    private static final int VIEW_MEAL = 0;

    private String username;

    private Button button_calendar;
    private Button button_totalDay;
    private Button button_totalWeek;
    private Button button_totalMonth;
    private View container_warning;
    private ListView mealListView;
    private View loadingIndicator;

    private MealArrayAdapter adapter;
    private int lastClickedMealPos;
    private Meal lastClickedMeal;

    private long currentDate;
    private ArrayList<Meal> meals = new ArrayList<>();
    private List<AsyncTask> syncTasks = new ArrayList<>();
    private AsyncTask loadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_meal_calendar);

        if (EndpointsHelper.getDownloadStatus() == Constants.DENSITY_NOT_DOWNLOADED) {
            EndpointsHelper.mEndpoints.new GetAllDensityEntriesTask(this).execute();
        }

        loadingIndicator = findViewById(R.id.loading);
        button_calendar = (Button) findViewById(R.id.button_calendar);
        button_totalDay = (Button) findViewById(R.id.button_total_day);
        button_totalWeek = (Button) findViewById(R.id.button_total_week);
        button_totalMonth = (Button) findViewById(R.id.button_total_month);
        mealListView = (ListView) findViewById(R.id.listView_meals);
        container_warning = findViewById(R.id.container_warning);

        loadingIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // consume click
            }
        });
        button_calendar.setOnClickListener(this);
        button_calendar.setOnLongClickListener(this);
        button_totalDay.setOnClickListener(this);
        button_totalWeek.setOnClickListener(this);
        button_totalMonth.setOnClickListener(this);
        container_warning.setOnClickListener(this);
        findViewById(R.id.imageButton_prev).setOnClickListener(this);
        findViewById(R.id.imageButton_next).setOnClickListener(this);
        adapter = new MealArrayAdapter(MealCalendarActivity.this, meals);
        adapter.setOnDeleteListener(this);
        mealListView.setAdapter(adapter);
        mealListView.setOnItemClickListener(this);

        username = getIntent().getStringExtra(LoginActivity.EXTRA_ACCOUNT_NAME);

        //TODO move this to run later?
        // delete Meals over 3 months old (28*3 days) if they are all synced with backend
        GregorianCalendar day = new GregorianCalendar();
        day.add(Calendar.DATE, -28 * 3);
        List<Meal> meals = SQLQueryHelper.getMealsBefore(day, true);
        boolean allSynced = true;
        for(Meal meal:meals){
            if(meal.isChanged()){
                allSynced = false;
                break;
            }
        }
        if(allSynced) {
            SQLQueryHelper.deleteMeals(meals);
        }

        if(savedInstanceState == null) {
            changeSelectedDay(System.currentTimeMillis(), false);
        } else {
            changeSelectedDay(savedInstanceState.getLong(SAVE_DATE), false);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle saved) {
        saved.putLong(SAVE_DATE, currentDate);
        cancelSyncing();
        cancelLoading();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadMeals();
        syncMeals();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_meal_calendar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Meal createMeal() {
        // time of meal is now
        GregorianCalendar mealTime = new GregorianCalendar();

        // currently selected date
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.setTimeInMillis(this.currentDate);

        // update time of meal with currently selected date
        mealTime.set(currentDate.get(GregorianCalendar.YEAR), currentDate.get(GregorianCalendar.MONTH), currentDate.get(GregorianCalendar.DAY_OF_MONTH));

        // get settings for 'guessed' meal
        Settings settings = Settings.getInstance();

        // create Meal
        Meal newMeal = new Meal(mealTime.getTimeInMillis(), settings.getCurrentMeal());

        // save meal to local storage
        long newId = SQLQueryHelper.insertMeal(newMeal);
        if(newId < 0) {
            ErrorDialogFragment.showErrorDialog(this, "Failed to write meal to local storage. Id returned = "+newId);
        }
        newMeal.setId(newId);

        // add meal to ui
        adapter.add(newMeal);
        updateUI();

        // no uploading to backend, because user will want to edit the meal right away
        // upload will take place when user returns to this activity

        return newMeal;
    }

    private void deleteMeal(int position){
        // Remove meal from calendar
        final Meal meal = adapter.getItem(position);
        adapter.remove(meal);
        updateUI();

        // Set delete flag just in case backend deletion fails
        meal.setIsDeleted();
        SQLQueryHelper.updateMeal(meal);

        // delete from backend and on success, delete from local
        syncMeals();
    }


    private void updateBackground(){
        if(adapter.getCount() <= 1) {
            GregorianCalendar day = new GregorianCalendar();
            day.setTimeInMillis(currentDate);
            if(day.get(Calendar.MONTH) == Calendar.APRIL && day.get(Calendar.DAY_OF_MONTH) == 1)
            {
                mealListView.setBackgroundResource(R.drawable.background_add_meal_fool);// April Fool's Easter Egg
            }
            else
            {
                mealListView.setBackgroundResource(R.drawable.background_add_meal);
            }
        }
        else{
            mealListView.setBackgroundResource(0);
        }
    }

    private void updateUI(){
        updateDayTotal();
        updateBackground();
    }

    private void viewMeal(Meal meal) {
        // Note: uses Serializable to pass the meal which may have high overhead. Consider Parcelable as alternative.
        Intent intent = new Intent(MealCalendarActivity.this, MealDetailsActivity.class);
        intent.putExtra("meal", meal);
        startActivityForResult(intent, VIEW_MEAL);
    }

    private void changeSelectedDay(GregorianCalendar cal) {
        changeSelectedDay(cal.getTimeInMillis());
    }

    private void changeSelectedDay(GregorianCalendar cal, boolean loadMeals) {
        changeSelectedDay(cal.getTimeInMillis(), loadMeals);
    }

    private void changeSelectedDay(long date) {
        changeSelectedDay(date, true);
    }

    private void changeSelectedDay(long date, boolean loadMeals) {
        currentDate = date;
        button_calendar.setText(Settings.getInstance().formatDate(date));
        if(loadMeals) {
            loadMeals(date);
        }
    }

    private void cancelLoading(){
        if(loadTask != null){
            loadTask.cancel(true);
        }
    }

    private void loadMeals() {
        loadMeals(currentDate);
    }

    private void loadMeals(long date) {//TODO remove redundant calls
        cancelLoading();

        GregorianCalendar day1 = new GregorianCalendar();
        day1.setTimeInMillis(date);
        GregorianCalendar day2 = new GregorianCalendar();
        day2.setTimeInMillis(date);

        // clear ui
        adapter.clear();

        // load meals from local into ui
        Object[] meals = SQLQueryHelper.getMeals(day1, day2, true).toArray();
        if(meals.length > 0) {
            Arrays.sort(meals);
            for(Object meal : meals) {
                adapter.add((Meal) meal);
            }
            updateUI();
        }
        else{// no meals found locally, check the server
            //TODO test this, with and without connection
            //TODO bug: executes right away for some reason
            /*loadTask = EndpointsHelper.mEndpoints.new GetMealsWithinDatesTask(new EndpointsHelper.TaskCompletionListener(){
                @Override
                public void onTaskCompleted(Bundle b) {
                    // save meals locally and add them to ui
                    Meal[] meals = (Meal[]) b.getSerializable(EndpointsHelper.TASKID_MEALS_GET);
                    if(meals != null) {
                        Arrays.sort(meals);
                        for(Meal meal:meals){
                            meal.setId(SQLQueryHelper.insertMeal(meal));
                            adapter.add(meal);
                        }
                    }
                    else{
                        //TODO better icon
                        MessageDialogFragment.newInstance("Please check your internet connection.", "Error: Loading meals from server failed", android.R.drawable.ic_dialog_alert).show(getFragmentManager(), "Server Load Fail");
                    }
                    loadingIndicator.setVisibility(View.GONE);
                    updateUI();
                    updateSyncWarnings();
                }
            });*/
            //loadTask.execute(new Date(date), new Date(date));//TODO

            //loadingIndicator.setVisibility(View.VISIBLE);//TODO
        }
    }

    private boolean isSyncing(){
        for(AsyncTask task:syncTasks){
            if(!task.isCancelled() && task.getStatus() != AsyncTask.Status.FINISHED){
                // task not cancelled and not finished
                return true;
            }
        }
        return false;
    }

    private void cancelSyncing(){
        // cancel all previous syncing tasks
        for(AsyncTask task:syncTasks){
            task.cancel(true);
        }
    }
    /**
     * Syncs meals with server.
     * Uploads changed meals, and deletes changed meals to be deleted.
     */
    private void syncMeals(){
        // TODO test deleting a nonuploaded meal
        // TODO test multiple calls to syncing before finished
        // TODO test no connection
        // TODO test what happens to tasks when the app process is killed by the user or phone powers off
        // TODO test for race condition server side
        cancelSyncing();

        List<Meal> unsyncedMeals = SQLQueryHelper.getChangedMeals();
        syncTasks = new ArrayList<>(unsyncedMeals.size());
        for(Meal meal : unsyncedMeals) {
            if(meal != null) {
                if(meal.isChanged()){
                    if(meal.isDeleted()) {// delete meal from backend
                        EndpointsHelper.DeleteMealTask task = EndpointsHelper.mEndpoints.new DeleteMealTask(new EndpointsHelper.TaskCompletionListener() {
                            @Override
                            public void onTaskCompleted(Bundle b) {
                                Meal meal = (Meal) b.getSerializable(EndpointsHelper.TASKID_MEAL_DELETE);
                                if(meal != null) {
                                    if(b.getBoolean(EndpointsHelper.TASKID_MEAL_DELETE, false)) {//TODO conflicting values for same key?
                                        // delete from local storage
                                        SQLQueryHelper.deleteMeal(meal);
                                    }
                                }
                                updateSyncWarnings();
                            }
                        });
                        //syncTasks.add(task);//TODO
                        //task.execute(meal);//TODO
                    }
                    else{// save/update meal in backend
                        EndpointsHelper.SaveMealTask task = EndpointsHelper.mEndpoints.new SaveMealTask(new EndpointsHelper.TaskCompletionListener() {
                            @Override
                            public void onTaskCompleted(Bundle b) {
                                Meal meal = (Meal) b.getSerializable(EndpointsHelper.TASKID_MEAL_SAVE);
                                if(meal != null) {
                                    if(b.getBoolean(EndpointsHelper.TASKID_MEAL_SAVE, false)) {//TODO conflicting values for same key?
                                        // update local storage
                                        if(meal.isMoreRecentlyChangedThan(SQLQueryHelper.getMeal(meal.getId()))){ // only set meal unchanged if it was not changed during syncing
                                            meal.setUnchanged();
                                            SQLQueryHelper.updateMeal(meal);
                                        }
                                    }
                                }
                                updateSyncWarnings();
                            }
                        });
                        //syncTasks.add(task);//TODO
                        //task.execute(meal);//TODO
                    }
                }
            }
        }

        updateSyncWarnings();
    }

    /**
     * Enables or disables sync warnings depending on whether or not the app is syncing.
     */
    private void updateSyncWarnings(){
        if(!isSyncing()){
            adapter.setWarningsEnabled(true);

            List<Meal> unsyncedMeals =  SQLQueryHelper.getChangedMeals();
            if(!unsyncedMeals.isEmpty()) {
                container_warning.setVisibility(View.VISIBLE);
            }
            else {
                container_warning.setVisibility(View.GONE);
            }
        }
        else{
            adapter.setWarningsEnabled(false);
            container_warning.setVisibility(View.GONE);
        }
    }

    private void updateDayTotal() {
        Map<String, Double> nutr = Nutritious.calculateTotalNutrition(meals);
        int cal = 0;
        if(nutr != null) {
            Double calD = nutr.get(FoodItem.KEY_CAL);
            if(calD != null) {
                cal = calD.intValue();
            }
        }
        button_totalDay.setText("Day: " + cal + " Cal");
    }


    @Override
    public void onClick(View v) {//TODO totals with backend meals
        switch(v.getId()) {
            case R.id.button_calendar:
                CalendarDialogFragment d = CalendarDialogFragment.newInstance(currentDate);
                d.show(getFragmentManager(), "Calendar");
                break;
            case R.id.button_total_day:{
                String title = "<b>Total Daily Nutrition</b><br>"+Settings.getInstance().formatDate(currentDate, Settings.DateFormat.mn_dn_yn);
                MessageDialogFragment dialog = MessageDialogFragment.newInstance(Nutritious.nutritionText(Nutritious.calculateTotalNutrition(meals)), Html.fromHtml(title), 0);
                dialog.show(getFragmentManager(), "Total Day");
                break;}
            case R.id.button_total_week:{
                GregorianCalendar day1 = new GregorianCalendar();
                day1.setTimeInMillis(currentDate);
                day1.add(Calendar.DATE, -6);// 7 days ago, including today

                GregorianCalendar day2 = new GregorianCalendar();
                day2.setTimeInMillis(currentDate);// today

                List<Meal> meals = SQLQueryHelper.getMeals(day1, day2, true);

                day2.add(Calendar.DATE, -1);
                String title = "<b>Total 7 Day Nutrition</b><br>"+Settings.getInstance().formatDate(day1, Settings.DateFormat.mn_dn_yn) + " - " + Settings.getInstance().formatDate(day2, Settings.DateFormat.mn_dn_yn);

                MessageDialogFragment dialog = MessageDialogFragment.newInstance(Nutritious.nutritionText(Nutritious.calculateTotalNutrition(meals)), Html.fromHtml(title), 0);
                dialog.show(getFragmentManager(), "Total Week");
                break;}
            case R.id.button_total_month:{
                GregorianCalendar day1 = new GregorianCalendar();
                day1.setTimeInMillis(currentDate);
                day1.add(Calendar.DATE, -27);// 28 days ago, including today

                GregorianCalendar day2 = new GregorianCalendar();
                day2.setTimeInMillis(currentDate);// today

                List<Meal> meals = SQLQueryHelper.getMeals(day1, day2, true);

                day2.add(Calendar.DATE, -1);

                String title = "<b>Total 28 Day Nutrition</b><br>"+Settings.getInstance().formatDate(day1, Settings.DateFormat.mn_dn_yn) + " - " + Settings.getInstance().formatDate(day2, Settings.DateFormat.mn_dn_yn);

                MessageDialogFragment dialog = MessageDialogFragment.newInstance(Nutritious.nutritionText(Nutritious.calculateTotalNutrition(meals)), Html.fromHtml(title), 0);
                dialog.show(getFragmentManager(), "Total Month");
                break;}
            case R.id.imageButton_prev:{
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTimeInMillis(currentDate);
                cal.add(Calendar.DATE, -1);
                changeSelectedDay(cal.getTimeInMillis());
                break;}
            case R.id.imageButton_next:{
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTimeInMillis(currentDate);
                cal.add(Calendar.DATE, 1);
                changeSelectedDay(cal.getTimeInMillis());
                break;}
            case R.id.container_warning:
                Log.d("MealCalendar", "CLICKED WARNING BAR");
                warningClick();
                break;
            default:
                // Do nothing
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if(v.getId() == R.id.button_calendar){
            changeSelectedDay(System.currentTimeMillis());
        }
        return true;
    }

    @Override
    public void onCalendarDialogDateSelected(GregorianCalendar date) {
        changeSelectedDay(date);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Meal meal = (Meal) parent.getItemAtPosition(position);
        if(meal == null) {
            meal = createMeal();
        }
        lastClickedMealPos = position;
        lastClickedMeal = meal;
        viewMeal(meal);
    }


    @Override
    public void onDelete(final MealArrayAdapter adapter, final int position) {

        // Create deletion confirmation dialog
        Meal selectedMeal = adapter.getItem(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this " +
                selectedMeal.getType().getName() + " meal entry?")
                .setTitle("Confirm Meal Deletion");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteMeal(position);

                // toast
                Toast butteredToast = Toast.makeText(getApplicationContext(), "Removed from calendar.",
                        Toast.LENGTH_SHORT);
                butteredToast.show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        // Create the AlertDialog
        AlertDialog confirmDialog = builder.create();
        confirmDialog.show();
    }

    public void warningClick(){
        AlertDialogFragment d = new AlertDialogFragment();
        d.setMessage("Sync meals with server?");
        d.setPositiveButton("Sync", new AlertDialogFragment.OnClickListener() {
            @Override
            public void onClick(AlertDialogFragment dialog, int which) {
                syncMeals();
            }
        });
        d.setNegativeButton("Cancel", null);
        d.show(getFragmentManager(), "Sync");
    }

    @Override
    public void onWarning(MealArrayAdapter adapter, int position) {
        warningClick();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case VIEW_MEAL:
                if(resultCode == RESULT_OK) {
                    Meal meal = (Meal) data.getSerializableExtra("meal");
                    SQLQueryHelper.updateMeal(meal);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onErrorDialogClose() {
        finish();
    }

}
