package com.jukuproject.jukutweet.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Interfaces.UserOperationsInterface;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.UserInfo;


/**
 * Collection of internal sqlite database calls related to User operations (adding users, removing, etc).
 *
 * @see com.jukuproject.jukutweet.MainActivity
 * @see com.jukuproject.jukutweet.Interfaces.DialogRemoveUserInteractionListener
 * @see com.jukuproject.jukutweet.Dialogs.AddUserCheckDialog
 * @see com.jukuproject.jukutweet.Dialogs.RemoveUserDialog
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


    /**
     * Removes a user from the database
     * @param userId user id string to remove
     * @return bool True if operation is succesful, false if an error occurs
     */
    public boolean deleteUser(String userId) {
        SQLiteDatabase db = sqlOpener.getWritableDatabase();
        try{
            db.delete(InternalDB.Tables.TABLE_USERS, InternalDB.Columns.TMAIN_COL0 + "= ? OR " + InternalDB.Columns.TMAIN_COL1 + "= ? ", new String[]{userId,userId});
            return true;
        } catch(SQLiteException exception) {
            return false;
        } finally {
            db.close();
        }
    }

    /**
     * Inserts the URI link to the location in the phone of a twitter user's saved profile image
     * @param URI URI of saved image
     * @param screenName screen name of user
     */
    public void addMediaURItoDB(String URI, String screenName) {
        SQLiteDatabase db = sqlOpener.getWritableDatabase();
        try {
        ContentValues values = new ContentValues();
        values.put(InternalDB.Columns.TMAIN_COL6, URI);
        db.update(InternalDB.Tables.TABLE_USERS, values, InternalDB.Columns.TMAIN_COL0 + "= ?", new String[] {screenName});
        if(BuildConfig.DEBUG){Log.d(TAG,"SUCESSFUL INSERT URI for name: " + screenName);}
        } catch (SQLiteException e) {
            Log.e(TAG,"addMediaURItoDB sqlite problem: " + e);
        } catch (Exception e) {
            Log.e(TAG,"addMediaURItoDB exception... " + e);
        } finally {
            db.close();
        }
    }

    /**
     * Updates Twitter User information in the database when a newer version of that user's info is
     * available (for example, when a user's timeline is clicked, the userInfo is downloaded as part of the package
     * and can this method will be called)
     * @param userInfo fresh UserInfo
     */
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
            if(BuildConfig.DEBUG){Log.d(TAG,"SUCESSFUL UPDATE OF USER INFO FOR: " + userInfo.getScreenName());}
        } catch (SQLiteException e) {
            Log.e(TAG,"updateUserInfo sqlite problem: " + e);
        } catch (Exception e) {
            Log.e(TAG,"updateUserInfo exception... " + e);
        } finally {
            db.close();
        }
    }

