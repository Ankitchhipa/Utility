package com.cam.scanner.scantopdf.android.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Rect;
import android.provider.Settings;
import android.util.Log;

import com.cam.scanner.scantopdf.android.models.ImageCropping;
import com.cam.scanner.scantopdf.android.util.Constants;

import java.util.ArrayList;

public class DBHandler {

    public static final String TAG = "-----DBHandler";
    private Context mContext;
    private Helper helper;
    private SQLiteDatabase database;

    public DBHandler(Context mContext) {
        this.mContext = mContext;
        helper = new Helper(mContext);
    }

    public void open() {
        database = helper.getWritableDatabase();
    }

    public void close() {
        helper.close();
    }


    class Helper extends SQLiteOpenHelper {

        public Helper(Context context) {
            super(context, DBConstants.DATABASE_NAME, null, DBConstants.DB_VERSION);
            mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DBConstants.QUERY_CREATE_TABLE_DOC_FILTERS);
            db.execSQL(DBConstants.QUERY_CREATE_TABLE_IS_APPLY_FILTER_ALL);
            db.execSQL(DBConstants.QUERY_CREATE_TABLE_ADS_RULES);
            db.execSQL(DBConstants.QUERY_CREATE_TABLE_ADS_STATUS);
            db.execSQL(DBConstants.QUERY_CREATE_TABLE_DEVICES_ALLOWED);

            //After build version 4.5
            db.execSQL(DBConstants.QUERY_CREATE_TABLE_OCR_RULES);
            db.execSQL(DBConstants.QUERY_CREATE_TABLE_OCR_STATUS);

