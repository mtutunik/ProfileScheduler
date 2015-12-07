package free.mtutunik.profilescheduler;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Calendar;

public class ProfileSchedulerActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private final String TAG = "ProfileScheduler";
    private static final int URL_LOADER = 0;
    public static final String SCHEME = "content";
    public static final String AUTHORITY = "free.mtutunik.profilescheduler";
    public static final Uri CONTENT_URI = Uri.parse(SCHEME + "://" + AUTHORITY);
    public static final Uri TABLE_URL = Uri.withAppendedPath(CONTENT_URI,
            DictionaryOpenHelper.DICTIONARY_TABLE_NAME);
    private static final int TABLE_QUERY = 1;
    private static final long REPEAT_INTERVAL = 24 * 60 * 60 * 1000;


    private DictionaryOpenHelper mDbHelper = new DictionaryOpenHelper(this);
    private Cursor mCursor = null;
    private SQLiteDatabase mDb;
    private SimpleCursorAdapter mDataAdapter = null;
    private Context mContext = null;
    private AlarmFilter mAlarmFilter = null;
    private BroadcastReceiver mAlarmReceiver = null;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private static final UriMatcher sUriMatcher;

    static {

        // Creates an object that associates content URIs with numeric codes
        sUriMatcher = new UriMatcher(0);

        sUriMatcher.addURI(AUTHORITY, DictionaryOpenHelper.DICTIONARY_TABLE_NAME, TABLE_QUERY);
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_scheduler);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProfile();
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        showList();

        getLoaderManager().initLoader(URL_LOADER, null, this);
    }

    private Cursor loadData() {
        Cursor cursor = null;
        try {
            if (mDb == null) {
                mDb = mDbHelper.getWritableDatabase();
            }

            cursor = mDb.query(DictionaryOpenHelper.DICTIONARY_TABLE_NAME,
                    DictionaryOpenHelper.ALL_FIELDS,
                    null, null, null, null, null);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }

        return cursor;
    }

    private void showList() {
        String[] columns = new String[]{DictionaryOpenHelper.NAME_FIELD, DictionaryOpenHelper.STATUS_FIELD};
        int[] ids = new int[]{R.id.profile_name, R.id.profile_status};

        mDataAdapter = new SimpleCursorAdapter(this, R.layout.profile_list_layout, null, columns, ids);
        ListView listView = (ListView) findViewById(R.id.profile_listView);

        mDataAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, final Cursor cursor, final int columnIndex) {
                int recId = cursor.getInt(0);
                Log.d(TAG, "setViewValue: columnIndex: " + columnIndex + ", val: " + cursor.getInt(columnIndex));
                switch (columnIndex) {
                    case 1:
                        TextView tv = (TextView)view;
                        tv.setText(cursor.getString(columnIndex));
                        break;
                    case 2:
                        CheckBox cb = (CheckBox) view;
                        int status = cursor.getInt(columnIndex);
                        cb.setChecked(status != 0);
                        cb.setOnClickListener(new CompoundButton.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                CheckBox checkBox = (CheckBox) v;
                                int recId = cursor.getInt(0);
                                ContentValues values = new ContentValues();
                                values.put(DictionaryOpenHelper.STATUS_FIELD, checkBox.isChecked() ? 1 : 0);
                                mDb.update(DictionaryOpenHelper.DICTIONARY_TABLE_NAME, values,
                                        DictionaryOpenHelper.ID + "=" + recId, null);
                                refreshData();
                                String name =
                                        cursor.getString(cursor.getColumnIndexOrThrow(DictionaryOpenHelper.NAME_FIELD));
                                String type =
                                        cursor.getString(cursor.getColumnIndexOrThrow(DictionaryOpenHelper.TYPE_FIELD));
                                String start =
                                        cursor.getString(cursor.getColumnIndexOrThrow(DictionaryOpenHelper.START_TIME_FIELD));
                                String end =
                                        cursor.getString(cursor.getColumnIndexOrThrow(DictionaryOpenHelper.END_TIME_FIELD));
                                handleStatusChanged(name, start, end, type, checkBox.isChecked());

                            }
                        });
                }
                return true;
            }
        });


        listView.setAdapter(mDataAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view,
                                    int position, long id) {
                // Get the cursor, positioned to the corresponding row in the result set
                Cursor cursor = (Cursor) listView.getItemAtPosition(position);

                int recId = cursor.getInt(cursor.getColumnIndexOrThrow(DictionaryOpenHelper.ID));
                String name =
                        cursor.getString(cursor.getColumnIndexOrThrow(DictionaryOpenHelper.NAME_FIELD));
                String type =
                        cursor.getString(cursor.getColumnIndexOrThrow(DictionaryOpenHelper.TYPE_FIELD));
                String start =
                        cursor.getString(cursor.getColumnIndexOrThrow(DictionaryOpenHelper.START_TIME_FIELD));
                String end =
                        cursor.getString(cursor.getColumnIndexOrThrow(DictionaryOpenHelper.END_TIME_FIELD));
                int status = cursor.getInt(cursor.getColumnIndexOrThrow(DictionaryOpenHelper.STATUS_FIELD));

                Intent intent = new Intent(view.getContext(), ProfileEdit.class);
                intent.putExtra(DictionaryOpenHelper.ID, recId);
                intent.putExtra(DictionaryOpenHelper.STATUS_FIELD, status);
                intent.putExtra(DictionaryOpenHelper.NAME_FIELD, name);
                intent.putExtra(DictionaryOpenHelper.TYPE_FIELD, type);
                intent.putExtra(DictionaryOpenHelper.START_TIME_FIELD, start);
                intent.putExtra(DictionaryOpenHelper.END_TIME_FIELD, end);

                ((Activity) listView.getContext()).startActivityForResult(intent, ProfileEdit.EDIT_PROFILE);

            }
        });
    }

    private void refreshData() {
        getLoaderManager().restartLoader(URL_LOADER, null, this);
    }

    public void addProfile() {
        Intent intent = new Intent(this, ProfileEdit.class);
        startActivityForResult(intent, ProfileEdit.NEW_PROFILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "onActivityResult requestCode: " + requestCode + ", resultCode: " + resultCode);

        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }

        boolean isOn = (intent.getIntExtra(DictionaryOpenHelper.STATUS_FIELD, -1) > 0);
        long recId = intent.getIntExtra(DictionaryOpenHelper.ID, -1);
        String name = intent.getStringExtra(DictionaryOpenHelper.NAME_FIELD);
        String startTime = intent.getStringExtra(DictionaryOpenHelper.START_TIME_FIELD);
        String endTime = intent.getStringExtra(DictionaryOpenHelper.END_TIME_FIELD);
        String profileType = intent.getStringExtra(DictionaryOpenHelper.TYPE_FIELD);

        ContentValues values = new ContentValues();
        values.put(DictionaryOpenHelper.NAME_FIELD, name);
        values.put(DictionaryOpenHelper.START_TIME_FIELD, startTime);
        values.put(DictionaryOpenHelper.END_TIME_FIELD, endTime);
        values.put(DictionaryOpenHelper.TYPE_FIELD, profileType);

        mDb = mDbHelper.getWritableDatabase();

        if (requestCode == ProfileEdit.NEW_PROFILE) {

            values.put(DictionaryOpenHelper.STATUS_FIELD, 1);
            isOn = true;
            recId = mDb.insert(DictionaryOpenHelper.DICTIONARY_TABLE_NAME, null, values);
        }
        else if (requestCode == ProfileEdit.EDIT_PROFILE) {
            if (resultCode == Activity.RESULT_OK) {
                mDb.update(DictionaryOpenHelper.DICTIONARY_TABLE_NAME, values,
                        DictionaryOpenHelper.ID + "=" + recId, null);
            }
            else if (resultCode == ProfileEdit.DELETE_PROFILE_RESULT) {
                isOn = false;
                mDb.delete(DictionaryOpenHelper.DICTIONARY_TABLE_NAME,
                           DictionaryOpenHelper.ID + "=" + recId, null);
            }
        }

        refreshData();
        handleStatusChanged(name, startTime, endTime, profileType, isOn);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile_scheduler, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "ProfileScheduler Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://free.mtutunik.profilescheduler/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);

        mContext = getApplicationContext();
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "ProfileScheduler Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://free.mtutunik.profilescheduler/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroyed()");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case URL_LOADER:
                // Returns a new CursorLoader
                return new CursorLoader(
                        this,   // Parent activity context
                        TABLE_URL,        // Table to query
                        DictionaryOpenHelper.ALL_FIELDS,     // Projection to return
                        null,            // No selection clause
                        null,            // No selection arguments
                        null             // Default sort order
                ) {
                    @Override
                    public Cursor loadInBackground() {

                        /*
                        mDb = mDbHelper.getWritableDatabase();

                        return mDb.query(DictionaryOpenHelper.DICTIONARY_TABLE_NAME,
                                DictionaryOpenHelper.ALL_FIELDS,
                                null, null, null, null, null);
                                */
                        return super.loadInBackground();
                    }
                };
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mDataAdapter.swapCursor(data);
        mAlarmFilter = new AlarmFilter(data);
        registerAlarmReceiver();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mDataAdapter.swapCursor(null);
    }

    private void scheduleAlarm(String name, String timeStr, String profileType) {
        String[] hh_mm = timeStr.split(":");
        Integer hh = Integer.parseInt(hh_mm[0]);
        Integer mm = Integer.parseInt(hh_mm[1]);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hh);
        calendar.set(Calendar.MINUTE, mm);

        AlarmManager alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(AlarmFilter.getActionName(name, timeStr));
        intent.putExtra(DictionaryOpenHelper.TYPE_FIELD, profileType);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                REPEAT_INTERVAL, alarmIntent);
    }

    private void cancelAlarm(String name, String timeStr, String profileType) {
        AlarmManager alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(AlarmFilter.getActionName(name, timeStr));
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        alarmMgr.cancel(alarmIntent);
    }

    private void handleStatusChanged(String name, String start, String end,
                                     String profileType, boolean isOn) {

        if (isOn) {
            scheduleAlarm(name, start, profileType);
            scheduleAlarm(name, end, profileType);
        }
        else {
            cancelAlarm(name, start, profileType);
            cancelAlarm(name, end, profileType);
        }

        if (!mAlarmFilter.hasAction(AlarmFilter.getActionName(name, start))) {
            mAlarmFilter.addAction(AlarmFilter.getActionName(name, start));
        }

        if (!mAlarmFilter.hasAction(AlarmFilter.getActionName(name, end))) {
            mAlarmFilter.addAction(AlarmFilter.getActionName(name, end));
        }

        registerAlarmReceiver();
    }

    private void registerAlarmReceiver() {
        if (mAlarmReceiver != null) {
            unregisterAlarmReceiver();
        }
        mAlarmReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "AlarmRecever");

            }
        };

        registerReceiver(mAlarmReceiver, mAlarmFilter);
    }

    private void unregisterAlarmReceiver() {
        if (mAlarmReceiver != null) {
            unregisterReceiver(mAlarmReceiver);
            mAlarmReceiver = null;
        }
    }
}
