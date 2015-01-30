package com.example.viktoria.reminderexample;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

/**
 * A main screen of application, that allow to create reminder in two ways: with notification from this app or with use of local calendar
 */
public class ReminderFragment extends Fragment {
    private View rootView;
    private Calendar calendar;
    private CheckBox checkBox;
    private EditText titleET, descrET;
    private TextView dateLabel, timeLabel;
    private Spinner remindTimeSpinner;
    private DatePickerDialog datePicker;
    private TimePickerDialog timePicker;
    private ActionBar actionBar;
    private Reminder r;
    OnReminderChangeListener mCallback;
    boolean editMode = false;
    /**
     * helps format date and time into selected format
     */
    public Format dateFormat = new SimpleDateFormat("dd MMMM yyyy");
    public Format timeFormat = new SimpleDateFormat("HH:mm");

    // interface to communicate with other fragment through activity, fragment shouldn't know about parent activity
    public interface OnReminderChangeListener {
        public void onReminderCreated(Reminder r);

        public void onReminderUpdate(Reminder r);

        public void onReminderDelete(Reminder r);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.reminder, container, false);
        if (getArguments() != null && getArguments().containsKey(getActivity().getString(R.string.reminderIntent))) {
            r = getArguments().getParcelable(getActivity().getString(R.string.reminderIntent));
            editMode = true;
        } else {
            r = new Reminder();
            editMode = false;
        }
        titleET = (EditText) rootView.findViewById(R.id.titleET);
        descrET = (EditText) rootView.findViewById(R.id.descrET);
        dateLabel = (TextView) rootView.findViewById(R.id.date);
        timeLabel = (TextView) rootView.findViewById(R.id.time);
        checkBox = (CheckBox) rootView.findViewById(R.id.checkBox);
        remindTimeSpinner = (Spinner) rootView.findViewById(R.id.remindTimeSpinner);
        if (r.getTitle() != null && !r.getTitle().isEmpty()) {
            titleET.setText(r.getTitle());
        }
        if (r.getDescription() != null && !r.getDescription().isEmpty()) {
            descrET.setText(r.getDescription());
        }
        if (r.isCalendarEventAdded()) {
            checkBox.setChecked(true);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_spinner_item, getResources().getStringArray(R.array.remind_time));
        remindTimeSpinner.setAdapter(adapter);
        switch (r.getMinutesBeforeEventTime()) {
            case 0:
                remindTimeSpinner.setSelection(0);
                break;
            case 1:
                remindTimeSpinner.setSelection(1);
                break;
            case 5:
                remindTimeSpinner.setSelection(2);
                break;
            case 60:
                remindTimeSpinner.setSelection(3);
                break;
        }

