package com.jukuproject.jukutweet.Database;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Sql helper for importing and updating the external sqlite database "JQuiz". The db contains
 * the Edit dictionary with info for Japanese words.
 */
public class ExternalDB extends SQLiteOpenHelper {

    //For copying the DB in the assets file to internal memory
    static final boolean debug = false;
    private static String DB_NAME = "JQuiz";
    public static String DATABASE_NAME = DB_NAME + ".db";
    public static String INTERNAL_DATABASE_NAME = DATABASE_NAME;
    private static final int DATABASE_VERSION = 2;
    private static final String SP_KEY_DB_VER = "db_ver";
    private final Context mContext;
    private static String TAG = "ExternalDB";

    private static int BUFFER_SIZE = 2*1024;
    public ExternalDB(Context context) {
        super(context,  DATABASE_NAME , null, DATABASE_VERSION);
        mContext  = context;
        if(debug){
            Log.d(TAG,"copyZip completed. Moving to initialize()...");}
        initialize_jquizinternal_copy(); //Run to copy JQuiz_Internal Xref (etc) db if it doesn't exist. Should only happen once an install
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion) {
    }


    /** You should move it over, then unzip it, then delete that zipped shit, then use the DB **/

//Copy new version of external DB to internal if new version exists
    private void initialize_jquizinternal_copy() {
        if (jquizinternal_exists()) {
            if(debug){Log.d(TAG, "JQuiz_Internal Exists");}
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(mContext);
            int dbVersion = prefs.getInt(SP_KEY_DB_VER, 1);
            if (DATABASE_VERSION != dbVersion) {
                File dbFile = mContext.getDatabasePath(INTERNAL_DATABASE_NAME);
                if (!dbFile.delete()) {
                    if(debug){Log.d(TAG, "JQuiz_Internal--Unable to update database");}
                }
            }
        }
        if (!jquizinternal_exists()) {
            if(debug){Log.d(TAG, "Creating JQuiz_Internal DB");}
            createJQuizInternalDatabase();
        }
    }


    /**
     * Returns true if database file exists, false otherwise.
     * @return
     */
    private boolean jquizinternal_exists() {
        File dbFile = mContext.getDatabasePath(INTERNAL_DATABASE_NAME);
        return dbFile.exists();
    }

    /**
     * Creates database by copying it from assets directory.
     */
    private void createJQuizInternalDatabase() {
        String parentPath = mContext.getDatabasePath(INTERNAL_DATABASE_NAME).getParent();
        String path = mContext.getDatabasePath(INTERNAL_DATABASE_NAME).getPath();
        File file = new File(parentPath);
        if (!file.exists()) {
            if (!file.mkdir()) {
                if(debug){Log.d("ERRORLOG_ExternalDB", "Unable to create database directory");}
                return;
            }
        }

        InputStream is = null;
        OutputStream os = null;
        try {
            is = mContext.getAssets().open(INTERNAL_DATABASE_NAME);
            os = new FileOutputStream(path);

            byte[] buffer = new byte[4*BUFFER_SIZE];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(SP_KEY_DB_VER, DATABASE_VERSION);
            editor.commit();
        } catch (IOException e) {
            e.printStackTrace();
            if(debug){Log.d("ERRORLOG_ExternalDB", "something didn't work");}
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }




}



