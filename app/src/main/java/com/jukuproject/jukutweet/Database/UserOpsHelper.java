package com.jukuproject.jukutweet.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Interfaces.UserOperationsInterface;
import com.jukuproject.jukutweet.Models.UserInfo;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by JClassic on 4/5/2017.
 */

public class UserOpsHelper implements UserOperationsInterface {
    private SQLiteOpenHelper sqlOpener;
    private static String TAG = "TEST-userops";

    public UserOpsHelper(SQLiteOpenHelper sqlOpener) {
        this.sqlOpener = sqlOpener;
    }

    /**
     * Checks to see if user is already saved in table
     * @param user user's twitter handle
     * @return boolean True if user already exists, false if user does not exist
     */
    public boolean duplicateUser(String user) {

        /** Before inserting record, check to see if feed already exists */
        SQLiteDatabase db = sqlOpener.getWritableDatabase();
        String queryRecordExists = "Select _id From " + InternalDB.Tables.TABLE_USERS + " where " + InternalDB.Columns.TMAIN_COL0 + " = ?" ;
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

    /**
     * Saves new user feed to DB
     * @param userInfo UserInfo object with user data pulled from twitter api
     * @return bool True if save worked, false if it failed
     */
    public boolean saveUser(UserInfo userInfo) {
        SQLiteDatabase db = sqlOpener.getWritableDatabase();
        try {
            if(userInfo.getScreenName() != null) {

                ContentValues values = new ContentValues();
                if(BuildConfig.DEBUG) {
                    Log.d(TAG,"saving user: " + userInfo.getScreenName() );
                }
                values.put(InternalDB.Columns.TMAIN_COL0, userInfo.getScreenName().trim());

                if(userInfo.getDescription() != null) {
                    values.put(InternalDB.Columns.TMAIN_COL1, userInfo.getUserId());
                }
                if(userInfo.getDescription() != null) {
                    values.put(InternalDB.Columns.TMAIN_COL2, userInfo.getDescription().trim());
                }
                if(userInfo.getFollowerCount() != null ) {
                    values.put(InternalDB.Columns.TMAIN_COL3, userInfo.getFollowerCount());
                }
                if(userInfo.getFriendCount() != null){
                    values.put(InternalDB.Columns.TMAIN_COL4, userInfo.getFriendCount());
                }
                if(userInfo.getProfileImageUrl() != null) {
                    values.put(InternalDB.Columns.TMAIN_COL5, userInfo.getProfileImageUrl().trim());
                }

                if(userInfo.getProfileImageUrl() != null) {
                    values.put(InternalDB.Columns.TMAIN_COL7, userInfo.getName().trim());
                }

                db.insert(InternalDB.Tables.TABLE_USERS, null, values);

                return true;
            } else {
                return false;
            }
        } catch(SQLiteException exception) {
            return false;
        } finally {
            db.close();
        }

    }




//TODO redo javadoc
    /**
     * Removes a user from the database
//     * @param user user screen_name to remove
     * @return bool True if operation is succesful, false if an error occurs
     */
    public boolean deleteUser(String userId) {
        SQLiteDatabase db = sqlOpener.getWritableDatabase();
        try{

//
//
//            String queryRecordExists = "Select _id From " + InternalDB.Tables.TABLE_USERS + " where " + InternalDB.Columns.TMAIN_COL0 + " = ?" ;
//            Cursor c = db.rawQuery(queryRecordExists, new String[]{userId});

//                if (c.moveToFirst()) {
                    db.delete(InternalDB.Tables.TABLE_USERS, InternalDB.Columns.TMAIN_COL0 + "= ? OR " + InternalDB.Columns.TMAIN_COL1 + "= ? ", new String[]{userId,userId});
//                } else {
//                    db.delete(InternalDB.Tables.TABLE_USERS, InternalDB.Columns.TMAIN_COL1 + "= ?", new String[]{userId});
//                }

//            c.close();
            return true;
        } catch(SQLiteException exception) {
            return false;
        } finally {
            db.close();
        }

    }



    //TODO == hook this to user id? instead of screenname
    public void addMediaURItoDB(String URI, String screenName) {
        SQLiteDatabase db = sqlOpener.getWritableDatabase();
        try {

        ContentValues values = new ContentValues();
        values.put(InternalDB.Columns.TMAIN_COL6, URI);
        db.update(InternalDB.Tables.TABLE_USERS, values, InternalDB.Columns.TMAIN_COL0 + "= ?", new String[] {screenName});
        Log.d(TAG,"SUCESSFUL INSERT URI for name: " + screenName);
        } catch (SQLiteException e) {
            Log.e(TAG,"addMediaURItoDB sqlite problem: " + e);
        } catch (Exception e) {
            Log.e(TAG,"addMediaURItoDB exception... " + e);
        } finally {
            db.close();
        }
    }


    public void updateUserInfo(UserInfo userInfo) {
        SQLiteDatabase db = sqlOpener.getWritableDatabase();
        try {

            ContentValues values = new ContentValues();

            if(userInfo.getUserId()!=null) {
                values.put(InternalDB.Columns.TMAIN_COL1, userInfo.getUserId());
            }
            if(userInfo.getFollowerCount()!=null) {
                values.put(InternalDB.Columns.TMAIN_COL3, userInfo.getFollowerCount());
            }
            if(userInfo.getFriendCount()!=null) {
                values.put(InternalDB.Columns.TMAIN_COL4, userInfo.getFriendCount());
            }
            if(userInfo.getDescription()!=null) {
                values.put(InternalDB.Columns.TMAIN_COL2, userInfo.getDescription());
            }
            if(userInfo.getName()!=null) {
                values.put(InternalDB.Columns.TMAIN_COL7, userInfo.getName());
            }
            if(userInfo.getProfileImageUrl() != null) {
                values.put(InternalDB.Columns.TMAIN_COL5, userInfo.getProfileImageUrl().trim());
            }

            db.update(InternalDB.Tables.TABLE_USERS, values, InternalDB.Columns.TMAIN_COL0 + "= ?", new String[] {userInfo.getScreenName()});
            Log.d(TAG,"SUCESSFUL UPDATE OF USER INFO FOR: " + userInfo.getScreenName());
        } catch (SQLiteException e) {
            Log.e(TAG,"updateUserInfo sqlite problem: " + e);
        } catch (Exception e) {
            Log.e(TAG,"updateUserInfo exception... " + e);
        } finally {
            db.close();
        }
    }

    /**
     * Pulls user information from db and fills a list of UserInfo objects, to
     * be used in the UserListFragment recycler
     * @return list of UserInfo objects, one for each followed user saved in the db
     */
    public List<UserInfo> getSavedUserInfo() {
        List<UserInfo> userInfoList = new ArrayList<UserInfo>();

        String querySelectAll = "Select distinct ScreenName" +
                ",IFNULL(Description,'')" +
                ",IFNULL(FollowerCount,-1)" +
                ", IFNULL(FriendCount,-1)" +
                ", IFNULL(ProfileImgUrl,'')" +
                ",UserId" +
                ", ProfileImgFilePath" +
                ", UserName " +
                "From " + InternalDB.Tables.TABLE_USERS;
        SQLiteDatabase db = sqlOpener.getReadableDatabase();
        Cursor c = db.rawQuery(querySelectAll, null);


        try {
            if (c.moveToFirst()) {
                do {
                    UserInfo userInfo = new UserInfo(c.getString(0));
                    userInfo.setDescription(c.getString(1));

//                    try {
//                        userInfo.setUserId(c.getString(5));
//                    } catch (SQLiteException e) {
//                        Log.e(TAG,"getSavedUserInfo adding user ID sqlite problem: " + e);
//                    } catch (Exception e) {
//                        Log.e(TAG,"getSavedUserInfo adding user ID other exception... " + e);
//                    }

                    try {
                        if(c.getString(5)!=null) {
                            userInfo.setUserId(c.getString(5));
                        }
                        if(c.getInt(2)>=0) {
                            userInfo.setFollowerCount(c.getInt(2));
                        }
                        if(c.getInt(3)>=0) {
                            userInfo.setFriendCount(c.getInt(3));
                        }
                        userInfo.setName(c.getString(7));
                        userInfo.setProfile_image_url(c.getString(4));
                    } catch (SQLiteException e) {
                        Log.e(TAG,"getSavedUserInfo adding extra user info sqlite problem: " + e);
                    } catch (Exception e) {
                        Log.e(TAG,"getSavedUserInfo adding extra user info exception... " + e);
                    }
//                    try {
//
//                    }  catch (SQLiteException e) {
//                        Log.e(TAG,"getSavedUserInfo adding setFriendCount sqlite problem: " + e);
//                    } catch (Exception e) {
//                        Log.e(TAG,"getSavedUserInfo adding setFriendCount other exception... " + e);
//                    }

                    try {
                        userInfo.setProfileImageFilePath(c.getString(6));
                    }  catch (SQLiteException e) {
                        Log.e(TAG,"getSavedUserInfo adding setProfileImgFilePath sqlite problem: " + e);
                    } catch (Exception e) {
                        Log.e(TAG,"getSavedUserInfo adding setProfileImgFilePath other exception... " + e);
                    }

//                    try {
//
//                    }  catch (SQLiteException e) {
//                        Log.e(TAG,"getSavedUserInfo adding setProfileImgFilePath sqlite problem: " + e);
//                    } catch (Exception e) {
//                        Log.e(TAG,"getSavedUserInfo adding setProfileImgFilePath other exception... " + e);
//                    }


                    userInfoList.add(userInfo);
                } while (c.moveToNext());
            }

            c.close();
        } finally {
            db.close();
        }

        return userInfoList;
    }

    //TODO instead use some kind of custom comparator? OR no?
    /**
     * Compares two UserInfo objects, and overwrites the old object in the database if values
     * have changed. Used when user timeline info is pulled, to check the user metadata attached to the tweets
     * against the saved data in the db
     * @param oldUserInfo old UserInfo object (saved info in db)
     * @param recentUserInfo new UserInfo object (pulled from tweet metadata)
     * @return
     *
     * @see com.jukuproject.jukutweet.Fragments.UserTimeLineFragment#pullTimeLineData(UserInfo)
     */
    public boolean compareUserInfoAndUpdate(UserInfo oldUserInfo, UserInfo recentUserInfo) {

        try {
            ContentValues values = new ContentValues();

            /*The user's screenName field can change, but the user id won't. So if possible user the userId as the
              key for making updates */
            if(oldUserInfo.getUserId() != null && !oldUserInfo.getScreenName().equals(recentUserInfo.getScreenName())) {
                values.put(InternalDB.Columns.TMAIN_COL0, recentUserInfo.getScreenName().trim());
            }
            if(recentUserInfo.getDescription() != null && !oldUserInfo.getDescription().equals(recentUserInfo.getDescription())) {
                values.put(InternalDB.Columns.TMAIN_COL2, recentUserInfo.getDescription().trim());
            }
            if(recentUserInfo.getFollowerCount() != null && oldUserInfo.getFollowerCount() != recentUserInfo.getFollowerCount()) {
                values.put(InternalDB.Columns.TMAIN_COL3, recentUserInfo.getFollowerCount());
            }
            if(recentUserInfo.getFriendCount() != null && oldUserInfo.getFriendCount() != recentUserInfo.getFriendCount()) {
                values.put(InternalDB.Columns.TMAIN_COL4, recentUserInfo.getFriendCount());
            }
            if(recentUserInfo.getProfileImageUrl() != null && !oldUserInfo.getProfileImageUrl().equals(recentUserInfo.getProfileImageUrl())) {
                values.put(InternalDB.Columns.TMAIN_COL5, recentUserInfo.getProfileImageUrl().trim());
            }

            if(values.size()>0) {
                if(oldUserInfo.getUserId() != null) {
                    SQLiteDatabase db = sqlOpener.getReadableDatabase();
                    db.update(InternalDB.Tables.TABLE_USERS, values, InternalDB.Columns.TMAIN_COL1 + "= ?", new String[]{String.valueOf(oldUserInfo.getUserId())});
                } else {
                    SQLiteDatabase db = sqlOpener.getReadableDatabase();
                    db.update(InternalDB.Tables.TABLE_USERS, values, InternalDB.Columns.TMAIN_COL0 + "= ?", new String[]{oldUserInfo.getScreenName()});
                }
            }

            return true;
        } catch (SQLiteException e) {
            Log.e(TAG,"compareUserInfo prob : " + e);
        }
        return false;
    }


    public boolean saveUserWithoutData(String screenName) {
        SQLiteDatabase db = sqlOpener.getWritableDatabase();
        try {
                ContentValues values = new ContentValues();
                if(BuildConfig.DEBUG) {
                    Log.d(TAG,"saving OFFLINE screenname placeholder: " + screenName );
                }
                values.put(InternalDB.Columns.TMAIN_COL0, screenName.trim());
                db.insert(InternalDB.Tables.TABLE_USERS, null, values);
                return true;

        } catch(SQLiteException exception) {
            return false;
        } finally {
            db.close();
        }
    };

//
//    public ArrayList<String> getListOfUserIds() {
//        ArrayList<String> userIds = new ArrayList<>();
//        SQLiteDatabase db = sqlOpener.getWritableDatabase();
//            String queryRecordExists = "Select DISTINCT UserId From " + InternalDB.Tables.TABLE_SAVED_TWEETS + " where UserId IS NOT NULL " ;
//            Cursor c = db.rawQuery(queryRecordExists, null);
//            try {
//                if (c.moveToFirst()) {
//                    while(!c.isAfterLast()) {
//                        userIds.add(c.getString(0));
//                    }
//                }
//            } catch (SQLiteException e) {
//                Log.e(TAG,"getListOfUserIds Sqlite exception: " + e);
//            } finally {
//                c.close();
//                db.close();
//            }
//
//            return userIds;
//    }

}