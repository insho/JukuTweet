package com.jukuproject.jukutweet.Dialogs;

        import android.app.Activity;
        import android.app.AlertDialog;
        import android.app.Dialog;
        import android.app.DialogFragment;
        import android.content.DialogInterface;
        import android.os.Bundle;
        import android.support.v4.content.ContextCompat;
        import android.view.Gravity;
        import android.widget.TextView;

        import com.jukuproject.jukutweet.Interfaces.DialogInteractionListener;
        import com.jukuproject.jukutweet.R;


public class RemoveUserDialog extends DialogFragment {

    public DialogInteractionListener mRemoveRSSDialogListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mRemoveRSSDialogListener = (DialogInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement mAddRSSDialogListener");
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
                mRemoveRSSDialogListener.onRemoveUserDialogPositiveClick(user);
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

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mRemoveRSSDialogListener.onUserDialogDismiss();
    }
}