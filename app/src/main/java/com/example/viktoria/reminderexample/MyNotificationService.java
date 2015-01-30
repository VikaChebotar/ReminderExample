package com.example.viktoria.reminderexample;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


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
        String titleVarName = getResources().getString(R.string.titleVarIntent);
        String descrVarName = getResources().getString(R.string.descrVarIntent);
        sendNotif(intent.getStringExtra(titleVarName), intent.getStringExtra(descrVarName));

        return START_REDELIVER_INTENT;
    }

    /**
     * Build notification, than send it to NotificationManager to show it.
     * @param title text to show as Content Title in notification
     * @param descr text to show as Content Description in notification
     */
   public void sendNotif(String title, String descr) {
        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(descr).setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL);
        Intent intent = new Intent(this, ReminderFragment.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        mBuilder.setContentIntent(pIntent);
        nm.notify(1, mBuilder.getNotification());
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }
}
