package com.example.viktoria.reminderexample.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.viktoria.reminderexample.view.MainActivity;
import com.example.viktoria.reminderexample.R;
import com.example.viktoria.reminderexample.utils.Reminder;


/**
 * Service that show notification with title and text from intent.
 */
public class MyNotificationService extends Service  {
    private NotificationManager nm;

    @Override
    public void onCreate() {
        super.onCreate();
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String reminderVarName = getResources().getString(R.string.reminderIntent);
        sendNotif((Reminder)intent.getParcelableExtra(reminderVarName));
        return START_REDELIVER_INTENT;
    }

    /**
     * Build notification, than send it to NotificationManager to show it.
     * @param r Reminder object that is represented by notification
     */
   public void sendNotif(Reminder r) {
        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(r.getTitle())
                        .setContentText(r.getDescription()).setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        mBuilder.setContentIntent(pIntent);
       //r.getId() - unique id to create one more notification or update existing
        nm.notify(r.getId(), mBuilder.getNotification());
       stopSelf();
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }
}
