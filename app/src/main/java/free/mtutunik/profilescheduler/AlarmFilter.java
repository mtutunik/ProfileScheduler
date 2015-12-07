package free.mtutunik.profilescheduler;

import android.content.IntentFilter;
import android.database.Cursor;

/**
 * Created by mtyutyun on 12/4/2015.
 */
public class AlarmFilter extends IntentFilter {


    public static String getActionName(String name, String time) {
        return name + "_" + time;
    }
    public AlarmFilter(Cursor cursor) {

        while (cursor.moveToNext()) {
            String name =
                    cursor.getString(cursor.getColumnIndexOrThrow(DictionaryOpenHelper.NAME_FIELD));
            String startTime =
                    cursor.getString(cursor.getColumnIndexOrThrow(DictionaryOpenHelper.START_TIME_FIELD));
            String endTime =
                    cursor.getString(cursor.getColumnIndexOrThrow(DictionaryOpenHelper.END_TIME_FIELD));

            addAction(getActionName(name, startTime));
            addAction(getActionName(name, endTime));
        }
    }
}
