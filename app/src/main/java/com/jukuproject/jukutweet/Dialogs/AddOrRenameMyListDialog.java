package com.jukuproject.jukutweet.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jukuproject.jukutweet.Database.InternalDB;
import com.jukuproject.jukutweet.Interfaces.DialogInteractionListener;
import com.jukuproject.jukutweet.R;

//import android.app.DialogFragment;

/**
 * Dialog for "following" a new twitter user. New user name is entered into edittext
 * and then input into the database
 */
public class AddOrRenameMyListDialog extends DialogFragment {

    public DialogInteractionListener mAddRSSDialogListener;
    String TAG = "TEST-AddMyList";

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mAddRSSDialogListener = (DialogInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement mAddUserDialogListener");
        }
    }

    public static AddOrRenameMyListDialog newInstance(String oldList) {

        AddOrRenameMyListDialog frag = new AddOrRenameMyListDialog();
        Bundle args = new Bundle();
        args.putString("oldList", oldList);
        args.putBoolean("isRename",true);
        frag.setArguments(args);
        return frag;
    }

    public static AddOrRenameMyListDialog newInstance() {

        AddOrRenameMyListDialog frag = new AddOrRenameMyListDialog();
        Bundle args = new Bundle();
        args.putBoolean("isRename",false);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Boolean isRename = getArguments().getBoolean("isRename");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_dialog, null);

        TextView title = new TextView(getActivity());
        TextView atsign = (TextView) dialogView.findViewById(R.id.txtatsign);
        atsign.setVisibility(View.GONE);
        if(isRename) {
            title.setText(R.string.dialog_mylist_rename_title);
        } else {
            title.setText(R.string.dialog_mylist_title);
        }

        title.setTextSize(18);
        title.setMinHeight(80);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(ContextCompat.getColor(getActivity(),android.R.color.white));
        title.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));
        builder.setCustomTitle(title);
        builder.setView(dialogView);

        final EditText editText = (EditText) dialogView.findViewById(R.id.input);

        /* Checks for a valid input, and if one exists, passes click event through DialogInterface
           to MainActivity */
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(editText.getText().toString().trim().length() == 0) {
                    Toast.makeText(getActivity(), "MyList name can not be blank", Toast.LENGTH_SHORT).show();
                } else if (editText.getText().toString().trim().length() > 30) {
                    Toast.makeText(getActivity(), "List name should be less than 30 characters", Toast.LENGTH_LONG).show();
                } else if(InternalDB.getInstance(getActivity()).duplicateMyList(editText.getText().toString().trim())) {
                    Toast.makeText(getActivity(), "MyList name already exists", Toast.LENGTH_SHORT).show();
                } else if(isRename){
                    mAddRSSDialogListener.onRenameMyListDialogPositiveClick(getArguments().getString("oldList"),editText.getText().toString().trim());
                    dialog.dismiss();
                } else {
                    mAddRSSDialogListener.onAddMyListDialogPositiveClick(editText.getText().toString().trim());
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
        mAddRSSDialogListener.onDialogDismiss();
    }


}