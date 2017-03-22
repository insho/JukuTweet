package com.jukuproject.jukutweet.Database;

        import android.content.ContentValues;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteException;
        import android.database.sqlite.SQLiteOpenHelper;
        import android.support.annotation.Nullable;
        import android.util.Log;


        import com.jukuproject.jukutweet.Models.UserInfo;
        import com.jukuproject.jukutweet.Models.WordLoader;

        import java.util.ArrayList;
        import java.util.HashMap;
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
    public static final String TMAIN_COL1 = "UserId";
    public static final String TMAIN_COL2 = "Description";
    public static final String TMAIN_COL3 = "FollowerCount";
    public static final String TMAIN_COL4 = "FriendCount";
    public static final String TMAIN_COL5 = "ProfileImgUrl";

    public static final String TABLE_SCOREBOARD = "JScoreboard";
    public static final String TSCOREBOARD_COL0 = "Total";
    public static final String TSCOREBOARD_COL1 = "Correct";


    public static final String TABLE_FAVORITES_LIST_ENTRIES = "JFavorites";

    public static final String TABLE_FAVORITES_LISTS = "JFavoritesLists";
    public static final String TFAVORITES_COL0 = "Name";
    public static final String TFAVORITES_COL1 = "Sys";

//    public static final String TABLE_HIGHSCORES = "JHighScore";



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
        String sqlQueryUsers =
                String.format("CREATE TABLE IF NOT EXISTS %s (" +
                                "%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "%s TEXT, " +
                                "%s INTEGER, " +
                                "%s TEXT, " +
                                "%s INTEGER, " +
                                "%s INTEGER, " +
                                "%s TEXT) ", TABLE_MAIN,
                        COL_ID,
                        TMAIN_COL0,
                        TMAIN_COL1,
                        TMAIN_COL2,
                        TMAIN_COL3,
                        TMAIN_COL4,
                        TMAIN_COL5);

        sqlDB.execSQL(sqlQueryUsers);


        String sqlQueryJScoreBoard =
                String.format("CREATE TABLE IF NOT EXISTS %s (" +
                                "%s INTEGER PRIMARY KEY, " +
                                "%s INTEGER, " +
                                "%s INTEGER)", TABLE_SCOREBOARD,
                        COL_ID, //_id
                        TSCOREBOARD_COL0, //Total
                        TSCOREBOARD_COL1); // Correct

        sqlDB.execSQL(sqlQueryJScoreBoard);

        //The "My Lists" table
        String sqlQueryJFavoritesLists =
                String.format("CREATE TABLE IF NOT EXISTS %s (" +
                                "%s TEXT)", TABLE_FAVORITES_LISTS,
                        TFAVORITES_COL0);

        sqlDB.execSQL(sqlQueryJFavoritesLists);

        String sqlQueryJFavoritesListEntries =
                String.format("CREATE TABLE IF NOT EXISTS  %s (" +
                                "%s INTEGER, " +
                                "%s TEXT, " +
                                "%s INTEGER)", TABLE_FAVORITES_LIST_ENTRIES,
                        COL_ID,
                        TFAVORITES_COL0,
                        TFAVORITES_COL1); // if this column = 1, it is a system table (i.e. blue, red, yellow), if user-created value is 0

        sqlDB.execSQL(sqlQueryJFavoritesListEntries);
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
                    values.put(TMAIN_COL1, userInfo.getUserId());
                }
                if(userInfo.getDescription() != null) {
                    values.put(TMAIN_COL2, userInfo.getDescription().trim());
                }
                if(userInfo.getFollowerCount() != null ) {
                    values.put(TMAIN_COL3, userInfo.getFollowerCount());
                }
                if(userInfo.getFriendCount() != null){
                    values.put(TMAIN_COL4, userInfo.getFriendCount());
                }
                if(userInfo.getProfile_image_url() != null) {
                    values.put(TMAIN_COL5, userInfo.getProfile_image_url().trim());
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

        String querySelectAll = "Select distinct ScreenName,IFNULL(Description,''),FollowerCount, FriendCount, IFNULL(ProfileImgUrl,''),UserId From " + TABLE_MAIN;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(querySelectAll, null);


        try {
            if (c.moveToFirst()) {
                do {
                    UserInfo userInfo = new UserInfo(c.getString(0));
                    userInfo.setDescription(c.getString(1));

                    try {
                        userInfo.setUserId(c.getInt(5));
                    } catch (SQLiteException e) {

                    } catch (Exception e) {

                    }

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

                    userInfoList.add(userInfo);

                } while (c.moveToNext());
            }

            c.close();
        } finally {
            db.close();
        }

        return userInfoList;
    }

