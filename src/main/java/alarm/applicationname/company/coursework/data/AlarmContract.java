package alarm.applicationname.company.coursework.data;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class AlarmContract {

    private AlarmContract(){}

    public static final String CONTENT_AUTHORITY = "alarm.applicationname.company.coursework";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static String PATH_VEHICLE = "alarm-path";
    // Creates the database tables with the needed columns
    public static final class AlarmEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_VEHICLE);
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VEHICLE;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VEHICLE;
        public final static String TABLE_NAME = "vehicles";
        public final static String _ID = BaseColumns._ID;
        public static final String KEY_TITLE = "title";
        public static final String KEY_TIME = "time";
        public static final String KEY_ACTIVE = "active";

    }
    // Gets the column string of a particular index
    public static String getColumnString(Cursor cursor, String columnName) {

        return cursor.getString( cursor.getColumnIndex(columnName) );
    }
}
