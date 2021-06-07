package alarm.applicationname.company.coursework;

import android.app.AlarmManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.Loader;
import android.support.v4.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import alarm.applicationname.company.coursework.data.AlarmContract;
import alarm.applicationname.company.coursework.data.AlarmDatabaseHelper;

public class MainActivity extends AppCompatActivity implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String CLIENT_ID = "e806cf26b32444d1b96c39292d6c630e";

    private static final String REDIRECT_URI = "coursework://callback";


    public Player mPlayer;
    private Button mAddAlarmButton;
    AlarmCursor mCursorAdapter;
    private FloatingActionButton WebView;
    //AlarmDatabaseHelper alarmDbHelper = new AlarmDatabaseHelper(this);
    ListView AlarmListView;
    TextView AlarmText;
    private String AlarmTitle = "";

    private static final int VEHICLE_LOADER = 0;
    private static final int REQUEST_CODE = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialises the user guide button
        WebView = findViewById(R.id.WebView);
        WebView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, UserGuide.class);
                    startActivity(intent);

                }
            });
        AlarmListView = findViewById(R.id.list);
        AlarmText = findViewById(R.id.AlarmPrompt);
        View emptyView = findViewById(R.id.empty_view);
        AlarmListView.setEmptyView(emptyView);
        mCursorAdapter = new AlarmCursor(this, null);
        AlarmListView.setAdapter(mCursorAdapter);

        // Initialise intents when listview items are clicked
        AlarmListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(MainActivity.this, AddAlarm.class);
                Uri currentVehicleUri = ContentUris.withAppendedId(AlarmContract.AlarmEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentVehicleUri);
                startActivity(intent);
            }
        });
        // Initialise Add alarm button
        mAddAlarmButton = findViewById(R.id.add);
        mAddAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Opens pop up dialog to create alarm title
                addAlarmTitle();
            }
        });
        getSupportLoaderManager().initLoader(VEHICLE_LOADER, null,  this);
    }
    public void Login(View view) {
        // Authentication for spotify
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        builder.setShowDialog(true);
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
}
// Gets the data to display in the list view
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                AlarmContract.AlarmEntry._ID,
                AlarmContract.AlarmEntry.KEY_TITLE,
                AlarmContract.AlarmEntry.KEY_TIME,
                AlarmContract.AlarmEntry.KEY_ACTIVE
        };

        return new CursorLoader(this,   // Parent activity context
                AlarmContract.AlarmEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }
// Shows the alarm prompt
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
        if (cursor.getCount() > 0){
            AlarmText.setVisibility(View.VISIBLE);
        }else{
            AlarmText.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    public void addAlarmTitle(){
        // Creates an alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Alarm Title");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        //This sets the alarm title in the new row in the database
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (input.getText().toString().isEmpty()){
                    return;
                }

                AlarmTitle = input.getText().toString();
                ContentValues values = new ContentValues();
                values.put(AlarmContract.AlarmEntry.KEY_TITLE, AlarmTitle);

                Uri newUri = getContentResolver().insert(AlarmContract.AlarmEntry.CONTENT_URI, values);
                restartLoader();

                if (newUri == null) {
                    Toast.makeText(getApplicationContext(), "Setting Alarm Title failed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Title set successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Cancels new alarm
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
    // Restarts the loader to display the new alarm
    public void restartLoader(){
        getSupportLoaderManager().restartLoader(VEHICLE_LOADER, null,  this);
    }
    // Initiallises the spotify player
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addNotificationCallback(MainActivity.this);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }
    // When logged into spotify, the player plays a song
    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
        mPlayer.playUri(null, "spotify:track:2TpxZ7JUBn3uw46aR7qd6V", 0, 0);
    }

    @Override
    public void onLoggedOut() {

    }

    @Override
    public void onLoginFailed(Error error) {

    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }


}
