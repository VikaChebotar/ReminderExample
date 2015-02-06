package com.example.viktoria.reminderexample.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.viktoria.reminderexample.R;
import com.example.viktoria.reminderexample.services.SyncBdaysIntentService;
import com.example.viktoria.reminderexample.services.WakefulIntentService;
import com.example.viktoria.reminderexample.utils.MyApplication;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKError;
import com.vk.sdk.dialogs.VKCaptchaDialog;

/**
 * Created by viktoria on 05.02.15.
 */
public class SyncBirthdaysActivity extends Activity {
    private static final String VK_APP_ID = "4766773";
    private static String sTokenKey = "VK_ACCESS_TOKEN";
    private static String[] sMyScope = new String[]{VKScope.FRIENDS};
    private MyBroadcastReceiver myBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VKSdk.initialize(sdkListener, VK_APP_ID, VKAccessToken.tokenFromSharedPreferences(this, sTokenKey));
        setContentView(R.layout.sync_bdays);
        VKUIHelper.onCreate(this);
        if (VKSdk.wakeUpSession()) {

        } else {
            VKSdk.authorize(sMyScope, true, false);
        }
        Button syncBtn = (Button) findViewById(R.id.syncBtn);
        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Log.e(MainActivity.TAG, "on click");
//                AlarmManager am=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
//                Intent intent = new Intent(getBaseContext(), SyncBdaysReceiver.class);
//                PendingIntent pi = PendingIntent.getBroadcast(getBaseContext(), 1, intent, 0);
//                am.setRepeating(AlarmManager.RTC_WAKEUP,
//                        Calendar.getInstance().getTimeInMillis()+100, AlarmManager.INTERVAL_DAY*7, pi);

                if (!MyApplication.isConnected(SyncBirthdaysActivity.this)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SyncBirthdaysActivity.this);
                    builder
                            .setMessage(
                                    getResources().getString(
                                            R.string.alert_dialog_text_no_internet))
                            .setCancelable(true)
                            .setPositiveButton(
                                    getResources().getString(
                                            R.string.alert_dialog_button_reload),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int id) {
                                            if (!MyApplication.isConnected(SyncBirthdaysActivity.this)) {
                                                dialog.cancel();
                                                startService();
                                            }

                                        }
                                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    startService();
                }
            }
        });
    }

    private void startService() {

        WakefulIntentService.acquireStaticLock(SyncBirthdaysActivity.this); //acquire a partial WakeLock
        Intent i = new Intent(SyncBirthdaysActivity.this, SyncBdaysIntentService.class);
        startService(i);
        myBroadcastReceiver = new MyBroadcastReceiver();
        //register BroadcastReceiver
        IntentFilter intentFilter = new IntentFilter(SyncBdaysIntentService.ACTION_SYNC_BDAYS_SERVICE);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(myBroadcastReceiver, intentFilter);
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(getString(R.string.result), SyncBdaysIntentService.RESULT_ERROR)) {
                case SyncBdaysIntentService.RESULT_OK:
                    SyncBirthdaysActivity.this.setResult(RESULT_OK);
                    finish();
                    break;
                case SyncBdaysIntentService.RESULT_ERROR:
                    SyncBirthdaysActivity.this.setResult(RESULT_CANCELED);
                    finish();
                    break;
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VKUIHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        VKUIHelper.onResume(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VKUIHelper.onDestroy(this);
        unregisterReceiver(myBroadcastReceiver);
    }

    private VKSdkListener sdkListener = new VKSdkListener() {
        @Override
        public void onCaptchaError(VKError captchaError) {
            Log.e(MainActivity.TAG, "onCaptchaError");
            new VKCaptchaDialog(captchaError).show();
        }

        @Override
        public void onTokenExpired(VKAccessToken expiredToken) {
            Log.e(MainActivity.TAG, "onTokenExpired");
            VKSdk.authorize(sMyScope);
        }

        @Override
        public void onAccessDenied(VKError authorizationError) {
            Log.e(MainActivity.TAG, "onAccessDenied");
            new AlertDialog.Builder(SyncBirthdaysActivity.this)
                    .setMessage(authorizationError.errorMessage)
                    .show();
        }

        @Override
        public void onReceiveNewToken(VKAccessToken newToken) {
            Log.e(MainActivity.TAG, "onReceiveNewToken");
            newToken.saveTokenToSharedPreferences(SyncBirthdaysActivity.this, sTokenKey);

        }

        @Override
        public void onAcceptUserToken(VKAccessToken token) {
            Log.e(MainActivity.TAG, "onAcceptUserToken");

        }

        @Override
        public void onRenewAccessToken(VKAccessToken token) {
            Log.e(MainActivity.TAG, "onRenewAccessToken");

        }

    };
}
