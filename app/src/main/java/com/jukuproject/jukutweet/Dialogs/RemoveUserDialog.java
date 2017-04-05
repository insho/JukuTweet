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

import com.jukuproject.jukutweet.Interfaces.DialogInteractionListener;
import com.jukuproject.jukutweet.R;

//        import android.app.DialogFragment;


public class RemoveUserDialog extends DialogFragment {

    public DialogInteractionListener mRemoveUserDialogListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mRemoveUserDialogListener = (DialogInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement mAddUserDialogListener");
        }
    }

    public static RemoveUserDialog newInstance(String user) {

        RemoveUserDialog frag = new RemoveUserDialog();
        Bundle args = new Bundle();
        args.putString("user", user);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final String user = getArguments().getString("user");

        TextView title = new TextView(getActivity());
        title.setText(getString(R.string.remove_dialog_title, user));
        title.setTextSize(18);
        title.setPadding(20,0,0,0);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setTextColor(ContextCompat.getColor(getActivity(),android.R.color.black));
        title.setMinHeight(80);

        builder.setView(title);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mRemoveUserDialogListener.onRemoveUserDialogPositiveClick(user);
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

//    @Override
//    public void onDismiss(DialogInterface dialog) {
//        super.onDismiss(dialog);
//        mRemoveUserDialogListener.onDialogDismiss();
//    }
}