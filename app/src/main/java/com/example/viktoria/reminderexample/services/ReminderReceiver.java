package com.example.viktoria.reminderexample.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.viktoria.reminderexample.R;


/**
 * Is registered for event in alarm manager.
 * Method onReceive will be invoked when event happens.
 */


public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, MyNotificationService.class);
        String reminderVarName = context.getResources().getString(R.string.reminderIntent);
        serviceIntent.putExtra(reminderVarName, intent.getParcelableExtra(reminderVarName));
        //start service that show notification
        context.startService(serviceIntent);
    }
}