package alarm.applicationname.company.coursework.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AlarmDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "alarm.db";

    private static final int DATABASE_VERSION = 1;

    public AlarmDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a String that contains the SQL statement to create the alarm table
        String SQL_CREATE_ALARM_TABLE =  "CREATE TABLE " + AlarmContract.AlarmEntry.TABLE_NAME + " ("
                + AlarmContract.AlarmEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + AlarmContract.AlarmEntry.KEY_TITLE + " TEXT, "
                + AlarmContract.AlarmEntry.KEY_TIME + " TEXT, "
                + AlarmContract.AlarmEntry.KEY_ACTIVE + " TEXT " + " );";

        // Execute the SQL statement
        sqLiteDatabase.execSQL(SQL_CREATE_ALARM_TABLE);


    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