//    /**
//     * Pulls user information from db and fills a list of UserInfo objects, to
//     * be used in the {@link com.jukuproject.jukutweet.Fragments.UserListFragment} recycler
//     * @return list of UserInfo objects, one for each followed user saved in the db
//     */
//    public List<MenuHeader> getSavedUserInfo() {
//        List<MenuHeader> userInfoList = new ArrayList<MenuHeader>();
//
//        String querySelectAll = "Select distinct ScreenName" +
//                ",IFNULL(Description,'')" +
//                ",IFNULL(FollowerCount,-1)" +
//                ", IFNULL(FriendCount,-1)" +
//                ", IFNULL(ProfileImgUrl,'')" +
//                ",UserId" +
//                ", ProfileImgFilePath" +
//                ", UserName " +
//                "From " + InternalDB.Tables.TABLE_USERS;
//        SQLiteDatabase db = sqlOpener.getReadableDatabase();
//        Cursor c = db.rawQuery(querySelectAll, null);
//
//
//        try {
//            if (c.moveToFirst()) {
//                do {
//
//                    UserInfo userInfo = new UserInfo(c.getString(0));
//                    userInfo.setDescription(c.getString(1));
//
//                    try {
//                        if(c.getString(5)!=null) {
//                            userInfo.setUserId(c.getString(5));
//                        }
//                        if(c.getInt(2)>=0) {
//                            userInfo.setFollowerCount(c.getInt(2));
//                        }
//                        if(c.getInt(3)>=0) {
//                            userInfo.setFriendCount(c.getInt(3));
//                        }
//                        userInfo.setName(c.getString(7));
//                        userInfo.setProfile_image_url(c.getString(4));
//                    } catch (SQLiteException e) {
//                        Log.e(TAG,"getSavedUserInfo adding extra user info sqlite problem: " + e);
//                    } catch (NullPointerException e) {
//                        Log.e(TAG,"getSavedUserInfo adding extra user info NullPointerException... " + e);
//                    }
//
//                    try {
//                        userInfo.setProfileImageFilePath(c.getString(6));
//                    }  catch (SQLiteException e) {
//                        Log.e(TAG,"getSavedUserInfo adding setProfileImgFilePath sqlite problem: " + e);
//                    } catch (NullPointerException e) {
//                        Log.e(TAG,"getSavedUserInfo adding setProfileImgFilePath other NullPointerException... " + e);
//                    }
//
//                    userInfoList.add(userInfo);
//                } while (c.moveToNext());
//            }
//
//            c.close();
//        } finally {
//            db.close();
//        }
//
//        return userInfoList;
//    }


    /**
     * Used to prepare colorblock information in {@link com.jukuproject.jukutweet.Fragments.TweetListSingleUserFragment}
     * @param colorThresholds Collection of thresholds which together determine what color to assign to a word/tweet based on its quiz scores
     *
     * @return Cursor with colorblock details for each saved TweetList in the db
     *
     * @see com.jukuproject.jukutweet.Fragments.TweetListSingleUserFragment
     */
    public Cursor getTweetListColorBlocksCursorForUserInfoMenuHeaders(ColorThresholds colorThresholds) {

        return  sqlOpener.getWritableDatabase().rawQuery(

                "Select [UserInformation].ScreenName" +
                        ",  [UserInformation].Description" +
                        ",  [UserInformation].FollowerCount" +
                        ", [UserInformation].FriendCount" +
                        ", [UserInformation].ProfileImgUrl" +
                        ",  [UserInformation].UserId" +
                        ", [UserInformation].ProfileImgFilePath" +
                        ", [UserInformation].UserName " +

                        ",[Total] " +
                        ",[Grey] " +
                        ",[Red] " +
                        ",[Yellow] " +
                        ",[Green] " +
                        ",[TweetCount] " +


                        "FROM " +
                        "(" +
                        "Select distinct ScreenName" +
                        ",IFNULL(Description,'') as [Description]" +
                        ",IFNULL(FollowerCount,-1)  as [FollowerCount]" +
                        ", IFNULL(FriendCount,-1)  as [FriendCount]" +
                        ", IFNULL(ProfileImgUrl,'') as [ProfileImgUrl]" +
                        ",UserId" +
                        ", ProfileImgFilePath" +
                        ", UserName " +
                        "From " + InternalDB.Tables.TABLE_USERS + " " +
                        ") as [UserInformation] " +
                        " LEFT JOIN " +
                        " ( " +

                "Select DISTINCT a.[UserId]" +
                        ",a.[Total] " +
                        ",a.[Grey] " +
                        ",a.[Red] " +
                        ",a.[Yellow] " +
                        ",a.[Green] " +
                        ",ifnull(b.[TweetCount],0) as [TweetCount] " +
                        "FROM " +
                        "(" +

                /* Now to pull together ListName, Tweet and the totals (by color) of the kanji in those tweets */
                        "SELECT  ListsTweetsAndAllKanjis.[UserId] " +
                        ",SUM([Grey]) + SUM([Red]) + SUM([Yellow]) + SUM([Green]) as [Total] " +
                        ",SUM([Grey]) as [Grey]" +
                        ",SUM([Red]) as [Red]" +
                        ",SUM([Yellow]) as [Yellow]" +
                        ",SUM([Green]) as [Green] " +
                        "FROM (" +

                        /* Now we have a big collection of list metadata (tweetlists), and all the kanji scores and colors for
                            each kanji (kanjilists) */
                        " Select DISTINCT TweetLists.[UserId] " +
//                        " ,TweetLists.[TweetCount]  " +
                        " ,TweetKanji.Edict_id "   +
                        ",(CASE WHEN [Total] is not NULL AND [Total] < " + colorThresholds.getGreyThreshold() + " THEN 1 ELSE 0 END) as [Grey] " +
                        ",(CASE WHEN [Total] is not NULL and [Total] >= " + colorThresholds.getGreyThreshold() + " and [Percent] < " + colorThresholds.getRedThreshold() + "  THEN 1  ELSE 0 END) as [Red] " +
                        ",(CASE WHEN [Total] is not NULL and [Total] >= " + colorThresholds.getGreyThreshold() + " and ([Percent] >= " + colorThresholds.getRedThreshold() + "  and [Percent] <  " + colorThresholds.getYellowThreshold() + ") THEN 1  ELSE 0 END) as [Yellow] " +
                        ",(CASE WHEN [Total] is not NULL and [Total] >= " + colorThresholds.getGreyThreshold() + " and [Percent] >= " + colorThresholds.getYellowThreshold() + " THEN 1 ELSE 0 END) as [Green] " +

                        "FROM " +

                        "(" +

                        /* Get A list of each saved tweet and the number of kanji in those tweets */
                        "SELECT  DISTINCT [UserId]" +
                        ", _id as [Tweet_id]" +
                        "FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
                        ") as TweetLists " +
                        " LEFT JOIN " +
                        " ( " +

                        /* Get a list of  kanji ids and their word scores for each tweet */
                        "SELECT a.[Tweet_id]" +
                        ",a.[Edict_id]" +
                        ",ifnull(b.[Total],0) as [Total] " +
                        ",ifnull(b.[Correct],0)  as [Correct]" +
                        ",CAST(ifnull(b.[Correct],0)  as float)/b.[Total] as [Percent] " +
                        "FROM " +
                        "( " +
                        " SELECT Tweet_id" +
                        ",Edict_id " +
                        "From " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + " " +
                        ") as a " +
                        "LEFT JOIN " +
                        " (" +
                        "SELECT [_id] as [Edict_id]" +
                        ",sum([Correct]) as [Correct]" +
                        ",sum([Total]) as [Total] FROM [JScoreboard] " +
                        "where [_id] in (SELECT DISTINCT [Edict_id] FROM " + InternalDB.Tables.TABLE_SAVED_TWEET_KANJI  + ")" +
                        " GROUP BY [_id]" +
                        ") as b " +
                        "ON a.[Edict_id] = b.[Edict_id] " +



                        " ) as TweetKanji " +
                        "On TweetLists.Tweet_id = TweetKanji.Tweet_id " +

                        ") as [ListsTweetsAndAllKanjis] " +
                        "GROUP BY [UserId]" +

                        ") as a " +
                        "LEFT JOIN " +
                        "(" +

                        /* Get A list of each saved tweet and the number of kanji in those tweets */
                        "SELECT [UserId]" +
                        ",COUNT(_id) as TweetCount " +
                        "FROM " +
                        " ( " +
                        "SELECT  DISTINCT [UserId]" +
                        ", _id " +
                        "FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " " +
                        ") as x " +
                        " Group by [UserId] " +
                        ")  as b " +
                        " ON a.[UserId] = b.[UserId] " +


                ")  as [UserSavedTweetColorInformation] " +
                                " ON [UserInformation].[UserId] = [UserSavedTweetColorInformation].[UserId] "

                ,null);
    }


    /**
     * Compares two UserInfo objects, and overwrites the old object in the database if values
     * have changed. Used when user timeline info is pulled, to check the user metadata attached to the tweets
     * against the saved data in the db
     * @param oldUserInfo old UserInfo object (saved info in db)
     * @param recentUserInfo new UserInfo object (pulled from tweet metadata)
     * @return bool true if compare and update was succesful, false if error
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

    /**
     * Inputs an entry into the Users table for just a Screen Name. Used when a new screen name is added in
     * the {@link com.jukuproject.jukutweet.Dialogs.AddUserDialog}, and the device is not able to access the internet
     * or generally to complete the search necessary to pull up the {@link com.jukuproject.jukutweet.Dialogs.AddUserCheckDialog}. The
     * potential screen name is added, and can be updated later with {@link #compareUserInfoAndUpdate(UserInfo, UserInfo)}
     * @param screenName
     * @return
     */
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

}