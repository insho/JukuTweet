package com.jukuproject.jukutweet.Fragments;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.jukuproject.jukutweet.Adapters.BrowseWordsAdapter;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Dialogs.CopyMyListItemsDialog;
import com.jukuproject.jukutweet.Dialogs.WordDetailPopupDialog;
import com.jukuproject.jukutweet.Interfaces.FragmentInteractionListener;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Interfaces.WordEntryFavoritesChangedListener;
import com.jukuproject.jukutweet.Interfaces.WordListOperationsInterface;
import com.jukuproject.jukutweet.Models.ColorThresholds;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.Models.WordEntry;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.SharedPrefManager;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.jukuproject.jukutweet.Dialogs.CopyMyListItemsDialog.getSelectedIntsAsString;
import static java.util.concurrent.TimeUnit.SECONDS;


/**
 * Displays a list of {@link WordEntry} objects for a WordList. User can edit lists (move/copy/remove) words, and
 * open the {@link WordDetailPopupDialog} with a long click.
 */
public class WordListBrowseFragment extends Fragment implements WordEntryFavoritesChangedListener {

    String TAG = "TEST-Wordbrowse";
    private RxBus mRxBus = new RxBus();
    private RecyclerView mRecyclerView;
    /*Tracks elapsed time since last click of a recyclerview row. Used to
    * keep from constantly recieving button clicks through the RxBus */
    private long mLastClickTime = 0;
    FragmentInteractionListener mCallback;
    private BrowseWordsAdapter mAdapter;
    private ArrayList<WordEntry> mWords;
    private MyListEntry mMyListEntry;
    private ColorThresholds mColorThresholds;
    private ArrayList<Integer> mSelectedEntries = new ArrayList<>(); //Tracks which entries in the adapter are currently selected (id key)
    private Subscription undoSubscription;

    public static WordListBrowseFragment newInstance(MyListEntry myListEntry) {
        WordListBrowseFragment fragment = new WordListBrowseFragment();
        Bundle args = new Bundle();
        args.putParcelable("mylistentry", myListEntry);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (FragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_browse, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerMain);
        return v;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState == null) {
            mMyListEntry = getArguments().getParcelable("mylistentry");
        } else {
            mMyListEntry = savedInstanceState.getParcelable("mylistentry");
            mWords = savedInstanceState.getParcelableArrayList("mWords");
            mSelectedEntries = savedInstanceState.getIntegerArrayList("mSelectedEntries");
        }
        mColorThresholds = SharedPrefManager.getInstance(getContext()).getColorThresholds();

        /*If orientation changed and some rows were selected before activity restarts,
         show the browse menu when activity is recreated */
        if(mSelectedEntries != null && mSelectedEntries.size()>0) {
            mCallback.showMenuMyListBrowse(true,2);
        }

