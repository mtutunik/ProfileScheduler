package free.mtutunik.profilescheduler;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;

import java.util.HashMap;

public class ProfileEdit extends AppCompatActivity {

    public static final int EDIT_PROFILE = 1;
    public static final int NEW_PROFILE = 2;
    public static final int DELETE_PROFILE_RESULT = 10;

    EditText mNameField;
    TimePicker mStartTime;
    TimePicker mEndTime;
    RadioGroup mRadioGroup;
    int mCurrentRec = -1;

    HashMap<String, Integer> mRadioBattonsMap = new HashMap<String, Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNameField = (EditText) findViewById(R.id.profile_name_editText);
        mStartTime = (TimePicker) findViewById(R.id.start_timePicker);
        mEndTime = (TimePicker) findViewById(R.id.end_timePicker);
        mRadioGroup = (RadioGroup) findViewById(R.id.profile_type_radioGroup);

        if (mRadioBattonsMap.isEmpty()) {
            RadioButton normal = (RadioButton) findViewById(R.id.normal_profile_radioButton);
            mRadioBattonsMap.put(normal.getText().toString(), R.id.normal_profile_radioButton);
            RadioButton vibrate = (RadioButton) findViewById(R.id.vibe_profile_radioButton);
            mRadioBattonsMap.put(vibrate.getText().toString(), R.id.vibe_profile_radioButton);
            RadioButton silent = (RadioButton) findViewById(R.id.silent_profile_radioButton);
            mRadioBattonsMap.put(silent.getText().toString(), R.id.silent_profile_radioButton);
        }

        Intent intent = getIntent();

        if (intent.hasExtra(DictionaryOpenHelper.ID)) {
            mCurrentRec = intent.getIntExtra(DictionaryOpenHelper.ID, -1);
            mNameField.setText(intent.getStringExtra(DictionaryOpenHelper.NAME_FIELD));
            setTimePicker(mStartTime, intent.getStringExtra(DictionaryOpenHelper.START_TIME_FILED));
            setTimePicker(mEndTime, intent.getStringExtra(DictionaryOpenHelper.END_TIME_FILED));
            String profileType = intent.getStringExtra(DictionaryOpenHelper.TYPE_FIELD);
            mRadioGroup.check(mRadioBattonsMap.get(profileType));
        }
        else {
            mCurrentRec = -1;
            mRadioGroup.check(R.id.normal_profile_radioButton);
        }
    }

    private void onSave() {
        Intent resultIntent = new Intent();
        if (mNameField.getText() != null) {
            resultIntent.putExtra(DictionaryOpenHelper.ID, mCurrentRec);
            String startTime = String.format("%d:%d", mStartTime.getCurrentHour(), mStartTime.getCurrentMinute());
            String endTime = String.format("%d:%d", mEndTime.getCurrentHour(), mStartTime.getCurrentMinute());
            String name = mNameField.getText().toString();
            resultIntent.putExtra(DictionaryOpenHelper.NAME_FIELD, name);
            resultIntent.putExtra(DictionaryOpenHelper.START_TIME_FILED, startTime);
            resultIntent.putExtra(DictionaryOpenHelper.END_TIME_FILED, endTime);
            if (mRadioGroup.getCheckedRadioButtonId() > 0) {
                RadioButton rb = (RadioButton) findViewById(mRadioGroup.getCheckedRadioButtonId());
                resultIntent.putExtra(DictionaryOpenHelper.TYPE_FIELD, rb.getText().toString());
            }

            setResult(Activity.RESULT_OK, resultIntent);

            finish();
        }
    }

    private void onDelete() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(DictionaryOpenHelper.ID, mCurrentRec);
        setResult(DELETE_PROFILE_RESULT, resultIntent);
        finish();
    }

    private void setTimePicker(TimePicker timePicker, String timeAsString) {
        String[] hh_mm = timeAsString.split(":");
        Integer hh = Integer.parseInt(hh_mm[0]);
        Integer mm = Integer.parseInt(hh_mm[1]);
        timePicker.setCurrentHour(hh);
        timePicker.setCurrentHour(mm);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.save_action:
                onSave();
                return true;
            case R.id.delete_action:
                onDelete();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
