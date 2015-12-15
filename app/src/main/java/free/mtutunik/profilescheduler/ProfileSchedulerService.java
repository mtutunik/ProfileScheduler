package free.mtutunik.profilescheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;

public class ProfileSchedulerService extends Service {
    private static String TAG = "ProfileSchedulerService";
    private static final long REPEAT_INTERVAL = 2 * 60 * 1000;//24 * 60 * 60 * 1000;

    private AlarmFilter mAlarmFilter = null;
    private AlarmBroadcastReceiver mAlarmReceiver = null;

    public ProfileSchedulerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mAlarmFilter = new AlarmFilter(DbHelper.getCursor());
        registerAlarmReceiver();

        long alarmTime = intent.getLongExtra(DictionaryOpenHelper.START_TIME_FIELD, -1);
        String name = intent.getStringExtra(DictionaryOpenHelper.NAME_FIELD);
        String profileType = intent.getStringExtra(DictionaryOpenHelper.TYPE_FIELD);
        boolean isOn = intent.getBooleanExtra(DictionaryOpenHelper.STATUS_FIELD, false);

        Log.d(TAG, "onStartCommand: alarm " + name + " time: " + alarmTime + ", isOn: " + isOn);

        if (isOn) {
            scheduleAlarm(name, alarmTime, profileType, getApplicationContext());
        }
        else {
            cancelAlarm(name, alarmTime, profileType, getApplicationContext());
        }


        return START_NOT_STICKY;
    }


    public void scheduleAlarm(String name, String timeStr, String profileType, Context context) {
        Log.d(TAG, "scheduleAlarm: name: " + name + ", timeStr: " + timeStr);
        String[] hh_mm = timeStr.split(":");
        Integer hh = Integer.parseInt(hh_mm[0]);
        Integer mm = Integer.parseInt(hh_mm[1]);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hh);
        calendar.set(Calendar.MINUTE, mm);

        scheduleAlarm(name, calendar.getTimeInMillis(), profileType,  context);
    }

    public void cancelAlarm(String name, long alarmTimeInMills, String profileType, Context context) {
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(name);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmMgr.cancel(alarmIntent);
    }

    private void scheduleAlarm(String name, long alarmTimeInMills, String profileType, Context context) {

        Log.d(TAG, "scheduleAlarm: name: " + name + ", alarmTimeInMills: " + alarmTimeInMills);
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(name);
        intent.putExtra(DictionaryOpenHelper.TYPE_FIELD, profileType);
        intent.putExtra(DictionaryOpenHelper.NAME_FIELD, name);
        intent.putExtra(DictionaryOpenHelper.START_TIME_FIELD, alarmTimeInMills);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        alarmMgr.setExact(AlarmManager.RTC_WAKEUP, alarmTimeInMills,
                alarmIntent);
    }

    private void registerAlarmReceiver() {
        Log.d(TAG, "registerAlarmReceiver:  mAlarmReceiver: " + mAlarmReceiver );


        if (mAlarmReceiver != null) {
            unregisterAlarmReceiver();
        }
        mAlarmReceiver = new AlarmBroadcastReceiver();

        registerReceiver(mAlarmReceiver, mAlarmFilter);
    }

    private void unregisterAlarmReceiver() {
        /*
        ComponentName receiver = new ComponentName(this, AlarmBroadcastReceiver.class);
        PackageManager pm = this.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        */
        if (mAlarmReceiver != null) {
            unregisterReceiver(mAlarmReceiver);
            mAlarmReceiver = null;
        }

    }
}
