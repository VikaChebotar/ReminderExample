package com.example.viktoria.reminderexample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VKSdk.initialize(sdkListener, VK_APP_ID, VKAccessToken.tokenFromSharedPreferences(this, sTokenKey));
        setContentView(R.layout.activity_main);
        VKUIHelper.onCreate(this);
        if (VKSdk.wakeUpSession()) {
            syncFriendsBdays();
        } else {
            VKSdk.authorize(sMyScope, true, false);
        }
    }

    private void syncFriendsBdays() {

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
            syncFriendsBdays();
        }

        @Override
        public void onAcceptUserToken(VKAccessToken token) {
            Log.e(MainActivity.TAG, "onAcceptUserToken");
            syncFriendsBdays();
        }

        @Override
        public void onRenewAccessToken(VKAccessToken token) {
            Log.e(MainActivity.TAG, "onRenewAccessToken");
            syncFriendsBdays();
        }

    };
}
