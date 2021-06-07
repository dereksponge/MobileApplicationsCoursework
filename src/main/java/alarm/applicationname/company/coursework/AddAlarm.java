package alarm.applicationname.company.coursework;

import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;
import java.util.Date;

import alarm.applicationname.company.coursework.alarm.AlarmScheduler;
import alarm.applicationname.company.coursework.data.AlarmContract;

public class AddAlarm extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_VEHICLE_LOADER = 0;

    private EditText mTitleText;
    private TextView mTimeText;
    private Switch mAlarmSwitch;
    private Calendar mCalendar;
    private int mHour, mMinute;
    private String mTitle;
    private String mTime;
    private String mActive;

    private Uri mCurrentAlarmUri;
    //private boolean mVehicleHasChanged = false;

    private static final long milMinute = 60000L;
    private static final long milHour = 3600000L;

    private static final String KEY_TITLE = "title_key";
    private static final String KEY_TIME = "time_key";
    private static final String KEY_ACTIVE = "active_key";
/*
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mVehicleHasChanged = true;
            return false;
        }
    };
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);
        // Gets the selected alarm Uri
        Intent intent = getIntent();
        mCurrentAlarmUri = intent.getData();

        setTitle(getString(R.string.add_alarm_edit_alarm));
        getLoaderManager().initLoader(EXISTING_VEHICLE_LOADER, null, this);

        mTitleText = findViewById(R.id.alarm_title);
        mTimeText = findViewById(R.id.set_time);

        mCalendar = Calendar.getInstance();
        mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        mMinute = mCalendar.get(Calendar.MINUTE);
        mTime = mHour + ":" + mMinute;
        // Allows the text to be edited
        mTitleText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTitle = s.toString().trim();
                mTitleText.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        });

        mTimeText.setText(mTime);

        mAlarmSwitch = findViewById(R.id.toggleAlarm);
        // Sets the alarm switch in the correct position depending on if the alarm is active, by default it is
        if (mActive == null) {
            mActive = "true";
            mAlarmSwitch.setChecked(true);
        } else {
            if (mActive.equals("true")) {
                mAlarmSwitch.setChecked(true);
            } else {
                mAlarmSwitch.setChecked(false);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putCharSequence(KEY_TITLE, mTitleText.getText());
        outState.putCharSequence(KEY_TIME, mTimeText.getText());
        outState.putCharSequence(KEY_ACTIVE, mActive);
    }

    // This function brings up the timepicker
    public void setTime(View v) {
        if (mCurrentAlarmUri == null) {
            Toast.makeText(this, "click again on the alarm list to set time alarm", Toast.LENGTH_LONG).show();
            return;
        }
        Calendar now = Calendar.getInstance();
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
        );
        tpd.setThemeDark(false);
        tpd.show(getFragmentManager(), "Timepickerdialog");
    }

    public void showDeleteConfirmationDialog(View v) {
        // Creates an Alert Dialog for deleting an alarm
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Delete the alarm.
                deleteAlarm();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the alarm.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void deleteAlarm() {
            // Call the ContentResolver to delete the alarm at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentAlarmUri
            // already identifies the reminder that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentAlarmUri, null, null);

            new AlarmScheduler().cancelAlarm(getApplicationContext(), mCurrentAlarmUri);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.delete_failure),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.delete_success),
                        Toast.LENGTH_SHORT).show();
            }
        // Close the activity
        finish();
    }
    // Saves the alarm
    public void saveAlarm(View v) {
        // Stops the user from saving an alarm without a title
        if (mTitleText.getText().toString().length() == 0) {
            mTitleText.setError("Alarm Title cannot be blank!");
        } else {
            ContentValues values = new ContentValues();
            // Passes the data to the database
            values.put(AlarmContract.AlarmEntry.KEY_TITLE, mTitle);
            values.put(AlarmContract.AlarmEntry.KEY_TIME, mTime);
            values.put(AlarmContract.AlarmEntry.KEY_ACTIVE, mActive);
            int currenthour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            int currentmin = Calendar.getInstance().get(Calendar.MINUTE);
            long selectedTimestamp;
            // Set up calender for creating the notification
            if (mHour >= currenthour) {
                mCalendar.set(Calendar.HOUR_OF_DAY, mHour);
                mCalendar.set(Calendar.MINUTE, mMinute);
                mCalendar.set(Calendar.SECOND, 0);
                selectedTimestamp = mCalendar.getTimeInMillis();
            } else {
                long mExtraHour = milHour * (24 - currenthour);
                long mExtraMinute = milMinute * (60 - currentmin);
                mCalendar.set(Calendar.HOUR_OF_DAY, mHour);
                mCalendar.set(Calendar.MINUTE, mMinute);
                mCalendar.set(Calendar.SECOND, 0);
                selectedTimestamp = mCalendar.getTimeInMillis() + mExtraHour + mExtraMinute;
            }

                int rowsAffected = getContentResolver().update(mCurrentAlarmUri, values, null, null);
                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, getString(R.string.update_alarm_failure),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.update_alarm_success),
                            Toast.LENGTH_SHORT).show();
                }
            // Create a new notification based on the alarm arguments
            if (mActive.equals("true")) {
                new AlarmScheduler().setAlarm(getApplicationContext(), selectedTimestamp, mCurrentAlarmUri);
            }
            finish();
        }
    }

    @NonNull
    @Override
    public android.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                AlarmContract.AlarmEntry._ID,
                AlarmContract.AlarmEntry.KEY_TITLE,
                AlarmContract.AlarmEntry.KEY_TIME,
                AlarmContract.AlarmEntry.KEY_ACTIVE,
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentAlarmUri,         // Query the content URI for the current alarm
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            int titleColumnIndex = cursor.getColumnIndex(AlarmContract.AlarmEntry.KEY_TITLE);
            int timeColumnIndex = cursor.getColumnIndex(AlarmContract.AlarmEntry.KEY_TIME);
            int activeColumnIndex = cursor.getColumnIndex(AlarmContract.AlarmEntry.KEY_ACTIVE);

            // Extract out the value from the Cursor for the given column index
            String title = cursor.getString(titleColumnIndex);

            String time = cursor.getString(timeColumnIndex);
            String active = cursor.getString(activeColumnIndex);
            if(active == null) {
                active = "true";
            }

            // Update the views on the screen with the values from the database
            mTitleText.setText(title);
            mTimeText.setText(time);
            if (active.equals("true")) {
                mActive = "true";
                mAlarmSwitch.setChecked(true);
            } else {
                mActive = "false";
                mAlarmSwitch.setChecked(false);
            }
            // Setup up the alarm switch
            mAlarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!isChecked) {
                        mActive = "false";
                    } else {
                        mActive = "true";
                    }
                }
            });
        }
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {

    }
    // passes the time set into a readable format
    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        mHour = hourOfDay;
        mMinute = minute;
        if (minute < 10) {
            mTime = hourOfDay + ":" + "0" + minute;
        } else {
            mTime = hourOfDay + ":" + minute;
        }
        mTimeText.setText(mTime);
    }
}

