package free.mtutunik.profilescheduler;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ProfileSchedulerService extends Service {
    public ProfileSchedulerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_NOT_STICKY;
    }
}
