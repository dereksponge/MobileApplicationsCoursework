package alarm.applicationname.company.coursework.alarm;

import android.app.IntentService;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.SpotifyPlayer;

import alarm.applicationname.company.coursework.AddAlarm;
import alarm.applicationname.company.coursework.MainActivity;
import alarm.applicationname.company.coursework.R;
import alarm.applicationname.company.coursework.data.AlarmContract;

public class AlarmService extends IntentService{

    private NotificationManager manager;
    private static final String TAG = AlarmService.class.getSimpleName();
    Cursor cursor;
    private static final int NOTIFICATION_ID = 42;
    //This is a deep link intent, and needs the task stack
    public static PendingIntent getAlarmPendingIntent(Context context, Uri uri) {

        Intent action = new Intent(context, AlarmService.class);
        action.setData(uri);
        return PendingIntent.getService(context, 0, action, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    public AlarmService() {
        super(TAG);
    }
    // This handles the alarm trigger and displays a notification
    @Override
    protected void onHandleIntent(Intent intent) {

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Uri uri = intent.getData();

        //Display a notification to view the task details
        Intent action = new Intent(this, AddAlarm.class);
        action.setData(uri);
        PendingIntent operation = TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(action)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        //Grab the task description
        if(uri != null){
            cursor = getContentResolver().query(uri, null, null, null, null);
        }

        String description = "";
        try {
            if (cursor != null && cursor.moveToFirst()) {
                description = AlarmContract.getColumnString(cursor, AlarmContract.AlarmEntry.KEY_TITLE);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }


        // Sets up a notification channel
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan1 = new NotificationChannel("default", "Alarm Channel", NotificationManager.IMPORTANCE_DEFAULT);
            chan1.setLightColor(Color.GREEN);
            chan1.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            getManager().createNotificationChannel(chan1);
        }
        // Builds the notification and then displays it
        Notification note = new NotificationCompat.Builder(this, "default")
                .setContentTitle(getString(R.string.alarm_title))
                .setContentText(description)
                .setSmallIcon(R.mipmap.tick)
                .setContentIntent(operation)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build();

        manager.notify(NOTIFICATION_ID, note);

    }

    private NotificationManager getManager() {

        if (manager == null) {
            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        return manager;
    }

}
