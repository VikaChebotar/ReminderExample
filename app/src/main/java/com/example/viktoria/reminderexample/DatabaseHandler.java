package com.example.viktoria.reminderexample;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by viktoria on 28.01.15.
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "remindersManager";

    // Reminder table name
    private static final String TABLE_REMINDER = "reminder";

    // Reminder Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCR = "description";
    private static final String KEY_EVENT_TIME = "eventTime";
    private static final String KEY_MINUTES_BET = "minutesBeforeEventTime";
    private static final String KEY_IS_CALENDAREVENT_ADDED = "isCalendarEventAdded";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_REMINDER + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TITLE + " TEXT,"
                + KEY_DESCR + " TEXT," + KEY_EVENT_TIME + " INTEGER," + KEY_MINUTES_BET + " INTEGER," + KEY_IS_CALENDAREVENT_ADDED + " INTEGER"+ ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDER);

        // Create tables again
        onCreate(db);
    }

    // Adding new reminder
    public void addReminder(Reminder r) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, r.getTitle());
        values.put(KEY_DESCR, r.getDescription());
        values.put(KEY_EVENT_TIME, r.getEventTime());
        values.put(KEY_MINUTES_BET, r.getMinutesBeforeEventTime().getValue());
        int isCalendarEventAdded = r.isCalendarEventAdded() ? 1 : 0;
        values.put(KEY_IS_CALENDAREVENT_ADDED, isCalendarEventAdded);

        // Inserting Row
        db.insert(TABLE_REMINDER, null, values);
        db.close(); // Closing database connection
    }

    public List<Reminder> getAllReminders() {
        List<Reminder> reminderList = new ArrayList<Reminder>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_REMINDER;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            while (cursor.isAfterLast() == false) {
                Reminder r = new Reminder();
                r.setId(Integer.parseInt(cursor.getString(0)));
                r.setTitle(cursor.getString(1));
                r.setDescription(cursor.getString(2));
                r.setEventTime(Long.parseLong(cursor.getString(3)));
                r.setMinutesBeforeEventTime(MinutesBeforeEventTime.getTypeByValue(Integer.parseInt(cursor.getString(4))));
                r.setCalendarEventAdded(Integer.parseInt(cursor.getString(5)) != 0);

                // Adding reminder to list
                reminderList.add(r);
                cursor.moveToNext();
            }
        }

        // return contact list
        return reminderList;
    }

    public void deleteReminder(Reminder r) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_REMINDER, KEY_ID + " = ?",
                new String[]{String.valueOf(r.getId())});
        db.close();
    }

    public int updateReminder(Reminder r) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, r.getTitle());
        values.put(KEY_DESCR, r.getDescription());
        values.put(KEY_EVENT_TIME, r.getEventTime());
        values.put(KEY_MINUTES_BET, r.getMinutesBeforeEventTime().getValue());
        int isCalendarEventAdded = r.isCalendarEventAdded() ? 1 : 0;
        values.put(KEY_IS_CALENDAREVENT_ADDED, isCalendarEventAdded);

        // updating row
        return db.update(TABLE_REMINDER, values, KEY_ID + " = ?",
                new String[]{String.valueOf(r.getId())});
    }
    // Getting reminders Count
    public int getRemindersCount() {
        String countQuery = "SELECT  * FROM " + TABLE_REMINDER;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }
}
