package com.jukuproject.jukutweet.Database;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Interfaces.UserOperationsInterface;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


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
        String queryRecordExists = "Select _id From " + InternalDB.Tables.TABLE_USERS + " where " + InternalDB.Columns.TMAIN_COL0 + " = ? OR " + InternalDB.Columns.TMAIN_COL1 + " = ?" ;
        Cursor c = sqlOpener.getReadableDatabase().rawQuery(queryRecordExists, new String[]{user});
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
        }
        return false;
    }



    /**
     * Saves new user feed to DB
     * @param userInfo UserInfo object with user data pulled from twitter api
     * @return bool True if save worked, false if it failed
     */
    public boolean saveUser(UserInfo userInfo) {
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

                sqlOpener.getWritableDatabase().insert(InternalDB.Tables.TABLE_USERS, null, values);

                return true;
            } else {
                return false;
            }
        } catch(SQLiteException exception) {
            return false;
        }
    }


    /**
     * Removes a user from the database
     * @param userId user id string to remove
     * @return bool True if operation is succesful, false if an error occurs
     */
    public boolean deleteUser(String userId) {
        try{
            sqlOpener.getWritableDatabase().delete(InternalDB.Tables.TABLE_USERS, InternalDB.Columns.TMAIN_COL0 + "= ? OR " + InternalDB.Columns.TMAIN_COL1 + "= ? ", new String[]{userId,userId});

            //Delete saved tweets for the user that aren't in any favorite lists
            sqlOpener.getWritableDatabase().delete(InternalDB.Tables.TABLE_SAVED_TWEETS," UserId = ? AND " +
                    "( SELECT Count(_id) FROM " + InternalDB.Tables.TABLE_FAVORITES_LISTS_TWEETS_ENTRIES + " WHERE UserId = ?) = 0 ",new String[]{userId,userId});

            //Delete saved tweet kanji that don't have any associated tweets
            sqlOpener.getWritableDatabase().delete(InternalDB.Tables.TABLE_SAVED_TWEET_KANJI," Tweet_id not in ( " +
                    "SELECT DISTINCT _id FROM " + InternalDB.Tables.TABLE_SAVED_TWEETS + " ) ",null);

            return true;
        } catch(SQLiteException exception) {
            return false;
        }
    }

    /**
     * Inserts the URI link to the location in the phone of a twitter user's saved profile image
     * @param URI URI of saved image
     * @param userId userId of user
     */
    public void addUserIconURItoDB(String URI, String userId) {
        try {
        ContentValues values = new ContentValues();
        values.put(InternalDB.Columns.TMAIN_COL6, URI);
            sqlOpener.getWritableDatabase().update(InternalDB.Tables.TABLE_USERS, values, InternalDB.Columns.TMAIN_COL1 + "= ?", new String[] {userId});
        if(BuildConfig.DEBUG){Log.d(TAG,"SUCESSFUL INSERT URI for id: " + userId);}
        } catch (SQLiteException e) {
            Log.e(TAG,"addUserIconURItoDB sqlite problem: " + e);
        }
    }

    /**
     * Inserts the URI link to the location in the phone of a twitter user's saved profile image
     * @param URI URI of saved image
     * @param userId userId of user
     */
    public void addTweetIconURItoDB(String URI, String userId) {
        try {
            ContentValues values = new ContentValues();
            values.put(InternalDB.Columns.TMAIN_COL6, URI);
            sqlOpener.getWritableDatabase().update(InternalDB.Tables.TABLE_SAVED_TWEETS, values, InternalDB.Columns.TMAIN_COL6 + " is null and " + InternalDB.Columns.TMAIN_COL1 + "= ?", new String[] {userId});
            if(BuildConfig.DEBUG){Log.d(TAG,"SUCESSFUL INSERT URI for id: " + userId);}
        } catch (SQLiteException e) {
            Log.e(TAG,"addUserIconURItoDB sqlite problem: " + e);
        }
    }



    /**
     * Updates Twitter User information in the database when a newer version of that user's info is
     * available (for example, when a user's timeline is clicked, the userInfo is downloaded as part of the package
     * and can this method will be called)
     * @param userInfo fresh UserInfo
     */
    public void updateUserInfo(UserInfo userInfo) {
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

            sqlOpener.getWritableDatabase().update(InternalDB.Tables.TABLE_USERS, values, InternalDB.Columns.TMAIN_COL0 + "= ?", new String[] {userInfo.getScreenName()});
            if(BuildConfig.DEBUG){Log.d(TAG,"SUCESSFUL UPDATE OF USER INFO FOR: " + userInfo.getScreenName());}
        } catch (SQLiteException e) {
            Log.e(TAG,"updateUserInfo sqlite problem: " + e);
        }
    }


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
            if(recentUserInfo.getFollowerCount() != null && !oldUserInfo.getFollowerCount().equals(recentUserInfo.getFollowerCount()) ) {
                values.put(InternalDB.Columns.TMAIN_COL3, recentUserInfo.getFollowerCount());
            }
            if(recentUserInfo.getFriendCount() != null && !oldUserInfo.getFriendCount().equals(recentUserInfo.getFriendCount())) {
                values.put(InternalDB.Columns.TMAIN_COL4, recentUserInfo.getFriendCount());
            }
            if(recentUserInfo.getProfileImageUrl() != null && !oldUserInfo.getProfileImageUrl().equals(recentUserInfo.getProfileImageUrl())) {
                values.put(InternalDB.Columns.TMAIN_COL5, recentUserInfo.getProfileImageUrl().trim());
            }

            if(values.size()>0) {
                if(oldUserInfo.getUserId() != null) {
                    sqlOpener.getWritableDatabase().update(InternalDB.Tables.TABLE_USERS, values, InternalDB.Columns.TMAIN_COL1 + "= ?", new String[]{String.valueOf(oldUserInfo.getUserId())});
                } else {
                    sqlOpener.getWritableDatabase().update(InternalDB.Tables.TABLE_USERS, values, InternalDB.Columns.TMAIN_COL0 + "= ?", new String[]{oldUserInfo.getScreenName()});
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
        try {
                ContentValues values = new ContentValues();
                if(BuildConfig.DEBUG) {
                    Log.d(TAG,"saving OFFLINE screenname placeholder: " + screenName );
                }
                values.put(InternalDB.Columns.TMAIN_COL0, screenName.trim());
                sqlOpener.getWritableDatabase().insert(InternalDB.Tables.TABLE_USERS, null, values);
                return true;

        } catch(SQLiteException exception) {
            return false;
        }
    };


    /**
     * Takes the url of an icon image from a UserInfo object, downloads the image with picasso
     * and saves it to a file
     * @param imageUrl Url of icon image
     * @param userId userid string which will become the file name of the icon
     */
    public void downloadUserIcon(final Context context, String imageUrl, final String userId) {

        Picasso.with(context).load(imageUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            File file = checkForImagePath(context, userId);
                            if(!file.exists()) {
                                FileOutputStream ostream = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);
                                ostream.flush();
                                ostream.close();
                            }

                            Uri uri = Uri.fromFile(file);
                            addUserIconURItoDB(uri.toString(),userId);
                        } catch (IOException e) {
                            Log.e("IOException", e.getLocalizedMessage());
                        }
                    }
                }).start();

            }
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }



    /**
     * Takes the url of an icon image from a UserInfo object, downloads the image with picasso
     * and saves it to a file
     * @param imageUrl Url of icon image
     * @param userId userid string for user whose tweet icon is being saved. user id which will become the file name of the icon
     *
     * @see com.jukuproject.jukutweet.MainActivity#downloadTweetUserIcons(UserInfo)
     * @see com.jukuproject.jukutweet.QuizActivity#downloadTweetUserIcons(UserInfo)
     * @see com.jukuproject.jukutweet.PostQuizStatsActivity#downloadTweetUserIcons(UserInfo)
     */
    public void downloadTweetUserIcon(final Context context, String imageUrl, final String userId) {
        Picasso.with(context).load(imageUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            File file = checkForImagePath(context, userId);
                            if(!file.exists()) {
                                FileOutputStream ostream = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);
                                ostream.flush();
                                ostream.close();
                            }
                            //If no saved users exist for this tweet, go ahead and update all tweets for this user
                            if(!duplicateUser(userId)) {
                                Uri uri = Uri.fromFile(file);
                                addTweetIconURItoDB(uri.toString(),userId);
                            }
                        } catch (IOException e) {
                            Log.e("IOException", e.getLocalizedMessage());
                        }
                    }
                }).start();

            }
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }

    /**
     * Checks whether directory for saving twitter user icons already exist. If not, it creates the directory
     * @param userId title of image icon
     * @return file for image icon (to then be saved)
     */
    private File checkForImagePath(Context context, String userId) {
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir("icons", Context.MODE_PRIVATE);

        if (!directory.exists()) {
            directory.mkdir();
        }
        if(BuildConfig.DEBUG){Log.i(TAG,"URI directory: " + directory.getAbsolutePath() + ", FILE: " + userId +".png" );}
        return new File(directory, userId + ".png");
    }


    /**
     * Searches for User Icons in db that aren't associated with any current tweet/saved user, and remove them
     */
    public void clearOutUnusedUserIcons(Context context) {

        try {
        ArrayList<String> userURLS = new ArrayList<>();
        Cursor c =  sqlOpener.getWritableDatabase().rawQuery(
                "Select DISTINCT ProfileImgUrl "    +
                        " FROM " +
                        "( " +
                "Select DISTINCT ProfileImgUrl "    +
                " FROM " + InternalDB.Tables.TABLE_SAVED_TWEETS +
                        " UNION " +
                "Select DISTINCT ProfileImgUrl "    +
                " FROM " + InternalDB.Tables.TABLE_USERS +
                        ") as imageurls ",null);

        if(c.getCount()>0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                userURLS.add(c.getString(0));
                c.moveToNext();
            }
        }
        c.close();

        if(userURLS.size()>0) {
            ContextWrapper cw = new ContextWrapper(context);
            File directory = cw.getDir("icons", Context.MODE_PRIVATE);
            File lister = directory.getAbsoluteFile();

            for (File file : directory.listFiles()) {
                Uri uri = Uri.fromFile(file);
                if(!userURLS.contains(uri.toString())) {
                        file.delete();
                }
            }
        }

        } catch (SQLiteException sqlexception){
            Log.e(TAG,"clearOutUnusedUserIcons sqlite exception " + sqlexception.getCause());
        }
    }

}