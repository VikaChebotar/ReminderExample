package com.example.viktoria.reminderexample;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by viktoria on 27.01.15.
 */
public class Reminder implements Parcelable {
    private int id;
    private String title;
    private String description = "";
    private long eventTime;
    private int minutesBeforeEventTime; //eventTime-minutesBeforeEventTime = time when to notify
    private boolean isCalendarEventAdded;

    private int requestCode;

    private Reminder(Parcel parcel) {
        title = parcel.readString();
        description = parcel.readString();
        eventTime = parcel.readLong();
        minutesBeforeEventTime = parcel.readInt();
        isCalendarEventAdded = parcel.readByte() != 0; //isCalendarEventAdded== true if byte != 0
        requestCode = parcel.readInt();
    }

    public Reminder() {

    }
    public Reminder(String title, String description, long eventTime, int minutesBeforeEventTime, boolean isCalendarEventAdded) {
        this.title = title;
        this.description = description;
        this.eventTime = eventTime;
        this.minutesBeforeEventTime = minutesBeforeEventTime;
        this.isCalendarEventAdded = isCalendarEventAdded;
    }

    public int getId() {
        return id;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public boolean isCalendarEventAdded() {
        return isCalendarEventAdded;
    }

    public void setCalendarEventAdded(boolean isCalendarEventAdded) {
        this.isCalendarEventAdded = isCalendarEventAdded;
    }

    public int getMinutesBeforeEventTime() {
        return minutesBeforeEventTime;
    }

    public void setMinutesBeforeEventTime(int minutesBeforeEventTime) {
        this.minutesBeforeEventTime = minutesBeforeEventTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeLong(eventTime);
        parcel.writeInt(minutesBeforeEventTime);
        parcel.writeByte((byte) (isCalendarEventAdded ? 1 : 0)); //if isCalendarEventAdded == true, byte == 1
        parcel.writeInt(requestCode);
    }

    public static final Parcelable.Creator<Reminder> CREATOR = new Parcelable.Creator<Reminder>() {
        // get object from parcel
        public Reminder createFromParcel(Parcel in) {
            return new Reminder(in);
        }

        public Reminder[] newArray(int size) {
            return new Reminder[size];
        }
    };

}