//TODO the screenname is not unique! for any of these. it can change... use twitter ID where possible!
    public boolean compareUserInfoAndUpdate(UserInfo oldUserInfo, UserInfo recentUserInfo) {

        try {
            ContentValues values = new ContentValues();

            /*The user's screenName field can change, but the user id won't. So if possible user the userId as the
              key for making updates */
            if(oldUserInfo.getUserId() != null && !oldUserInfo.getScreenName().equals(recentUserInfo.getScreenName())) {
                values.put(InternalDB.TMAIN_COL0, recentUserInfo.getScreenName().trim());
            }
            if(!oldUserInfo.getDescription().equals(recentUserInfo.getDescription())) {
                values.put(InternalDB.TMAIN_COL2, recentUserInfo.getDescription().trim());
            }
            if(oldUserInfo.getFollowerCount() != recentUserInfo.getFollowerCount()) {
                values.put(InternalDB.TMAIN_COL3, recentUserInfo.getFollowerCount());
            }
            if(oldUserInfo.getFriendCount() != recentUserInfo.getFriendCount()) {
                values.put(InternalDB.TMAIN_COL4, recentUserInfo.getFriendCount());
            }
            if(!oldUserInfo.getProfile_image_url().equals(recentUserInfo.getProfile_image_url())) {
                values.put(InternalDB.TMAIN_COL5, recentUserInfo.getProfile_image_url().trim());
            }

            if(values.size()>0) {
                if(oldUserInfo.getUserId() != null) {
                    SQLiteDatabase db = this.getReadableDatabase();
                    db.update(TABLE_MAIN, values, TMAIN_COL1 + "= ?", new String[]{String.valueOf(oldUserInfo.getUserId())});
                } else {
                    SQLiteDatabase db = this.getReadableDatabase();
                    db.update(TABLE_MAIN, values, TMAIN_COL0 + "= ?", new String[]{oldUserInfo.getScreenName()});
                }
            }

            return true;
        } catch (SQLiteException e) {
            Log.e(TAG,"compareUserInfo prob : " + e);
        }
        return false;
    }


    public WordLoader getWordLists(@Nullable SQLiteDatabase inputDB) {
        SQLiteDatabase db;
        if(inputDB == null) {
            db = this.getWritableDatabase();
        } else {
            db = inputDB;
        }
        ArrayList<String> hiragana = new ArrayList<>();
        ArrayList<String> katakana = new ArrayList<>();
        ArrayList<String> symbols = new ArrayList<>();
        HashMap<String,ArrayList<String>> romajiMap = new HashMap<>();
        ArrayList<String> verbEndingsRoot = new ArrayList<>();
        ArrayList<String> verbEndingsConjugation = new ArrayList<>();
        HashMap<String,ArrayList<String>> verbEndingMap = new HashMap<>();

        try {
            ArrayList<String> romaji = new ArrayList<>();
            Cursor c = db.rawQuery("SELECT DISTINCT Type, Key, Value FROM [Characters] ORDER BY Type",null);
            if (c.moveToFirst()) {
                while (!c.isAfterLast()) {
                    switch (c.getString(0)) {
                        case "Hiragana":
                            hiragana.add(c.getString(1));
                            break;
                        case "Katakana":
                            katakana.add(c.getString(1));
                            break;
                        case "Symbols":
                            symbols.add(c.getString(1));
                            break;
                        case "Romaji":
                            romaji.add(c.getString(1));
                            break;
                        case "VerbEndings":
                            verbEndingsRoot.add(c.getString(1));
                            verbEndingsConjugation.add(c.getString(2));

                            if(verbEndingMap.containsKey(c.getString(2))) {
                                ArrayList<String> tmp = verbEndingMap.get(c.getString(2));
                                tmp.add(c.getString(1));
                                verbEndingMap.put(c.getString(1),tmp);
                            } else {
                                ArrayList<String> tmp = new ArrayList<>();
                                tmp.add(c.getString(1));
                                verbEndingMap.put(c.getString(2),tmp);
                            }

                            break;
                    }
                    c.moveToNext();
                }
                c.close();

                //Add to Romaji HashMap
                for(int i=0;i<romaji.size();i++) {
                    ArrayList<String> tmp;
                    if(romajiMap.containsKey(romaji.get(i))){
                        tmp = romajiMap.get(romaji.get(i));
                    } else {
                        tmp = new ArrayList<>();
                    }

                    tmp.add(hiragana.get(i));
                    tmp.add(katakana.get(i));
                    romajiMap.put(romaji.get(i),tmp);

                }


                ArrayList<String> extratmp = new ArrayList<>();
                extratmp.add("ん");
                extratmp.add("ン");
                romajiMap.put("n",extratmp);


            }
        }catch(Exception e){
            e.printStackTrace();
        }


        if(inputDB == null && db.isOpen()) {
            db.close();
        }
        return new WordLoader(hiragana,katakana,symbols,romajiMap,verbEndingMap,verbEndingsRoot,verbEndingsConjugation);
    }


//    public void getUserCreatedMyLists() {
//        SQLiteDatabase db = this.getReadableDatabase();
//
//        if(totalactivelists == 0) {
//            Cursor z = db.rawQuery("Select Count(Name) as [ListCount] from JFavoritesLists", null);
//
//            z.moveToFirst();
//            if (z.getCount() > 0) {
//                totalactivelists = z.getInt(0);
//
//            }
//            z.close();
//
//
//            totalactivelists += colors.size();
//        };
//    }
}