package com.example.viktoria.reminderexample.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
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
import java.util.Date;

/**
 * Service that runs VK request to get friends info and then create reminders and adds to db all upcoming birthdays
 */
public class SyncBdaysService extends Service {
    private VKRequest request;
    private ArrayList<Reminder> bday_list;
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    //action for broadcast receiver
    public static final String ACTION_SYNC_BDAYS_SERVICE = "com.example.viktoria.reminderexample.services.syncbdays";
    //results of service work and activity for result
    public static final int RESULT_OK = 1;
    public static final int RESULT_ERROR = 2;
    public static final int RESULT_CANCEL = 3;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendRequest();
        return START_REDELIVER_INTENT;
    }

    /**
     * Sends VK request to get list of friends.
     * If complete succesful then parse, add to db and create alarm reminders. Else set error status.
     */
    private void sendRequest() {

        request = new VKRequest("friends.get", VKParameters.from("fields", "bdate", "name_case", "gen"));
        request.parseModel = false;
        request.executeWithListener(new VKRequest.VKRequestListener() {
            int result;

            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                try {
                    bday_list = parseResponse(response.responseString);
                    DatabaseHandler db = DatabaseHandler.getInstance(SyncBdaysService.this);
                    db.deleteBirthdayReminders();
                    bday_list = db.addListOfReminders(bday_list);
                    for (int i = 0; i < bday_list.size(); i++) {
                        MainActivity.cancelAlarmManagerReminder(bday_list.get(i), SyncBdaysService.this);
                        MainActivity.setAlarmService(bday_list.get(i), SyncBdaysService.this);
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
                stopSelf();
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                super.attemptFailed(request, attemptNumber, totalAttempts);
                stopSelf();
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);

                stopSelf();
            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                super.onProgress(progressType, bytesLoaded, bytesTotal);
               stopSelf();
            }
        });

    }

    /**
     * Parse response of VK request to get friends
     * @param response String representation of json response
     * @return list of reminder objects that have upcoming reminder date
     * @throws JSONException
     * @throws ParseException
     */
    private ArrayList<Reminder> parseResponse(String response) throws JSONException, ParseException {
        String title_part = getString(R.string.bday);
        StringBuilder title;
        Date eventTime;
        Reminder r;
        String bdate;
        ArrayList<Reminder> bday_list = new ArrayList<Reminder>();
        JSONObject reader = new JSONObject(response);
        JSONArray array = reader.getJSONObject(getString(R.string.responseJSON)).getJSONArray(getString(R.string.itemsJSON));
        for (int i = 0; i < array.length(); i++) {
            JSONObject friend = array.getJSONObject(i);
            bdate = friend.optString(getString(R.string.bdateJSON));
            if (!bdate.isEmpty()) {
                title = new StringBuilder(title_part).append(" ").append(friend.getString(getString(R.string.first_nameJSON))).
                        append(" ").append(friend.getString(getString(R.string.last_nameJSON)));
                if (bdate.split("\\.").length > 2) {
                    bdate = bdate.substring(0, bdate.length() - 5);
                }
                bdate += "." + Calendar.getInstance().get(Calendar.YEAR);
                eventTime = dateFormat.parse(bdate);
                eventTime.setHours(9);
                eventTime.setMinutes(0);
                if (eventTime.after(Calendar.getInstance().getTime())) {
                    r = new Reminder(title.toString(), "", eventTime.getTime(), MinutesBeforeEventTime.ONE_DAY, false);
                    r.setBirthday(true);
                    bday_list.add(r);
                }
            }
        }

        return bday_list;
    }


}
