package com.example.viktoria.reminderexample;

/**
 * Created by Вика on 30.01.2015.
 */
public enum MinutesBeforeEventTime {
    ON_TIME(0),ONE_MINUTE(1), FIVE_MINUTES(5), ONE_DAY(24*60);

    public int getValue() {
        return value;
    }

    private final int value;

    private MinutesBeforeEventTime(int value) {
        this.value = value;
    }

    public static MinutesBeforeEventTime getTypeByValue(int value) {
        for(MinutesBeforeEventTime e: MinutesBeforeEventTime.values()) {
            if(e.value == value) {
                return e;
            }
        }
        return null;// not found
    }
}
