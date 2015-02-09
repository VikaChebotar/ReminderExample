package com.example.viktoria.reminderexample.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import com.example.viktoria.reminderexample.services.SyncBdaysService;
import com.example.viktoria.reminderexample.utils.DatabaseHandler;
import com.example.viktoria.reminderexample.utils.MyApplication;
import com.example.viktoria.reminderexample.utils.Reminder;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKError;
import com.vk.sdk.dialogs.VKCaptchaDialog;

import java.util.List;

/**
 * The activity that allows to get date of friends birthdays from VK.
 * Asks to authorize in VK and then run service on button click, that executes request to get friends birthdays.
 * Have possibility to delete all birthday reminders.
 */
public class SyncBirthdaysActivity extends Activity {
    //unique VK id for initialization of app
    private static final String VK_APP_ID = "4766773";
    //key for shared preference
    private static final String sTokenKey = "VK_ACCESS_TOKEN";
    //list of permissions we ask user
    private static final String[] sMyScope = new String[]{VKScope.FRIENDS};
    private MyBroadcastReceiver myBroadcastReceiver;
    private ProgressDialog pd;


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
        Button deleteBtn = (Button) findViewById(R.id.deleteBtn);
        if (isBdaysAlreadyAdded()) {
            deleteBtn.setVisibility(View.VISIBLE);
            syncBtn.setVisibility(View.GONE);
        } else {
            deleteBtn.setVisibility(View.GONE);
            syncBtn.setVisibility(View.VISIBLE);
        }
        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startServiceIfConnected();
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteBirthdays();
                //return result to MainActivity
                SyncBirthdaysActivity.this.setResult(SyncBdaysService.RESULT_OK);
                finish();
            }
        });
    }

    /**
     * Checks if there is already reminders of friends birthdays
     * @return
     */
    private boolean isBdaysAlreadyAdded() {
        DatabaseHandler db = DatabaseHandler.getInstance(this);
        if (db.getBirthdayRemindersCount() > 0) {
            return true;
        }
        return false;
    }

    /**
     * Starts service of downloading friends info. Shows progress dialog.
     * Register broadcast receiver to get result when service finish.
     */
    private void startService() {
        pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.loading));
        pd.show();
        Intent i = new Intent(SyncBirthdaysActivity.this, SyncBdaysService.class);
        startService(i);
        myBroadcastReceiver = new MyBroadcastReceiver();
        //register BroadcastReceiver
        IntentFilter intentFilter = new IntentFilter(SyncBdaysService.ACTION_SYNC_BDAYS_SERVICE);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(myBroadcastReceiver, intentFilter);
    }

    /**
     * Checks network connection and call startService() when connection exists
     */
    private void startServiceIfConnected() {
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

                                    dialog.cancel();
                                    startServiceIfConnected();

                                }
                            });

            AlertDialog alert = builder.create();
            alert.show();
        } else {
            startService();
        }
    }

    /**
     * Deletes all birthdays from db and cancel alarm reminders of them.
     */
    private void deleteBirthdays() {
        DatabaseHandler db = DatabaseHandler.getInstance(this);
        List<Reminder> r = db.getAllBirthdayReminders();
        for (Reminder item : r) {
            MainActivity.cancelAlarmManagerReminder(item, this);
        }
        db.deleteBirthdayReminders();
    }

    /**
     * BroadcastReceiver that handles response from SyncBdayService
     */
    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {;
            pd.cancel();
            switch (intent.getIntExtra(getString(R.string.result), SyncBdaysService.RESULT_ERROR)) {
                case SyncBdaysService.RESULT_OK:
                    SyncBirthdaysActivity.this.setResult(SyncBdaysService.RESULT_OK);
                    finish();
                    break;
                case SyncBdaysService.RESULT_ERROR:
                    SyncBirthdaysActivity.this.setResult(SyncBdaysService.RESULT_ERROR);
                    finish();
                    break;
            }

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopService(new Intent(SyncBirthdaysActivity.this, SyncBdaysService.class));
        SyncBirthdaysActivity.this.setResult(SyncBdaysService.RESULT_CANCEL);
        finish();
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
        if (myBroadcastReceiver != null) {
            unregisterReceiver(myBroadcastReceiver);
        }
    }

    /**
     * VK listener to handle token
     */
    public VKSdkListener sdkListener = new VKSdkListener() {
        @Override
        public void onCaptchaError(VKError captchaError) {

            new VKCaptchaDialog(captchaError).show();
        }

        @Override
        public void onTokenExpired(VKAccessToken expiredToken) {

            VKSdk.authorize(sMyScope);
        }

        @Override
        public void onAccessDenied(VKError authorizationError) {

            new AlertDialog.Builder(SyncBirthdaysActivity.this)
                    .setMessage(authorizationError.errorMessage)
                    .show();
        }

        @Override
        public void onReceiveNewToken(VKAccessToken newToken) {

            newToken.saveTokenToSharedPreferences(SyncBirthdaysActivity.this, sTokenKey);

        }

        @Override
        public void onAcceptUserToken(VKAccessToken token) {

        }

        @Override
        public void onRenewAccessToken(VKAccessToken token) {

        }

    };
}
