package free.mtutunik.profilescheduler;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mtutunik on 11/21/15.
 */
public class DictionaryOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "ProfileSchedulerDB";

    public static final String DICTIONARY_TABLE_NAME = "profiles";
    public static final String NAME_FIELD = "profile_name";
    public static final String TYPE_FIELD = "profile_type";
    public static final String START_TIME_FILED = "start_time";
    public static final String END_TIME_FILED = "end_time";
    public static final String STATUS_FILED = "status";
    public static final String ID = "_id";


    public static final String[] ALL_FIELDS = new String[] {ID, NAME_FIELD, STATUS_FILED, TYPE_FIELD, START_TIME_FILED, END_TIME_FILED};

    private static final String DICTIONARY_TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS " + DICTIONARY_TABLE_NAME +
                    " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    NAME_FIELD + " TEXT, " +
                    TYPE_FIELD + " TEXT, " +
                    START_TIME_FILED + "  TEXT, " +
                    END_TIME_FILED + " TEXT, " +
                    STATUS_FILED + " INTEGER);";

    public static final String ALL_DATA_QUERY = "SELECT * FROM " + DICTIONARY_TABLE_NAME;

    DictionaryOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DICTIONARY_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
