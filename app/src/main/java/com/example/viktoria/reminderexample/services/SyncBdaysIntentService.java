package com.example.viktoria.reminderexample.services;

import android.content.Intent;
import android.util.Log;

import com.example.viktoria.reminderexample.R;
import com.example.viktoria.reminderexample.utils.DatabaseHandler;
import com.example.viktoria.reminderexample.utils.MinutesBeforeEventTime;
import com.example.viktoria.reminderexample.utils.Reminder;
import com.example.viktoria.reminderexample.view.MainActivity;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Вика on 06.02.2015.
 */
public class SyncBdaysIntentService extends WakefulIntentService {
    private VKRequest request;
    private ArrayList<Reminder> bday_list;
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MMMM.yyyy");
    public static final String ACTION_SYNC_BDAYS_SERVICE = "com.example.viktoria.reminderexample.services.syncbdays";
    public static final int RESULT_OK = 1;
    public static final int RESULT_ERROR = 2;

    public SyncBdaysIntentService() {
        super(SyncBdaysIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        request = new VKRequest("friends.get", VKParameters.from("fields", "bdate", "name_case", "gen"));
        request.parseModel = false;
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
              //  super.onComplete(response);
                Log.d(MainActivity.TAG, "onComplete " + response.responseString);
                int result;
                try {
                    bday_list = parseResponse(response.responseString);
                    DatabaseHandler db = DatabaseHandler.getInstance(SyncBdaysIntentService.this);
                    bday_list = db.addListOfReminders(bday_list);
                    for (int i = 0; i < 10; i++) {
                        Log.e(MainActivity.TAG, bday_list.get(i).getId() + "");
                        MainActivity.setAlarmService(bday_list.get(i), SyncBdaysIntentService.this);
                    }
                    result = RESULT_OK;
                } catch (JSONException e) {
                    Log.e(MainActivity.TAG, e.getMessage());
                    result = RESULT_ERROR;
                } catch (ParseException e) {
                    Log.e(MainActivity.TAG, e.getMessage());
                    result = RESULT_ERROR;
                } catch (Exception e) {
                    Log.e(MainActivity.TAG, e.getMessage());
                    result = RESULT_ERROR;
                }

                Intent intentResponse = new Intent();
                intentResponse.setAction(ACTION_SYNC_BDAYS_SERVICE);
                intentResponse.addCategory(Intent.CATEGORY_DEFAULT);
                intentResponse.putExtra(getString(R.string.result), result);
                sendBroadcast(intentResponse);
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
               // super.attemptFailed(request, attemptNumber, totalAttempts);
                Log.d(MainActivity.TAG, "attemptFailed " + request + " " + attemptNumber + " " + totalAttempts);
            }

            @Override
            public void onError(VKError error) {
              //  super.onError(error);
                Log.d(MainActivity.TAG, "onError: " + error);
            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
             //   super.onProgress(progressType, bytesLoaded, bytesTotal);
                Log.d(MainActivity.TAG, "onProgress " + progressType + " " + bytesLoaded + " " + bytesTotal);
            }
        });
        super.onHandleIntent(intent);
    }


    private ArrayList<Reminder> parseResponse(String response) throws JSONException, ParseException {
        String title_part = getString(R.string.bday);
        StringBuilder title;
        long eventTime;
        Reminder r;
        String bdate;
        ArrayList<Reminder> bday_list = new ArrayList<Reminder>();
        JSONObject reader = new JSONObject(response);
        JSONArray array = reader.getJSONArray("items");
        for (int i = 0; i < array.length(); i++) {
            JSONObject friend = array.getJSONObject(i);
            bdate = friend.optString("bdate");
            if (!bdate.isEmpty()) {
                title = new StringBuilder(title_part).append(" ").append(friend.getString("first_name")).append(" ").append(friend.getString("last_name"));
                if (bdate.split("\\.").length > 2) {
                    bdate = bdate.substring(0, bdate.length() - 5);
                }
                bdate += "." + Calendar.getInstance().get(Calendar.YEAR);
                eventTime = dateFormat.parse(bdate).getTime();
                r = new Reminder(title.toString(), "", eventTime, MinutesBeforeEventTime.ONE_DAY, false);
                bday_list.add(r);
            }
        }

        return bday_list;
    }

}
