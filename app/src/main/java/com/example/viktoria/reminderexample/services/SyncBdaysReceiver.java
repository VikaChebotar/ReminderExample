package com.example.viktoria.reminderexample.services;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Вика on 06.02.2015.
 */
public class SyncBdaysReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            context.startService(new Intent(context, SyncBdaysService.class));
        }
}
