package com.example.viktoria.reminderexample;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by viktoria on 28.01.15.
 */
public class MainActivity extends Activity implements ReminderListFragment.ReminderListListener, ReminderFragment.OnReminderChangeListener {
    private ArrayList<Reminder> reminderItems;
    /**
     * helps format date and time into selected format
     */
    public static final Format dateFormat = new SimpleDateFormat("dd MMMM yyyy");
    public static final Format timeFormat = new SimpleDateFormat("HH:mm");
    public static final String TAG = "REMINDER_LOGS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            reminderItems = retrieveReminders();
            ReminderListFragment list_fr = new ReminderListFragment();
            Bundle args = new Bundle();
            args.putParcelableArrayList(getString(R.string.reminderListIntent), reminderItems);
            list_fr.setArguments(args);
            getFragmentManager().beginTransaction().replace(R.id.content_frame, list_fr,
                    "list_fr").commit(); //set ReminderListFragment as visible one
        } else {
            reminderItems = savedInstanceState.getParcelableArrayList(getString(R.string.reminderListIntent)); //retain list from saved state, no need to go to db again
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //get all reminders from db
    private ArrayList<Reminder> retrieveReminders() {
        DatabaseHandler db = new DatabaseHandler(this);
        reminderItems = (ArrayList<Reminder>) db.getAllReminders();
        return reminderItems;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(getString(R.string.reminderListIntent), reminderItems);
    }


    @Override
    public void onItemClick(int position) {
        ReminderFragment reminderFragment = new ReminderFragment();
        Bundle arg = new Bundle();
        arg.putParcelable(getString(R.string.reminderIntent), reminderItems.get(position));
        reminderFragment.setArguments(arg);
        getFragmentManager().beginTransaction().replace(R.id.content_frame, reminderFragment,
                "reminder_fr").addToBackStack(
                "reminder_fr").commit();
    }


    @Override
    public void onReminderCreated(Reminder r) {
        DatabaseHandler db = new DatabaseHandler(this);
        reminderItems.add(r);
        r.setId(reminderItems.size() - 1);
        db.addReminder(r);
        ((ReminderListFragment) getFragmentManager().findFragmentByTag("list_fr")).setReminderItems(reminderItems);
        getFragmentManager().popBackStack();
        Log.e(MainActivity.TAG, getString(R.string.logMes) + " " + r.getTitle() + " (" + r.getId() + ") " + getString(R.string.logMesCreated) + " " + dateFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime() * 60 * 1000) + ", " + timeFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime() * 60 * 1000));
    }

    @Override
    public void onReminderUpdate(Reminder r) {
        DatabaseHandler db = new DatabaseHandler(this);
        db.updateReminder(r);
        for (int i = 0; i < reminderItems.size(); i++) {
            if (reminderItems.get(i).equals(r)) {
                reminderItems.set(i, r);
                break;
            }
        }
        ((ReminderListFragment) getFragmentManager().findFragmentByTag("list_fr")).setReminderItems(reminderItems);
        getFragmentManager().popBackStack();
        Log.e(MainActivity.TAG, getString(R.string.logMes) + " " + r.getTitle() + " (" + r.getId() + ") " + getString(R.string.logMesUpdated) + " " + dateFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime() * 60 * 1000) + ", " + timeFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime() * 60 * 1000));

    }

    @Override
    public void onReminderDelete(Reminder r) {
        DatabaseHandler db = new DatabaseHandler(this);
        db.deleteReminder(r);
        reminderItems.remove(r);
        ((ReminderListFragment) getFragmentManager().findFragmentByTag("list_fr")).setReminderItems(reminderItems);
        getFragmentManager().popBackStack();
        Log.e(MainActivity.TAG, getString(R.string.logMes) + " " + r.getTitle() + " (" + r.getId() + ") " + getString(R.string.logMesDeleted));
        Log.e(MainActivity.TAG,"amount of reminders:"+ db.getRemindersCount());
        if (r.isCalendarEventAdded()) {

        } else {
            Intent intentAlarm = new Intent(this, MyReceiver.class);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(PendingIntent.getBroadcast(this, 1, intentAlarm, 0));
        }
    }

    @Override
    public void onReminderBatchDelete(ArrayList<Reminder> reminders) {
        DatabaseHandler db = new DatabaseHandler(this);
        for (Reminder r : reminders) {
            db.deleteReminder(r);
            reminderItems.remove(r);
            Log.e(MainActivity.TAG, getString(R.string.logMes) + " " + r.getTitle() + " (" + r.getId() + ") " + getString(R.string.logMesDeleted));
        }
        Log.e(MainActivity.TAG,"amount of reminders:"+ db.getRemindersCount());
    }

}
