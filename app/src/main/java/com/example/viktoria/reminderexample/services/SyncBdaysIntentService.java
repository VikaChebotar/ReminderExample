package com.example.viktoria.reminderexample.services;

import android.content.Intent;
import android.util.Log;

import com.example.viktoria.reminderexample.view.MainActivity;

/**
 * Created by Вика on 06.02.2015.
 */
public class SyncBdaysIntentService extends WakefulIntentService {
    public SyncBdaysIntentService() {
        super(SyncBdaysIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e(MainActivity.TAG, "on handle!");
        super.onHandleIntent(intent);
    }
}
