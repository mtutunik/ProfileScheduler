package free.mtutunik.profilescheduler;

import android.content.IntentFilter;
import android.database.Cursor;

/**
 * Created by mtyutyun on 12/4/2015.
 */
public class AlarmFilter extends IntentFilter {

    public AlarmFilter(Cursor cursor) {

        while (cursor.moveToNext()) {
            String name =
                    cursor.getString(cursor.getColumnIndexOrThrow(DictionaryOpenHelper.NAME_FIELD));
            addAction(name);
        }
    }
}
