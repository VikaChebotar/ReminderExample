package com.example.viktoria.reminderexample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 * Is registered for event in alarm manager.
 * Method onReceive will be invoked when event happens.
 */


public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, MyNotificationService.class);
        String reminderVarName = context.getResources().getString(R.string.reminderIntent);
        serviceIntent.putExtra(reminderVarName, intent.getParcelableExtra(reminderVarName));
        //start service that show notification
        context.startService(serviceIntent);
    }
}