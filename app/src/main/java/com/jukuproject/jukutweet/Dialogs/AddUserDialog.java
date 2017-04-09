package com.jukuproject.jukutweet.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Interfaces.DialogInteractionListener;
import com.jukuproject.jukutweet.R;

import java.util.Arrays;
import java.util.List;

//import android.app.Dialog;
//import android.app.DialogFragment;

/**
 * Dialog for "following" a new twitter user. New user name is entered into edittext
 * and then input into the database
 */
public class AddUserDialog extends DialogFragment {

    public DialogInteractionListener mAddUserDialogListener;
    String TAG = "TEST-AddUser";

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mAddUserDialogListener = (DialogInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement mAddUserDialogListener");
        }
    }

    public static AddUserDialog newInstance() {
        return new AddUserDialog();
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

        //TODO test user, remove!!
//        editText.append(getString(R.string.testUser));
        //TODO -- Remove this after testing
        /** TESTING! Queue up the test feeds**/
        String[] feeds = getResources().getStringArray(R.array.testfeeds);
        /** TEST FEEDS */
        List<String> testFeeds = Arrays.asList(feeds);
        for(int i =0; i<testFeeds.size(); i++) {
            Log.d("TEST","TESTFEEDS: " + testFeeds.get(i));
            if(!InternalDB.getUserInterfaceInstance(getActivity()).duplicateUser(testFeeds.get(i)) && editText.getText().toString().isEmpty()) {
                editText.setText(testFeeds.get(i));
            }
        }

        /* Checks for a valid input, and if one exists, passes click event through DialogInterface
           to MainActivity */
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG,"mAddUserDialogListener: " + mAddUserDialogListener);
                String cleanString = atSignCheckandRemove(editText.getText().toString().trim());
                if(cleanString.trim().length() == 0) {
                    Toast.makeText(getActivity(), "Enter a user handle (without @ sign)", Toast.LENGTH_SHORT).show();
                } else {
                    mAddUserDialogListener.onAddUserDialogPositiveClick(editText.getText().toString().trim());
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

    /**
     * Checks whether a string begins with the "@" sign, and removes it if it exists
     * @param inputString raw input string (of prospective user handle)
     * @return string without an @ sign
     */
    public String atSignCheckandRemove(String inputString) {

        if(inputString.length() > 0 && inputString.substring(0,1).equals(getString(R.string.atsign))) {
            return inputString.substring(1,inputString.length());
        } else {
            return inputString;
        }
    }


}