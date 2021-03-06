package free.mtutunik.profilescheduler;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
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


    private SimpleCursorAdapter mDataAdapter = null;
    private boolean mIsExiting = false;
    private static final UriMatcher sUriMatcher;

    static {

        // Creates an object that associates content URIs with numeric codes
        sUriMatcher = new UriMatcher(0);

        sUriMatcher.addURI(AUTHORITY, DictionaryOpenHelper.DICTIONARY_TABLE_NAME, TABLE_QUERY);
    }

    ;


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

        mIsExiting = false;

        DbHelper.init(this);
        showList();
        mDataAdapter.swapCursor(DbHelper.getCursor());

/*
        showList();
        getLoaderManager().initLoader(URL_LOADER, null, this);
        */
    }


    private void showList() {
        String[] columns = new String[]{DictionaryOpenHelper.NAME_FIELD,
                DictionaryOpenHelper.START_TIME_FIELD,
                DictionaryOpenHelper.STATUS_FIELD};
        int[] ids = new int[]{R.id.profile_name, R.id.start_time, R.id.profile_status};

        mDataAdapter = new SimpleCursorAdapter(this, R.layout.profile_list_layout, null, columns, ids);
        ListView listView = (ListView) findViewById(R.id.profile_listView);

        mDataAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, final Cursor cursor, final int columnIndex) {
                int recId = cursor.getInt(0);
                Log.d(TAG, "setViewValue: columnIndex: " + columnIndex + ", val: " + cursor.getInt(columnIndex));
                switch (columnIndex) {
                    case 1:
                        TextView nameTextView = (TextView) view;
                        nameTextView.setText(cursor.getString(columnIndex));
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
                                DbHelper.update(recId, values);
                                refreshData();
                                String name =
                                        cursor.getString(cursor.getColumnIndexOrThrow(DictionaryOpenHelper.NAME_FIELD));
                                String type =
                                        cursor.getString(cursor.getColumnIndexOrThrow(DictionaryOpenHelper.TYPE_FIELD));
                                String start =
                                        cursor.getString(cursor.getColumnIndexOrThrow(DictionaryOpenHelper.START_TIME_FIELD));
                                handleStatusChanged(name, start, type, checkBox.isChecked());

                            }
                        });
                        break;

                    case 4:
                        TextView timeTextView = (TextView) view;
                        timeTextView.setText(cursor.getString(columnIndex));
                        break;
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
                int status = cursor.getInt(cursor.getColumnIndexOrThrow(DictionaryOpenHelper.STATUS_FIELD));

                Intent intent = new Intent(view.getContext(), ProfileEdit.class);
                intent.putExtra(DictionaryOpenHelper.ID, recId);
                intent.putExtra(DictionaryOpenHelper.STATUS_FIELD, status);
                intent.putExtra(DictionaryOpenHelper.NAME_FIELD, name);
                intent.putExtra(DictionaryOpenHelper.TYPE_FIELD, type);
                intent.putExtra(DictionaryOpenHelper.START_TIME_FIELD, start);

                ((Activity) listView.getContext()).startActivityForResult(intent, ProfileEdit.EDIT_PROFILE);

            }
        });
    }

    private void refreshData() {
        //getLoaderManager().restartLoader(URL_LOADER, null, this);
        mDataAdapter.swapCursor(null);
        mDataAdapter.swapCursor(DbHelper.refreshCursor());
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
        String profileType = intent.getStringExtra(DictionaryOpenHelper.TYPE_FIELD);

        ContentValues values = new ContentValues();
        values.put(DictionaryOpenHelper.NAME_FIELD, name);
        values.put(DictionaryOpenHelper.START_TIME_FIELD, startTime);
        values.put(DictionaryOpenHelper.TYPE_FIELD, profileType);

        if (requestCode == ProfileEdit.NEW_PROFILE) {

            values.put(DictionaryOpenHelper.STATUS_FIELD, 1);
            isOn = true;
            recId = DbHelper.insert(values);
        } else if (requestCode == ProfileEdit.EDIT_PROFILE) {
            if (resultCode == Activity.RESULT_OK) {
                onRecordEdited(recId, values);
            } else if (resultCode == ProfileEdit.DELETE_PROFILE_RESULT) {
                isOn = false;
                DbHelper.delete(recId);
            }
        }

        handleStatusChanged(name, startTime, profileType, isOn);
        refreshData();
    }


    private void onRecordEdited(long recId, ContentValues values) {
        String[] oldRecFields = {DictionaryOpenHelper.NAME_FIELD, DictionaryOpenHelper.START_TIME_FIELD,
                DictionaryOpenHelper.END_TIME_FIELD, DictionaryOpenHelper.TYPE_FIELD};
        //Cursor oldRec = mDb.query(DictionaryOpenHelper.DICTIONARY_TABLE_NAME,
        //                          oldRecFields, DictionaryOpenHelper.ID + "=" + recId,
        //                          null, null, null, null);

        Cursor oldRec = DbHelper.queryRec(recId);
        oldRec.moveToFirst();
        String oldStart = oldRec.getString(oldRec.getColumnIndexOrThrow(DictionaryOpenHelper.START_TIME_FIELD));
        String oldName = oldRec.getString(oldRec.getColumnIndexOrThrow(DictionaryOpenHelper.NAME_FIELD));
        String oldType = oldRec.getString(oldRec.getColumnIndexOrThrow(DictionaryOpenHelper.TYPE_FIELD));

        oldRec.close();


        handleStatusChanged(oldName, oldStart, oldType, false);


        DbHelper.update(recId, values);

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
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mIsExiting = true;
        getLoaderManager().destroyLoader(URL_LOADER);
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
                        return DbHelper.queryAll();
                    }
                };
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mDataAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mDataAdapter.swapCursor(null);
    }


    private void handleStatusChanged(String name, String start,
                                     String profileType, boolean isOn) {
        /*
        if (isOn) {
            if (!mAlarmFilter.hasAction(name)) {
                mAlarmFilter.addAction(name);
            }
            mAlarmReceiver.scheduleAlarm(name, start, profileType, this);
        }
        else {
            mAlarmReceiver.cancelAlarm(name, start, profileType, this);
        }
        */

        String[] hh_mm = start.split(":");
        Integer hh = Integer.parseInt(hh_mm[0]);
        Integer mm = Integer.parseInt(hh_mm[1]);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hh);
        calendar.set(Calendar.MINUTE, mm);

        Intent serviceIntent = new Intent(this, ProfileSchedulerServiceBroadcastReceiver.class);
        serviceIntent.putExtra(DictionaryOpenHelper.NAME_FIELD, name);
        serviceIntent.putExtra(DictionaryOpenHelper.START_TIME_FIELD, calendar.getTimeInMillis());
        serviceIntent.putExtra(DictionaryOpenHelper.TYPE_FIELD, profileType);
        serviceIntent.putExtra(DictionaryOpenHelper.STATUS_FIELD, isOn);
        sendBroadcast(serviceIntent);
    }
}
