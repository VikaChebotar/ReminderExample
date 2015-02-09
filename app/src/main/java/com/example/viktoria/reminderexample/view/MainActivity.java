package com.example.viktoria.reminderexample.view;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.viktoria.reminderexample.R;
import com.example.viktoria.reminderexample.services.ReminderReceiver;
import com.example.viktoria.reminderexample.services.SyncBdaysService;
import com.example.viktoria.reminderexample.utils.DatabaseHandler;
import com.example.viktoria.reminderexample.utils.Reminder;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Activity that consist of 2 screens - list of reminders and detail reminder screen.
 * Activity show all existing reminders, allow to add new, edit or delete reminder. List of reminders are saved in SQLite db.
 * Implements ReminderListFragment.ReminderListListener and ReminderFragment.OnReminderChangeListener to enable communication between fragments and activity.
 */
public class MainActivity extends Activity implements ReminderListFragment.ReminderListListener, ReminderFragment.OnReminderChangeListener, PrefFragment.OnLanguageChangeListener {
    private ArrayList<Reminder> reminderItems;
    /**
     * helps format date and time into selected format
     */
    public static final Format dateFormat = new SimpleDateFormat("dd MMMM yyyy");
    public static final Format timeFormat = new SimpleDateFormat("HH:mm");
    /**
     * tag for all logs in app
     */
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
                    getString(R.string.listFr)).commit(); //set ReminderListFragment as visible one
        } else {
            reminderItems = savedInstanceState.getParcelableArrayList(getString(R.string.reminderListIntent)); //retain list from saved state, no need to go to db again
        }
    }

    /**
     * Reload PrefFragment when language changed.
     */
    @Override
    public void onLanguageChanged() {
        Fragment currentFragment = getFragmentManager().findFragmentByTag(getString(R.string.prefFr));
        FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
        fragTransaction.remove(currentFragment).commit();
        getFragmentManager().popBackStack();
        fragTransaction = getFragmentManager().beginTransaction();
        fragTransaction.replace(R.id.content_frame, new PrefFragment(),
                getString(R.string.prefFr)).addToBackStack(
                getString(R.string.prefFr)).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
            case R.id.action_settings:
                getFragmentManager().beginTransaction().replace(R.id.content_frame, new PrefFragment(),
                        getString(R.string.prefFr)).addToBackStack(
                        getString(R.string.prefFr)).commit();
                return true;
            case R.id.action_birthdays:
                Intent i = new Intent(MainActivity.this, SyncBirthdaysActivity.class);
                startActivityForResult(i, 0);
                return true;
        }
        return false;
    }

    /**
     * Get all reminders from db
     *
     * @return list of reminders
     */
    private ArrayList<Reminder> retrieveReminders() {
        DatabaseHandler db = DatabaseHandler.getInstance(this);
        reminderItems = (ArrayList<Reminder>) db.getAllReminders();
        return reminderItems;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(getString(R.string.reminderListIntent), reminderItems);
    }

    /**
     * Called when reminder item in list is clicked. Open ReminderFragment to view and edit this reminder.
     *
     * @param position position of clicked item
     */
    @Override
    public void onItemClick(int position) {
        ReminderFragment reminderFragment = new ReminderFragment();
        Bundle arg = new Bundle();
        arg.putParcelable(getString(R.string.reminderIntent), reminderItems.get(position));
        reminderFragment.setArguments(arg);
        getFragmentManager().beginTransaction().replace(R.id.content_frame, reminderFragment,
                getString(R.string.reminderFr)).addToBackStack(
                getString(R.string.reminderFr)).commit();
    }

    /**
     * Called when new reminder was created. Saves it to db, add it to list, call setReminder() and opens ReminderListFragment.
     *
     * @param r new reminder
     */
    @Override
    public void onReminderCreated(Reminder r) {
        DatabaseHandler db = DatabaseHandler.getInstance(this);
        r = db.addReminder(r);
        reminderItems.add(r);
        setReminder(r);
        ((ReminderListFragment) getFragmentManager().findFragmentByTag(getString(R.string.listFr))).setReminderItems(reminderItems);
        getFragmentManager().popBackStack();
        StringBuilder sb = new StringBuilder(getString(R.string.logMes));
        sb.append(" ").append(r.getTitle()).append(getString(R.string.logMesCreated)).append(" ");
        sb.append(dateFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime().getValue() * 60 * 1000));
        sb.append(", ").append(timeFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime().getValue() * 60 * 1000));
        Log.e(MainActivity.TAG, sb.toString());
    }

    /**
     * Called when updating reminder. Update it in db, update in list, reschedule alarm by setReminder() and opens ReminderListFragment.
     *
     * @param r updated reminder
     */
    @Override
    public void onReminderUpdate(Reminder r) {
        DatabaseHandler db = DatabaseHandler.getInstance(this);
        int rowsUpdated = db.updateReminder(r);
        for (int i = 0; i < reminderItems.size(); i++) {
            if (reminderItems.get(i).equals(r)) {
                reminderItems.set(i, r);
                break;
            }
        }
        setReminder(r);
        ((ReminderListFragment) getFragmentManager().findFragmentByTag(getString(R.string.listFr))).setReminderItems(reminderItems);
        getFragmentManager().popBackStack();
        if (rowsUpdated > 0) {
            StringBuilder sb = new StringBuilder(getString(R.string.logMes));
            sb.append(" ").append(r.getTitle()).append(getString(R.string.logMesUpdated)).append(" ");
            sb.append(dateFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime().getValue() * 60 * 1000));
            sb.append(", ").append(timeFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime().getValue() * 60 * 1000));

            Log.e(MainActivity.TAG, sb.toString());
        }
    }

    /**
     * Called when deleting reminder. Delete from db, delete from list, cancel reminder and opens ReminderListFragment.
     *
     * @param r reminder to delete
     */
    @Override
    public void onReminderDelete(Reminder r) {
        DatabaseHandler db = DatabaseHandler.getInstance(this);
        db.deleteReminder(r);
        reminderItems.remove(r);
        ((ReminderListFragment) getFragmentManager().findFragmentByTag(getString(R.string.listFr))).setReminderItems(reminderItems);
        getFragmentManager().popBackStack();
        Log.e(MainActivity.TAG, getString(R.string.logMes) + " " + r.getTitle() + " " + getString(R.string.logMesDeleted));
        Log.e(MainActivity.TAG, getString(R.string.amountOfReminders) + db.getRemindersCount());
        if (r.isCalendarEventAdded()) {
            deleteEventFromCalendarProvider(r);
        } else {
            cancelAlarmManagerReminder(r, this);
        }
    }

    /**
     * Called when deleting batch of reminders. Delete them from db, delete from list, cancel reminders and opens ReminderListFragment.
     *
     * @param reminders list of reminder to delete
     */
    @Override
    public void onReminderBatchDelete(ArrayList<Reminder> reminders) {
        DatabaseHandler db = DatabaseHandler.getInstance(this);
        for (Reminder r : reminders) {
            db.deleteReminder(r);
            reminderItems.remove(r);
            Log.e(MainActivity.TAG, getString(R.string.logMes) + " " + r.getTitle() + " " + getString(R.string.logMesDeleted));
            if (r.isCalendarEventAdded()) {
                deleteEventFromCalendarProvider(r);
            } else {
                cancelAlarmManagerReminder(r, this);
            }
        }
        Log.e(MainActivity.TAG, getString(R.string.amountOfReminders) + db.getRemindersCount());
    }

    /**
     * Open empty ReminderFragment.
     */
    @Override
    public void onReminderCreateNew() {
        getFragmentManager().beginTransaction().replace(R.id.content_frame, new ReminderFragment(),
                getString(R.string.reminderFr)).addToBackStack(
                getString(R.string.reminderFr)).commit();
    }

    /**
     * Depends on checkbox:
     * inserts event with reminder in local calendar
     * or sets service from app that will show notification in reminderTime
     *
     * @param r reminder to set alarm
     */
    public void setReminder(Reminder r) {
        if (r.isCalendarEventAdded()) {
            cancelAlarmManagerReminder(r, this);
            insertEventToCalendarProvider(r);
        } else {
            if (r.getEventId() != 0) {
                deleteEventFromCalendarProvider(r);
            }
            setAlarmService(r, this);
            StringBuilder sb = new StringBuilder(getString(R.string.toastReminderSetTo));
            sb.append(" ");
            sb.append(dateFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime().getValue() * 60 * 1000));
            sb.append(", ");
            sb.append(timeFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime().getValue() * 60 * 1000));
            Toast.makeText(MainActivity.this, sb.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Cancel reminder notification setted by AlarmManager
     *
     * @param r       reminder to cancel
     * @param context
     */
    public static void cancelAlarmManagerReminder(Reminder r, Context context) {
        Intent intentAlarm = new Intent(context, ReminderReceiver.class);
        intentAlarm.putExtra(context.getString(R.string.reminderIntent), r);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(PendingIntent.getBroadcast(context, r.getId(), intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    /**
     * Cancel reminder that was added to CalendarProvider
     *
     * @param r reminder to cancel
     */
    public void deleteEventFromCalendarProvider(Reminder r) {
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        Uri deleteUri = null;
        deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, r.getEventId());
        AsyncQueryHandler handler =
                new AsyncQueryHandler(cr) {
                };
        handler.startDelete(-1, null, deleteUri, null, null);
    }

    /**
     * Set alarm service to show notification
     *
     * @param r       reminder to set as alarm
     * @param context
     */
    public static void setAlarmService(Reminder r, Context context) {

        Intent intentAlarm = new Intent(context, ReminderReceiver.class);
        intentAlarm.putExtra(context.getString(R.string.reminderIntent), r);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long triggerAtMillis = r.getEventTime() - r.getMinutesBeforeEventTime().getValue() * 60 * 1000;
        PendingIntent pi = PendingIntent.getBroadcast(context
                , r.getId(), intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);

    }

    /**
     * Insert event and reminder to CalendarProvider.
     *
     * @param r reminder to add
     */
    public void insertEventToCalendarProvider(final Reminder r) {
        final ContentResolver cr = getContentResolver();
        ContentValues calEvent = new ContentValues();
        calEvent.put(CalendarContract.Events.CALENDAR_ID, 1); //default calendar
        calEvent.put(CalendarContract.Events.TITLE, r.getTitle());
        calEvent.put(CalendarContract.Events.DESCRIPTION, r.getDescription());
        calEvent.put(CalendarContract.Events.DTSTART, r.getEventTime());
        calEvent.put(CalendarContract.Events.DTEND, r.getEventTime());
        calEvent.put(CalendarContract.Events.HAS_ALARM, 1);
        calEvent.put(CalendarContract.Events.EVENT_TIMEZONE, CalendarContract.Calendars.CALENDAR_TIME_ZONE);
       /*
         if (r.getEventId() == 0) {
         create new row in tables
         }
         else{
         update already existing rows by eventId and reminderId
         }
        */
        if (r.getEventId() == 0) {
            //inserts and updates should be done in an asynchronous thread
            AsyncQueryHandler handler =
                    new AsyncQueryHandler(cr) {
                        @Override
                        protected void onInsertComplete(int token, Object cookie, Uri uri) {
                            super.onInsertComplete(token, cookie, uri);
                            //get id of event
                            final int eventId = Integer.parseInt(uri.getLastPathSegment());

                            ContentValues reminder = new ContentValues();
                            reminder.put(CalendarContract.Reminders.EVENT_ID, eventId);
                            reminder.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
                            reminder.put(CalendarContract.Reminders.MINUTES, r.getMinutesBeforeEventTime().getValue());
                            //Uri uri2 = cr.insert(CalendarContract.Reminders.CONTENT_URI, reminder);
                            AsyncQueryHandler handler2 =
                                    new AsyncQueryHandler(cr) {
                                        @Override
                                        protected void onInsertComplete(int token, Object cookie, Uri uri) {
                                            super.onInsertComplete(token, cookie, uri);
                                            int reminderId = Integer.parseInt(uri.getLastPathSegment());
                                            r.setEventId(eventId);
                                            r.setReminderId(reminderId);
                                            DatabaseHandler db = DatabaseHandler.getInstance(MainActivity.this);
                                            db.updateReminder(r);
                                            for (int i = 0; i < reminderItems.size(); i++) {
                                                if (reminderItems.get(i).equals(r)) {
                                                    reminderItems.set(i, r);
                                                    break;
                                                }
                                            }
                                            StringBuilder sb = new StringBuilder(getString(R.string.toastReminderSetTo));
                                            sb.append(" ");
                                            sb.append(dateFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime().getValue() * 60 * 1000));
                                            sb.append(", ");
                                            sb.append(timeFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime().getValue() * 60 * 1000));
                                            Toast.makeText(MainActivity.this, sb.toString(), Toast.LENGTH_SHORT).show();
                                        }
                                    };
                            handler2.startInsert(-1, null, CalendarContract.Reminders.CONTENT_URI, reminder);
                        }
                    };

            handler.startInsert(-1, null, CalendarContract.Events.CONTENT_URI, calEvent);

        } else {
            //inserts and updates should be done in an asynchronous thread
            AsyncQueryHandler handler =
                    new AsyncQueryHandler(cr) {

                        @Override
                        protected void onUpdateComplete(int token, Object cookie, int result) {
                            super.onUpdateComplete(token, cookie, result);
                            if (result > 0) {
                                ContentValues reminder = new ContentValues();
                                reminder.put(CalendarContract.Reminders.EVENT_ID, r.getEventId());
                                reminder.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
                                reminder.put(CalendarContract.Reminders.MINUTES, r.getMinutesBeforeEventTime().getValue());
                                AsyncQueryHandler handler2 =
                                        new AsyncQueryHandler(cr) {
                                            @Override
                                            protected void onUpdateComplete(int token, Object cookie, int result) {
                                                super.onUpdateComplete(token, cookie, result);
                                                if (result > 0) {
                                                    StringBuilder sb = new StringBuilder(getString(R.string.toastReminderSetTo));
                                                    sb.append(" ");
                                                    sb.append(dateFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime().getValue() * 60 * 1000));
                                                    sb.append(", ");
                                                    sb.append(timeFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime().getValue() * 60 * 1000));
                                                    Toast.makeText(MainActivity.this, sb.toString(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        };
                                handler2.startUpdate(-1, null, ContentUris.withAppendedId(CalendarContract.Reminders.CONTENT_URI, r.getReminderId()), reminder,
                                        null, null);
                            }
                        }
                    };
            handler.startUpdate(-1, null, ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, r.getEventId()), calEvent, null, null);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Make sure the request was successful
        if (resultCode == SyncBdaysService.RESULT_OK) {
            reminderItems = retrieveReminders();
            ReminderListFragment list_fr = new ReminderListFragment();
            Bundle args = new Bundle();
            args.putParcelableArrayList(getString(R.string.reminderListIntent), reminderItems);
            list_fr.setArguments(args);
            getFragmentManager().beginTransaction().replace(R.id.content_frame, list_fr,
                    getString(R.string.listFr)).commit(); //set ReminderListFragment as visible one
        } else if (resultCode == SyncBdaysService.RESULT_ERROR) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder
                    .setMessage(
                            getResources().getString(
                                    R.string.alert_dialog_text_sync_error))
                    .setNegativeButton(getResources().getString(
                            R.string.alert_dialog_button_cancel), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();
        }
        else if(resultCode == SyncBdaysService.RESULT_CANCEL){

        }
    }
}
