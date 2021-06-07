package alarm.applicationname.company.coursework;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import alarm.applicationname.company.coursework.data.AlarmContract;

public class AlarmCursor extends CursorAdapter {

    private TextView mTitleText, mTimeText;
    private TextView mAlarmStatus;

    public AlarmCursor(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.alarm_items, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        mTitleText = view.findViewById(R.id.recycle_title);
        mTimeText = view.findViewById(R.id.recycle_time);
        mAlarmStatus = view.findViewById(R.id.status);

        // Gets the alarm data
        int titleColumnIndex = cursor.getColumnIndex(AlarmContract.AlarmEntry.KEY_TITLE);
        int timeColumnIndex = cursor.getColumnIndex(AlarmContract.AlarmEntry.KEY_TIME);
        int activeColumnIndex = cursor.getColumnIndex(AlarmContract.AlarmEntry.KEY_ACTIVE);

        String title = cursor.getString(titleColumnIndex);
        String time = cursor.getString(timeColumnIndex);
        String active = cursor.getString(activeColumnIndex);
        setAlarmTitle(title);
        // Displays the alarm data
        if (time != null){
            setAlarmTimeText(time);
        }else{
            mTimeText.setText("Time not set");
        }

        if (active != null && active.equals("false")){
            mAlarmStatus.setText("Disabled");
        } else {
            mAlarmStatus.setText("Enabled");
        }
    }

    // Set alarm title
    public void setAlarmTitle(String title) {
        mTitleText.setText(title);
    }

    // Set time text
    public void setAlarmTimeText(String datetime) {
        mTimeText.setText(datetime);
   }
}
