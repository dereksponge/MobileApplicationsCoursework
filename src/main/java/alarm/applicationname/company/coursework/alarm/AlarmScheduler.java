package alarm.applicationname.company.coursework.alarm;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import alarm.applicationname.company.coursework.R;

public class AlarmScheduler {

    public void setAlarm(Context context, long alarmTime, Uri AlarmTask) {
        // This initialises the AlarmmMnager and sets the alarm at a specific time
        AlarmManager manager = AlarmManagerProvider.getAlarmManager(context);
        PendingIntent operation = AlarmService.getAlarmPendingIntent(context, AlarmTask);

        if (Build.VERSION.SDK_INT >= 23) {

            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, operation);

        } else if (Build.VERSION.SDK_INT >= 19) {

            manager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, operation);

        } else {

            manager.set(AlarmManager.RTC_WAKEUP, alarmTime, operation);

        }
    }
    // This initialises the AlarmManager and cancels the specific alarm
    public void cancelAlarm(Context context, Uri alarmTask) {

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent operation = AlarmService.getAlarmPendingIntent(context, alarmTask);
        manager.cancel(operation);
    }
}
