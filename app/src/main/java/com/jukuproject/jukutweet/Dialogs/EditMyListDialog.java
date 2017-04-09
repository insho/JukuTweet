package com.jukuproject.jukutweet.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jukuproject.jukutweet.Interfaces.DialogInteractionListener;
import com.jukuproject.jukutweet.R;

//import android.app.DialogFragment;


public class EditMyListDialog extends DialogFragment {

    public DialogInteractionListener mEditMyListDialogListener;
    private int mSelectedItem = 0;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mEditMyListDialogListener = (DialogInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement mAddUserDialogListener");
        }
    }

    public static EditMyListDialog newInstance(String listType, String listName, boolean isStarFavorite) {

        EditMyListDialog frag = new EditMyListDialog();
        Bundle args = new Bundle();
        args.putString("listType",listType);
        args.putString("listName", listName);
        args.putBoolean("isStarFavorite",isStarFavorite);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final String listName = getArguments().getString("listName");
        final boolean isStarFavorite = getArguments().getBoolean("isStarFavorite");
        final String listType = getArguments().getString("listType");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_editmylists, null);

        TextView textTitle = (TextView) view.findViewById(R.id.txtDialogTitle);
        textTitle.setText(listName);
        final TextView textClear = (TextView) view.findViewById(R.id.txtClear);
        final TextView textRename = (TextView) view.findViewById(R.id.txtRename);
        final TextView textDelete = (TextView) view.findViewById(R.id.txtDelete);



        /* Star Favorite lists (aka 'system lists') can not be deleted or renamed, only cleared.
        * So if it is a star favorite, only show the clear option */
        if(isStarFavorite) {
            textClear.setText(getString(R.string.listclearlist));
            textDelete.setVisibility(TextView.GONE);
            textRename.setVisibility(TextView.GONE);
            mSelectedItem =1;
        } else {
            textClear.setText(getString(R.string.listoptionclearlist));
            textRename.setText(getString(R.string.listoptionrenamelist));
            textDelete.setText(getString(R.string.listoptionremovelist));
            textDelete.setVisibility(TextView.VISIBLE);
            textRename.setVisibility(TextView.VISIBLE);
        }



        textClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* If it is a star favorite, there is only one option (clear list), so no
                * need to show the row as selected if the user clicks it*/
                if(!isStarFavorite) {

                    textClear.setSelected(true);
                    textRename.setSelected(false);
                    textDelete.setSelected(false);
                    mSelectedItem = 1;

                    //Set the background as selected
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        textClear.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.bg_key));
                        textRename.setBackground(null);
                        textDelete.setBackground(null);
                    } else {
                        textClear.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.bg_key));
                        textRename.setBackgroundDrawable(null);
                        textDelete.setBackgroundDrawable(null);
                    }

                }

            }
        });


        textRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textClear.setSelected(false);
                textRename.setSelected(true);
                textDelete.setSelected(false);
                mSelectedItem =2;

                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    textRename.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.bg_key));
                    textClear.setBackground(null);
                    textDelete.setBackground(null);
                } else {
                    textRename.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.bg_key));
                    textClear.setBackgroundDrawable(null);
                    textDelete.setBackgroundDrawable(null);
                }


            }
        });

        textDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textClear.setSelected(false);
                textRename.setSelected(false);
                textDelete.setSelected(true);
                mSelectedItem =3;

                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    textDelete.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.bg_key));
                    textClear.setBackground(null);
                    textRename.setBackground(null);

                } else {
                    textDelete.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.bg_key));
                    textClear.setBackgroundDrawable(null);
                    textRename.setBackgroundDrawable(null);
                }
            }
        });



        builder.setView(view);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(mSelectedItem == 0) {
                    Toast.makeText(getActivity(), getString(R.string.clickanoption), Toast.LENGTH_SHORT).show();
                } else {
                    mEditMyListDialogListener.onEditMyListDialogPositiveClick(listType, mSelectedItem, listName,isStarFavorite);
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

}