package com.jukuproject.jukutweet;

        import android.content.ContentValues;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteException;
        import android.database.sqlite.SQLiteOpenHelper;
        import android.util.Log;


        import com.jukuproject.jukutweet.Models.UserInfo;

        import java.util.ArrayList;
        import java.util.List;

/**
 * Database helper
 */
public class InternalDB extends SQLiteOpenHelper {

    private static boolean debug = true;
    private static String TAG = "TEST-Internal";
    private static InternalDB sInstance;

    public static  String DB_NAME =  "JQuiz";
    public static String DATABASE_NAME = DB_NAME + ".db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_MAIN = "Users";
    public static final String COL_ID = "_id";
    public static final String TMAIN_COL0 = "ScreenName";
    public static final String TMAIN_COL1 = "Description";
    public static final String TMAIN_COL2 = "FollowerCount";
    public static final String TMAIN_COL3 = "FriendCount";
    public static final String TMAIN_COL4 = "ProfileImgUrl";

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
                                "%s TEXT, " +
                                "%s TEXT, " +
                                "%s INTEGER, " +
                                "%s INTEGER, " +
                                "%s TEXT) ", TABLE_MAIN,
                        COL_ID,
                        TMAIN_COL0,
                        TMAIN_COL1,
                        TMAIN_COL2,
                        TMAIN_COL3,
                        TMAIN_COL4);

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
        String queryRecordExists = "Select _id From " + TABLE_MAIN + " where " + TMAIN_COL0 + " = ?" ;
        Cursor c = db.rawQuery(queryRecordExists, new String[]{user});
        try {
            if (c.moveToFirst()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLiteException e) {
            Log.e(TAG,"Sqlite exception: " + e);
        } finally {
            c.close();
            db.close();
        }
      return false;
    }

    //TODO figure out what the hell the saveUser int returns...
    /**
     * Saves new user feed to DB
     * @param userInfo UserInfo object with user data pulled from twitter api
     * @return bool True if save worked, false if it failed
     */
    public boolean saveUser(UserInfo userInfo) {

        try {


            if(userInfo.getScreenName() != null) {
                SQLiteDatabase db = this.getWritableDatabase();
                ContentValues values = new ContentValues();
                if(debug) {
                    Log.d(TAG,"saving user: " + userInfo.getScreenName() );
                }
                values.put(TMAIN_COL0, userInfo.getScreenName().trim());

                if(userInfo.getDescription() != null) {
                    values.put(TMAIN_COL1, userInfo.getDescription().trim());
                }
                if(userInfo.getFollowerCount() != null ) {
                    values.put(TMAIN_COL2, userInfo.getFollowerCount());
                }
                if(userInfo.getFriendCount() != null){
                    values.put(TMAIN_COL3, userInfo.getFriendCount());
                }
                if(userInfo.getProfile_image_url() != null) {
                    values.put(TMAIN_COL4, userInfo.getProfile_image_url().trim());
                }

                db.insert(TABLE_MAIN, null, values);
                db.close();
                return true;
            } else {
                return false;
            }
        } catch(SQLiteException exception) {
        return false;
        }

    }


    /**
     * Remove (i.e. "unfollow") a user from the database
     * @param user
     * @return bool True if operation is succesful, false if an error occurs
     */
    public boolean deleteUser(String user) {
        try{
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_MAIN, TMAIN_COL0 + "= ?", new String[]{user});
            db.close();
            return true;
        } catch(SQLiteException exception) {
            return false;
        }

    }


    /**
     * Pulls user information from db and fills a list of UserInfo objects, to
     * be used in the UserListFragment recycler
     * @return list of UserInfo objects, one for each followed user saved in the db
     */
    public List<UserInfo> getSavedUserInfo() {
        List<UserInfo> userInfoList = new ArrayList<UserInfo>();

        String querySelectAll = "Select distinct ScreenName,IFNULL(Description,''),FollowerCount, FriendCount, IFNULL(ProfileImgUrl,'') From " + TABLE_MAIN;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(querySelectAll, null);


        try {
            if (c.moveToFirst()) {
                do {
                    UserInfo userInfo = new UserInfo(c.getString(0));
                    userInfo.setDescription(c.getString(1));

                    try {
                        userInfo.setFollowerCount(c.getInt(2));
                    } catch (SQLiteException e) {

                    } catch (Exception e) {

                    }
                    try {
                        userInfo.setFriendCount(c.getInt(3));
                    } catch (SQLiteException e) {

                    } catch (Exception e) {

                    }

                    userInfo.setProfile_image_url(c.getString(4));

//                      if(debug) {
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
                    userInfoList.add(userInfo);

                } while (c.moveToNext());
            }

            c.close();
        } finally {
            db.close();
        }

//        Log.d(TAG,"users count: " + followedUsers.size());
        //Now look for and attach images to the RSS LIST
        return userInfoList;
    }

//TODO the screenname is not unique! for any of these. it can change... use twitter ID where possible!
    public boolean compareUserInfoAndUpdate(UserInfo oldUserInfo, UserInfo recentUserInfo) {

        try {
            ContentValues values = new ContentValues();
            if(!oldUserInfo.getDescription().equals(recentUserInfo.getDescription())) {
                values.put(InternalDB.TMAIN_COL1, recentUserInfo.getDescription().trim());
            }
            if(oldUserInfo.getFollowerCount() != recentUserInfo.getFollowerCount()) {
                values.put(InternalDB.TMAIN_COL2, recentUserInfo.getFollowerCount());
            }
            if(oldUserInfo.getFriendCount() != recentUserInfo.getFriendCount()) {
                values.put(InternalDB.TMAIN_COL3, recentUserInfo.getFriendCount());
            }
            if(!oldUserInfo.getProfile_image_url().equals(recentUserInfo.getProfile_image_url())) {
                values.put(InternalDB.TMAIN_COL4, recentUserInfo.getProfile_image_url().trim());
            }

            if(values.size()>0) {
                SQLiteDatabase db = this.getReadableDatabase();
                db.update(TABLE_MAIN, values, TMAIN_COL0 + "= ?", new String[]{oldUserInfo.getScreenName()});
            }

            return true;
        } catch (SQLiteException e) {
            Log.e(TAG,"compareUserInfo prob : " + e);
        }
        return false;
    }
}