        if(savedInstanceState == null || mWords == null) {
            //Pull list of word entries in the database for a given list
            mWords = InternalDB.getWordInterfaceInstance(getContext()).getWordsFromAWordList(mMyListEntry
                    ,mColorThresholds
                    ,"'Grey','Red','Yellow','Green'"
                    ,null
                    ,null);
        }
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //Create UserListAdapter and attach rxBus click listeners to it
        if(mWords != null && mWords.size() > 0) {
            mAdapter = new BrowseWordsAdapter(getContext(),mWords,mColorThresholds,mRxBus,mSelectedEntries);

            mRxBus.toClickObserverable()
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object event) {

                            if(isUniqueClick(100) && event instanceof Integer) {

                                Integer id = (Integer) event;

                                if(!mSelectedEntries.contains(id)) {
                                    if(mSelectedEntries.size()==0) {
                                        mCallback.showMenuMyListBrowse(true,2);
                                    }
                                    mSelectedEntries.add(id);
                                } else {
                                    mSelectedEntries.remove(id);
                                }

                                if(mSelectedEntries.size()==0) {
                                    mCallback.showMenuMyListBrowse(false,2);
                                }
                            }

                        }

                    });




            mRxBus.toLongClickObserverable()
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object event) {
                            if(isUniqueClick(1000) && event instanceof WordEntry) {
                                WordEntry wordEntry = (WordEntry) event;
                                WordDetailPopupDialog wordDetailPopupDialog = WordDetailPopupDialog.newInstance(wordEntry);
                                wordDetailPopupDialog.setTargetFragment(WordListBrowseFragment.this, 0);
                                wordDetailPopupDialog.show(getFragmentManager(),"wordDetailPopup");
                            }

                        }

                    });
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setVerticalScrollBarEnabled(true);

        }
    }




    /**
     * Checks how many milliseconds have elapsed since the last time "mLastClickTime" was updated
     * If enough time has elapsed, returns True and updates mLastClickTime.
     * This is to stop unwanted rapid clicks of the same button
     * @param elapsedMilliSeconds threshold of elapsed milliseconds before a new button click is allowed
     * @return bool True if enough time has elapsed, false if not
     */
    public boolean isUniqueClick(int elapsedMilliSeconds) {
        if(SystemClock.elapsedRealtime() - mLastClickTime > elapsedMilliSeconds) {
            mLastClickTime = SystemClock.elapsedRealtime();
            return true;
        } else {
            return false;
        }
    }

    public void deselectAll(){
        mSelectedEntries.clear();
        mAdapter.notifyDataSetChanged();
    }

    public void selectAll() {

        if(mSelectedEntries.size() != mWords.size()) {
            mSelectedEntries.clear();
            for(WordEntry entry : mWords) {
                mSelectedEntries.add(entry.getId());
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Opens {@link CopyMyListItemsDialog}. Called when user clicks copy button in action bar.
     * Main Activity callback launches this method.
     */
    public void showCopyMyListDialog(){
        if(getActivity().getSupportFragmentManager().findFragmentByTag("dialogCopy") == null || !getActivity().getSupportFragmentManager().findFragmentByTag("dialogCopy").isAdded()) {
            CopyMyListItemsDialog.newInstance(mMyListEntry,mSelectedEntries).show(getActivity().getSupportFragmentManager(),"dialogCopy");
        }
    }





    /**
     * Recieves result of {@link com.jukuproject.jukutweet.Dialogs.CopyMyListItemsDialog} , passed from {@link com.jukuproject.jukutweet.MainActivity#saveAndUpdateMyLists(String, ArrayList, boolean, MyListEntry)}.
     * This method is where the changes are entered into the database, the recycler is refreshed, and the user notified
     *
     * @param kanjiIdString concatenated string of kanji ids that will be moved/copied
     * @param listsToCopyTo MyList object of word lists that the words will be copied to
     * @param move bool true for MOVE the words, false to only COPY the words
     * @param currentList The current list from which the copy dialog is called. This is so the list is not included
     *                    as an option to move/copy to.
     */
    public void saveAndUpdateMyLists(String kanjiIdString,ArrayList<MyListEntry> listsToCopyTo, boolean move,MyListEntry currentList) {

        WordListOperationsInterface helperWordOps = InternalDB.getWordInterfaceInstance(getContext());
        try {
            for(MyListEntry entry : listsToCopyTo) {
                helperWordOps.addMultipleWordsToWordList(entry,kanjiIdString);
            }

            if(move) {
                removeKanjiFromList(kanjiIdString,currentList);
                Toast.makeText(getContext(), "Items moved successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Items copied successfully", Toast.LENGTH_SHORT).show();
            }
            deselectAll();
            mCallback.showMenuMyListBrowse(false,2);
        } catch (NullPointerException e) {
            Log.e(TAG,"Nullpointer in WordListBrowseFragment saveAndUpdateMyLists : " + e);
            Toast.makeText(getContext(), "Unable to update lists", Toast.LENGTH_SHORT).show();
        } catch (SQLiteException e) {
            Log.e(TAG,"SQLiteException in WordListBrowseFragment saveAndUpdateMyLists : " + e);
            Toast.makeText(getContext(), "Unable to update lists", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Deletes items from the dataset and updates adapter, when User has "moved" them to a new list via
     * the {@link CopyMyListItemsDialog}
     * @param kanjiIdString concatenate (and comma seperated) string of kanji ids to move
     * @param currentList The current my list that is being browsed
     */
    public void removeKanjiFromList(String kanjiIdString, MyListEntry currentList){
        try {
            InternalDB.getWordInterfaceInstance(getContext()).removeMultipleWordsFromWordList(kanjiIdString,currentList);
            mWords = InternalDB.getWordInterfaceInstance(getContext()).getWordsFromAWordList(mMyListEntry
                    ,mColorThresholds
                    ,"'Grey','Red','Yellow','Green'"
                    ,null
                    ,null);

            /*Notify other tabs that a word has been removed.
            * If there is only one word, update the lists for that word entry. If
            * there are more, however, (Because there could be hundreds), have the tabs update themselves entirely
            */
            if(mSelectedEntries.size()==1) {
                for(WordEntry wordEntry : mWords) {
                    if(wordEntry.getId().equals(mSelectedEntries.get(0))) {
                        updateWordEntryFavoritesForOtherTabs(wordEntry);
                    }
                }

            } else {
                mCallback.notifySavedWordFragmentsChanged(getSelectedIntsAsString(mSelectedEntries));
            }

            mSelectedEntries.clear();
            mAdapter.swapDataSet(mWords,mSelectedEntries);
        } catch (NullPointerException e) {
            Log.e(TAG,"Nullpointer in WordListBrowseFragment removeKanjiFromList : " + e);
            Toast.makeText(getContext(), "Unable to delete entries", Toast.LENGTH_SHORT).show();
        } catch (SQLiteException e) {
            Log.e(TAG,"SQLiteException in WordListBrowseFragment removeKanjiFromList : " + e);
            Toast.makeText(getContext(), "Unable to delete entries", Toast.LENGTH_SHORT).show();
        }
    }



    /**
     * Deletes selected items from the dataset and updates adapter, when User has clicked on the trash icon. Activated
     *  via a callback from {@link com.jukuproject.jukutweet.MainActivity#onOptionsItemSelected(MenuItem)}
     */
    public void removeKanjiFromList(){
        try {
            final String kanjiString = getSelectedIntsAsString(mSelectedEntries);
            InternalDB.getWordInterfaceInstance(getContext()).removeMultipleWordsFromWordList(kanjiString,mMyListEntry);
            mWords = InternalDB.getWordInterfaceInstance(getContext()).getWordsFromAWordList(mMyListEntry
                    ,mColorThresholds
                    ,"'Grey','Red','Yellow','Green'"
                    ,null
                    ,null);


            if(mSelectedEntries.size()==1) {
                for(WordEntry wordEntry : mWords) {
                    if(wordEntry.getId().equals(mSelectedEntries.get(0))) {
                        updateWordEntryFavoritesForOtherTabs(wordEntry);
                    }
                }

            } else {
                mCallback.notifySavedWordFragmentsChanged(getSelectedIntsAsString(mSelectedEntries));
            }

            mSelectedEntries.clear();
            mAdapter.swapDataSet(mWords,mSelectedEntries);
            showUndoPopup(kanjiString,mMyListEntry);


        } catch (NullPointerException e) {
            Log.e(TAG,"Nullpointer in WordListBrowseFragment removeKanjiFromList : " + e);
            Toast.makeText(getContext(), "Unable to delete entries", Toast.LENGTH_SHORT).show();
        } catch (SQLiteException e) {
            Log.e(TAG,"SQLiteException in WordListBrowseFragment removeKanjiFromList : " + e);
            Toast.makeText(getContext(), "Unable to delete entries", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Shows the "undo changes" popup window after user deletes selected rows. The window stays visible for 3 seconds,
     * giving the user a chance to click on the "undo" button and reverse the changes.
     * @param kanjiIdString concatenate (and comma seperated) string of kanji ids to move
     * @param currentList The current my list that is being browsed
     */
    public void showUndoPopup(final String kanjiIdString, final MyListEntry currentList) {

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = layoutInflater.inflate(R.layout.popup_undo, null);

        final PopupWindow popupWindow = new PopupWindow(getContext());
        popupWindow.setFocusable(true);
        popupWindow.setClippingEnabled(false);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setWidth((int)(metrics.widthPixels*.66f));

        TextView undoButton = (TextView) v.findViewById(R.id.undoButton);
        undoSubscription  =  Observable.timer(3, SECONDS).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        popupWindow.dismiss();
                    }
                });
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    InternalDB.getWordInterfaceInstance(getContext()).addMultipleWordsToWordList(currentList,kanjiIdString);
                    mWords = InternalDB.getWordInterfaceInstance(getContext()).getWordsFromAWordList(mMyListEntry
                            ,mColorThresholds
                            ,"'Grey','Red','Yellow','Green'"
                            ,null
                            ,null);

                    if(mSelectedEntries.size()==1) {
                        for(WordEntry wordEntry : mWords) {
                            if(wordEntry.getId().equals(mSelectedEntries.get(0))) {
                                updateWordEntryFavoritesForOtherTabs(wordEntry);
                            }
                        }

                    } else {
                        mCallback.notifySavedWordFragmentsChanged(getSelectedIntsAsString(mSelectedEntries));
                    }

                    mSelectedEntries.clear();
                    mAdapter.swapDataSet(mWords,mSelectedEntries);

                        popupWindow.dismiss();
                        undoSubscription.unsubscribe();


                } catch (NullPointerException e) {
                    Log.e(TAG,"Nullpointer in WordListBrowseFragment showUndoPopup : re-add" + e);
                    Toast.makeText(getContext(), "Unable to undo delete!", Toast.LENGTH_SHORT).show();
                } catch (SQLiteException e) {
                    Log.e(TAG,"SQLiteException in WordListBrowseFragment showUndoPopup re-add : " + e);
                    Toast.makeText(getContext(), "Unable to undo delete!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.popup_drawable));
        popupWindow.setContentView(v);
        popupWindow.showAtLocation(mRecyclerView, Gravity.BOTTOM, 0, (int)(metrics.heightPixels / (float)9.5));
    }

    /**
     *  When word entry favorites star is clicked on the {@link BrowseWordsAdapter}, a callback is initiated and the
     *  dataset in this fragment is updated. OR, when this fragment is open in one tab, and the user changes the favorite
     *  star for a word in another tab, this method may be called from the {@link com.jukuproject.jukutweet.MainActivity#notifySavedWordFragmentsChanged(WordEntry)}
     *  It keeps the favorite list stars synchronized across tabs.
     * @param wordEntry WordEntry whose "FavoriteItems" object was updated (i.e. the favorite star was clicked and changed in the adapter)
     */
    public void updateWordEntryItemFavorites(WordEntry wordEntry) {
        boolean wordExistsinList = InternalDB.getWordInterfaceInstance(getContext()).myListContainsWordEntry(mMyListEntry,wordEntry);
        boolean wordEntryFound = false;
        for(WordEntry datasetWordEntry : mWords) {
            if(datasetWordEntry.getId().equals(wordEntry.getId())) {
                wordEntryFound = true;
                if(!wordExistsinList) {
                    //If the word that appeared in the popup window no longer is contained in this list, remove it
                    mWords.remove(datasetWordEntry);
                    if(mWords.size()==0) {
                        //Kick the user back to the main menu if the word that was removed was the last word in the list
                        mCallback.onBackPressed();
                    } else {
                        //Remove word entry from selected entries if applicable
                        if(mSelectedEntries.contains(datasetWordEntry.getId())) {
                            mSelectedEntries.remove(datasetWordEntry.getId());
                        }
                        if(mSelectedEntries.size()==0) {
                            mCallback.showMenuMyListBrowse(false,2);
                        }

                        mAdapter.notifyDataSetChanged();
                    }
                } else {
                    mWords.get(mWords.indexOf(wordEntry)).setItemFavorites(wordEntry.getItemFavorites());
                    mAdapter.notifyDataSetChanged();
                }

            }
        }

        //If no word entry was found in the list and the word should be there, add the word to the list
        if(wordExistsinList && !wordEntryFound) {
            mWords.add(wordEntry);
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     *  Initiates the {@link com.jukuproject.jukutweet.MainActivity#notifySavedWordFragmentsChanged(WordEntry)} method when the favorites entries
     *  for a word have changed in this tab, so that other tabs that might contain the same word are also updated.
     *  It keeps the favorite list stars synchronized across tabs.
     * @param wordEntry WordEntry whose "FavoriteItems" object was updated (i.e. the favorite star was clicked and changed in the adapter)
     */
    public void updateWordEntryFavoritesForOtherTabs(WordEntry wordEntry) {
        mCallback.notifySavedWordFragmentsChanged(wordEntry);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(undoSubscription!=null) {
            undoSubscription.unsubscribe();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(undoSubscription!=null) {
            undoSubscription.unsubscribe();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("myListEntry", mMyListEntry);
        outState.putParcelableArrayList("mWords",mWords);
        outState.putIntegerArrayList("mSelectedEntries",mSelectedEntries);
    }
}
