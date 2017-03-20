package com.jukuproject.jukutweet;

        import android.content.ContentUris;
        import android.content.ContentValues;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.DatabaseErrorHandler;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteException;
        import android.database.sqlite.SQLiteOpenHelper;
        import android.graphics.Bitmap;
        import android.net.Uri;
        import android.provider.MediaStore;
        import android.support.annotation.NonNull;
        import android.support.annotation.Nullable;
        import android.util.Log;


        import com.jukuproject.jukutweet.Models.User;

        import java.util.ArrayList;
        import java.util.List;


public class InternalDB extends SQLiteOpenHelper {

    private static boolean debug = true;
    private static String TAG = "TEST -- Internal";
    private static InternalDB sInstance;

    public static  String DB_NAME =  "JQuiz";
    public static String DATABASE_NAME = DB_NAME + ".db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_MAIN = "Users";
    public static final String COL_ID = "_id";

    public static final String COL0 = "Name";


    public static synchronized InternalDB getInstance(Context context) {

        if (sInstance == null) {
            sInstance = new InternalDB(context.getApplicationContext());
        }
        return sInstance;
    }


    public InternalDB(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);

    }


    @Override
    public void onCreate(SQLiteDatabase sqlDB) {
        String sqlQueryMain =
                String.format("CREATE TABLE IF NOT EXISTS %s (" +
                                "%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "%s TEXT) ", TABLE_MAIN,
                        COL_ID,
                        COL0);

        sqlDB.execSQL(sqlQueryMain);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqlDB, int i, int i2) {
        onCreate(sqlDB);
    }


    /**
     * Checks to see if user is already saved in table
     * @param user user's twitter handle
     * @return boolean True if user already exists, false if user does not exist
     */
    public boolean duplicateUser(String user) {

        /** Before inserting record, check to see if feed already exists */
        SQLiteDatabase db = this.getWritableDatabase();
        String queryRecordExists = "Select _id From " + TABLE_MAIN + " where " + COL0 + " = ?" ;
        Cursor c = db.rawQuery(queryRecordExists, new String[]{user});
        if (c.moveToFirst()) {
            return true;
        }
        c.close();
        return false;
    }

    //TODO figure out what the hell the saveUser int returns...
    /**
     * Saves new user feed to DB
     * @param user user's twitter handle
     * @return
     */
    public int saveUser(@NonNull String user) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if(debug) {
            Log.d(TAG,"saving initial url: " + user );
        }

        values.put(COL0, user.trim());
        long x=db.insert(TABLE_MAIN, null, values);
        db.close();
        if(debug) {
            Log.d(TAG,"save inital user, return value:" + x);
        }
        return (int)x;
    }


    public boolean deleteUser(String user) {
        try{
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_MAIN, COL0 + "= ?", new String[]{user});
            db.close();
            Log.d("TEST", "RETURNING TRUE");
            return true;
        } catch(SQLiteException exception) {
            Log.d("TEST", "RETURNING FALSE");
            return false;
        }

    }


    /** This pulls the RSSList dataset for the mainfragment recycler **/
    /** IT is also chained to the attachImagestoRSSLists which matches downloaded thumbnails to the RSSList rows **/
    public List<User> getFollowedUsers(Context context) {
        List<User> followedUsers = new ArrayList<User>();

        String querySelectAll = "Select distinct Name From " + TABLE_MAIN;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(querySelectAll, null);


        try {
            if (c.moveToFirst()) {
                do {
                    User user = new User(c.getString(0));
//                    if(debug) {
//                        Log.d(TAG, "putting id: " + c.getInt(0));
//                        Log.d(TAG, "putting url: " + c.getString(1));
//                        Log.d(TAG, "putting title: " + c.getString(2));
//                        Log.d(TAG, "putting imageURL: " + c.getString(3));
//                        Log.d(TAG, "putting imageURI: " + c.getString(4));
//                    }
//                    itemData.setId(c.getInt(0));
//                    itemData.setURL(c.getString(1));
//                    itemData.setTitle(c.getString(2));
//                    itemData.setImageURL(c.getString(3));
//                    itemData.setImageURI(c.getString(4));
                    followedUsers.add(user);

                } while (c.moveToNext());
            }

            c.close();
        } finally {
            db.close();
        }

//        Log.d(TAG,"users count: " + followedUsers.size());
        //Now look for and attach images to the RSS LIST
        return followedUsers;
    }
}