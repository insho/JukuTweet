package com.jukuproject.jukutweet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jukuproject.jukutweet.InternalDB;

import java.util.Arrays;
import java.util.List;

public class AddFeedDialog extends DialogFragment {

    public DialogInteractionListener mAddRSSDialogListener;
    String TAG = "TEST-AddFeed";


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mAddRSSDialogListener = (DialogInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement mAddRSSDialogListener");
        }
    }

    public static AddFeedDialog newInstance() {
        AddFeedDialog frag = new AddFeedDialog();
//        Bundle args = new Bundle();
//        args.putBoolean("renamelist", renamelist);
//        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_dialog, null);

        TextView title = new TextView(getActivity());
        title.setText(R.string.dialog_title);
        title.setTextSize(18);
        title.setMinHeight(80);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(ContextCompat.getColor(getActivity(),android.R.color.white));
        title.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));
        builder.setCustomTitle(title);
        builder.setView(dialogView);

        final EditText editText = (EditText) dialogView.findViewById(R.id.input);
        editText.setText("@");
        Selection.setSelection(editText.getText(), editText.getText().length());

        /* User should not be able to delete the "@" prefix */
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

//            @Override
//            public void afterTextChanged(Editable s) {
//                if(!s.toString().contains("@")){
//                    editText.setText("@");
//                    Selection.setSelection(editText.getText(), editText.getText().length());
//
//                }
//
//            }
            @Override
            public void afterTextChanged(Editable s) {
                String prefix = "@";
                if (!s.toString().startsWith(prefix)) {
                    String cleanString;
                    String deletedPrefix = prefix.substring(0, prefix.length() - 1);
                    if (s.toString().startsWith(deletedPrefix)) {
                        cleanString = s.toString().replaceAll(deletedPrefix, "");
                    } else {
                        cleanString = s.toString().replaceAll(prefix, "");
                    }
                    editText.setText(prefix + cleanString);
                    editText.setSelection(prefix.length());
                }
            }
        });

        // show soft keyboard
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
//        for(int i =0; i<testFeeds.size(); i++) {
//            Log.d("TEST","TESTFEEDS: " + testFeeds.get(i));
//            if(!InternalDB.getInstance(getActivity()).existingRSSListValues(testFeeds.get(i), true) && editText.getText().toString().isEmpty()) {
//                editText.setText(testFeeds.get(i));
////                testFeeds.remove(testFeeds.get(i));
//            }
//        }

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG,"mAddRSSDialogListener: " + mAddRSSDialogListener);
                if(editText.getText().toString().trim().length() == 0 ||
                        editText.getText().toString().trim().equals("@")) {
                    Toast.makeText(getActivity(), "Enter a @user handle", Toast.LENGTH_SHORT).show();
                } else {
                    mAddRSSDialogListener.onAddRSSDialogPositiveClick(editText.getText().toString().trim());
                    dialog.dismiss();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setCancelable(true);
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mAddRSSDialogListener.onAddRSSDialogDismiss();
    }



}