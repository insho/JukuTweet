package com.jukuproject.jukutweet.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.jukuproject.jukutweet.Adapters.CopyMyListItemsAdapter;
import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Interfaces.DialogInteractionListener;
import com.jukuproject.jukutweet.Interfaces.RxBus;
import com.jukuproject.jukutweet.Models.MyListEntry;
import com.jukuproject.jukutweet.R;
import com.jukuproject.jukutweet.SharedPrefManager;

import java.util.ArrayList;

import rx.functions.Action1;

//import android.app.DialogFragment;
//import com.jukuproject.jukutweet.Interfaces.MyListCopyDialogListener;

/**
 * Dialog for "following" a new twitter user. New user name is entered into edittext
 * and then input into the database
 */
public class CopyMyListItemsDialog extends DialogFragment {

    private AlertDialog.Builder builder;
    public DialogInteractionListener mCallback;

    private ArrayList<String> mActiveFavoriteStars;
    private MyListEntry mCurrentList;
    private ArrayList<MyListEntry> mFavoritesLists;
    /*Tracks elapsed time since last click of a recyclerview row. Used to
* keep from constantly recieving button clicks through the RxBus */
    private long mLastClickTime = 0;
    private boolean moveSelected =false;
    private ArrayList<MyListEntry> mListsToCopyTo = new ArrayList<>();


    private RxBus mRxBus = new RxBus();

    String TAG = "TEST-AddUser";

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (DialogInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement mAddUserDialogListener");
        }


        Log.d(TAG,"ON ATTACH");



    }



    public static CopyMyListItemsDialog newInstance(MyListEntry currentList, ArrayList<Integer> selectedEntries) {

        CopyMyListItemsDialog frag = new CopyMyListItemsDialog();
        Bundle args = new Bundle();
        args.putParcelable("currentList",currentList);
        args.putIntegerArrayList("selectedEntries", selectedEntries);
        frag.setArguments(args);
        return frag;
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        final UserInfo userInfo = getArguments().getParcelable("userInfo");

        mActiveFavoriteStars  = SharedPrefManager.getInstance(getContext()).getActiveFavoriteStars();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        //If there is no banner, show the simple different layout
        View dialogView = inflater.inflate(R.layout.dialog_copymylist, null);

        final TextView move = (TextView) dialogView.findViewById(R.id.textViewCopyDialogMove);
        final TextView copy = (TextView) dialogView.findViewById(R.id.textViewCopyDialogCopy);

        setButtonActive(copy,true);
        setButtonActive(move,false);


        /** Add the "Move / Copy"  buttons*/

        move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButtonActive(copy,false);
                setButtonActive(move,true);
                moveSelected = true;
            }
        });

        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButtonActive(copy,true);
                setButtonActive(move,false);
                moveSelected = false;
            }

        });

        final ArrayList<Integer> selectedEntries = getArguments().getIntegerArrayList("selectedEntries");
        mCurrentList = getArguments().getParcelable("currentList");
        final String kanjiString = getSelectedIntsAsString(selectedEntries);
        mFavoritesLists = InternalDB.getWordInterfaceInstance(getContext()).getWordListsForAWord(mActiveFavoriteStars,"",mCurrentList);

        if(mFavoritesLists.contains(mCurrentList)) {
            mFavoritesLists.remove(mCurrentList);
        }

        if(BuildConfig.DEBUG){Log.d(TAG,"FAVORITE LISTS SIZE: " + mFavoritesLists.size());}
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        RecyclerView mRecyclerView = (RecyclerView) dialogView.findViewById(R.id.listView);
        mRecyclerView.setLayoutManager(mLayoutManager);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        RecyclerView.Adapter adapter = new CopyMyListItemsAdapter(getContext(),metrics.density,mRxBus,mFavoritesLists);
        mRecyclerView.setAdapter(adapter);


        mRxBus.toClickObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {

                        if(isUniqueClick(1000) && event instanceof MyListEntry) {
                            MyListEntry myListEntry = (MyListEntry) event;
                            if(mListsToCopyTo.contains((myListEntry))) {
                                mListsToCopyTo.remove(myListEntry);
                            } else {
                                mListsToCopyTo.add(myListEntry);
                            }
                        }

                    }

                });

        builder.setView(dialogView);


        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCallback.saveAndUpdateMyLists(kanjiString,mListsToCopyTo,moveSelected,mCurrentList);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setCancelable(true);

        Log.d(TAG,"BUilder create");
        return builder.create();
    }

//    @Override
//    public void onDismiss(DialogInterface dialog) {
//        super.onDismiss(dialog);
//        mCallback.onDialogDismiss();
//    }


    private void setButtonActive(TextView textView,boolean active){
        textView.setSelected(active);

        if(active) {
            textView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            textView.setAlpha(1.0f);
            textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
        } else {
            textView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
            textView.setAlpha(.80f);
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorTextDark));
        }

    }

    //TODO CONSOLIDATE
    public String getSelectedIntsAsString(ArrayList<Integer> list ) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < list.size(); ++i) {
            if (i>0) {
                sb.append(", ");
            }
            sb.append(list.get(i).toString());
        }
        return sb.toString();
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

}