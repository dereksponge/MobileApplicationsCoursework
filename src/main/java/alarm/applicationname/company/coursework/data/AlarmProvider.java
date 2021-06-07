package alarm.applicationname.company.coursework.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class AlarmProvider extends ContentProvider{

    private static final int ALARM = 100;

    private static final int ALARM_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        sUriMatcher.addURI(AlarmContract.CONTENT_AUTHORITY, AlarmContract.PATH_VEHICLE, ALARM);
        sUriMatcher.addURI(AlarmContract.CONTENT_AUTHORITY, AlarmContract.PATH_VEHICLE + "/#", ALARM_ID);
    }

    private AlarmDatabaseHelper mDbHelper;
    // Initialises the database helper
    @Override
    public boolean onCreate() {

        mDbHelper = new AlarmDatabaseHelper(getContext());
        return true;
    }
    // Query the data base
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        // This cursor will hold the result of the query
        Cursor cursor = null;

        int match = sUriMatcher.match(uri);
        switch (match) {
            // If the query is for the whole table
            case ALARM:
                cursor = database.query(AlarmContract.AlarmEntry.TABLE_NAME, projection, selection, selectionArgs,null, null, sortOrder);
                break;
            // If the query is for one row of the database
            case ALARM_ID:
                selection = AlarmContract.AlarmEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(AlarmContract.AlarmEntry.TABLE_NAME, projection, selection, selectionArgs,null, null, sortOrder);
                break;
            // If the is not recognised then throw an exception
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }
    // Gets the type of data returned by the Uri
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ALARM:
                return AlarmContract.AlarmEntry.CONTENT_LIST_TYPE;
            case ALARM_ID:
                return AlarmContract.AlarmEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
    // Inserts a new row into the provider
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ALARM:
                return insertAlarm(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }
    // Called in insert() to insert the alarm into the database
    private Uri insertAlarm(Uri uri, ContentValues values) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(AlarmContract.AlarmEntry.TABLE_NAME, null, values);

     /*   if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }*/
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }
    // Deletes specific rows from the database
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ALARM:
                // This will delete the whole database
                rowsDeleted = database.delete(AlarmContract.AlarmEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ALARM_ID:
                // This will delete specific rows in the database
                selection = AlarmContract.AlarmEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(AlarmContract.AlarmEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ALARM:
                return updateAlarm(uri, contentValues, selection, selectionArgs);
            case ALARM_ID:
                selection = AlarmContract.AlarmEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateAlarm(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }
    // This makes the update to the database
    private int updateAlarm(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.size() == 0) {
            return 0;
        }
        // This triggers the update
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(AlarmContract.AlarmEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
