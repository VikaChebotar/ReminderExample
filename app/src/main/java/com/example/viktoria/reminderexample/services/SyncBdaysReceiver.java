package com.example.viktoria.reminderexample.services;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.viktoria.reminderexample.view.MainActivity;

/**
 * Created by Вика on 06.02.2015.
 */
public class SyncBdaysReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(MainActivity.TAG, "on receive");
            WakefulIntentService.acquireStaticLock(context); //acquire a partial WakeLock
            context.startService(new Intent(context, SyncBdaysIntentService.class)); //start TaskButlerService
        }
}