            //After build version 4.9 (in db v 4)
            db.execSQL(DBConstants.QUERY_CREATE_TABLE_OCR_CREDITS_API);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            Log.e(TAG, "onUpgrade: old "+oldVersion);
            Log.e(TAG, "onUpgrade: new "+newVersion);
            if (oldVersion < DBConstants.DB_VERSION) {
                // Add the RECT column if not already present
                db.execSQL("ALTER TABLE doc_editing ADD COLUMN rect TEXT");
            }
            versionHandling(db);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
            db.execSQL("PRAGMA foreign_keys = 1");
        }
    }

    public long insertApplyAllFilter(String folderName, int value) {
        ContentValues values = new ContentValues();
        values.put(DBConstants.COL_FOLDER_NAME, folderName);
        values.put(DBConstants.COL_IS_APPLY_FILTER_ALL, value);

        return database.insert(DBConstants.TABLE_APPLY_FILTER_ALL, null, values);
    }

    public long deleteApplyFilterFolder(String folderName) {
        folderName = folderName.replace("'", "''");
        return database.delete(DBConstants.TABLE_APPLY_FILTER_ALL, DBConstants.COL_FOLDER_NAME + " = '" +
                folderName + "'", null);
    }

    public long updateApplyFilterFolder(String newFolderName, String oldFolderName) {

        newFolderName = newFolderName.replace("'", "''");
        oldFolderName = oldFolderName.replace("'", "''");

        ContentValues values = new ContentValues();
        values.put(DBConstants.COL_FOLDER_NAME, newFolderName);

        return database.update(DBConstants.TABLE_APPLY_FILTER_ALL, values, DBConstants.COL_FOLDER_NAME + " = '" + oldFolderName +
                "'", null);
    }

    public boolean isApplyAllFilterOptionAvailable(String folderName) {
        folderName = folderName.replace("'", "''");

        String query = "select * from " + DBConstants.TABLE_APPLY_FILTER_ALL + " where " + DBConstants.COL_FOLDER_NAME + " = '" + folderName + "'";

        Cursor c = database.rawQuery(query, null);

        if (c.getCount() > 0) {
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);

                int applyFilterAllVal = c.getInt(c.getColumnIndex(DBConstants.COL_IS_APPLY_FILTER_ALL));

                return applyFilterAllVal >= DBConstants.APPLY_ALL_FILTER_VALUE;
            }
        }

        return false;
    }

    public long insertDefaultFilter(String folderName, String fileName, int filterType, String x, String y, int rotation, Rect rect) {

        ContentValues values = new ContentValues();
        values.put(DBConstants.COL_FOLDER_NAME, folderName);
        values.put(DBConstants.COL_FILE_NAME, fileName);
        values.put(DBConstants.COL_FILTER_TYPE, filterType);
        values.put(DBConstants.COL_POINT_X, x);
        values.put(DBConstants.COL_POINT_Y, y);
        values.put(DBConstants.COL_ROTATION, rotation);
        String rectString;
        if(rect == null) {
            rectString = "";
        }else {
            rectString = rect.left + "," + rect.top + "," + rect.right + "," + rect.bottom;
        }
        values.put(DBConstants.COL_RECT, rectString);

        return database.insert(DBConstants.TABLE_DOC_EDITING, null, values);
    }

    public boolean isRecordExists(String folderName, String fileName) {
        String query = "select " + DBConstants.COL_FILE_NAME + " AND " + DBConstants.COL_FOLDER_NAME + " from " + DBConstants.TABLE_DOC_EDITING +
                " where " + DBConstants.COL_FOLDER_NAME + " = '" + folderName + "' AND " + DBConstants.COL_FILE_NAME + " = '" + fileName + "'";

        Cursor c = database.rawQuery(query, null);
        try {
            return c.getCount() > 0;
        } finally {
            if (c != null) c.close();
            c = null;
            query = null;
        }
    }

    public ArrayList<ImageCropping> fetchAllRecords(String folderName) {

        ArrayList<ImageCropping> croppings = null;

        String query = "select * from " + DBConstants.TABLE_DOC_EDITING +
                " where " + DBConstants.COL_FOLDER_NAME + " = '" + folderName + "'";

        Cursor c = database.rawQuery(query, null);
        try {
            if (c.getCount() > 0) {
                croppings = new ArrayList<>();
                for (int i = 0; i < c.getCount(); i++) {
                    ImageCropping cropping = new ImageCropping();
                    c.moveToPosition(i);

                    cropping.id = c.getInt(c.getColumnIndex(DBConstants.COL_ID));
                    cropping.filterType = c.getInt(c.getColumnIndex(DBConstants.COL_FILTER_TYPE));
                    cropping.fileName = c.getString(c.getColumnIndex(DBConstants.COL_FILE_NAME));
                    cropping.rotation = c.getInt(c.getColumnIndex(DBConstants.COL_ROTATION));
                    cropping.x = c.getString(c.getColumnIndex(DBConstants.COL_POINT_X));
                    cropping.y = c.getString(c.getColumnIndex(DBConstants.COL_POINT_Y));
                    String rect = c.getString(c.getColumnIndex(DBConstants.COL_RECT));
                    if(rect  != null && !rect.isEmpty()) {
                        String[] rectString = rect.split(",");
                        Log.e(TAG, "fetchRecord: "+rectString[0] +" "+rectString[1]+" "+rectString[2]+" "+rectString[3]);
                        cropping.cropRect = new Rect(Integer.parseInt(rectString[0].trim()), Integer.parseInt(rectString[1].trim()), Integer.parseInt(rectString[2].trim()), Integer.parseInt(rectString[3].trim()));
                    }else {
                        cropping.cropRect = null;
                    }

                    croppings.add(cropping);
                }
            }
        } finally {
            if (c != null) c.close();
            c = null;
            query = null;
        }
        return croppings;
    }

    public ImageCropping fetchRecord(String folderName, String fileName) {

        ImageCropping cropping = null;

        //folderName = folderName.replace("'", "''");
        //fileName = fileName.replace("'", "''");
        Log.e(TAG, "fetchRecord: "+folderName );
        Log.e(TAG, "fetchRecord: "+fileName );

        String query = "select * from " + DBConstants.TABLE_DOC_EDITING +
                " where " + DBConstants.COL_FOLDER_NAME + " = '" + folderName + "' AND " + DBConstants.COL_FILE_NAME + " = '" + fileName + "'";

        Cursor c = database.rawQuery(query, null);
        try {
            if (c != null && c.moveToFirst()) {
                Log.e(TAG, "fetchRecord: " + c.getCount());

                cropping = new ImageCropping();

                cropping.id = c.getInt(c.getColumnIndex(DBConstants.COL_ID));
                cropping.filterType = c.getInt(c.getColumnIndex(DBConstants.COL_FILTER_TYPE));
                cropping.fileName = c.getString(c.getColumnIndex(DBConstants.COL_FILE_NAME));
                cropping.rotation = c.getInt(c.getColumnIndex(DBConstants.COL_ROTATION));
                cropping.x = c.getString(c.getColumnIndex(DBConstants.COL_POINT_X));
                cropping.y = c.getString(c.getColumnIndex(DBConstants.COL_POINT_Y));
                String rect = c.getString(c.getColumnIndex(DBConstants.COL_RECT));
                if(rect  != null && !rect.isEmpty()) {
                    String[] rectString = rect.split(",");
                    Log.e(TAG, "fetchRecord: "+rectString[0] +" "+rectString[1]+" "+rectString[2]+" "+rectString[3]);
                    cropping.cropRect = new Rect(Integer.parseInt(rectString[0].trim()), Integer.parseInt(rectString[1].trim()), Integer.parseInt(rectString[2].trim()), Integer.parseInt(rectString[3].trim()));
                }else {
                    cropping.cropRect = null;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            c.close();
        }
        return cropping;
    }

    public long updatePoints(String folderName, String fileName, String x, String y) {

        ContentValues values = new ContentValues();
        values.put(DBConstants.COL_POINT_X, x);
        values.put(DBConstants.COL_POINT_Y, y);

        folderName = folderName.replace("'", "''");
        fileName = fileName.replace("'", "''");

        return database.update(DBConstants.TABLE_DOC_EDITING, values, DBConstants.COL_FOLDER_NAME + " = '" + folderName +
                "' AND " + DBConstants.COL_FILE_NAME + " = '" + fileName + "'", null);
    }

    public long updateRotation(String folderName, String fileName, int rotation) {

        ContentValues values = new ContentValues();
        values.put(DBConstants.COL_ROTATION, rotation);

        folderName = folderName.replace("'", "''");
        fileName = fileName.replace("'", "''");

        return database.update(DBConstants.TABLE_DOC_EDITING, values, DBConstants.COL_FOLDER_NAME + " = '" + folderName +
                "' AND " + DBConstants.COL_FILE_NAME + " = '" + fileName + "'", null);
    }

    public long updateFilter(String folderName, String fileName, int filterType) {

        ContentValues values = new ContentValues();
        values.put(DBConstants.COL_FILTER_TYPE, filterType);

        folderName = folderName.replace("'", "''");
        fileName = fileName.replace("'", "''");

        return database.update(DBConstants.TABLE_DOC_EDITING, values, DBConstants.COL_FILE_NAME + " = '" + fileName + "' " +
                "AND " + DBConstants.COL_FOLDER_NAME + " = '" + folderName + "'", null);
    }

    public long updateRect(String folderName, String fileName, Rect rect) {

        ContentValues values = new ContentValues();
        String rectString = rect.left + "," + rect.top + "," + rect.right + "," + rect.bottom;
        values.put(DBConstants.COL_RECT, rectString);

        folderName = folderName.replace("'", "''");
        fileName = fileName.replace("'", "''");

        return database.update(DBConstants.TABLE_DOC_EDITING, values, DBConstants.COL_FILE_NAME + " = '" + fileName + "' " +
                "AND " + DBConstants.COL_FOLDER_NAME + " = '" + folderName + "'", null);
    }

    public void deleteFile(String folderName, String fileName) {

        folderName = folderName.replace("'", "''");
        fileName = fileName.replace("'", "''");

        database.delete(DBConstants.TABLE_DOC_EDITING, DBConstants.COL_FOLDER_NAME + " = '" +
                folderName + "' AND " + DBConstants.COL_FILE_NAME + " = '" + fileName + "'", null);
    }

    public void updateFolderName(String oldName, String newName) {

        oldName = oldName.replace("'", "''");
        newName = newName.replace("'", "''");

        ContentValues values = new ContentValues();
        values.put(DBConstants.COL_FOLDER_NAME, newName);

        database.update(DBConstants.TABLE_DOC_EDITING, values, DBConstants.COL_FOLDER_NAME + " = '" + oldName + "'",
                null);
    }

    public void updateFileName(String oldName, String newName) {

        oldName = oldName.replace("'", "''");
        newName = newName.replace("'", "''");

        ContentValues values = new ContentValues();
        values.put(DBConstants.COL_FILE_NAME, newName);

        database.update(DBConstants.TABLE_DOC_EDITING, values, DBConstants.COL_FILE_NAME + " = '" + oldName + "'",
                null);
    }

    public boolean existDevicesAllowed(String androidId) {
        boolean exist = false;

        String[] cols = {DBConstants.COL_ID};
        Cursor cursor = null;

        Log.i(TAG, "database: " + database);

        cursor = database.query(DBConstants.TABLE_DEVICES_ALLOWED, cols,
                DBConstants.COL_ANDROID_ID + " = '" + androidId + "'"
                , null, null, null, null);
        if (cursor.moveToFirst()) {
            exist = true;
        }

        cursor.close();

        return exist;
    }

    public boolean existDevicesAllowed() {
        boolean exist = false;

        String[] cols = {DBConstants.COL_ID};
        Cursor cursor = null;

        Log.i(TAG, "database: " + database);

        cursor = database.query(DBConstants.TABLE_DEVICES_ALLOWED, cols,
                null
                , null, null, null, null);
        if (cursor.moveToFirst()) {
            exist = true;
        }

        cursor.close();

        return exist;
    }

    public long insertDevicesAllowed(String androidId) {
        long rowIdInserted = 0;

        ContentValues cv = new ContentValues();
        cv.put(DBConstants.COL_ANDROID_ID, androidId);
        cv.put(DBConstants.COL_CREATED_ON, System.currentTimeMillis());
        cv.put(DBConstants.COL_UPDATED_ON, System.currentTimeMillis());

        rowIdInserted = database.insert(DBConstants.TABLE_DEVICES_ALLOWED, null, cv);
        return rowIdInserted;
    }

    public ArrayList<String> getDevicesAllowed() {
        ArrayList<String> al = new ArrayList<>();
        Cursor cursor = null;

        String[] cols = {DBConstants.COL_ANDROID_ID};

        try {
            cursor = database.query(DBConstants.TABLE_DEVICES_ALLOWED, cols,
                    null,
                    null,
                    null,
                    null,
                    null);
            if (cursor.moveToFirst()) {
                do {
                    String androidId = cursor.getString(cursor.getColumnIndex
                            (DBConstants.COL_ANDROID_ID));
                    al.add(androidId);
                } while (cursor.moveToNext());
            }
        } finally {
            // this gets called even if there is an exception somewhere above
            if (cursor != null)
                cursor.close();
        }
        return al;
    }

    public boolean existAdsRules() {
        boolean exist = false;

        String[] cols = {DBConstants.COL_ID};
        Cursor cursor = null;

        cursor = database.query(DBConstants.TABLE_ADS_RULES, cols,
                DBConstants.COL_ID + " = 1"
                , null, null, null, null);
        if (cursor.moveToFirst()) {
            exist = true;
        }

        cursor.close();

        return exist;
    }

    public int updateAdsRules(int showIntersSplash, int showIntersExit, int showIntersCreation,
                              int showNative,
                              int intersSplashAfter, int intersCreateFreq) {
        int rowAffected = 0;

        ContentValues cv = new ContentValues();
        cv.put(DBConstants.COL_SHOW_INTERS_SPLASH, showIntersSplash);
        cv.put(DBConstants.COL_SHOW_INTERS_EXIT, showIntersExit);
        cv.put(DBConstants.COL_SHOW_INTERS_CREATION, showIntersCreation);
        cv.put(DBConstants.COL_SHOW_NATIVE, showNative);
        cv.put(DBConstants.COL_INTERS_SPLASH_AFTER, intersSplashAfter);
        cv.put(DBConstants.COL_INTERS_CREATE_FREQ_IN_SESSION, intersCreateFreq);
        cv.put(DBConstants.COL_UPDATED_ON, System.currentTimeMillis());

        rowAffected = database.update(DBConstants.TABLE_ADS_RULES, cv,
                DBConstants.COL_ID + " = 1", null);

        return rowAffected;
    }

    public long insertAdsRules(int showIntersSplash, int showIntersExit, int showIntersCreation,
                               int showNative,
                               int intersSplashAfter, int intersCreateFreq) {
        long rowIdInserted = 0;

        ContentValues cv = new ContentValues();
        cv.put(DBConstants.COL_SHOW_INTERS_SPLASH, showIntersSplash);
        cv.put(DBConstants.COL_SHOW_INTERS_EXIT, showIntersExit);
        cv.put(DBConstants.COL_SHOW_INTERS_CREATION, showIntersCreation);
        cv.put(DBConstants.COL_SHOW_NATIVE, showNative);
        cv.put(DBConstants.COL_INTERS_SPLASH_AFTER, intersSplashAfter);
        cv.put(DBConstants.COL_INTERS_CREATE_FREQ_IN_SESSION, intersCreateFreq);
        cv.put(DBConstants.COL_CREATED_ON, System.currentTimeMillis());
        cv.put(DBConstants.COL_UPDATED_ON, System.currentTimeMillis());

        rowIdInserted = database.insert(DBConstants.TABLE_ADS_RULES, null, cv);
        return rowIdInserted;
    }

    public boolean showIntesSplash() {
        int i = 0;
        String[] cols = {DBConstants.COL_SHOW_INTERS_SPLASH};
        Cursor cursor = null;
        try {
            cursor = database.query(DBConstants.TABLE_ADS_RULES, cols,
                    DBConstants.COL_ID + " = 1"
                    , null, null, null, DBConstants.COL_ID + " DESC");
            if (cursor.moveToFirst()) {
                i = cursor.getInt(cursor.getColumnIndex
                        (DBConstants.COL_SHOW_INTERS_SPLASH));
            }
        } finally {
            // this gets called even if there is an exception somewhere above
            if (cursor != null)
                cursor.close();
        }
        boolean show = i == 1;
        return show;
    }

    public boolean showNative() {
        int i = 0;
        String[] cols = {DBConstants.COL_SHOW_NATIVE};
        Cursor cursor = null;
        try {
            cursor = database.query(DBConstants.TABLE_ADS_RULES, cols,
                    DBConstants.COL_ID + " = 1"
                    , null, null, null, DBConstants.COL_ID + " DESC");
            if (cursor.moveToFirst()) {
                i = cursor.getInt(cursor.getColumnIndex
                        (DBConstants.COL_SHOW_NATIVE));
            }
        } finally {
            // this gets called even if there is an exception somewhere above
            if (cursor != null)
                cursor.close();
        }
        boolean show = i == 1;
        return show;
    }

    public boolean showIntersExit() {
        int i = 0;
        String[] cols = {DBConstants.COL_SHOW_INTERS_EXIT};
        Cursor cursor = null;
        try {
            cursor = database.query(DBConstants.TABLE_ADS_RULES, cols,
                    DBConstants.COL_ID + " = 1"
                    , null, null, null, DBConstants.COL_ID + " DESC");
            if (cursor.moveToFirst()) {
                i = cursor.getInt(cursor.getColumnIndex
                        (DBConstants.COL_SHOW_INTERS_EXIT));
            }
        } finally {
            // this gets called even if there is an exception somewhere above
            if (cursor != null)
                cursor.close();
        }
        boolean show = i == 1;
        return show;
    }

    public boolean showIntersCreation() {
        int i = 0;
        String[] cols = {DBConstants.COL_SHOW_INTERS_CREATION};
        Cursor cursor = null;
        try {
            cursor = database.query(DBConstants.TABLE_ADS_RULES, cols,
                    DBConstants.COL_ID + " = 1"
                    , null, null, null, DBConstants.COL_ID + " DESC");
            if (cursor.moveToFirst()) {
                i = cursor.getInt(cursor.getColumnIndex
                        (DBConstants.COL_SHOW_INTERS_CREATION));
            }
        } finally {
            // this gets called even if there is an exception somewhere above
            if (cursor != null)
                cursor.close();
        }
        boolean show = i == 1;
        return show;
    }

    public int intersSplashAfter() {
        int i = 0;
        String[] cols = {DBConstants.COL_INTERS_SPLASH_AFTER};
        Cursor cursor = null;
        try {
            cursor = database.query(DBConstants.TABLE_ADS_RULES, cols,
                    DBConstants.COL_ID + " = 1"
                    , null, null, null, DBConstants.COL_ID + " DESC");
            if (cursor.moveToFirst()) {
                i = cursor.getInt(cursor.getColumnIndex
                        (DBConstants.COL_INTERS_SPLASH_AFTER));
            }
        } finally {
            // this gets called even if there is an exception somewhere above
            if (cursor != null)
                cursor.close();
        }
        return i;
    }

    public int intersCreateFreqInSession() {
        int i = 0;
        String[] cols = {DBConstants.COL_INTERS_CREATE_FREQ_IN_SESSION};
        Cursor cursor = null;
        try {
            cursor = database.query(DBConstants.TABLE_ADS_RULES, cols,
                    DBConstants.COL_ID + " = 1"
                    , null, null, null, DBConstants.COL_ID + " DESC");
            if (cursor.moveToFirst()) {
                i = cursor.getInt(cursor.getColumnIndex
                        (DBConstants.COL_INTERS_CREATE_FREQ_IN_SESSION));
            }
        } finally {
            // this gets called even if there is an exception somewhere above
            if (cursor != null)
                cursor.close();
        }
        return i;
    }

    public boolean existSplashAttempted() {
        boolean exist = false;

        String[] cols = {DBConstants.COL_ID};
        Cursor cursor = null;

        cursor = database.query(DBConstants.TABLE_ADS_STATUS, cols,
                null
                , null, null, null, DBConstants.COL_ID + " DESC");
        if (cursor.moveToFirst()) {
            exist = true;
        }

        cursor.close();

        return exist;
    }

    public long insertSplashAttempt(int attemptCount) {
        long rowIdInserted = 0;

        ContentValues cv = new ContentValues();
        cv.put(DBConstants.COL_SPLASH_ATTEMPTED, attemptCount);
        cv.put(DBConstants.COL_UPDATED_ON, System.currentTimeMillis());

        rowIdInserted = database.insert(DBConstants.TABLE_ADS_STATUS, null, cv);
        return rowIdInserted;
    }

    public int updateSplashAttempt(int attemptCount) {
        int rowAffected = 0;

        ContentValues cv = new ContentValues();
        cv.put(DBConstants.COL_SPLASH_ATTEMPTED, attemptCount);
        cv.put(DBConstants.COL_UPDATED_ON, System.currentTimeMillis());

        rowAffected = database.update(DBConstants.TABLE_ADS_STATUS, cv,
                null, null);

        return rowAffected;
    }

    public int getSplashAttempted() {
        int splashAttempted = 0;
        String[] cols = {DBConstants.COL_SPLASH_ATTEMPTED};
        Cursor cursor = null;
        try {
            cursor = database.query(DBConstants.TABLE_ADS_STATUS, cols,
                    null
                    , null, null, null, DBConstants.COL_ID + " DESC");
            if (cursor.moveToFirst()) {
                splashAttempted = cursor.getInt(cursor.getColumnIndex
                        (DBConstants.COL_SPLASH_ATTEMPTED));
            }
        } finally {
            // this gets called even if there is an exception somewhere above
            if (cursor != null)
                cursor.close();
        }
        return splashAttempted;
    }

    public boolean existIntersCreateFreq() {
        boolean exist = false;

        String[] cols = {DBConstants.COL_ID};
        Cursor cursor = null;

        cursor = database.query(DBConstants.TABLE_ADS_STATUS, cols,
                null
                , null, null, null, DBConstants.COL_ID + " DESC");
        if (cursor.moveToFirst()) {
            exist = true;
        }

        cursor.close();

        return exist;
    }

    public long insertIntersCreateFreq(int freq) {
        long rowIdInserted = 0;

        ContentValues cv = new ContentValues();
        cv.put(DBConstants.COL_AD_INTERS_CREATE_ATTEMPTED_IN_SESSION, freq);
        cv.put(DBConstants.COL_UPDATED_ON, System.currentTimeMillis());

        rowIdInserted = database.insert(DBConstants.TABLE_ADS_STATUS, null, cv);
        return rowIdInserted;
    }

    public int updateIntersCreateFreq(int freq) {
        int rowAffected = 0;

        ContentValues cv = new ContentValues();
        cv.put(DBConstants.COL_AD_INTERS_CREATE_ATTEMPTED_IN_SESSION, freq);
        cv.put(DBConstants.COL_UPDATED_ON, System.currentTimeMillis());

        rowAffected = database.update(DBConstants.TABLE_ADS_STATUS, cv,
                null, null);

        return rowAffected;
    }

    public int getIntersCreateFreq() {
        int freq = 0;
        String[] cols = {DBConstants.COL_AD_INTERS_CREATE_ATTEMPTED_IN_SESSION};
        Cursor cursor = null;
        try {
            cursor = database.query(DBConstants.TABLE_ADS_STATUS, cols,
                    null
                    , null, null, null, DBConstants.COL_ID + " DESC");
            if (cursor.moveToFirst()) {
                freq = cursor.getInt(cursor.getColumnIndex
                        (DBConstants.COL_AD_INTERS_CREATE_ATTEMPTED_IN_SESSION));
            }
        } finally {
            // this gets called even if there is an exception somewhere above
            if (cursor != null)
                cursor.close();
        }
        return freq;
    }

    public boolean deleteDeviceAllowed() {
        boolean deleted = database.delete(DBConstants.TABLE_DEVICES_ALLOWED, null, null) > 0;
        String resetAIDeviceAllowed = "delete from sqlite_sequence where name='" + DBConstants.TABLE_DEVICES_ALLOWED + "'";
        database.execSQL(resetAIDeviceAllowed);
        return deleted;
    }

    private void versionHandling(SQLiteDatabase db) {

        if (!tableExists(db, DBConstants.TABLE_APPLY_FILTER_ALL))
            db.execSQL(DBConstants.QUERY_CREATE_TABLE_IS_APPLY_FILTER_ALL);

        //After build version 4.5
        if (!tableExists(db, DBConstants.TABLE_OCR_RULES))
            db.execSQL(DBConstants.QUERY_CREATE_TABLE_OCR_RULES);

        if (!tableExists(db, DBConstants.TABLE_OCR_STATUS))
            db.execSQL(DBConstants.QUERY_CREATE_TABLE_OCR_STATUS);

        //After build version 4.9 (in db v 4)
        if (!tableExists(db, DBConstants.TABLE_OCR_CREDITS_API))
            db.execSQL(DBConstants.QUERY_CREATE_TABLE_OCR_CREDITS_API);

        //Alter Changes
        if (!isFieldExist(db, DBConstants.TABLE_OCR_CREDITS_API, DBConstants.COL_DEVICE_ID))
            db.execSQL(DBConstants.QUERY_ALTER_TABLE_OCR_CREDITS_API);

        if (!isFieldExist(db, DBConstants.TABLE_OCR_CREDITS_API, DBConstants.COL_SUBSCRIPTION_ID))
            db.execSQL(DBConstants.QUERY_ALTER_TABLE_OCR_CREDITS_API_2);

        if (!isFieldExist(db, DBConstants.TABLE_OCR_CREDITS_API, DBConstants.COL_EMAIL))
            db.execSQL(DBConstants.QUERY_ALTER_TABLE_OCR_CREDITS_API_3);

        if (!isFieldExist(db, DBConstants.TABLE_OCR_CREDITS_API, DBConstants.COL_IS_OCR))
            db.execSQL(DBConstants.QUERY_ALTER_TABLE_OCR_CREDITS_API_4);

        if (!isFieldExist(db, DBConstants.TABLE_OCR_CREDITS_API, DBConstants.COL_IS_PREMIUM))
            db.execSQL(DBConstants.QUERY_ALTER_TABLE_OCR_CREDITS_API_5);

        if (!isFieldExist(db, DBConstants.TABLE_OCR_CREDITS_API, DBConstants.COL_PLAN_ID))
            db.execSQL(DBConstants.QUERY_ALTER_TABLE_OCR_CREDITS_API_6);


        //Create Changes
        //Example
        /*if (!tableExists(db, DBConstants.TABLE_C2CPOOL))
            db.execSQL(DBConstants.QUERY_CREATE_TABLE_C2CPOOL);*/
        ////
        ////

        //Alter Changes
        //Example
        /*if (!isFieldExist(db, DBConstants.TABLE_KID_FEATURES, DBConstants.COL_SETTING_TIMESTAMP))
            db.execSQL(DBConstants.QUERY_ALTER_TABLE_KID_FEATURES);*/
        ////
        ////
    }

    private boolean tableExists(SQLiteDatabase database, String tableName) {
        if (tableName == null || database == null || !database.isOpen()) {
            Log.i(TAG, "Return from db null or not open");
            return false;
        }


        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[]{"table", tableName});

        if (!cursor.moveToFirst()) {
            Log.i(TAG, "Return from cursor count is 0");
            cursor.close();
            return false;
        }

        int count = cursor.getInt(0);
        Log.i(TAG, "Return from count value====" + count);
        cursor.close();
        return count > 0;
    }

    public boolean isFieldExist(SQLiteDatabase database, String tableName, String fieldName) {
        if (database == null || !database.isOpen()) {
            Log.i(TAG, " return from here");
            return false;
        }


        Cursor mCursor = null;
        try {
            // Query 1 row
            mCursor = database.rawQuery("SELECT * FROM " + tableName + " LIMIT 0", null);

            // getColumnIndex() gives us the index (0 to ...) of the column - otherwise we get a -1
            int coloumnIndex = mCursor.getColumnIndex(fieldName);
            Log.i(TAG, " Coloumn index with coloumn namee=====" + tableName + " ==== " + fieldName + " ====== " + coloumnIndex);
            if (coloumnIndex != -1)
                return true;
            else
                return false;

        } catch (Exception Exp) {
            Exp.printStackTrace();
            return false;
        } finally {
            if (mCursor != null) mCursor.close();
        }

    }

    public boolean existOcrRules() {
        boolean exist = false;

        String[] cols = {DBConstants.COL_ID};
        Cursor cursor = null;

        cursor = database.query(DBConstants.TABLE_OCR_RULES, cols,
                DBConstants.COL_ID + " = 1"
                , null, null, null, null);
        if (cursor.moveToFirst()) {
            exist = true;
        }

        cursor.close();

        return exist;
    }

    public int updateOcrRules(int free, int premiumYearly, int ocrMonthly,
                              int testFree, int testPremiumYearly, int testOcrMonthly) {
        int rowAffected = 0;

        ContentValues cv = new ContentValues();
        cv.put(DBConstants.COL_FREE, free);
        cv.put(DBConstants.COL_PREMIUM_YARLY, premiumYearly);
        cv.put(DBConstants.COL_OCR_MONTHLY, ocrMonthly);
        cv.put(DBConstants.COL_TEST_FREE, testFree);
        cv.put(DBConstants.COL_TEST_PREMIUM_YARLY, testPremiumYearly);
        cv.put(DBConstants.COL_TEST_OCR_MONTHLY, testOcrMonthly);
        cv.put(DBConstants.COL_UPDATED_ON, System.currentTimeMillis());

        rowAffected = database.update(DBConstants.TABLE_OCR_RULES, cv,
                DBConstants.COL_ID + " = 1", null);

        return rowAffected;
    }

    public long insertOcrRules(int free, int premiumYearly, int ocrMonthly,
                               int testFree, int testPremiumYearly, int testOcrMonthly) {
        long rowIdInserted = 0;

        ContentValues cv = new ContentValues();
        cv.put(DBConstants.COL_FREE, free);
        cv.put(DBConstants.COL_PREMIUM_YARLY, premiumYearly);
        cv.put(DBConstants.COL_OCR_MONTHLY, ocrMonthly);
        cv.put(DBConstants.COL_TEST_FREE, testFree);
        cv.put(DBConstants.COL_TEST_PREMIUM_YARLY, testPremiumYearly);
        cv.put(DBConstants.COL_TEST_OCR_MONTHLY, testOcrMonthly);
        cv.put(DBConstants.COL_CREATED_ON, System.currentTimeMillis());
        cv.put(DBConstants.COL_UPDATED_ON, System.currentTimeMillis());

        rowIdInserted = database.insert(DBConstants.TABLE_OCR_RULES, null, cv);
        return rowIdInserted;
    }

    public int getAllowedFreeOcr() {

        String selfAndroidId = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        Log.i(TAG, "self android_id: " + selfAndroidId);

        boolean isTestDevice = false;

        if (existDevicesAllowed(selfAndroidId)) {
            isTestDevice = true;
        }

        int allowedFree = 0;
        String[] cols = {DBConstants.COL_FREE, DBConstants.COL_TEST_FREE};
        Cursor cursor = null;
        try {
            cursor = database.query(DBConstants.TABLE_OCR_RULES, cols,
                    null
                    , null, null, null, DBConstants.COL_ID + " DESC");
            if (cursor.moveToFirst()) {
                if (isTestDevice) {
                    allowedFree = cursor.getInt(cursor.getColumnIndex
                            (DBConstants.COL_TEST_FREE));
                } else {
                    allowedFree = cursor.getInt(cursor.getColumnIndex
                            (DBConstants.COL_FREE));
                }
            }
        } finally {
            // this gets called even if there is an exception somewhere above
            if (cursor != null)
                cursor.close();
        }
        return allowedFree;
    }

    public int getAllowedPremiumYearlyOcr() {

        String selfAndroidId = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        Log.i(TAG, "self android_id: " + selfAndroidId);

        boolean isTestDevice = false;

        if (existDevicesAllowed(selfAndroidId)) {
            isTestDevice = true;
        }

        int allowedPremiumYearly = 0;
        String[] cols = {DBConstants.COL_PREMIUM_YARLY, DBConstants.COL_TEST_PREMIUM_YARLY};
        Cursor cursor = null;
        try {
            cursor = database.query(DBConstants.TABLE_OCR_RULES, cols,
                    null
                    , null, null, null, DBConstants.COL_ID + " DESC");
            if (cursor.moveToFirst()) {
                if (isTestDevice) {
                    allowedPremiumYearly = cursor.getInt(cursor.getColumnIndex
                            (DBConstants.COL_TEST_PREMIUM_YARLY));
                } else {
                    allowedPremiumYearly = cursor.getInt(cursor.getColumnIndex
                            (DBConstants.COL_PREMIUM_YARLY));
                }
            }
        } finally {
            // this gets called even if there is an exception somewhere above
            if (cursor != null)
                cursor.close();
        }
        return allowedPremiumYearly;
    }

    public int getAllowedOcrMonthly() {

        String selfAndroidId = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        Log.i(TAG, "self android_id: " + selfAndroidId);

        boolean isTestDevice = false;

        if (existDevicesAllowed(selfAndroidId)) {
            isTestDevice = true;
        }

        int allowed = 0;
        String[] cols = {DBConstants.COL_OCR_MONTHLY, DBConstants.COL_TEST_OCR_MONTHLY};
        Cursor cursor = null;
        try {
            cursor = database.query(DBConstants.TABLE_OCR_RULES, cols,
                    null
                    , null, null, null, DBConstants.COL_ID + " DESC");
            if (cursor.moveToFirst()) {
                if (isTestDevice) {
                    allowed = cursor.getInt(cursor.getColumnIndex
                            (DBConstants.COL_TEST_OCR_MONTHLY));
                } else {
                    allowed = cursor.getInt(cursor.getColumnIndex
                            (DBConstants.COL_OCR_MONTHLY));
                }
            }
        } finally {
            // this gets called even if there is an exception somewhere above
            if (cursor != null)
                cursor.close();
        }
        return allowed;
    }

    public int getOcrFreeAttempted() {
        int attempted = 0;
        String[] cols = {DBConstants.COL_OCR_FREE_ATTEMPTED};
        Cursor cursor = null;
        try {
            cursor = database.query(DBConstants.TABLE_OCR_STATUS, cols,
                    null
                    , null, null, null, DBConstants.COL_ID + " DESC");
            if (cursor.moveToFirst()) {
                attempted = cursor.getInt(cursor.getColumnIndex
                        (DBConstants.COL_OCR_FREE_ATTEMPTED));
            }
        } finally {
            // this gets called even if there is an exception somewhere above
            if (cursor != null)
                cursor.close();
        }
        return attempted;
    }

    public int getOcrMonthlyAttempted() {
        int attempted = 0;
        String[] cols = {DBConstants.COL_OCR_MONTHLY_ATTEMPTED};
        Cursor cursor = null;
        try {
            cursor = database.query(DBConstants.TABLE_OCR_STATUS, cols,
                    null
                    , null, null, null, DBConstants.COL_ID + " DESC");
            if (cursor.moveToFirst()) {
                attempted = cursor.getInt(cursor.getColumnIndex
                        (DBConstants.COL_OCR_MONTHLY_ATTEMPTED));
            }
        } finally {
            // this gets called even if there is an exception somewhere above
            if (cursor != null)
                cursor.close();
        }
        return attempted;
    }

    public int getOcrPremiumYearlyAttempted() {
        int attempted = 0;
        String[] cols = {DBConstants.COL_OCR_PREMIUM_YEARLY_ATTEMPTED};
        Cursor cursor = null;
        try {
            cursor = database.query(DBConstants.TABLE_OCR_STATUS, cols,
                    null
                    , null, null, null, DBConstants.COL_ID + " DESC");
            if (cursor.moveToFirst()) {
                attempted = cursor.getInt(cursor.getColumnIndex
                        (DBConstants.COL_OCR_PREMIUM_YEARLY_ATTEMPTED));
            }
        } finally {
            // this gets called even if there is an exception somewhere above
            if (cursor != null)
                cursor.close();
        }
        return attempted;
    }

    public boolean existOcrAttempted() {
        boolean exist = false;

        String[] cols = {DBConstants.COL_ID};
        Cursor cursor = null;

        cursor = database.query(DBConstants.TABLE_OCR_STATUS, cols,
                null
                , null, null, null, DBConstants.COL_ID + " DESC");
        if (cursor.moveToFirst()) {
            exist = true;
        }

        cursor.close();

        return exist;
    }

    public long insertOcrAttempt(int attemptCount, int whichPlan) {
        long rowIdInserted = 0;

        ContentValues cv = new ContentValues();

        String column = null;
        switch (whichPlan) {
            case Constants.PLAN_FREE:
                column = DBConstants.COL_OCR_FREE_ATTEMPTED;
                break;
            case Constants.PLAN_OCR_MONTHLY:
                column = DBConstants.COL_OCR_MONTHLY_ATTEMPTED;
                break;
            case Constants.PLAN_PEMIUM_YEARLY:
                column = DBConstants.COL_OCR_PREMIUM_YEARLY_ATTEMPTED;
                break;
        }

        if (column != null) {
            cv.put(column, attemptCount);
            cv.put(DBConstants.COL_UPDATED_ON, System.currentTimeMillis());

            rowIdInserted = database.insert(DBConstants.TABLE_OCR_STATUS, null, cv);
        }

        return rowIdInserted;
    }

    public int updateOcrAttempt(int attemptCount, int whichPlan) {
        int rowAffected = 0;

        ContentValues cv = new ContentValues();

        String column = null;
        switch (whichPlan) {
            case Constants.PLAN_FREE:
                column = DBConstants.COL_OCR_FREE_ATTEMPTED;
                break;
            case Constants.PLAN_OCR_MONTHLY:
                column = DBConstants.COL_OCR_MONTHLY_ATTEMPTED;
                break;
            case Constants.PLAN_PEMIUM_YEARLY:
                column = DBConstants.COL_OCR_PREMIUM_YEARLY_ATTEMPTED;
                break;
        }

        if (column != null) {
            cv.put(column, attemptCount);
            cv.put(DBConstants.COL_UPDATED_ON, System.currentTimeMillis());

            rowAffected = database.update(DBConstants.TABLE_OCR_STATUS, cv,
                    null, null);
        }

        return rowAffected;
    }

    public boolean existCreditsFromApi() {
        boolean exist = false;

        String[] cols = {DBConstants.COL_ID};
        Cursor cursor = null;

        cursor = database.query(DBConstants.TABLE_OCR_CREDITS_API, cols,
                null
                , null, null, null, DBConstants.COL_ID + " DESC");
        if (cursor.moveToFirst()) {
            exist = true;
        }

        cursor.close();

        return exist;
    }

    public boolean existCreditsFromApi(String deviceId) {
        boolean exist = false;

        String[] cols = {DBConstants.COL_ID};
        Cursor cursor = null;

        cursor = database.query(DBConstants.TABLE_OCR_CREDITS_API, cols,
                DBConstants.COL_DEVICE_ID + " = '" + deviceId + "'"
                , null, null, null, DBConstants.COL_ID + " DESC");
        if (cursor.moveToFirst()) {
            exist = true;
        }

        cursor.close();

        return exist;
    }

    public String getEmail(String deviceId) {
        String email = null;
        String[] cols = {DBConstants.COL_EMAIL};
        Cursor cursor = null;
        try {
            cursor = database.query(DBConstants.TABLE_OCR_CREDITS_API, cols,
                    DBConstants.COL_DEVICE_ID + " = '" + deviceId + "'"
                    , null, null, null, DBConstants.COL_ID + " DESC");
            if (cursor.moveToFirst()) {
                email = cursor.getString(cursor.getColumnIndex
                        (DBConstants.COL_EMAIL));
            }
        } finally {
            // this gets called even if there is an exception somewhere above
            if (cursor != null)
                cursor.close();
        }
        return email;
    }

    public long insertDeviceId(String deviceId) {
        long rowIdInserted = 0;

        ContentValues cv = new ContentValues();

        cv.put(DBConstants.COL_DEVICE_ID, deviceId);
        cv.put(DBConstants.COL_UPDATED_ON, System.currentTimeMillis());

        rowIdInserted = database.insert(DBConstants.TABLE_OCR_CREDITS_API, null, cv);
        return rowIdInserted;
    }

    public int updateDeviceId(String deviceId) {
        int rowAffected = 0;

        ContentValues cv = new ContentValues();

        cv.put(DBConstants.COL_DEVICE_ID, deviceId);
        cv.put(DBConstants.COL_UPDATED_ON, System.currentTimeMillis());

        rowAffected = database.update(DBConstants.TABLE_OCR_CREDITS_API, cv,
                null, null);

        return rowAffected;
    }

    public long insertSubscriptionId(String subscriptionId) {
        long rowIdInserted = 0;

        ContentValues cv = new ContentValues();

        cv.put(DBConstants.COL_SUBSCRIPTION_ID, subscriptionId);
        cv.put(DBConstants.COL_UPDATED_ON, System.currentTimeMillis());

        rowIdInserted = database.insert(DBConstants.TABLE_OCR_CREDITS_API, null, cv);
        return rowIdInserted;
    }

    public int updateSubscriptionId(String subscriptionId) {
        int rowAffected = 0;

        ContentValues cv = new ContentValues();

        cv.put(DBConstants.COL_SUBSCRIPTION_ID, subscriptionId);
        cv.put(DBConstants.COL_UPDATED_ON, System.currentTimeMillis());

        rowAffected = database.update(DBConstants.TABLE_OCR_CREDITS_API, cv,
                null, null);

        return rowAffected;
    }

    public long insertEmailSubscriptionId(String subscriptionId, String emailAddress) {
        long rowIdInserted = 0;

        ContentValues cv = new ContentValues();

        if (subscriptionId != null) {
            cv.put(DBConstants.COL_SUBSCRIPTION_ID, subscriptionId);
        }
        cv.put(DBConstants.COL_EMAIL, emailAddress);
        cv.put(DBConstants.COL_UPDATED_ON, System.currentTimeMillis());

        rowIdInserted = database.insert(DBConstants.TABLE_OCR_CREDITS_API, null, cv);
        return rowIdInserted;
    }

    public int updateEmailSubscriptionId(String subscriptionId, String emailAddress, String deviceId) {
        int rowAffected = 0;

        ContentValues cv = new ContentValues();

        if (subscriptionId != null) {
            cv.put(DBConstants.COL_SUBSCRIPTION_ID, subscriptionId);
        }
        cv.put(DBConstants.COL_EMAIL, emailAddress);
        cv.put(DBConstants.COL_UPDATED_ON, System.currentTimeMillis());

        rowAffected = database.update(DBConstants.TABLE_OCR_CREDITS_API, cv,
                DBConstants.COL_DEVICE_ID + " = '" + deviceId + "'", null);

        return rowAffected;
    }

    public long insertCreditsFromApi(int creditsFromApi, String deviceId) {
        long rowIdInserted = 0;

        ContentValues cv = new ContentValues();

        cv.put(DBConstants.COL_CREDITS_FROM_API, creditsFromApi);
        cv.put(DBConstants.COL_DEVICE_ID, deviceId);
        cv.put(DBConstants.COL_UPDATED_ON, System.currentTimeMillis());

        rowIdInserted = database.insert(DBConstants.TABLE_OCR_CREDITS_API, null, cv);
        return rowIdInserted;
    }

    public int updateCreditsFromApi(int creditsFromApi, String deviceId) {
        int rowAffected = 0;

        ContentValues cv = new ContentValues();

        cv.put(DBConstants.COL_CREDITS_FROM_API, creditsFromApi);
        cv.put(DBConstants.COL_UPDATED_ON, System.currentTimeMillis());

        rowAffected = database.update(DBConstants.TABLE_OCR_CREDITS_API, cv,
                DBConstants.COL_DEVICE_ID + " = '" + deviceId + "'", null);

        return rowAffected;
    }

    public long insertCreditsFromApi(int creditsFromApi, String deviceId, String subscriptionId,
                                     int isOcr, int planId, int isPremium, String email) {
        long rowIdInserted = 0;

        ContentValues cv = new ContentValues();

        if (email != null && !email.isEmpty()) {
            cv.put(DBConstants.COL_EMAIL, email);
        }

        cv.put(DBConstants.COL_CREDITS_FROM_API, creditsFromApi);
        cv.put(DBConstants.COL_DEVICE_ID, deviceId);

        if (subscriptionId != null && !subscriptionId.isEmpty()) {
            cv.put(DBConstants.COL_SUBSCRIPTION_ID, subscriptionId);
        }

        cv.put(DBConstants.COL_IS_OCR, isOcr);
        cv.put(DBConstants.COL_PLAN_ID, planId);
        cv.put(DBConstants.COL_IS_PREMIUM, isPremium);
        cv.put(DBConstants.COL_UPDATED_ON, System.currentTimeMillis());

        rowIdInserted = database.insert(DBConstants.TABLE_OCR_CREDITS_API, null, cv);
        return rowIdInserted;
    }

    public int updateCreditsFromApi(int creditsFromApi, String deviceId, String subscriptionId,
                                    int isOcr, int planId, int isPremium, String email) {
        int rowAffected = 0;

        ContentValues cv = new ContentValues();

        if (email != null && !email.isEmpty()) {
            cv.put(DBConstants.COL_EMAIL, email);
        }

        cv.put(DBConstants.COL_CREDITS_FROM_API, creditsFromApi);

        if (subscriptionId != null && !subscriptionId.isEmpty()) {
            cv.put(DBConstants.COL_SUBSCRIPTION_ID, subscriptionId);
        }

        cv.put(DBConstants.COL_IS_OCR, isOcr);
        cv.put(DBConstants.COL_PLAN_ID, planId);
        cv.put(DBConstants.COL_IS_PREMIUM, isPremium);
        cv.put(DBConstants.COL_UPDATED_ON, System.currentTimeMillis());

        rowAffected = database.update(DBConstants.TABLE_OCR_CREDITS_API, cv,
                DBConstants.COL_DEVICE_ID + " = '" + deviceId + "'", null);

        return rowAffected;
    }

    public long insertCreditsFromApi(int creditsFromApi, String deviceId, String subscriptionId,
                                     int isOcr, int planId, int isPremium) {
        long rowIdInserted = 0;

        ContentValues cv = new ContentValues();

        cv.put(DBConstants.COL_CREDITS_FROM_API, creditsFromApi);
        cv.put(DBConstants.COL_DEVICE_ID, deviceId);

        if (subscriptionId != null && !subscriptionId.isEmpty()) {
            cv.put(DBConstants.COL_SUBSCRIPTION_ID, subscriptionId);
        }

        cv.put(DBConstants.COL_IS_OCR, isOcr);
        cv.put(DBConstants.COL_PLAN_ID, planId);
        cv.put(DBConstants.COL_IS_PREMIUM, isPremium);
        cv.put(DBConstants.COL_UPDATED_ON, System.currentTimeMillis());

        rowIdInserted = database.insert(DBConstants.TABLE_OCR_CREDITS_API, null, cv);
        return rowIdInserted;
    }

    public int updateCreditsFromApi(int creditsFromApi, String deviceId, String subscriptionId,
                                    int isOcr, int planId, int isPremium) {
        int rowAffected = 0;

        ContentValues cv = new ContentValues();

        cv.put(DBConstants.COL_CREDITS_FROM_API, creditsFromApi);

        if (subscriptionId != null && !subscriptionId.isEmpty()) {
            cv.put(DBConstants.COL_SUBSCRIPTION_ID, subscriptionId);
        }

        cv.put(DBConstants.COL_IS_OCR, isOcr);
        cv.put(DBConstants.COL_PLAN_ID, planId);
        cv.put(DBConstants.COL_IS_PREMIUM, isPremium);
        cv.put(DBConstants.COL_UPDATED_ON, System.currentTimeMillis());

        rowAffected = database.update(DBConstants.TABLE_OCR_CREDITS_API, cv,
                DBConstants.COL_DEVICE_ID + " = '" + deviceId + "'", null);

        return rowAffected;
    }

    public ArrayList<Cursor> getData(String Query) {
        //get writable database
        SQLiteDatabase sqlDB = database;
        String[] columns = new String[]{"mesage"};
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2 = new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);


        try {
            String maxQuery = Query;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);


            //add value to cursor2
            Cursor2.addRow(new Object[]{"Success"});

            alc.set(1, Cursor2);
            if (null != c && c.getCount() > 0) {


                alc.set(0, c);
                c.moveToFirst();

                return alc;
            }
            return alc;
        } catch (SQLException sqlEx) {
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[]{"" + sqlEx.getMessage()});
            alc.set(1, Cursor2);
            return alc;
        } catch (Exception ex) {

            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[]{"" + ex.getMessage()});
            alc.set(1, Cursor2);
            return alc;
        }


    }
}