        calendar = Calendar.getInstance();
        //set previous selected date if it was saved
        if (r.getEventTime() != 0) {
            calendar.setTimeInMillis(r.getEventTime());
        }
        dateLabel.setText(dateFormat.format(calendar.getTime()));
        timeLabel.setText(timeFormat.format(calendar.getTime()));
        datePicker = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        //update calendar instance and dateLabel
                        calendar.set(year, monthOfYear, dayOfMonth);
                        dateLabel.setText(dateFormat.format(calendar.getTime()));
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        timePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                //update calendar instance and timeLabel
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                calendar.set(Calendar.MINUTE, selectedMinute);
                calendar.set(Calendar.SECOND,0);
                timeLabel.setText(timeFormat.format(calendar.getTime()));
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        dateLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker.show();
            }
        });
        timeLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePicker.show();
            }
        });

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.action_add).setVisible(false);
        if (editMode) {
            menu.findItem(R.id.action_delete).setVisible(true);
        }
        menu.findItem(R.id.action_accept).setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        actionBar = (getActivity()).getActionBar();
        if (savedInstanceState != null) {
            r = savedInstanceState.getParcelable(getActivity().getString(R.string.reminderIntent));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // this makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnReminderChangeListener) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnReminderCreatedListener");
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        actionBar.setDisplayHomeAsUpEnabled(true); //this is to enable up navigation
        actionBar.setTitle(getActivity().getString(R.string.reminder_fragment_title));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(getActivity().getString(R.string.reminderIntent), r);
    }

    /**
     * Sets reminder depends on input data
     */
    public void setReminder(Reminder r) {

        r.setTitle(titleET.getText().toString());
        r.setDescription(descrET.getText().toString());

        r.setEventTime(calendar.getTimeInMillis());
        switch (remindTimeSpinner.getSelectedItemPosition()) {
            case 0:
                r.setMinutesBeforeEventTime(0);
                break;
            case 1:
                r.setMinutesBeforeEventTime(1);
                break;
            case 2:
                r.setMinutesBeforeEventTime(5);
                break;
            case 3:
                r.setMinutesBeforeEventTime(60);
                break;
        }
        r.setCalendarEventAdded(checkBox.isChecked());
        /*
        depends on checkbox:
        inserts event with reminder in local calendar
        or sets service from app that will show notification in reminderTime
         */
        if (r.isCalendarEventAdded()) {
            insertEventToCalendarProvider(r);
        } else {
            setAlarmService(r);
        }
    }


    public void insertEventToCalendarProvider(final Reminder r) {
        final ContentResolver cr = getActivity().getContentResolver();
        ContentValues calEvent = new ContentValues();
        calEvent.put(CalendarContract.Events.CALENDAR_ID, 1); //default calendar
        calEvent.put(CalendarContract.Events.TITLE, r.getTitle());
        calEvent.put(CalendarContract.Events.DESCRIPTION, r.getDescription());
        calEvent.put(CalendarContract.Events.DTSTART, r.getEventTime());
        calEvent.put(CalendarContract.Events.DTEND, r.getEventTime());
        calEvent.put(CalendarContract.Events.HAS_ALARM, 1);
        calEvent.put(CalendarContract.Events.EVENT_TIMEZONE, CalendarContract.Calendars.CALENDAR_TIME_ZONE);
//inserts and updates should be done in an asynchronous thread
        AsyncQueryHandler handler =
                new AsyncQueryHandler(cr) {
                    @Override
                    protected void onInsertComplete(int token, Object cookie, Uri uri) {
                        super.onInsertComplete(token, cookie, uri);
                        //get id of event
                        int id = Integer.parseInt(uri.getLastPathSegment());
                        ContentValues reminder = new ContentValues();
                        reminder.put(CalendarContract.Reminders.EVENT_ID, id);
                        reminder.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
                        reminder.put(CalendarContract.Reminders.MINUTES, r.getMinutesBeforeEventTime());
                        //Uri uri2 = cr.insert(CalendarContract.Reminders.CONTENT_URI, reminder);
                        AsyncQueryHandler handler =
                                new AsyncQueryHandler(cr) {
                                    @Override
                                    protected void onInsertComplete(int token, Object cookie, Uri uri) {
                                        super.onInsertComplete(token, cookie, uri);
                                        Toast.makeText(getActivity(), getString(R.string.toastReminderSetTo) + " " + dateFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime() * 60 * 1000) + ", " + timeFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime() * 60 * 1000), Toast.LENGTH_SHORT).show();
                                    }
                                };
                        handler.startInsert(-1, null, CalendarContract.Reminders.CONTENT_URI, reminder);
                    }
                };
        handler.startInsert(-1, null, CalendarContract.Events.CONTENT_URI, calEvent);
        //  Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, calEvent);


    }

    public void setAlarmService(Reminder r) {
        Intent intentAlarm = new Intent(getActivity(), MyReceiver.class);
        //pass title and description to receiver and then to service to show them in notification
        intentAlarm.putExtra(getResources().getString(R.string.titleVarIntent), r.getTitle());
        intentAlarm.putExtra(getResources().getString(R.string.descrVarIntent), r.getDescription());
        int flag;
      //  if(editMode){
            flag = PendingIntent.FLAG_UPDATE_CURRENT;
       // }
//        else{
//            flag = PendingIntent.FLAG_ONE_SHOT;
//        }
        r.setRequestCode(new Random().nextInt((1000 - 0) + 1) + 0);
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, r.getEventTime() - r.getMinutesBeforeEventTime() * 60, PendingIntent.getBroadcast(getActivity(), 1, intentAlarm, flag));
        Toast.makeText(getActivity(), getString(R.string.toastReminderSetTo) + " " + dateFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime() * 60 * 1000) + ", " + timeFormat.format(r.getEventTime() - r.getMinutesBeforeEventTime() * 60 * 1000), Toast.LENGTH_SHORT).show();
       }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().getFragmentManager().popBackStack();
                return true;
            case R.id.action_delete:
                mCallback.onReminderDelete(r);
                return true;
            case R.id.action_accept:
                //it's not allowed to make event without name
                if (titleET.getText().toString() == null || titleET.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), getString(R.string.toastNoTitle), Toast.LENGTH_SHORT).show();

                } else {
                    setReminder(r);
                    if (editMode) {
                        mCallback.onReminderUpdate(r);
                    } else {
                        mCallback.onReminderCreated(r);
                    }
                }
                return true;
        }
        return false;
    }
}
