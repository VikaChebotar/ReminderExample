package com.example.viktoria.reminderexample;

/**
 * Enum that represents all existing values of time behind the event to alarm. User can set it in ReminderFragment's spinner.
 */
public enum MinutesBeforeEventTime {
    ON_TIME(0),ONE_MINUTE(1), FIVE_MINUTES(5), ONE_DAY(24*60);

    /**
     * Get minutes
     * @return
     */
    public int getValue() {
        return value;
    }

    private final int value;

    private MinutesBeforeEventTime(int value) {
        this.value = value;
    }

    /**
     * Get Enum instance by value(minutes)
     * @param value value(minutes) of this Enum instance
     * @return Enum instance
     */
    public static MinutesBeforeEventTime getTypeByValue(int value) {
        for(MinutesBeforeEventTime e: MinutesBeforeEventTime.values()) {
            if(e.value == value) {
                return e;
            }
        }
        return null;// not found
    }
}
