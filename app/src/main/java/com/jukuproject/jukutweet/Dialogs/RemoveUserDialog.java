package com.jukuproject.jukutweet.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.widget.TextView;

import com.jukuproject.jukutweet.Interfaces.DialogRemoveUserInteractionListener;
import com.jukuproject.jukutweet.Models.UserInfo;
import com.jukuproject.jukutweet.R;

public class RemoveUserDialog extends DialogFragment {

    public DialogRemoveUserInteractionListener mRemoveUserDialogListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mRemoveUserDialogListener = (DialogRemoveUserInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement mAddUserDialogListener");
        }
    }

    public static RemoveUserDialog newInstance(UserInfo userInfo) {

        RemoveUserDialog frag = new RemoveUserDialog();
        Bundle args = new Bundle();
        args.putParcelable("userInfo", userInfo);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final UserInfo userInfo = getArguments().getParcelable("userInfo");

        TextView title = new TextView(getActivity());
        title.setText(getString(R.string.remove_dialog_title, userInfo.getScreenName()));
        title.setTextSize(18);
        title.setPadding(20,0,0,0);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setTextColor(ContextCompat.getColor(getActivity(),android.R.color.black));
        title.setMinHeight(80);

        builder.setView(title);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userIdentifier;
                if(userInfo.getUserId()==null) {
                    userIdentifier = userInfo.getScreenName();
                } else {
                    userIdentifier = userInfo.getUserId();
                }
                mRemoveUserDialogListener.onRemoveUserDialogPositiveClick(userIdentifier);
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

        return builder.create();
    }
}