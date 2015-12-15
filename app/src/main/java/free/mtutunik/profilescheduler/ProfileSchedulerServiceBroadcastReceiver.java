package free.mtutunik.profilescheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ProfileSchedulerServiceBroadcastReceiver extends BroadcastReceiver {
    public ProfileSchedulerServiceBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, ProfileSchedulerService.class);

        serviceIntent.putExtras(intent);

        context.startService(serviceIntent);
    }
}
