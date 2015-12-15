package free.mtutunik.profilescheduler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by mtutunik on 12/14/15.
 */



public class DbHelper {
    private static String TAG = "DbHelper";
    private static DictionaryOpenHelper mDbHelper = null;
    private static Cursor mCursor = null;
    private static SQLiteDatabase mDb;

    private static DbHelper sInstance = null;

    public static void init(Context context) {
        if (sInstance == null) {
            sInstance = new DbHelper(context);
        }
    }

    DbHelper(Context context) {
        mDbHelper = new DictionaryOpenHelper(context);
        mDb = mDbHelper.getWritableDatabase();
        mCursor = loadData();
    }

    private Cursor loadData() {
        Cursor cursor = null;
        try {
            if (mDb == null) {
                mDb = mDbHelper.getWritableDatabase();
            }

            cursor = mDb.query(DictionaryOpenHelper.DICTIONARY_TABLE_NAME,
                    DictionaryOpenHelper.ALL_FIELDS,
                    null, null, null, null, null);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }

        return cursor;
    }

    public static Cursor getCursor() {
        return mCursor;
    }


    public static Cursor refreshCursor() {
        mCursor = sInstance.loadData();
        return mCursor;
    }

    public static int update(long recId, ContentValues values) {
        mDb = mDbHelper.getWritableDatabase();
        return mDb.update(DictionaryOpenHelper.DICTIONARY_TABLE_NAME, values,
                          DictionaryOpenHelper.ID + "=" + recId, null);
    }

    public static long insert(ContentValues values) {
        mDb = mDbHelper.getWritableDatabase();
        return mDb.insert(DictionaryOpenHelper.DICTIONARY_TABLE_NAME, null, values);
    }

    public static int delete(long recId) {
        mDb = mDbHelper.getWritableDatabase();
        return mDb.delete(DictionaryOpenHelper.DICTIONARY_TABLE_NAME,
                DictionaryOpenHelper.ID + "=" + recId, null);
    }

    public static Cursor queryRec(long recId) {
        mDb = mDbHelper.getWritableDatabase();
        return mDb.rawQuery("select * from " + DictionaryOpenHelper.DICTIONARY_TABLE_NAME +
                " where " + DictionaryOpenHelper.ID + "=" + recId, null);
    }

    public static Cursor queryAll() {
        mDb = mDbHelper.getWritableDatabase();
        return  mDb.query(DictionaryOpenHelper.DICTIONARY_TABLE_NAME,
                DictionaryOpenHelper.ALL_FIELDS,
                null, null, null, null, null);
    }
}
