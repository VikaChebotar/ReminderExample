package com.example.viktoria.reminderexample.view;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
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

import com.example.viktoria.reminderexample.R;
import com.example.viktoria.reminderexample.utils.MinutesBeforeEventTime;
import com.example.viktoria.reminderexample.utils.Reminder;

import java.util.Calendar;

/**
 * Detail screen of reminder. Allow to create reminder in two ways: with notification from this app or with use of local calendar
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
    private OnReminderChangeListener mCallback;
    boolean editMode = false; //true when modifying existing reminder, false when creating new reminder

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
        //set spinner selection according to minutesBeforeEventTime field in Reminder
        switch (r.getMinutesBeforeEventTime()) {
            case ON_TIME:
                remindTimeSpinner.setSelection(0);
                break;
            case ONE_MINUTE:
                remindTimeSpinner.setSelection(1);
                break;
            case FIVE_MINUTES:
                remindTimeSpinner.setSelection(2);
                break;
            case ONE_DAY:
                remindTimeSpinner.setSelection(3);
                break;
        }

        calendar = Calendar.getInstance();
        //set previous selected date if it was saved
        if (r.getEventTime() != 0) {
            calendar.setTimeInMillis(r.getEventTime());
        }
        dateLabel.setText(MainActivity.dateFormat.format(calendar.getTime()));
        timeLabel.setText(MainActivity.timeFormat.format(calendar.getTime()));
        datePicker = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        //update calendar instance and dateLabel
                        calendar.set(year, monthOfYear, dayOfMonth);
                        dateLabel.setText(MainActivity.dateFormat.format(calendar.getTime()));
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        timePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                //update calendar instance and timeLabel
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                calendar.set(Calendar.MINUTE, selectedMinute);
                calendar.set(Calendar.SECOND, 0);
                timeLabel.setText(MainActivity.timeFormat.format(calendar.getTime()));
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
        menu.findItem(R.id.action_settings).setVisible(false);
        if (editMode) {
            menu.findItem(R.id.action_delete).setVisible(true);
        }
        menu.findItem(R.id.action_accept).setVisible(true);

        menu.findItem(R.id.action_birthdays).setVisible(false);
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
                    + " " + getActivity().getString(R.string.castExc) + " " + OnReminderChangeListener.class);
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                mCallback.onReminderDelete(r);
                return true;
            case R.id.action_accept:
                //it's not allowed to make event without name
                if (titleET.getText().toString() == null || titleET.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), getString(R.string.toastNoTitle), Toast.LENGTH_SHORT).show();
                } else {
                    initReminder(r);

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

    /**
     * Get all input data to set them to reminder object
     *
     * @param r object where selected input data would be set
     */
    private void initReminder(Reminder r) {
        r.setTitle(titleET.getText().toString());
        r.setDescription(descrET.getText().toString());
        r.setEventTime(calendar.getTimeInMillis());
        switch (remindTimeSpinner.getSelectedItemPosition()) {
            case 0:
                r.setMinutesBeforeEventTime(MinutesBeforeEventTime.ON_TIME);
                break;
            case 1:
                r.setMinutesBeforeEventTime(MinutesBeforeEventTime.ONE_MINUTE);
                break;
            case 2:
                r.setMinutesBeforeEventTime(MinutesBeforeEventTime.FIVE_MINUTES);
                break;
            case 3:
                r.setMinutesBeforeEventTime(MinutesBeforeEventTime.ONE_DAY);
                break;
        }
        r.setCalendarEventAdded(checkBox.isChecked());
    }
}
