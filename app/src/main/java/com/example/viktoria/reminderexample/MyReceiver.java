package com.example.viktoria.reminderexample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * Is registered for event in alarm manager.
 * Method onReceive will be invoked when event happens.
 */


public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, MyNotificationService.class);
        String titleVarName = context.getResources().getString(R.string.titleVarIntent);
        String descrVarName = context.getResources().getString(R.string.descrVarIntent);
        serviceIntent.putExtra(titleVarName, intent.getStringExtra(titleVarName));
        serviceIntent.putExtra(descrVarName, intent.getStringExtra(descrVarName));
        //start service that show notification
        context.startService(serviceIntent);
    }
}