package free.mtutunik.profilescheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by mtutunik on 12/12/15.
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmBroadcastReceiver";
    private static final long REPEAT_INTERVAL = 2 * 60 * 1000;//24 * 60 * 60 * 1000;


    @Override
    public void onReceive(Context context, Intent intent) {
        long alarmTime = intent.getLongExtra(DictionaryOpenHelper.START_TIME_FIELD, -1);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(alarmTime + REPEAT_INTERVAL);
        Log.d(TAG, "AlarmRecever: alarm " + intent.getAction() + " time: " + alarmTime);


        scheduleAlarm(intent.getStringExtra(DictionaryOpenHelper.NAME_FIELD),
                intent.getStringExtra(DictionaryOpenHelper.TYPE_FIELD),
                alarmTime + REPEAT_INTERVAL, context);
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

        scheduleAlarm(name, profileType, calendar.getTimeInMillis(), context);
    }

    public void cancelAlarm(String name, String timeStr, String profileType, Context context) {
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(name);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmMgr.cancel(alarmIntent);
    }

    private void scheduleAlarm(String name, String profileType, long alarmTimeInMills, Context context) {

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
}
