package senior_project.foodscanner.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import senior_project.foodscanner.TestingModeService;
import senior_project.foodscanner.backend_helpers.EndpointsHelper;
import senior_project.foodscanner.database.SQLQueryHelper;
import senior_project.foodscanner.fragments.AlertDialogFragment;
import senior_project.foodscanner.fragments.LoadingDialogFragment;
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
 *
 * Implementation Notes:
 *      Meal Queries By Day:
 *          Queries for meals should always be for the entire day, not part of a day.
 *          The app uses this assumption by only pulling meals from the server if there are no meals found locally for that day.
 *          This is not best practice.
 *      Background Task: Load Meals from Server for Calendar
 *          Always gets cancelled as soon as user leaves activity.
 *          Cancelled when another loadMeals task is started.
 *      Background Task: Load Meals from Server for Total Dialog
 *          Same as above.
 *      Background Task: Sync Meals with Server
 *          Always continues running in background even after user leaves activity.
 *          Cancelled when another syn meals task is started.
 *          Cancelled when user goes to SettingsActivity, because syncing should not run if the user logs out.
 *      Old Meal Deletion:
 *          Whenever user
 *
 *
 *  TODO Unhandled Edge Cases:
 *      Race condition server side untested
 *      Multiple devices logged in as same user can cause syncing conflicts
 *      Moving between time zones may cause inconsistencies
 *      Saving meal when storage full
 *  TODO Optimization:
 *      Backend server should support bulk deletes and save queries.
 *      It may be better to have some sort of locally stored data structure to check off the days that are not synced.
 *      loadMeals_Total algorithm might be improved.
 *      Storing a time-zone independent timestamp for lastChanged in Meal would prevent conflicts.
 *  TODO Useful Unimplemented Features:
 *      Able to create custom food item with custom nutrition values and quantity type.
 *      Camera level UI so user can align perfectly vertical and horizontal.
 */
public class MealCalendarActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, CalendarDialogFragment.CalendarDialogListener, AdapterView.OnItemClickListener, MealArrayAdapter.MealArrayAdapterListener, ErrorDialogFragment.ErrorDialogListener {
    private static final String SAVE_DATE = "currentDate";
    private static final int VIEW_MEAL = 0;
    private static final int MONTH = 30; // used for total month nutrition
    private static final int WEEK = 7; // used for total week nutrition
    private static final int LOCAL_STORAGE_LIMIT = MONTH*3; // number of days before today to keep meals locally for

    private Button button_calendar;
    private Button button_totalDay;
    private Button button_totalWeek;
    private Button button_totalMonth;
    private View container_warning;
    private TextView textView_warning;
    private ImageView imageView_warning;
    private ProgressBar progressBar_syncing;
    private ListView mealListView;
    private View loadingIndicator;

    private MealArrayAdapter adapter;

    private long currentDate;
    private ArrayList<Meal> meals = new ArrayList<>();
    private AsyncTaskList syncTasks = new AsyncTaskList();
    private AsyncTask loadTask_Calendar;
    private AsyncTask loadTask_Total;
    LoadingDialogFragment dialog_total_loading;

    private enum WarningBarUIState{
        WARNING, SYNCING, HIDDEN;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MealCalendarActivity", "onCreate()");

        if (EndpointsHelper.getDownloadStatus() == Constants.DENSITY_NOT_DOWNLOADED) {
            EndpointsHelper.mEndpoints.new GetAllDensityEntriesTask(this).execute();
        }

        deleteOldMeals();

        setContentView(R.layout.activity_meal_calendar);

        loadingIndicator = findViewById(R.id.loading);
        button_calendar = (Button) findViewById(R.id.button_calendar);
        button_totalDay = (Button) findViewById(R.id.button_total_day);
        button_totalWeek = (Button) findViewById(R.id.button_total_week);
        button_totalMonth = (Button) findViewById(R.id.button_total_month);
        mealListView = (ListView) findViewById(R.id.listView_meals);
        container_warning = findViewById(R.id.container_warning);
        progressBar_syncing = (ProgressBar)container_warning.findViewById(R.id.progressBar_syncing);
        imageView_warning = (ImageView)container_warning.findViewById(R.id.imageView_icon);
        textView_warning = (TextView)container_warning.findViewById(R.id.textView);

