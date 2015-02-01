package com.example.viktoria.reminderexample;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;

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
    public static int LAST_ID = 0;

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
        r = db.addReminder(r);
        reminderItems.add(r);
        setReminder(r);
        ((ReminderListFragment) getFragmentManager().findFragmentByTag("list_fr")).setReminderItems(reminderItems);
        getFragmentManager().popBackStack();
        Log.e(MainActivity.TAG, getString(R.string.logMes) + " " + r.getTitle() + " (" + r.getId() + ") " + getString(R.string.logMesCreated) + " " + dateFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime().getValue() * 60 * 1000) + ", " + timeFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime().getValue() * 60 * 1000));
    }

    @Override
    public void onReminderUpdate(Reminder r) {
        DatabaseHandler db = new DatabaseHandler(this);
        int rowsUpdated = db.updateReminder(r);
        for (int i = 0; i < reminderItems.size(); i++) {
            if (reminderItems.get(i).equals(r)) {
                reminderItems.set(i, r);
                break;
            }
        }
        setReminder(r);
        ((ReminderListFragment) getFragmentManager().findFragmentByTag("list_fr")).setReminderItems(reminderItems);
        getFragmentManager().popBackStack();
        if (rowsUpdated > 0) {
            Log.e(MainActivity.TAG, getString(R.string.logMes) + " " + r.getTitle() + " (" + r.getId() + ") " + getString(R.string.logMesUpdated) + " " + dateFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime().getValue() * 60 * 1000) + ", " + timeFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime().getValue() * 60 * 1000));
        }
    }

    @Override
    public void onReminderDelete(Reminder r) {
        DatabaseHandler db = new DatabaseHandler(this);
        db.deleteReminder(r);
        reminderItems.remove(r);
        ((ReminderListFragment) getFragmentManager().findFragmentByTag("list_fr")).setReminderItems(reminderItems);
        getFragmentManager().popBackStack();
        Log.e(MainActivity.TAG, getString(R.string.logMes) + " " + r.getTitle() + " (" + r.getId() + ") " + getString(R.string.logMesDeleted));
        Log.e(MainActivity.TAG, "amount of reminders:" + db.getRemindersCount());
        if (r.isCalendarEventAdded()) {
            deleteEventFromCalendarProvider(r);
        } else {
            cancelAlarmManagerReminder(r);
        }
    }

    @Override
    public void onReminderBatchDelete(ArrayList<Reminder> reminders) {
        DatabaseHandler db = new DatabaseHandler(this);
        for (Reminder r : reminders) {
            db.deleteReminder(r);
            reminderItems.remove(r);
            Log.e(MainActivity.TAG, getString(R.string.logMes) + " " + r.getTitle() + " (" + r.getId() + ") " + getString(R.string.logMesDeleted));
            if (r.isCalendarEventAdded()) {
                deleteEventFromCalendarProvider(r);
            } else {
                cancelAlarmManagerReminder(r);
            }
        }
        Log.e(MainActivity.TAG, "amount of reminders:" + db.getRemindersCount());
    }

    /**
     * depends on checkbox:
     * inserts event with reminder in local calendar
     * or sets service from app that will show notification in reminderTime
     */
    public void setReminder(Reminder r) {
        if (r.isCalendarEventAdded()) {
            cancelAlarmManagerReminder(r);
            insertEventToCalendarProvider(r);
        } else {
            if(r.getEventId()!=0){
                deleteEventFromCalendarProvider(r);
            }
            setAlarmService(r);
        }
    }

    public void cancelAlarmManagerReminder(Reminder r) {
            Intent intentAlarm = new Intent(this, MyReceiver.class);
            intentAlarm.putExtra(getResources().getString(R.string.titleVarIntent), r.getTitle());
            intentAlarm.putExtra(getResources().getString(R.string.descrVarIntent), r.getDescription());
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(PendingIntent.getBroadcast(this, r.getId(), intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
    }

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
                                            DatabaseHandler db = new DatabaseHandler(MainActivity.this);
                                            db.updateReminder(r);
                                            for (int i = 0; i < reminderItems.size(); i++) {
                                                if (reminderItems.get(i).equals(r)) {
                                                    reminderItems.set(i, r);
                                                    break;
                                                }
                                            }
                                            Toast.makeText(MainActivity.this, getString(R.string.toastReminderSetTo) + " " + dateFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime().getValue() * 60 * 1000) + ", " + timeFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime().getValue() * 60 * 1000), Toast.LENGTH_SHORT).show();
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
                                                    Toast.makeText(MainActivity.this, getString(R.string.toastReminderSetTo) + " " + dateFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime().getValue() * 60 * 1000) + ", " + timeFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime().getValue() * 60 * 1000), Toast.LENGTH_SHORT).show();
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

    public void setAlarmService(Reminder r) {

        Intent intentAlarm = new Intent(MainActivity.this, MyReceiver.class);
        //pass title and description to receiver and then to service to show them in notification
        intentAlarm.putExtra(getString(R.string.reminderIntent), r);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, r.getEventTime() - r.getMinutesBeforeEventTime().getValue() * 60 * 1000, PendingIntent.getBroadcast(MainActivity.this
                , r.getId(), intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
        Toast.makeText(MainActivity.this, getString(R.string.toastReminderSetTo) + " " + dateFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime().getValue() * 60 * 1000) + ", " + timeFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime().getValue() * 60 * 1000), Toast.LENGTH_SHORT).show();
    }


}
