package alarm.applicationname.company.coursework.alarm;

import android.app.AlarmManager;
import android.content.Context;

public class AlarmManagerProvider {
    // This sets up the alarm manager
    private static AlarmManager sAlarmManager;
    static synchronized AlarmManager getAlarmManager(Context context) {
        if (sAlarmManager == null) {
            sAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }
        return sAlarmManager;
    }

}
