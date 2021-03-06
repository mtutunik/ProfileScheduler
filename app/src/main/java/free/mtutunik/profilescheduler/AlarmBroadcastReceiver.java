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

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent serviceIntent = new Intent(context, ProfileSchedulerService.class);

        serviceIntent.putExtras(intent);

        context.startService(serviceIntent);
    }


}