        button_totalMonth.setText(MONTH + " Day");
        button_totalWeek.setText(WEEK + " Day");

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

        if(savedInstanceState == null) {
            changeSelectedDay(System.currentTimeMillis(), false);
        } else {
            changeSelectedDay(savedInstanceState.getLong(SAVE_DATE), false);
        }
    }

    private void deleteOldMeals(){
        GregorianCalendar day = new GregorianCalendar();
        day.add(Calendar.DATE, -LOCAL_STORAGE_LIMIT);
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
    }

    @Override
    protected void onSaveInstanceState(Bundle saved) {
        Log.d("MealCalendarActivity", "onSavedInstanceState()");
        saved.putLong(SAVE_DATE, currentDate);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("MealCalendarActivity", "onRestart()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("MealCalendarActivity", "onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MealCalendarActivity", "onResume()");
        loadMeals_Calendar_Start();
        syncMeals_Start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MealCalendarActivity", "onPause()");
        loadMeals_Total_Cancel();
        loadMeals_Calendar_Cancel();
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d("MealCalendarActivity", "onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MealCalendarActivity", "onDestroy()");
        deleteOldMeals();
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
            cancelSyncingTasks();
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
        syncMeals_Start();
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
        loadMeals_Calendar_Cancel();

        currentDate = date;
        button_calendar.setText(Settings.getInstance().formatDate(date));
        if(loadMeals) {
            loadMeals_Calendar_Start();
        }
    }

    private void cancelLoadingTask_Total(){
        if(loadTask_Total != null){
            if(!loadTask_Total.isCancelled() && loadTask_Total.getStatus() != AsyncTask.Status.FINISHED) {
                loadTask_Total.cancel(true);
                loadTask_Total = null;
            }
        }
    }

    private void cancelLoadingTask_Calendar(){
        if(loadTask_Calendar != null){
            if(!loadTask_Calendar.isCancelled() && loadTask_Calendar.getStatus() != AsyncTask.Status.FINISHED) {
                loadTask_Calendar.cancel(true);
                loadTask_Calendar = null;
            }
        }
    }

    private void loadMeals_Total_Cancel(){
        cancelLoadingTask_Total();
        if(dialog_total_loading != null){
            dialog_total_loading.dismiss();
        }
    }

    private void loadMeals_Total_Start(final int days){//TODO test correctness: server pull
        cancelLoadingTask_Total();

        final GregorianCalendar day1 = new GregorianCalendar();
        day1.setTimeInMillis(currentDate);
        day1.add(Calendar.DATE, -(days - 1));// x days ago, including today

        final GregorianCalendar day2 = new GregorianCalendar();
        day2.setTimeInMillis(currentDate);// today

        final CharSequence title = Html.fromHtml("<b>Total "+days+" Day Nutrition</b><br>"+Settings.getInstance().formatDate(day1, Settings.DateFormat.mn_dn_yn) + " - " + Settings.getInstance().formatDate(day2, Settings.DateFormat.mn_dn_yn));

        dialog_total_loading = new LoadingDialogFragment();
        dialog_total_loading.setTitle(title);
        dialog_total_loading.setMessage("Loading meals from server...");
        dialog_total_loading.setPositiveButton("Cancel", new AlertDialogFragment.OnClickListener() {
            @Override
            public void onClick(AlertDialogFragment dialog, int which) {
                loadMeals_Total_Cancel();
            }
        });
        dialog_total_loading.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                loadMeals_Total_Cancel();
            }
        });
        dialog_total_loading.show(getFragmentManager(), "Loading Dialog");

        // download meals
        loadTask_Total = EndpointsHelper.mEndpoints.new GetMealsWithinDatesTask(new EndpointsHelper.TaskCompletionListener(){
            @Override
            public void onTaskCompleted(AsyncTask task, Bundle b) {
                if(task.isCancelled()) {
                    Log.d("MealCalendarActivity","LOADING TOTAL: CANCELLED");
                    return;
                }
                Log.d("MealCalendarActivity", "LOADING TOTAL: RETURNED");
                boolean error = false;
                List<Meal> meals = new ArrayList<>();
                ArrayList<Meal> mealsServer = (ArrayList<Meal>) b.getSerializable(EndpointsHelper.TASKID_MEALS_GET);
                if(mealsServer != null) {
                    // get all local meals, add server meals to list if dont exist locally (incorrect if user is using multiple devices)
                    meals = SQLQueryHelper.getMeals(day1, day2, true);
                    for(Meal serverMeal : mealsServer) {
                        boolean existsLocally = false;
                        for(Meal localMeal:meals){
                            if(serverMeal.getId() == localMeal.getId()){
                                existsLocally = true;
                                break;
                            }
                        }
                        if(!existsLocally){
                            serverMeal.setId(SQLQueryHelper.insertMeal(serverMeal));// store server meals locally
                            meals.add(serverMeal);
                        }
                    }
                } else {
                    // search all days locally, if day is empty then error
                    for(int i = 0; i < days; i++){
                        GregorianCalendar day2 = new GregorianCalendar();
                        day2.setTimeInMillis(day1.getTimeInMillis());
                        day2.add(Calendar.DATE, i);
                        List<Meal> localMeals = SQLQueryHelper.getMeals(day1, day2,true);
                        if(!localMeals.isEmpty()){
                            meals.addAll(localMeals);
                        }
                        else{
                            error = true;
                            break;
                        }
                    }
                }
                if(error) {
                    String message = "Unknown Reason";
                    String title = "Error: Loading meals from server failed";
                    int icon = R.drawable.ic_error_outline_black;
                    Exception e = (Exception) b.getSerializable(EndpointsHelper.TASKID_MEALS_GET_EXCEPTION);
                    if(e != null) {
                        if(isConnectedToInternet()) {
                            message = "Network error.";
                        } else {
                            message = "You are not connected to the internet.";
                        }
                    }
                    MessageDialogFragment.newInstance(message, title, icon).show(getFragmentManager(), "Server Load Fail");
                }
                else {
                    dialog_total_loading.dismiss();
                    MessageDialogFragment dialog = MessageDialogFragment.newInstance(Nutritious.nutritionText(Nutritious.calculateTotalNutrition(meals)), title, 0);
                    dialog.show(getFragmentManager(), "Total");
                }
            }
        }, TestingModeService.TESTMODE_CALENDAR_FAKE_SERVER).execute(new Date(day1.getTimeInMillis()), new Date(day2.getTimeInMillis()));
    }

    private void loadMeals_Calendar_Finish(){
        loadingIndicator.setVisibility(View.GONE);
        updateUI();
    }

    private void loadMeals_Calendar_Start() {
        loadMeals_Calendar_Start(currentDate);
    }

    private void loadMeals_Calendar_Cancel(){
        cancelLoadingTask_Calendar();
        loadMeals_Calendar_Finish();
    }

    private void loadMeals_Calendar_Start(long date) {
        loadMeals_Calendar_Cancel();

        GregorianCalendar day1 = new GregorianCalendar();
        day1.setTimeInMillis(date);
        GregorianCalendar day2 = new GregorianCalendar();
        day2.setTimeInMillis(date);

        // clear ui
        adapter.clear();

        // load meals from local into ui
        Object[] meals = SQLQueryHelper.getMeals(day1, day2, true).toArray();
        if(meals.length > 0) {
            Log.d("MealCalendarActivity", "LOADING LOCAL");
            Arrays.sort(meals);
            for(Object meal : meals) {
                adapter.add((Meal) meal);
            }
        }
        else{// no meals found locally, check the server
            Log.d("MealCalendarActivity","LOADING BACKEND");
            loadingIndicator.setVisibility(View.VISIBLE);
            loadTask_Calendar = EndpointsHelper.mEndpoints.new GetMealsWithinDatesTask(new EndpointsHelper.TaskCompletionListener(){
                @Override
                public void onTaskCompleted(AsyncTask task, Bundle b) {
                    if(task.isCancelled()) {
                        Log.d("MealCalendarActivity","LOADING BACKEND: CANCELLED");
                        return;
                    }
                    Log.d("MealCalendarActivity", "LOADING BACKEND: RETURNED");
                    // save meals locally and add them to ui
                    ArrayList<Meal> mealsList = (ArrayList<Meal>) b.getSerializable(EndpointsHelper.TASKID_MEALS_GET);
                    if(mealsList != null) {
                        Object[] meals = mealsList.toArray();
                        Arrays.sort(meals);
                        for(Object obj : meals) {
                            Meal meal = (Meal) obj;
                            meal.setId(SQLQueryHelper.insertMeal(meal));
                            adapter.add(meal);
                        }
                    } else {
                        String message = "Unknown Reason";
                        String title = "Error: Loading meals from server failed";
                        int icon = R.drawable.ic_error_outline_black;
                        Exception e = (Exception) b.getSerializable(EndpointsHelper.TASKID_MEALS_GET_EXCEPTION);
                        if(e != null) {
                            if(isConnectedToInternet()) {
                                message = "Network error.";
                            } else {
                                message = "You are not connected to the internet.";
                            }
                        }
                        MessageDialogFragment.newInstance(message, title, icon).show(getFragmentManager(), "Server Load Fail");
                    }
                    loadMeals_Calendar_Finish();
                }
            }, TestingModeService.TESTMODE_CALENDAR_FAKE_SERVER).execute(new Date(date), new Date(date));
        }
        updateUI();
    }

    private boolean isConnectedToInternet(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private boolean isSyncing(){
        for(AsyncTask task:syncTasks){
            if(!syncTasks.isFinished(task)){
                // task not cancelled and not finished
                return true;
            }
        }
        return false;
    }

    private void cancelSyncingTasks(){
        // cancel all previous syncing tasks
        for(AsyncTask task:syncTasks){
            task.cancel(true);
        }
        syncTasks.clear();
    }

    private void syncMeals_End(){
        if(!isSyncing()){
            // reload meals into calendar since their states have been changed
            loadMeals_Calendar_Start();

            // update warning bar ui
            adapter.setWarningsEnabled(true);
            List<Meal> unsyncedMeals =  SQLQueryHelper.getChangedMeals();
            if(!unsyncedMeals.isEmpty()) {
                setWarningBarUIState(WarningBarUIState.WARNING);
            }
            else {
                container_warning.setVisibility(View.GONE);
                setWarningBarUIState(WarningBarUIState.HIDDEN);
            }
        }
        else{
            // update warning bar ui
            adapter.setWarningsEnabled(false);
            setWarningBarUIState(WarningBarUIState.SYNCING);
        }
    }

    /**
     * Syncs meals with server.
     * Uploads changed meals, and deletes changed meals to be deleted.
     */
    private void syncMeals_Start(){
        // TODO test deleting a nonuploaded meal
        Log.d("MealCalendarActivity", "SYNC START");
        cancelSyncingTasks();

        List<Meal> unsyncedMeals = SQLQueryHelper.getChangedMeals();
        syncTasks.clear();
        for(Meal meal : unsyncedMeals) {
            if(meal != null) {
                if(meal.isChanged()){
                    final int currentIndex = unsyncedMeals.indexOf(meal);
                    if(meal.isDeleted()) {// delete meal from backend
                        //TODO java.lang.IllegalArgumentException: DELETE with non-zero content length is not supported
                        Log.d("MealCalendarActivity", "SYNC DELETE " + meal);
                        syncTasks.add(EndpointsHelper.mEndpoints.new DeleteMealTask(new EndpointsHelper.TaskCompletionListener() {
                            @Override
                            public void onTaskCompleted(AsyncTask task, Bundle b) {
                                if(!task.isCancelled()) {
                                    Meal meal = (Meal) b.getSerializable(EndpointsHelper.TASKID_MEAL_DELETE);
                                    Log.d("MealCalendarActivity", "SYNC DELETE RETURNED " + meal);
                                    if(meal != null) {
                                        if(b.getBoolean(EndpointsHelper.TASKID_MEAL_DELETE_SUCCESS, false)) {
                                            // delete from local storage
                                            SQLQueryHelper.deleteMeal(meal);
                                        }
                                    }
                                }
                                else{
                                    Log.d("MealCalendarActivity", "SYNC DELETE CANCELLED");
                                }
                                try {
                                    syncTasks.setFinished(task, true);
                                }
                                catch(IndexOutOfBoundsException e){
                                    // Do nothing, this task doesn't matter anymore
                                }
                                syncMeals_End();
                            }
                        }, TestingModeService.TESTMODE_CALENDAR_FAKE_SERVER).execute(meal));
                    } else {// save/update meal in backend
                        //TODO 400 Bad Request
                        Log.d("MealCalendarActivity", "SYNC SAVE " + meal);
                        final int changed = meal.isChangedCount();
                        meal.setUnchanged();
                        syncTasks.add(EndpointsHelper.mEndpoints.new SaveMealTask(new EndpointsHelper.TaskCompletionListener() {
                            @Override
                            public void onTaskCompleted(AsyncTask task, Bundle b) {
                                if(!task.isCancelled()) {
                                    Meal meal = (Meal) b.getSerializable(EndpointsHelper.TASKID_MEAL_SAVE);
                                    Log.d("MealCalendarActivity", "SYNC SAVE RETURNED " + meal);
                                    if(meal != null) {
                                        if(b.getBoolean(EndpointsHelper.TASKID_MEAL_SAVE_SUCCESS, false)) {
                                            // update local storage
                                            Meal localMeal = SQLQueryHelper.getMeal(meal.getId());
                                            if(changed == localMeal.isChangedCount()) { // only update locally if it was not changed during syncing
                                                SQLQueryHelper.updateMeal(meal);
                                            }
                                        }
                                    }
                                }
                                else{
                                    Log.d("MealCalendarActivity", "SYNC SAVE CANCELLED");
                                }
                                try {
                                    syncTasks.setFinished(task, true);
                                }
                                catch(IndexOutOfBoundsException e){
                                    // Do nothing, this task doesn't matter anymore
                                }
                                syncMeals_End();
                            }
                        }, TestingModeService.TESTMODE_CALENDAR_FAKE_SERVER).execute(meal));
                    }
                }
            }
        }

        syncMeals_End();
    }

    private void setWarningBarUIState(WarningBarUIState state){
        switch(state){
            case HIDDEN:
                container_warning.setVisibility(View.GONE);
                break;
            case WARNING:
                container_warning.setVisibility(View.VISIBLE);
                container_warning.setBackgroundResource(R.color.Warning);
                progressBar_syncing.setVisibility(View.GONE);
                imageView_warning.setVisibility(View.VISIBLE);
                textView_warning.setText("Meals are not synced with the server!");
                container_warning.setEnabled(true);
                break;
            case SYNCING:
                container_warning.setVisibility(View.VISIBLE);
                container_warning.setBackgroundResource(R.color.Syncing);
                imageView_warning.setVisibility(View.GONE);
                progressBar_syncing.setVisibility(View.VISIBLE);
                textView_warning.setText("Sync in progress...");
                container_warning.setEnabled(false);
                break;
            default:
                break;
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
    public void onClick(View v) {
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
                loadMeals_Total_Start(WEEK);
                break;}
            case R.id.button_total_month:{
                loadMeals_Total_Start(MONTH);
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
        d.setTitle("Sync meals with server?");
        d.setMessage("This will be done in the background.");
        d.setPositiveButton("Sync", new AlertDialogFragment.OnClickListener() {
            @Override
            public void onClick(AlertDialogFragment dialog, int which) {
                syncMeals_Start();
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

    /**
     * Custom way to set an AsyncTask as finished.
     */
    private class AsyncTaskList extends ArrayList<AsyncTask>{
        private List<Boolean> syncTasks_Finished = new ArrayList<>();

        @Override
        public boolean add(AsyncTask task){
            syncTasks_Finished.add(false);
            return super.add(task);
        }

        @Override
        public void clear(){
            syncTasks_Finished.clear();
            super.clear();
        }

        public boolean isFinished(AsyncTask task){
            int i = indexOf(task);
            return syncTasks_Finished.get(i) || task.getStatus() == AsyncTask.Status.FINISHED || task.isCancelled();
        }

        public void setFinished(AsyncTask task, boolean isFinished){
            int i = indexOf(task);
            syncTasks_Finished.set(i, isFinished);
        }
    }



}
